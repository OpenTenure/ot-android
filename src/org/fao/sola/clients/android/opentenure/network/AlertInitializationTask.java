/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice,this list
 *       of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice,this list
 *       of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
 *       promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.fao.sola.clients.android.opentenure.network;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.ViewHolder;
import org.fao.sola.clients.android.opentenure.maps.MainMapFragment;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.network.response.GetClaimsInput;
import org.fao.sola.clients.android.opentenure.network.response.ViewHolderResponse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AlertInitializationTask extends
		AsyncTask<GetClaimsInput, GetClaimsInput, GetClaimsInput> {

	private ProgressDialog dialog;

	public AlertInitializationTask(Activity activity) {

		this.dialog = new ProgressDialog(activity);

	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		// Setting the alert dialog
		dialog.setMessage(OpenTenureApplication.getContext().getResources()
				.getString(R.string.message_app_initializing));
		dialog.setTitle(R.string.message_title_app_initializing);
		dialog.show();

	}

	@Override
	protected GetClaimsInput doInBackground(GetClaimsInput... params) {
		// TODO Auto-generated method stub

		GetClaimsInput input = (GetClaimsInput) params[0];

		int i = 0;
		while (i <= 100) {
			i = i + Claim.getAllClaims().size() - Claim.getAllClaims().size();

			try {
				Thread.sleep(80);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			i++;
		}

		return input;
	}

	protected void onPostExecute(final GetClaimsInput input) {

		dialog.dismiss();

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(input
				.getMapView().getContext());
		if (!Boolean.parseBoolean(Configuration.getConfigurationByName(
				"isInitialized").getValue())
				&& OpenTenureApplication.getInstance().isOnline()) {

			alertDialog.setTitle(R.string.message_title_app_not_initialized);
			alertDialog.setMessage(OpenTenureApplication.getContext()
					.getResources()
					.getString(R.string.message_app_not_initialized));

		} else if (!Boolean.parseBoolean(Configuration.getConfigurationByName(
				"isInitialized").getValue())
				&& !OpenTenureApplication.getInstance().isOnline()) {

			alertDialog.setTitle(R.string.message_title_app_not_initialized);
			alertDialog.setMessage(OpenTenureApplication.getContext()
					.getResources()
					.getString(R.string.message_app_not_initialized_network));
		} else {
			alertDialog.setTitle(R.string.message_title_app_initialized);
			alertDialog
					.setMessage(OpenTenureApplication.getContext()
							.getResources()
							.getString(R.string.message_app_initialized));
		}

		alertDialog.setPositiveButton(R.string.confirm, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AlertDialog.Builder newPasswordDialog = new AlertDialog.Builder(
						input.getMapView().getContext());

				return;

			}
		});

		alertDialog.show();

	}

}
