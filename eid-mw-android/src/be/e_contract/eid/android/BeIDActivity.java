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

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main activity for the eID viewer.
 * 
 * @author Frank Cornelis
 * 
 */
public class BeIDActivity extends Activity {

	private static final String LOG_TAG = "BeID";

	private TextView nameTextView;

	private TextView firstNameTextView;

	private TextView dateOfBirthTextView;

	private TextView streetAndNumberTextView;

	private TextView municipalityTextView;

	private TextView zipTextView;

	private ImageView photoImageView;

	private BeIDServiceCallback serviceCallback = new BeIDServiceCallback() {

		@Override
		public int eIDCardInserted() throws RemoteException {
			return BeIDServiceConstants.READ_IDENTITY;
		}

		@Override
		public void eIDCardRemoved() throws RemoteException {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					BeIDActivity.this.nameTextView.setText("");
					BeIDActivity.this.firstNameTextView.setText("");
					BeIDActivity.this.dateOfBirthTextView.setText("");
					BeIDActivity.this.streetAndNumberTextView.setText("");
					BeIDActivity.this.municipalityTextView.setText("");
					BeIDActivity.this.zipTextView.setText("");
					BeIDActivity.this.photoImageView.setImageBitmap(null);
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
					BeIDActivity.this.nameTextView.setText(identity.name);
					BeIDActivity.this.firstNameTextView
							.setText(identity.firstName);
					java.text.DateFormat dateFormat = DateFormat
							.getDateFormat(BeIDActivity.this);
					BeIDActivity.this.dateOfBirthTextView.setText(dateFormat
							.format(identity.dateOfBirth.getTime()));
				}
			});
			return BeIDServiceConstants.READ_ADDRESS;
		}

		@Override
		public int eIDAddress(final Address address) throws RemoteException {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					BeIDActivity.this.streetAndNumberTextView
							.setText(address.streetAndNumber);
					BeIDActivity.this.municipalityTextView
							.setText(address.municipality);
					BeIDActivity.this.zipTextView.setText(address.zip);
				}
			});
			return BeIDServiceConstants.READ_PHOTO;
		}

		@Override
		public int eIDPhoto(final byte[] photo) throws RemoteException {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Bitmap bitmap = BitmapFactory.decodeByteArray(photo, 0,
							photo.length);
					BeIDActivity.this.photoImageView.setImageBitmap(bitmap);
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
		this.firstNameTextView = (TextView) findViewById(R.id.firstNameTextView);
		this.dateOfBirthTextView = (TextView) findViewById(R.id.dateOfBirthTextView);
		this.streetAndNumberTextView = (TextView) findViewById(R.id.streetAndNumberTextView);
		this.municipalityTextView = (TextView) findViewById(R.id.municipalityTextView);
		this.zipTextView = (TextView) findViewById(R.id.zipTextView);
		this.photoImageView = (ImageView) findViewById(R.id.photoImageView);
		this.serviceCallback.onCreate(this);
	}

	@Override
	protected void onDestroy() {
		this.serviceCallback.onDestroy(this);
		super.onDestroy();
	}
}
