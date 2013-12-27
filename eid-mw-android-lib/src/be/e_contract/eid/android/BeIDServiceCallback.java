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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * BeID service callback. Usage within your Android activity:
 * 
 * <pre>
 * BeIDServiceCallback serviceCallback = new BeIDServiceCallback() {
 *  ...
 * };
 * 
 * public void onCreate(Bundle savedInstanceState) {
 * 	...
 * 	this.serviceCallback.onCreate(this);
 * }
 * 
 * protected void onDestroy() {
 * 	this.serviceCallback.onDestroy(this);
 * 	super.onDestroy();
 * }
 * </pre>
 * 
 * @author Frank Cornelis
 * 
 */
public abstract class BeIDServiceCallback extends IBeIDServiceCallback.Stub {

	private static final String LOG_TAG = "BeID";

	private IBeIDService beIDService;

	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(LOG_TAG, "onServiceConnect");
			BeIDServiceCallback.this.beIDService = IBeIDService.Stub
					.asInterface(service);
			try {
				BeIDServiceCallback.this.beIDService
						.registerCallback(BeIDServiceCallback.this);
			} catch (RemoteException e) {
				Log.e(LOG_TAG, "remote exception: " + e.getMessage());
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			BeIDServiceCallback.this.beIDService = null;
		}
	};

	/**
	 * Invoke this method within your Activity onCreate method.
	 * 
	 * @param context
	 *            the current Activity.
	 */
	public boolean onCreate(Context context) {
		if (null != this.beIDService) {
			return true;
		}
		boolean bindResult = context.bindService(
				new Intent(IBeIDService.class.getName()),
				this.serviceConnection, Context.BIND_AUTO_CREATE);
		Log.d(LOG_TAG, "bind result: " + bindResult);
		return bindResult;
	}

	/**
	 * Invoke this method within your Activity onDestroy method.
	 * 
	 * @param context
	 *            the current Activity.
	 */
	public void onDestroy(Context context) {
		context.unbindService(this.serviceConnection);
		this.beIDService = null;
	}

	/**
	 * Called when a smart card reader gets attached to the device.
	 */
	@Override
	public void smartCardReaderAttached() throws RemoteException {
	}

	/**
	 * Called when a smart card reader gets detached from your device.
	 */
	@Override
	public void smartCardReaderDetached() throws RemoteException {
	}

	/**
	 * Called when an eID card get inserted. You should return here the action
	 * that you want to perform. For example
	 * {@link BeIDServiceConstants#READ_IDENTITY}.
	 * 
	 * @return the next action to be performed, or 0 to stop.
	 * @see BeIDServiceConstants
	 */
	@Override
	public int eIDCardInserted() throws RemoteException {
		return 0;
	}

	/**
	 * Called when an eID card gets removed from the smart card reader.
	 */
	@Override
	public void eIDCardRemoved() throws RemoteException {
	}

	/**
	 * Called when eID identity data is available.
	 * 
	 * @return the next action to be performed, or 0 to stop.
	 * @see BeIDServiceConstants
	 */
	@Override
	public int eIDIdentity(Identity identity) throws RemoteException {
		return 0;
	}

	/**
	 * Called when eID address data is available.
	 * 
	 * @return the next action to be performed, or 0 to stop.
	 * @see BeIDServiceConstants
	 */
	@Override
	public int eIDAddress(Address address) throws RemoteException {
		return 0;
	}

	/**
	 * Called when eID photo data is available.
	 * 
	 * @return the next action to be performed, or 0 to stop.
	 * @see BeIDServiceConstants
	 */
	@Override
	public int eIDPhoto(byte[] photo) throws RemoteException {
		return 0;
	}
}
