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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;
import org.fao.sola.clients.android.opentenure.network.response.Attachment;
import org.fao.sola.clients.android.opentenure.network.response.SaveClaimResponse;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class SaveClaimTask extends AsyncTask<String, Void, SaveClaimResponse> {

	@Override
	protected SaveClaimResponse doInBackground(String... params) {

		String json = FileSystemUtilities.getJsonClaim(params[0]);
		SaveClaimResponse res = CommunityServerAPI.saveClaim(json);
		res.setClaimId(params[0]);
		return res;
	}

	protected void onPostExecute(final SaveClaimResponse res) {

		Toast toast;

		Claim claim = Claim.getClaim(res.getClaimId());

		switch (res.getHttpStatusCode()) {
		case 200:

			try {

				TimeZone tz = TimeZone.getTimeZone("UTC");
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss");
				sdf.setTimeZone(tz);
				Date date = sdf.parse(res.getChallengeExpiryDate());

				claim.setChallengeExpiryDate(new java.sql.Date(date.getTime()));

				claim.setStatus(ClaimStatus._UNMODERATED);
				claim.update();

			} catch (Exception e) {
				Log.d("CommunityServerAPI",
						"SAVE CLAIM JSON RESPONSE " + res.getMessage());
				e.printStackTrace();
			}

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					R.string.message_submitted, Toast.LENGTH_SHORT);
			toast.show();

			break;

		case 403:

			Log.d("CommunityServerAPI",
					"SAVE CLAIM JSON RESPONSE " + res.getMessage());

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					R.string.message_login_no_more_valid, Toast.LENGTH_SHORT);
			toast.show();

			OpenTenureApplication.setLoggedin(false);

			break;

		case 452:

			claim.setStatus(ClaimStatus._UPLOADING);
			claim.update();

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					R.string.message_uploading, Toast.LENGTH_SHORT);
			toast.show();

			List<Attachment> list = res.getAttachments();

			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Attachment attachment = (Attachment) iterator.next();

				SaveAttachmentTask saveAttachmentTask = new SaveAttachmentTask();
				saveAttachmentTask.execute(attachment.getId());

			}

			break;

		case 450:

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					R.string.message_submission_error + res.getMessage(),
					Toast.LENGTH_SHORT);
			toast.show();

			break;

		case 400:

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					R.string.message_submission_error + res.getMessage(),
					Toast.LENGTH_SHORT);
			toast.show();

			break;

		default:
			break;
		}

		return;

	}

}
