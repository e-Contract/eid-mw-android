/*
 * Android eID Middleware Project.
 * Copyright (C) 2013 e-Contract.be BVBA.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see 
 * http://www.gnu.org/licenses/.
 */

package be.e_contract.eid.android;

import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

/**
 * The BeID Android Service implementation.
 * 
 * @author Frank Cornelis
 * 
 */
public class BeIDService extends Service {

	private static final String LOG_TAG = "BeID";

	final RemoteCallbackList<IBeIDServiceCallback> remoteCallbackList = new RemoteCallbackList<IBeIDServiceCallback>();

	private UsbManager usbManager;

	private class IBeIDServiceImpl extends IBeIDService.Stub {

		@Override
		public void registerCallback(IBeIDServiceCallback callback)
				throws RemoteException {
			Log.d(LOG_TAG, "registerCallback");
			BeIDService.this.remoteCallbackList.register(callback);
			startSmartCardReaderPolling();
		}

		@Override
		public void unregisterCallback(IBeIDServiceCallback callback)
				throws RemoteException {
			Log.d(LOG_TAG, "unregisterCallback");
			BeIDService.this.remoteCallbackList.unregister(callback);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		String action = intent.getAction();
		Log.d(LOG_TAG, "service onBind: " + action);
		if (IBeIDService.class.getName().equals(action)) {
			return new IBeIDServiceImpl();
		}
		return null;
	}

	private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(LOG_TAG, "service onCreate");

		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		filter.addAction(ACTION_USB_PERMISSION);
		// unfortunately we can only receive ATTACHED via an Activity
		registerReceiver(this.broadcastReceiver, filter);

		this.usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
	}

	private void startSmartCardReaderPolling() {
		if (null != this.scheduledThreadPoolExecutor) {
			return;
		}
		this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
		this.scheduledThreadPoolExecutor.scheduleWithFixedDelay(
				this.smartCardReaderPollingRunnable, 100, 250,
				TimeUnit.MILLISECONDS);
	}

	private void stopSmartCardReaderPolling() {
		this.scheduledThreadPoolExecutor.shutdown();
		this.scheduledThreadPoolExecutor = null;
	}

	private CCID ccid;

