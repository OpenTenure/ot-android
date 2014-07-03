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
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.AttachmentStatus;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.network.response.UploadChunksResponse;
import org.fao.sola.clients.android.opentenure.network.response.ViewHolderResponse;

import android.os.AsyncTask;
import android.view.View;

/**
 * Here transfers one chunk at time. Return true if all the chunks of an
 * attachment are correctly transferred .
 */

public class UploadChunksTask extends
		AsyncTask<Object, Void, ViewHolderResponse> {

	@Override
	protected ViewHolderResponse doInBackground(Object... params) {

		UploadChunks ulc = new UploadChunks();
		UploadChunksResponse res = ulc.execute((String) params[0]);

		ViewHolderResponse vhr = new ViewHolderResponse();

		vhr.setRes(res);
		vhr.setVh((ViewHolder) params[1]);

		return vhr;
	}

	protected void onPostExecute(final ViewHolderResponse vhr) {

		UploadChunksResponse res = (UploadChunksResponse) vhr.getRes();
		ViewHolder vh = (ViewHolder) vhr.getVh();

		if (res.getSuccess()) {

			/**
			 * All the Chunk of the claim are uploaded . Call SaveAttachment to
			 * close the flow. There 's the risk of a infinite loop
			 ***/

			SaveAttachmentTask sat = new SaveAttachmentTask();
			sat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					res.getAttachmentId(), vhr.getVh());

		} else {

			int progress;
			Attachment att;

			switch (res.getHttpStatusCode()) {
			case 100:

				/*
				 * 
				 * UnknowHostException
				 */

				att = Attachment.getAttachment(res.getAttachmentId());

				if (att.getStatus().equals(AttachmentStatus._UPLOADING)) {

					att.setStatus(AttachmentStatus._UPLOAD_INCOMPLETE);
					att.update();

					Claim claim = Claim.getClaim(att.getClaimId());
					claim.setStatus(ClaimStatus._UPLOAD_INCOMPLETE);
					claim.update();

					progress = FileSystemUtilities.getUploadProgress(claim);
					System.out.println("UploadChunkTask Qui il progress e' : "
							+ progress);
					vh.getBar().setProgress(progress);

					vh.getStatus().setText(
							ClaimStatus._UPLOADING + ": " + progress + " %");
					vh.getStatus().setTextColor(
							OpenTenureApplication.getContext().getResources()
									.getColor(R.color.status_created));
					vh.getStatus().setVisibility(View.VISIBLE);

					vh.getIconLocal().setVisibility(View.VISIBLE);
					vh.getIconUnmoderated().setVisibility(View.GONE);

				}

				break;

			case 105:

				att = Attachment.getAttachment(res.getAttachmentId());

				if (att.getStatus().equals(AttachmentStatus._UPLOADING)) {

					att.setStatus(AttachmentStatus._UPLOAD_ERROR);
					att.update();

					Claim claim = Claim.getClaim(att.getClaimId());
					claim.setStatus(ClaimStatus._UPLOAD_ERROR);
					claim.update();

					vh.getStatus().setText(ClaimStatus._UPLOAD_ERROR);
					vh.getStatus().setTextColor(
							OpenTenureApplication.getContext().getResources()
									.getColor(R.color.status_created));
					vh.getStatus().setVisibility(View.VISIBLE);

					vh.getIconLocal().setVisibility(View.VISIBLE);
					vh.getIconUnmoderated().setVisibility(View.GONE);

				}

				break;

			case 400:

				att = Attachment.getAttachment(res.getAttachmentId());

				if (att.getStatus().equals(AttachmentStatus._UPLOADING)) {

					att.setStatus(AttachmentStatus._UPLOAD_ERROR);
					att.update();

					Claim claim = Claim.getClaim(att.getClaimId());
					claim.setStatus(ClaimStatus._UPLOAD_ERROR);
					claim.update();

					vh.getStatus().setText(ClaimStatus._UPLOAD_ERROR);
					vh.getStatus().setTextColor(
							OpenTenureApplication.getContext().getResources()
									.getColor(R.color.status_created));
					vh.getStatus().setVisibility(View.VISIBLE);

					vh.getIconLocal().setVisibility(View.VISIBLE);
					vh.getIconUnmoderated().setVisibility(View.GONE);

				}

				break;

			case 404:

				att = Attachment.getAttachment(res.getAttachmentId());

				if (att.getStatus().equals(AttachmentStatus._UPLOADING)) {

					att.setStatus(AttachmentStatus._UPLOAD_ERROR);
					att.update();

					Claim claim = Claim.getClaim(att.getClaimId());
					claim.setStatus(ClaimStatus._UPLOAD_ERROR);
					claim.update();

					vh.getStatus().setText(ClaimStatus._UPLOAD_ERROR);
					vh.getStatus().setTextColor(
							OpenTenureApplication.getContext().getResources()
									.getColor(R.color.status_created));
					vh.getStatus().setVisibility(View.VISIBLE);

					vh.getIconLocal().setVisibility(View.VISIBLE);
					vh.getIconUnmoderated().setVisibility(View.GONE);

				}

				break;

			default:

				att = Attachment.getAttachment(res.getAttachmentId());

				att.setStatus(AttachmentStatus._UPLOAD_INCOMPLETE);
				att.update();

				Claim claim = Claim.getClaim(att.getClaimId());
				claim.setStatus(ClaimStatus._UPLOAD_INCOMPLETE);
				claim.update();

				progress = FileSystemUtilities.getUploadProgress(claim);
				vh.getBar().setProgress(progress);

				vh.getStatus().setText(
						ClaimStatus._UPLOADING + ": " + progress + " %");
				vh.getStatus().setTextColor(
						OpenTenureApplication.getContext().getResources()
								.getColor(R.color.status_created));
				vh.getStatus().setVisibility(View.VISIBLE);

				vh.getIconLocal().setVisibility(View.VISIBLE);
				vh.getIconUnmoderated().setVisibility(View.GONE);

				break;
			}

		}

	}

}
