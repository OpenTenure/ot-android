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

import java.util.Iterator;
import java.util.List;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;

import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.AttachmentStatus;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;
import org.fao.sola.clients.android.opentenure.network.response.SaveAttachmentResponse;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Task which perform the upload of the meta data of the uploading file If the
 * response is OK , the task check if all attachments are uploaded and in case
 * close the uploading of the claims
 * 
 * */

public class SaveAttachmentTask extends
		AsyncTask<String, Void, SaveAttachmentResponse> {

	@Override
	protected SaveAttachmentResponse doInBackground(String... params) {
		// TODO Auto-generated method stub

		String json = FileSystemUtilities.getJsonAttachment(params[0]);

		Attachment toUpdate = Attachment.getAttachment(params[0]);
		toUpdate.setStatus(AttachmentStatus._UPLOADING);
		Attachment.updateAttachment(toUpdate);

		SaveAttachmentResponse res = CommunityServerAPI.saveAttachment(json,
				params[0]);

		Log.d("CommunityServerAPI",
				"SAVE ATTACHMENT JSON RESPONSE " + res.getMessage());

		return res;
	}

	protected void onPostExecute(final SaveAttachmentResponse res) {

		Claim claim = null;
		Attachment toUpdate = null;

		switch (res.getHttpStatusCode()) {

		case 100:
			/*
			 * 
			 * Unknownhost exception
			 */

			Log.d("CommunityServerAPI",
					"SAVE ATTACHMENT JSON RESPONSE " + res.getMessage());
			toUpdate = Attachment.getAttachment(res.getAttachmentId());

			if (toUpdate.getStatus().equals(AttachmentStatus._UPLOADING))
				toUpdate.setStatus(AttachmentStatus._UPLOAD_INCOMPLETE);

			Attachment.updateAttachment(toUpdate);

			claim = Claim.getClaim(toUpdate.getClaimId());
			if (claim.getStatus().equals(ClaimStatus._UPLOADING)) {
				claim.setStatus(ClaimStatus._UPLOAD_INCOMPLETE);
				claim.update();
			}

			break;

		case 105:
			/*
			 * 
			 * Unknownhost exception
			 */

			Log.d("CommunityServerAPI",
					"SAVE ATTACHMENT JSON RESPONSE " + res.getMessage());
			toUpdate = Attachment.getAttachment(res.getAttachmentId());

			if (toUpdate.getStatus().equals(AttachmentStatus._UPLOADING))
				toUpdate.setStatus(AttachmentStatus._UPLOAD_INCOMPLETE);

			Attachment.updateAttachment(toUpdate);

			claim = Claim.getClaim(toUpdate.getClaimId());
			if (claim.getStatus().equals(ClaimStatus._UPLOADING)) {
				claim.setStatus(ClaimStatus._UPLOAD_INCOMPLETE);
				claim.update();
			}
			break;

		case 200:
			/*
			 * OK
			 */

			Log.d("CommunityServerAPI",
					"SAVE ATTACHMENT JSON RESPONSE " + res.getMessage());
			toUpdate = Attachment.getAttachment(res.getAttachmentId());
			toUpdate.setStatus(AttachmentStatus._UPLOADED);

			Attachment.updateAttachment(toUpdate);

			claim = Claim.getClaim(toUpdate.getClaimId());

			if (!claim.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE)) {
				claim.setStatus(ClaimStatus._UPLOAD_INCOMPLETE);
				claim.update();
			}

			/*
			 * Now check the list of attachment for that Claim . If all the
			 * attachments are uploaded I can call saveClaim.
			 */
			String claimId = Attachment.getAttachment(res.getAttachmentId())
					.getClaimId();

			List<Attachment> attachments = Claim.getClaim(claimId)
					.getAttachments();

			int action = 0;
			for (Iterator iterator = attachments.iterator(); iterator.hasNext();) {
				Attachment attachment = (Attachment) iterator.next();
				if (!attachment.getStatus().equals(AttachmentStatus._UPLOADED)) {
					action = 1;
				}
				if (attachment.getStatus().equals(
						AttachmentStatus._UPLOAD_INCOMPLETE)) {
					action = 2;
					break;
				}
			}

			switch (action) {
			case 1:

				break;
			case 2:

				Claim claim2 = Claim.getClaim(claimId);
				if (!claim2.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE)) {
					claim2.setStatus(ClaimStatus._UPLOAD_INCOMPLETE);
					claim2.update();
				}

				break;

			default: {

				SaveClaimTask saveClaim = new SaveClaimTask();
				saveClaim.execute(claimId);
				break;
			}

			}

			break;
		case 403:

			/*
			 * "Login Error."
			 */

			Log.d("CommunityServerAPI",
					"SAVE ATTACHMENT JSON RESPONSE " + res.getMessage());

			Toast toast;
			toast = Toast.makeText(
					OpenTenureApplication.getContext(),
					R.string.message_login_no_more_valid + " "
							+ res.getHttpStatusCode() + " " + res.getMessage(),
					Toast.LENGTH_LONG);
			toast.show();

			OpenTenureApplication.setLoggedin(false);

			break;

		case 400:

			/*
			 * "Bad Request ."
			 */

			Log.d("CommunityServerAPI",
					"SAVE ATTACHMENT JSON RESPONSE " + res.getMessage());

			toUpdate = Attachment.getAttachment(res.getAttachmentId());
			toUpdate.setStatus(AttachmentStatus._UPLOAD_ERROR);

			Attachment.updateAttachment(toUpdate);

			claim = Claim.getClaim(toUpdate.getClaimId());
			if (!claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR)) {
				claim.setStatus(ClaimStatus._UPLOAD_ERROR);
				claim.update();
			}
			break;

		case 404:

			/* Error Login */

			Log.d("CommunityServerAPI", "SAVE SAVE ATTACHMENT JSON RESPONSE "
					+ R.string.message_service_not_available);

			if (!claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR)) {
				claim.setStatus(ClaimStatus._UPLOAD_ERROR);
				claim.update();
			}

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					R.string.message_submission_error + " "
							+ R.string.message_service_not_available,
					Toast.LENGTH_SHORT);
			toast.show();
			break;

		case 450:

			/*
			 * "Malformed JSON input. Failed to convert."
			 */
			Log.d("CommunityServerAPI",
					"SAVE ATTACHMENT JSON RESPONSE " + res.getMessage());

			toUpdate = Attachment.getAttachment(res.getAttachmentId());
			toUpdate.setStatus(AttachmentStatus._UPLOAD_ERROR);

			Attachment.updateAttachment(toUpdate);

			claim = Claim.getClaim(toUpdate.getClaimId());
			if (!claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR)) {
				claim.setStatus(ClaimStatus._UPLOAD_ERROR);
				claim.update();
			}
			break;

		case 454:

			/*
			 * "Object already exists."
			 */

			Log.d("CommunityServerAPI",
					"SAVE ATTACHMENT JSON RESPONSE " + res.getMessage());

			Attachment.getAttachment(res.getAttachmentId()).setStatus(
					AttachmentStatus._UPLOADED);
			Attachment.getAttachment(res.getAttachmentId()).update();
			break;

		case 455:
			/*
			 * "MD5 is not matching."
			 */
			Log.d("CommunityServerAPI",
					"SAVE ATTACHMENT JSON RESPONSE " + res.getMessage());

			break;

		case 456:

			/*
			 * "Attachment chunks not found."
			 */
			Log.d("CommunityServerAPI",
					"SAVE ATTACHMENT JSON RESPONSE " + res.getMessage());

			Attachment.getAttachment(res.getAttachmentId()).setStatus(
					AttachmentStatus._UPLOADING);
			Attachment.getAttachment(res.getAttachmentId()).update();

			toUpdate = Attachment.getAttachment(res.getAttachmentId());
			toUpdate.setStatus(AttachmentStatus._UPLOADING);
			Attachment.updateAttachment(toUpdate);

			UploadChunksTask uploadTask = new UploadChunksTask();
			uploadTask.execute(res.getAttachmentId());

			break;

		default:
			break;
		}

	}

}