	private Runnable smartCardReaderPollingRunnable = new Runnable() {
		@Override
		public void run() {
			HashMap<String, UsbDevice> deviceList = BeIDService.this.usbManager
					.getDeviceList();
			Log.d(LOG_TAG, "timer task: " + deviceList.size());
			for (UsbDevice usbDevice : deviceList.values()) {
				for (int idx = 0; idx < usbDevice.getInterfaceCount(); idx++) {
					UsbInterface usbInterface = usbDevice.getInterface(idx);
					if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_CSCID) {
						Log.d(LOG_TAG, "found smart card device");
						BeIDService.this.ccid = new CCID(
								BeIDService.this.usbManager, usbDevice,
								BeIDService.this);
						// next kick-starts the CCID system
						BeIDService.this.usbManager.requestPermission(
								usbDevice, PendingIntent.getBroadcast(
										getApplicationContext(), 0, new Intent(
												ACTION_USB_PERMISSION), 0));
						stopSmartCardReaderPolling();
						notifySmartCardReaderAttached();
						return;
					}
				}
			}
		}
	};

	private void notifySmartCardReaderAttached() {
		int count = this.remoteCallbackList.beginBroadcast();
		for (int callbackIdx = 0; callbackIdx < count; callbackIdx++) {
			try {
				this.remoteCallbackList.getBroadcastItem(callbackIdx)
						.smartCardReaderAttached();
			} catch (RemoteException e) {
				Log.e(LOG_TAG, "remote error: " + e.getMessage());
			}
		}
		this.remoteCallbackList.finishBroadcast();
	}

	public int notifyIdentity(Identity identity) {
		int count = this.remoteCallbackList.beginBroadcast();
		int eIDOperationsRequested = 0;
		for (int callbackIdx = 0; callbackIdx < count; callbackIdx++) {
			try {
				int eIDOperationRequested = this.remoteCallbackList
						.getBroadcastItem(callbackIdx).eIDIdentity(identity);
				eIDOperationsRequested |= eIDOperationRequested;
			} catch (RemoteException e) {
				Log.e(LOG_TAG, "remote error: " + e.getMessage());
			}
		}
		this.remoteCallbackList.finishBroadcast();
		return eIDOperationsRequested;
	}

	@Override
	public void onDestroy() {
		Log.d(LOG_TAG, "service onDestroy");
		unregisterReceiver(this.broadcastReceiver);
		this.remoteCallbackList.kill();
		if (null != this.scheduledThreadPoolExecutor) {
			this.scheduledThreadPoolExecutor.shutdown();
			this.scheduledThreadPoolExecutor = null;
		}
		super.onDestroy();
	}

	private void notifySmartCardReaderDetached() {
		int count = this.remoteCallbackList.beginBroadcast();
		for (int callbackIdx = 0; callbackIdx < count; callbackIdx++) {
			try {
				this.remoteCallbackList.getBroadcastItem(callbackIdx)
						.smartCardReaderDetached();
			} catch (RemoteException e) {
				Log.e(LOG_TAG, "remote error: " + e.getMessage());
			}
		}
		this.remoteCallbackList.finishBroadcast();
	}

	int notifyBeIDCardInserted() {
		int count = this.remoteCallbackList.beginBroadcast();
		int eIDOperationsRequested = 0;
		for (int callbackIdx = 0; callbackIdx < count; callbackIdx++) {
			try {
				int eIDOperationRequested = this.remoteCallbackList
						.getBroadcastItem(callbackIdx).eIDCardInserted();
				eIDOperationsRequested |= eIDOperationRequested;
				Log.d(LOG_TAG, "requested eID operation: "
						+ eIDOperationRequested);
			} catch (RemoteException e) {
				Log.e(LOG_TAG, "remote error: " + e.getMessage());
			}
		}
		this.remoteCallbackList.finishBroadcast();
		return eIDOperationsRequested;
	}

	private static final String ACTION_USB_PERMISSION = "be.e_contract.eid.android.USB_PERMISSION";

	/**
	 * We use a broadcast receiver to get informed about USB device detach
	 * events.
	 */
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				Log.d(LOG_TAG, "usb device detached");
				UsbDevice usbDevice = (UsbDevice) intent
						.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				for (int idx = 0; idx < usbDevice.getInterfaceCount(); idx++) {
					UsbInterface usbInterface = usbDevice.getInterface(idx);
					if (UsbConstants.USB_CLASS_CSCID == usbInterface
							.getInterfaceClass()) {
						Log.d(LOG_TAG, "smart card reader detached");
						notifySmartCardReaderDetached();
						BeIDService.this.ccid.stop();
						BeIDService.this.ccid = null;
						startSmartCardReaderPolling();
					}
				}
			} else if (ACTION_USB_PERMISSION.equals(action)) {
				Log.d(LOG_TAG, "usb permission received");
				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,
						false)) {
					BeIDService.this.ccid.start();
				}
			}
		}
	};

	public void notifyCardRemoved() {
		int count = this.remoteCallbackList.beginBroadcast();
		for (int callbackIdx = 0; callbackIdx < count; callbackIdx++) {
			try {
				this.remoteCallbackList.getBroadcastItem(callbackIdx)
						.eIDCardRemoved();
				;
			} catch (RemoteException e) {
				Log.e(LOG_TAG, "remote error: " + e.getMessage());
			}
		}
		this.remoteCallbackList.finishBroadcast();
	}

	public int notifyAddress(Address address) {
		int count = this.remoteCallbackList.beginBroadcast();
		int eIDOperationsRequested = 0;
		for (int callbackIdx = 0; callbackIdx < count; callbackIdx++) {
			try {
				int eIDOperationRequested = this.remoteCallbackList
						.getBroadcastItem(callbackIdx).eIDAddress(address);
				eIDOperationsRequested |= eIDOperationRequested;
			} catch (RemoteException e) {
				Log.e(LOG_TAG, "remote error: " + e.getMessage());
			}
		}
		this.remoteCallbackList.finishBroadcast();
		return eIDOperationsRequested;
	}

	public int notifyPhoto(byte[] photo) {
		int count = this.remoteCallbackList.beginBroadcast();
		int eIDOperationsRequested = 0;
		for (int callbackIdx = 0; callbackIdx < count; callbackIdx++) {
			try {
				int eIDOperationRequested = this.remoteCallbackList
						.getBroadcastItem(callbackIdx).eIDPhoto(photo);
				eIDOperationsRequested |= eIDOperationRequested;
			} catch (RemoteException e) {
				Log.e(LOG_TAG, "remote error: " + e.getMessage());
			}
		}
		this.remoteCallbackList.finishBroadcast();
		return eIDOperationsRequested;
	}
}
