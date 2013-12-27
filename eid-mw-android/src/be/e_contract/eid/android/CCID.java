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

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

/**
 * Implementation of the CCID smart card reader interfacing using USB host mode.
 * 
 * @author Frank Cornelis
 * 
 */
public class CCID {

	private static final String LOG_TAG = "BeID";

	private final UsbManager usbManager;

	private final UsbDevice usbDevice;

	private final HandlerThread handlerThread;

	private final Handler handler;

	private final BeIDService beIDService;

	public CCID(UsbManager usbManager, UsbDevice usbDevice,
			BeIDService beIDService) {
		this.usbManager = usbManager;
		this.usbDevice = usbDevice;
		this.beIDService = beIDService;

		this.handlerThread = new HandlerThread("CCID");
		this.handlerThread.start();
		Looper looper = this.handlerThread.getLooper();
		this.handler = new Handler(looper);
	}

	public void start() {
		this.handler.post(new DetectCard());
	}

	public void stop() {
		this.handlerThread.quit();
	}

	/**
	 * This class provides the main loop.
	 * 
	 * @author Frank Cornelis
	 * 
	 */
	private class DetectCard implements Runnable {

		@Override
		public void run() {
			Log.d(LOG_TAG, "running DetectCard");
			UsbInterface usbInterface = CCID.this.usbDevice.getInterface(0);
			int endpointCount = usbInterface.getEndpointCount();
			UsbEndpoint bulkInUsbEndpoint = null;
			UsbEndpoint bulkOutUsbEndpoint = null;
			UsbEndpoint intUsbEndpoint = null;
			UsbDeviceConnection usbDeviceConnection = CCID.this.usbManager
					.openDevice(CCID.this.usbDevice);
			for (int endpointIdx = 0; endpointIdx < endpointCount; endpointIdx++) {
				UsbEndpoint usbEndpoint = usbInterface.getEndpoint(endpointIdx);
				if (usbEndpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
					if (usbEndpoint.getDirection() == UsbConstants.USB_DIR_IN) {
						bulkInUsbEndpoint = usbEndpoint;
					} else {
						bulkOutUsbEndpoint = usbEndpoint;
					}
				} else if (usbEndpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {
					intUsbEndpoint = usbEndpoint;
				}
			}
			if (null == bulkInUsbEndpoint) {
				Log.w(LOG_TAG, "no bulk in USB endpoint");
				return;
			}
			if (null == bulkOutUsbEndpoint) {
				Log.w(LOG_TAG, "no bulk out USB endpoint");
				return;
			}
			usbDeviceConnection.claimInterface(usbInterface, true);
			GetSlotStatus getSlotStatus = new GetSlotStatus(bulkInUsbEndpoint,
					bulkOutUsbEndpoint, usbDeviceConnection);
			NotifySlotChange notifySlotChange;
			if (null != intUsbEndpoint) {
				notifySlotChange = new NotifySlotChange(bulkInUsbEndpoint,
						bulkOutUsbEndpoint, usbDeviceConnection, intUsbEndpoint);
			} else {
				notifySlotChange = null;
			}
			IccPowerOn iccPowerOn = new IccPowerOn(bulkInUsbEndpoint,
					bulkOutUsbEndpoint, usbDeviceConnection);
			while (true) {
				waitBeIDCard(getSlotStatus, notifySlotChange, iccPowerOn);
				int operations = CCID.this.beIDService.notifyBeIDCardInserted();
				do {
					if ((operations & BeIDServiceConstants.READ_IDENTITY) == BeIDServiceConstants.READ_IDENTITY) {
						operations &= ~BeIDServiceConstants.READ_IDENTITY;
						BeID beID = new BeID(bulkInUsbEndpoint,
								bulkOutUsbEndpoint, usbDeviceConnection);
						Identity identity = beID.getIdentity();
						if (null != identity) {
							Log.d(LOG_TAG, "name: " + identity.name);
							operations = CCID.this.beIDService
									.notifyIdentity(identity);
						}
					}
					if ((operations & BeIDServiceConstants.READ_ADDRESS) == BeIDServiceConstants.READ_ADDRESS) {
						operations &= ~BeIDServiceConstants.READ_ADDRESS;
						BeID beID = new BeID(bulkInUsbEndpoint,
								bulkOutUsbEndpoint, usbDeviceConnection);
						Address address = beID.getAddress();
						if (null != address) {
							operations = CCID.this.beIDService
									.notifyAddress(address);
						}
					}
					if ((operations & BeIDServiceConstants.READ_PHOTO) == BeIDServiceConstants.READ_PHOTO) {
						operations &= ~BeIDServiceConstants.READ_PHOTO;
						BeID beID = new BeID(bulkInUsbEndpoint,
								bulkOutUsbEndpoint, usbDeviceConnection);
						byte[] photo = beID.getPhoto();
						if (null != photo) {
							operations = CCID.this.beIDService
									.notifyPhoto(photo);
						}
					}
				} while (operations != 0);
				waitForCardRemoval(getSlotStatus, notifySlotChange);
				CCID.this.beIDService.notifyCardRemoved();
			}
		}
	}

	private void waitForCardRemoval(GetSlotStatus getSlotStatus,
			NotifySlotChange notifySlotChange) {
		if (getSlotStatus.check(0, false)) {
			return;
		}
		while (true) {
			if (null != notifySlotChange) {
				/*
				 * event driven is better than the getSlotStatus polling.
				 */
				if (notifySlotChange.check(false)) {
					return;
				}
			} else {
				if (getSlotStatus.check(0, false)) {
					return;
				}
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Wait for an eID card to be come available.
	 * 
	 * @param getSlotStatus
	 * @param notifySlotChange
	 * @param iccPowerOn
	 */
	private void waitBeIDCard(GetSlotStatus getSlotStatus,
			NotifySlotChange notifySlotChange, IccPowerOn iccPowerOn) {
		if (getSlotStatus.check(0, true)) {
			if (isBeID(iccPowerOn)) {
				return;
			}
		}
		while (true) {
			if (null != notifySlotChange) {
				/*
				 * event driven is better than the getSlotStatus polling.
				 */
				if (notifySlotChange.check(true)) {
					if (isBeID(iccPowerOn)) {
						return;
					}
				}
			} else {
				if (getSlotStatus.check(0, true)) {
					if (isBeID(iccPowerOn)) {
						return;
					}
				}
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
		}
	}

	private boolean isBeID(IccPowerOn iccPowerOn) {
		byte[] atr = iccPowerOn.getATR();
		if (null == atr) {
			Log.d(LOG_TAG, "null ATR");
			return false;
		}
		return BeIDATR.isBeID(atr);
	}
}
