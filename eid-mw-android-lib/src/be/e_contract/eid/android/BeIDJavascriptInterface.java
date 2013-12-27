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

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.webkit.WebView;

/**
 * @author Frank Cornelis
 * 
 */
public class BeIDJavascriptInterface {

	private final WebView webView;

	private final Context context;

	private String identityCallback;

	private boolean initialized;

	private BeIDServiceCallback serviceCallback = new BeIDServiceCallback() {

		@Override
		public void smartCardReaderDetached() throws RemoteException {
		}

		@Override
		public void smartCardReaderAttached() throws RemoteException {
		}

		@Override
		public int eIDIdentity(Identity identity) throws RemoteException {
			Log.d("BeID", "identity");
			if (BeIDJavascriptInterface.this.identityCallback != null) {
				BeIDJavascriptInterface.this.webView.loadUrl("javascript:"
						+ BeIDJavascriptInterface.this.identityCallback + "('"
						+ identity.name + "', '" + identity.firstName + "');");
			}
			return 0;
		}

		@Override
		public void eIDCardRemoved() throws RemoteException {
		}

		@Override
		public int eIDCardInserted() throws RemoteException {
			return 1;
		}

		@Override
		public int eIDAddress(Address address) throws RemoteException {
			return 0;
		}

		@Override
		public int eIDPhoto(byte[] photo) throws RemoteException {
			return 0;
		}
	};

	public BeIDJavascriptInterface(WebView webView, Context context) {
		this.webView = webView;
		this.context = context;
	}

	public void getIdentity(String callback) {
		Log.d("BeID", "getIdentity: " + callback);
		this.identityCallback = callback;
		initialize();
	}

	private void initialize() {
		if (this.initialized) {
			return;
		}
		this.serviceCallback.onCreate(this.context);
		this.initialized = true;
	}
}
