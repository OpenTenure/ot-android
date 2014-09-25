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
package org.fao.sola.clients.android.opentenure.button.listener;

import java.util.List;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.ViewHolder;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.json.JsonUtilities;
import org.fao.sola.clients.android.opentenure.form.FieldConstraint;
import org.fao.sola.clients.android.opentenure.form.FormPayload;
import org.fao.sola.clients.android.opentenure.form.FormTemplate;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.Vertex;
import org.fao.sola.clients.android.opentenure.network.SaveClaimTask;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class SubmitClaimListener implements OnClickListener {

	String claimId;
	ViewHolder vh;

	public SubmitClaimListener(String claimId, ViewHolder vh) {

		this.claimId = claimId;
		this.vh = vh;
	}

	@Override
	public void onClick(View v) {
		doIt(v);
	}

	private void doIt(View v) {

		if (!OpenTenureApplication.isLoggedin()) {
			Toast toast = Toast.makeText(v.getContext(),
					R.string.message_login_before, Toast.LENGTH_LONG);
			toast.show();
			return;

		} else {

			if (claimId != null) {

				List<Vertex> vertices = Vertex.getVertices(claimId);

				if (vertices.size() < 3) {

					Toast toast = Toast.makeText(v.getContext(),
							R.string.message_map_not_yet_draw,
							Toast.LENGTH_LONG);
					toast.show();
					return;

				}
				JsonUtilities.createClaimJson(claimId);

				Log.d(this.getClass().getName(),
						"mapGeometry: " + Vertex.mapWKTFromVertices(vertices));
				Log.d(this.getClass().getName(),
						"gpsGeometry: " + Vertex.gpsWKTFromVertices(vertices));
				
				Claim claim = Claim.getClaim(claimId);
				
				FormPayload payload = claim.getSurveyForm();

				if (payload != null) {
					
					FormTemplate template = payload.getFormTemplate();

					if(template != null){
						
						FieldConstraint failedConstraint = template.getFailedConstraint(payload);
						
						if(failedConstraint != null){
							Toast toast = Toast.makeText(v.getContext(),
									failedConstraint.getErrorMsg(),
									Toast.LENGTH_LONG);
							toast.show();
							return;
						}
					}
				}

				int progress = FileSystemUtilities.getUploadProgress(claimId, claim.getStatus(), claim.getAttachments());

				vh.getBar().setVisibility(View.VISIBLE);
				vh.getBar().setProgress(progress);

				String status = claim.getStatus();
				if (status.equals(ClaimStatus._MODERATED)
						|| status.equals(ClaimStatus._UPDATE_ERROR)
						|| status.equals(ClaimStatus._UPDATE_INCOMPLETE))
					vh.getStatus().setText(
							ClaimStatus._UPDATING + ": " + progress + " %");
				else
					vh.getStatus().setText(
							ClaimStatus._UPLOADING + ": " + progress + " %");
				vh.getStatus().setTextColor(
						OpenTenureApplication.getContext().getResources()
								.getColor(R.color.status_created));
				vh.getStatus().setVisibility(View.VISIBLE);

				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				SaveClaimTask saveClaimtask = new SaveClaimTask();
				saveClaimtask.execute(claimId, vh);
			} else {
				Toast toast = Toast.makeText(v.getContext(),
						R.string.message_save_claim_before_submit,
						Toast.LENGTH_SHORT);
				toast.show();
			}
			return;
		}

	}

}
