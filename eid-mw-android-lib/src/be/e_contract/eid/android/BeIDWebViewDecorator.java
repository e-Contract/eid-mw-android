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
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * @author Frank Cornelis
 * 
 */
public class BeIDWebViewDecorator {

	private final WebView webView;

	private final Context context;

	public BeIDWebViewDecorator(WebView webView, Context context) {
		this.webView = webView;
		this.context = context;
	}

	public void enable() {
		WebSettings webSettings = this.webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setDomStorageEnabled(true);
		this.webView.setWebChromeClient(new WebChromeClient());
		BeIDJavascriptInterface beIDJavascriptInterface = new BeIDJavascriptInterface(
				this.webView, this.context);
		this.webView.addJavascriptInterface(beIDJavascriptInterface, "BeID");
	}
}
