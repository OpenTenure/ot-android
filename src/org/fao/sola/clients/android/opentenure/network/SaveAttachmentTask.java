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
import org.fao.sola.clients.android.opentenure.ViewHolder;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.AttachmentStatus;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;
import org.fao.sola.clients.android.opentenure.network.response.SaveAttachmentResponse;
import org.fao.sola.clients.android.opentenure.network.response.ViewHolderResponse;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Task which perform the upload of the meta-data of the uploading file. If the
 * response is OK , the task check if all attachments are uploaded and in case
 * close the uploading of the claims
 * 
 * */

public class SaveAttachmentTask extends
		AsyncTask<Object, Void, ViewHolderResponse> {

	@Override
	protected ViewHolderResponse doInBackground(Object... params) {

		ViewHolderResponse vhr = new ViewHolderResponse();

		String json = FileSystemUtilities.getJsonAttachment((String) params[0]);

		Attachment toUpdate = Attachment.getAttachment((String) params[0]);
		toUpdate.setStatus(AttachmentStatus._UPLOADING);
		Attachment.updateAttachment(toUpdate);

		Claim claim = Claim.getClaim(toUpdate.getClaimId());


		String status = claim.getStatus();
		if (!status.equals(ClaimStatus._CREATED)
				&& !status.equals(ClaimStatus._UPLOADING)
				&& !status.equals(ClaimStatus._UPLOAD_ERROR)
				&& !status.equals(ClaimStatus._UPLOAD_INCOMPLETE)) {

			claim.setStatus(ClaimStatus._UPDATING);
			claim.update();

		}

		SaveAttachmentResponse res = CommunityServerAPI.saveAttachment(json,
				(String) params[0]);

		Log.d("CommunityServerAPI",
				"SAVE ATTACHMENT JSON RESPONSE " + res.getMessage());

		vhr.setRes(res);
		vhr.setVh((ViewHolder) params[1]);

		return vhr;
	}

	protected void onPostExecute(final ViewHolderResponse vhr) {

		Claim claim = null;
		Attachment toUpdate = null;

		int progress = 0;

		SaveAttachmentResponse res = (SaveAttachmentResponse) vhr.getRes();
		ViewHolder vh = vhr.getVh();

		switch (res.getHttpStatusCode()) {

		case 100:
			/*
			 * 
			 * Unknownhost exception
			 */

			Log.d("CommunityServerAPI",
					"SAVE ATTACHMENT JSON RESPONSE " + res.getMessage());
			toUpdate = Attachment.getAttachment(res.getAttachmentId());

			if (toUpdate.getStatus().equals(AttachmentStatus._UPLOADING)) {
				toUpdate.setStatus(AttachmentStatus._UPLOAD_INCOMPLETE);
				toUpdate.update();
			}

			Attachment.updateAttachment(toUpdate);

			claim = Claim.getClaim(toUpdate.getClaimId());
			if (claim.getStatus().equals(ClaimStatus._UPLOADING)) {
				claim.setStatus(ClaimStatus._UPLOAD_INCOMPLETE);
				claim.update();
			}
			if (claim.getStatus().equals(ClaimStatus._UPDATING)) {
				claim.setStatus(ClaimStatus._UPDATE_INCOMPLETE);
				claim.update();
			}

			progress = FileSystemUtilities.getUploadProgress(claim.getClaimId(), claim.getStatus(), claim.getAttachments());

			vh.getStatus().setText(claim.getStatus() + ": " + progress + " %");
			vh.getStatus().setTextColor(
					OpenTenureApplication.getContext().getResources()
							.getColor(R.color.status_created));
			vh.getStatus().setVisibility(View.VISIBLE);

			if (claim.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE)) {
				vh.getIconLocal().setVisibility(View.VISIBLE);
				vh.getIconUnmoderated().setVisibility(View.GONE);
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
			{
				toUpdate.update();
			}

			Attachment.updateAttachment(toUpdate);

			claim = Claim.getClaim(toUpdate.getClaimId());
			if (claim.getStatus().equals(ClaimStatus._UPLOADING)) {
				claim.setStatus(ClaimStatus._UPLOAD_INCOMPLETE);
				claim.update();
			}
			if (claim.getStatus().equals(ClaimStatus._UPDATING)) {
				claim.setStatus(ClaimStatus._UPDATE_INCOMPLETE);
				claim.update();
			}

			progress = FileSystemUtilities.getUploadProgress(claim.getClaimId(), claim.getStatus(), claim.getAttachments());

			vh.getStatus().setText(claim.getStatus() + ": " + progress + " %");
			vh.getStatus().setTextColor(
					OpenTenureApplication.getContext().getResources()
							.getColor(R.color.status_created));
			vh.getStatus().setVisibility(View.VISIBLE);

			if (claim.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE)) {
				vh.getIconLocal().setVisibility(View.VISIBLE);
				vh.getIconUnmoderated().setVisibility(View.GONE);
			}

			break;

		case 200:
			/*
			 * D * OK
			 */

			Log.d("CommunityServerAPI",
					"SAVE ATTACHMENT JSON RESPONSE " + res.getMessage());
			toUpdate = Attachment.getAttachment(res.getAttachmentId());
			toUpdate.setStatus(AttachmentStatus._UPLOADED);

			Attachment.updateAttachment(toUpdate);

			String claimId = toUpdate.getClaimId();
			claim = Claim.getClaim(claimId);

			float factor = (float) toUpdate.getUploadedBytes() / toUpdate.getSize();
			progress = (int) (factor * 100);

			vh.getBar().setProgress(progress);

			if (claim.getStatus().equals(ClaimStatus._UPLOADING)) {
				vh.getStatus().setText(
						ClaimStatus._UPLOADING + ": " + progress + " %");
				vh.getStatus().setTextColor(
						OpenTenureApplication.getContext().getResources()
								.getColor(R.color.status_created));
				vh.getStatus().setVisibility(View.VISIBLE);

				vh.getIconLocal().setVisibility(View.VISIBLE);
				vh.getIconUnmoderated().setVisibility(View.GONE);
			}
			// if (!claim.getStatus().equals(ClaimStatus._UPLOADING)) {
			// claim.setStatus(ClaimStatus._UPLOADING);
			// claim.update();
			// }

			/*
			 * Now check the list of attachment for that Claim . If all the
			 * attachments are uploaded I can call saveClaim.
			 */

			if ((claim.getStatus().equals(ClaimStatus._UPDATING))) {

				AddClaimantAttachmentTask task = new AddClaimantAttachmentTask();
				vhr.getRes().setClaimId(claimId);
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,vhr);
			}

			else {
				List<Attachment> attachments = claim.getAttachments();

				int action = 0;
				for (Iterator<Attachment> iterator = attachments.iterator(); iterator
						.hasNext();) {
					Attachment attachment = (Attachment) iterator.next();
					if (!attachment.getStatus().equals(
							AttachmentStatus._UPLOADED)) {
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

					// DO NOTHING

					break;
				case 2:

					// JUST UPDATE THE STATUS OF CLAIM IN CASE OF INCOMPLETE

					// Claim claim2 = Claim.getClaim(claimId);
					if (claim.getStatus().equals(ClaimStatus._UPLOADING)) {
						claim.setStatus(ClaimStatus._UPLOAD_INCOMPLETE);
						claim.update();

					}
					if (claim.getStatus().equals(ClaimStatus._UPDATING)) {
						claim.setStatus(ClaimStatus._UPDATE_INCOMPLETE);
						claim.update();

					}

					break;

				default: {

					// CALL THE SAVE CLAIM TASK TO CLOSE THE FLOW OF SAVE CLAIM

					if (claim.getStatus().equals(ClaimStatus._UPLOADING)) {
						SaveClaimTask saveClaim = new SaveClaimTask();
						saveClaim.execute(claimId, vh);
						break;
					}
				}

				}
			}
			
			OpenTenureApplication.getDocumentsFragment().update();

			break;
		case 403:

			/*
			 * "Login Error."
			 */

			Log.d("CommunityServerAPI",
					"SAVE ATTACHMENT JSON RESPONSE " + res.getMessage());

			Toast toast;
			toast = Toast.makeText(OpenTenureApplication.getContext(),
					OpenTenureApplication.getContext().getResources()
							.getString(R.string.message_login_no_more_valid)
							+ " " + res.getMessage(), Toast.LENGTH_LONG);
			toast.show();

			OpenTenureApplication.setLoggedin(false);

			Attachment att = Attachment.getAttachment(res.getAttachmentId());

			if (att.getStatus().equals(ClaimStatus._UPLOADING)) {

				att.setStatus(AttachmentStatus._CREATED);
				att.update();

				vh.getBar().setVisibility(View.INVISIBLE);
				vh.getStatus().setVisibility(View.VISIBLE);
				vh.getSend().setVisibility(View.VISIBLE);

			}

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
			if (!claim.getStatus().equals(ClaimStatus._UNMODERATED)
					&& !claim.getStatus().equals(ClaimStatus._UPDATE_ERROR)
					&& !claim.getStatus()
							.equals(ClaimStatus._UPDATE_INCOMPLETE)
					&& !claim.getStatus().equals(ClaimStatus._UPDATING)) {
				claim.setStatus(ClaimStatus._UPLOAD_ERROR);
				claim.update();
			} else {
				claim.setStatus(ClaimStatus._UPDATE_ERROR);
				claim.update();
			}
			factor = (float) toUpdate.getUploadedBytes() / toUpdate.getSize();
			progress = (int) (factor * 100);
			
			vh.getBar().setProgress(progress);

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					OpenTenureApplication.getContext().getResources()
							.getString(R.string.message_submission_error)
							+ " " + res.getMessage(), Toast.LENGTH_LONG);
			toast.show();

			vh.getStatus().setText(claim.getStatus());
			vh.getStatus().setTextColor(
					OpenTenureApplication.getContext().getResources()
							.getColor(R.color.status_created));
			vh.getStatus().setVisibility(View.VISIBLE);

			if (claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR)) {
				vh.getIconLocal().setVisibility(View.VISIBLE);
				vh.getIconUnmoderated().setVisibility(View.GONE);
			}

			vh.getBar().setVisibility(View.GONE);

			break;

		case 404:

			/* Error */

			Log.d("CommunityServerAPI", "SAVE SAVE ATTACHMENT JSON RESPONSE "
					+ R.string.message_service_not_available);

			toUpdate = Attachment.getAttachment(res.getAttachmentId());
			claim = Claim.getClaim(toUpdate.getClaimId());

			if (!claim.getStatus().equals(ClaimStatus._UNMODERATED)
					&& !claim.getStatus().equals(ClaimStatus._UPDATE_ERROR)
					&& !claim.getStatus()
							.equals(ClaimStatus._UPDATE_INCOMPLETE)
					&& !claim.getStatus().equals(ClaimStatus._UPDATING)) {
				claim.setStatus(ClaimStatus._UPLOAD_ERROR);
				claim.update();
			} else {
				claim.setStatus(ClaimStatus._UPDATE_ERROR);
				claim.update();
			}

			toast = Toast
					.makeText(
							OpenTenureApplication.getContext(),
							OpenTenureApplication
									.getContext()
									.getResources()
									.getString(
											R.string.message_submission_error)
									+ " "
									+ OpenTenureApplication
											.getContext()
											.getResources()
											.getString(
													R.string.message_service_not_available),
							Toast.LENGTH_LONG);
			toast.show();
			

			vh.getStatus().setText(claim.getStatus());
			vh.getStatus().setTextColor(
					OpenTenureApplication.getContext().getResources()
							.getColor(R.color.status_created));
			vh.getStatus().setVisibility(View.VISIBLE);

			if (claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR)) {
				vh.getIconLocal().setVisibility(View.VISIBLE);
				vh.getIconUnmoderated().setVisibility(View.GONE);
			}

			vh.getBar().setVisibility(View.GONE);

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
			if (!claim.getStatus().equals(ClaimStatus._UNMODERATED)
					&& !claim.getStatus().equals(ClaimStatus._UPDATE_ERROR)
					&& !claim.getStatus()
							.equals(ClaimStatus._UPDATE_INCOMPLETE)
					&& !claim.getStatus().equals(ClaimStatus._UPDATING)) {
				claim.setStatus(ClaimStatus._UPLOAD_ERROR);
				claim.update();
			} else {
				claim.setStatus(ClaimStatus._UPDATE_ERROR);
				claim.update();
			}

			factor = (float) toUpdate.getUploadedBytes() / toUpdate.getSize();
			progress = (int) (factor * 100);

			vh.getBar().setProgress(progress);

			vh.getStatus().setText(claim.getStatus());
			vh.getStatus().setTextColor(
					OpenTenureApplication.getContext().getResources()
							.getColor(R.color.status_created));
			vh.getStatus().setVisibility(View.VISIBLE);

			if (claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR)) {
				vh.getIconLocal().setVisibility(View.VISIBLE);
				vh.getIconUnmoderated().setVisibility(View.GONE);
			}

			vh.getBar().setVisibility(View.GONE);

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

			toUpdate = Attachment.getAttachment(res.getAttachmentId());
			toUpdate.setStatus(AttachmentStatus._UPLOADING);
			Attachment.updateAttachment(toUpdate);

			claim = Claim.getClaim(toUpdate.getClaimId());

			factor = (float) toUpdate.getUploadedBytes() / toUpdate.getSize();
			progress = (int) (factor * 100);
			
			vh.getBar().setProgress(progress);

			vh.getStatus().setText(claim.getStatus() + ": " + progress + " %");
			vh.getStatus().setTextColor(
					OpenTenureApplication.getContext().getResources()
							.getColor(R.color.status_created));
			vh.getStatus().setVisibility(View.VISIBLE);

			// vh.getIconLocal().setVisibility(View.VISIBLE);
			// vh.getIconUnmoderated().setVisibility(View.GONE);

			if (claim.getStatus().equals(ClaimStatus._CREATED)
					|| claim.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE)
					|| claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR) || claim.getStatus().equals(ClaimStatus._UPLOADING)) {
				claim.setStatus(ClaimStatus._UPLOADING);
				claim.update();

			} else {
				claim.setStatus(ClaimStatus._UPDATING);
				claim.update();
			}



			UploadChunksTask uploadTask = new UploadChunksTask();
			uploadTask.execute(res.getAttachmentId(), vh);



			break;

		default:
			break;
		}

	}

}
