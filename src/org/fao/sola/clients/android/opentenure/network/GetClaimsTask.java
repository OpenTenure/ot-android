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
import org.fao.sola.clients.android.opentenure.filesystem.json.SaveDownloadedClaim;
import org.fao.sola.clients.android.opentenure.model.AdjacenciesNotes;
import org.fao.sola.clients.android.opentenure.model.Adjacency;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.Owner;
import org.fao.sola.clients.android.opentenure.model.Person;
import org.fao.sola.clients.android.opentenure.model.PropertyLocation;
import org.fao.sola.clients.android.opentenure.model.ShareProperty;
import org.fao.sola.clients.android.opentenure.model.Vertex;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;
import org.fao.sola.clients.android.opentenure.network.response.Claim;
import org.fao.sola.clients.android.opentenure.network.response.GetClaimsInput;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * Asynk task that downloads from server all the claims of the list passed as
 * input
 * 
 * */
public class GetClaimsTask extends
		AsyncTask<GetClaimsInput, GetClaimsInput, GetClaimsInput> {

	@Override
	protected GetClaimsInput doInBackground(GetClaimsInput... params) {
		int i = 0;
		boolean success = true;

		GetClaimsInput input = params[0];
		List<Claim> claims = input.getClaims();

		input.setResult(true);
		input.setDownloaded(0);

		for (Iterator iterator = claims.iterator(); iterator.hasNext();) {
			Claim claimToDownload = (Claim) iterator.next();

			/* For each claim downloadable check the status and the version */

			org.fao.sola.clients.android.opentenure.model.Claim claim = org.fao.sola.clients.android.opentenure.model.Claim
					.getClaim(claimToDownload.getId());

			if (claim != null
					&& (claimToDownload.getStatusCode().equals(
							ClaimStatus._WITHDRAWN) || claimToDownload
							.getStatusCode().equals(ClaimStatus._REJECTED))) {

				/* In this case the claim will be removed locally */
				Log.d(this.getClass().getName(), "The claim  "
						+ claimToDownload.getId() + " should be deleted");

				List<ShareProperty> list = ShareProperty.getShares(claim
						.getClaimId());

				for (Iterator iterator2 = list.iterator(); iterator2.hasNext();) {
					ShareProperty share = (ShareProperty) iterator2.next();
					share.deleteShare();
					List<Owner> OwnersTBD = Owner.getOwners(share.getId());
					for (Iterator iteratorT = OwnersTBD.iterator(); iteratorT
							.hasNext();) {
						Owner owner = (Owner) iteratorT.next();
						Person personTBD = Person
								.getPerson(owner.getPersonId());
						owner.delete();
						personTBD.delete();

					}
				}

				List<Vertex> vertexList = claim.getVertices();
				for (Iterator iterator2 = vertexList.iterator(); iterator2
						.hasNext();) {
					Vertex vertex = (Vertex) iterator2.next();
					vertex.delete();
				}

				List<Attachment> attachments = claim.getAttachments();

				for (Iterator iterator2 = attachments.iterator(); iterator2
						.hasNext();) {
					Attachment attachment = (Attachment) iterator2.next();

					attachment.delete();

				}

				List<PropertyLocation> locations = claim.getPropertyLocations();
				for (Iterator iterator2 = locations.iterator(); iterator2
						.hasNext();) {
					PropertyLocation location = (PropertyLocation) iterator2
							.next();
					location.delete();
				}

				List<Adjacency> adjacencies = Adjacency.getAdjacencies(claim
						.getClaimId());
				for (Iterator iterator2 = adjacencies.iterator(); iterator2
						.hasNext();) {
					Adjacency adjacency = (Adjacency) iterator2.next();

					adjacency.delete();

				}

				AdjacenciesNotes adjacenciesNotes = AdjacenciesNotes
						.getAdjacenciesNotes(claim.getClaimId());
				if (adjacenciesNotes != null)
					adjacenciesNotes.delete();

				if (claim.delete() != 0) {

					FileSystemUtilities.deleteClaim(claim.getClaimId());
				}

				i++;
				input.setDownloaded(input.getDownloaded() + 1);
				publishProgress(input);

			}
			if (claim == null
					&& (claimToDownload.getStatusCode().equals(
							ClaimStatus._WITHDRAWN) || claimToDownload
							.getStatusCode().equals(ClaimStatus._REJECTED))) {

				Log.d(this.getClass().getName(),
						"The claim in not present locally  "
								+ claimToDownload.getId()
								+ "but shall not be downloaded");

				/*
				 * In this case the claim is not present locally and due is
				 * stage the client does not have to retrieve it
				 */

				i++;
				input.setDownloaded(input.getDownloaded() + 1);
				publishProgress(input);

			} else if ((claim == null)
					|| (!claimToDownload.getVersion()
							.equals(claim.getVersion()))) {

				Log.d(this.getClass().getName(), "The claim  "
						+ claimToDownload.getId() + " shall be downloaded");

				org.fao.sola.clients.android.opentenure.filesystem.json.model.Claim downloadedClaim = CommunityServerAPI
						.getClaim(claimToDownload.getId());

				if (downloadedClaim == null) {
					success = false;

				} else {
					success = SaveDownloadedClaim.save(downloadedClaim);
				}

				if (success == false) {

					input.setResult(success);
					i++;

				} else {

					i++;
					input.setDownloaded(input.getDownloaded() + 1);
					publishProgress(input);

				}

			}

			else {
				Log.d(this.getClass().getName(), "The claim  "
						+ claimToDownload.getId() + " shall not be downloaded");

				i++;
				input.setDownloaded(input.getDownloaded() + 1);
				publishProgress(input);
			}
		}

		return input;

	}

	protected void onProgressUpdate(GetClaimsInput... progress) {

		GetClaimsInput input = progress[0];

		View mapView = input.getMapView();

		if (mapView != null) {

			ProgressBar bar = (ProgressBar) mapView
					.findViewById(R.id.progress_bar);

			bar.setVisibility(View.VISIBLE);

			TextView label = (TextView) mapView
					.findViewById(R.id.download_claim_label);
			label.setVisibility(View.VISIBLE);

			bar.setProgress(calculateProgress(input.getDownloaded(), input
					.getClaims().size()));
		}

	}

	@Override
	protected void onPostExecute(final GetClaimsInput input) {

		Toast toast;

		if (input.isResult()) {

			OpenTenureApplication.getMapFragment().refreshMap();
			OpenTenureApplication.getPersonsFragment().refresh();

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					OpenTenureApplication.getContext().getResources()
							.getString(R.string.message_claims_downloaded),
					Toast.LENGTH_LONG);

			toast.show();

			View mapView = input.getMapView();

			if (mapView != null) {

				ProgressBar bar = (ProgressBar) mapView
						.findViewById(R.id.progress_bar);
				bar.setVisibility(View.GONE);

				TextView label = (TextView) mapView
						.findViewById(R.id.download_claim_label);
				label.setVisibility(View.GONE);

			}

		} else {

			String message = String.format(
					OpenTenureApplication
							.getContext()
							.getResources()
							.getString(
									R.string.message_error_downloading_claims),
					input.getDownloaded());

			OpenTenureApplication.getMapFragment().refreshMap();
			OpenTenureApplication.getPersonsFragment().refresh();

			toast = Toast.makeText(OpenTenureApplication.getContext(), message,
					Toast.LENGTH_LONG);
			toast.show();

			View mapView = input.getMapView();

			if (mapView != null) {

				ProgressBar bar = (ProgressBar) mapView
						.findViewById(R.id.progress_bar);

				bar.setVisibility(View.GONE);
				TextView label = (TextView) mapView
						.findViewById(R.id.download_claim_label);
				label.setVisibility(View.GONE);
			}
		}

	}

	private int calculateProgress(int downloaded, int total) {

		int progress;

		float factor = (float) downloaded / total;

		progress = (int) (factor * 100);

		return progress;
	}

}
