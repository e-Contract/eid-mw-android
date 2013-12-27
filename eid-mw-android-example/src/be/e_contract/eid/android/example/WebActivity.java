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

package be.e_contract.eid.android.example;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.webkit.WebView;
import be.e_contract.eid.android.BeIDServiceCallback;
import be.e_contract.eid.android.BeIDWebViewDecorator;
import be.e_contract.eid.android.Identity;

public class WebActivity extends Activity {

	private WebView webView;

	private BeIDServiceCallback serviceCallback = new BeIDServiceCallback() {

		@Override
		public void smartCardReaderDetached() throws RemoteException {
		}

		@Override
		public void smartCardReaderAttached() throws RemoteException {
		}

		@Override
		public int eIDIdentity(Identity identity) throws RemoteException {
			return 0;
		}

		@Override
		public void eIDCardRemoved() throws RemoteException {
		}

		@Override
		public int eIDCardInserted() throws RemoteException {
			return 0;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web_view);
		this.webView = (WebView) findViewById(R.id.webView);
		BeIDWebViewDecorator beIDWebViewDecorator = new BeIDWebViewDecorator(
				this.webView, this);
		beIDWebViewDecorator.enable();
		this.webView
				.loadData(
						"<html>"
								+ "<head>"
								+ "<script type=\"text/javascript\">"
								+ "function callback(name, firstname) { alert(name + ' ' + firstname); }"
								+ "BeID.getIdentity('callback');" + "</script>"
								+ "</head>" + "<body>" + "<h1>hello world</h1>"
								+ "</body>" + "</html>", "text/html", null);
	}
}
