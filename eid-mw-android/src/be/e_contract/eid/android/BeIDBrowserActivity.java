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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Main activity for the browser "beid:..." URL handling.
 * 
 * @author Frank Cornelis
 * 
 */
public class BeIDBrowserActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser_view);
		setTitle(R.string.browser_app_name);

		Intent intent = getIntent();
		Uri uri = intent.getData();
		String endpoint = uri.getSchemeSpecificPart();

		new GetTask().execute(endpoint);
	}

	private class GetTask extends AsyncTask<String, Void, String> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			this.progressDialog = ProgressDialog.show(BeIDBrowserActivity.this,
					"Loading...", "Loading eID operations...");
		}

		@Override
		protected String doInBackground(String... params) {
			String endpoint = params[0];
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(endpoint);
			try {
				HttpResponse httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntity = httpResponse.getEntity();
				String response = EntityUtils.toString(httpEntity);
				JSONObject jsonObject = new JSONObject(response);
				return jsonObject.getString("action");
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			this.progressDialog.dismiss();
			if (null == result) {
				Toast.makeText(BeIDBrowserActivity.this, "Error",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(BeIDBrowserActivity.this, result,
						Toast.LENGTH_LONG).show();

				Intent intent = getIntent();
				Uri uri = intent.getData();
				String endpoint = uri.getSchemeSpecificPart();
				Intent browserIntent = new Intent(Intent.ACTION_VIEW);
				browserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				browserIntent.setData(Uri.parse(endpoint));
				startActivity(browserIntent);
			}
		}
	}
}
