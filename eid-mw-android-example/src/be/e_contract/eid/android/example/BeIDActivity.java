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
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import be.e_contract.eid.android.BeIDServiceCallback;
import be.e_contract.eid.android.BeIDServiceConstants;
import be.e_contract.eid.android.Identity;

public class BeIDActivity extends Activity {

	private static final String LOG_TAG = "BeID";

	private TextView nameTextView;

	private BeIDServiceCallback serviceCallback = new BeIDServiceCallback() {

		@Override
		public int eIDCardInserted() throws RemoteException {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(),
							"eID card inserted", Toast.LENGTH_SHORT).show();
				}
			});
			return BeIDServiceConstants.READ_IDENTITY;
		}

		@Override
		public void eIDCardRemoved() throws RemoteException {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), "card removed",
							Toast.LENGTH_SHORT).show();
					BeIDActivity.this.nameTextView.setText("");
				}
			});
		}

		@Override
		public void smartCardReaderAttached() throws RemoteException {
			Log.d(LOG_TAG, "smart card reader attached event");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(),
							"smart card reader attached", Toast.LENGTH_SHORT)
							.show();
				}
			});
		}

		@Override
		public void smartCardReaderDetached() throws RemoteException {
			Log.d(LOG_TAG, "smart card reader detached event");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(),
							"smart card reader detached", Toast.LENGTH_SHORT)
							.show();
				}
			});
		}

		@Override
		public int eIDIdentity(final Identity identity) throws RemoteException {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(),
							"name: " + identity.name, Toast.LENGTH_SHORT)
							.show();
					BeIDActivity.this.nameTextView.setText(identity.name);
				}
			});
			return 0;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(LOG_TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_view);
		this.nameTextView = (TextView) findViewById(R.id.nameTextView);
		boolean result = this.serviceCallback.onCreate(this);
		if (false == result) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(),
							"Install eID Middleware for Android first.",
							Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	@Override
	protected void onDestroy() {
		this.serviceCallback.onDestroy(this);
		super.onDestroy();
	}
}
