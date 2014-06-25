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
import org.fao.sola.clients.android.opentenure.model.Adjacency;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.Owner;
import org.fao.sola.clients.android.opentenure.model.Vertex;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;
import org.fao.sola.clients.android.opentenure.network.response.ApiResponse;

import android.app.Application;
import android.os.AsyncTask;
import android.widget.Toast;

public class WithdrawClaim extends AsyncTask<String, Void, ApiResponse> {

	@Override
	protected ApiResponse doInBackground(String... params) {

		String claimId = params[0];
		return CommunityServerAPI.withdrawClaim(claimId);

	}

	@Override
	protected void onPostExecute(ApiResponse res) {

		int httpCode = res.getHttpStatusCode();
		Claim claim = Claim.getClaim(res.getClaimId());
		Toast toast;

		switch (httpCode) {
		case 200:

			List<Owner> list = Owner.getOwners(res.getClaimId());
			for (Iterator iterator = list.iterator(); iterator
					.hasNext();) {
				Owner owner = (Owner) iterator.next();
				owner.delete();
			}

			List<Vertex> vertexList = claim.getVertices();
			for (Iterator iterator = vertexList.iterator(); iterator
					.hasNext();) {
				Vertex vertex = (Vertex) iterator.next();
				vertex.delete();
			}
			
			List<Attachment> attachments = claim.getAttachments();
		
			
			for (Iterator iterator = attachments.iterator(); iterator
					.hasNext();) {
				Attachment attachment = (Attachment) iterator
						.next();
				
				attachment.delete();
				
			}
			
			List<Adjacency> adjacencies = Adjacency.getAdjacencies(res.getClaimId());
			for (Iterator iterator = adjacencies.iterator(); iterator
					.hasNext();) {
				Adjacency adjacency = (Adjacency) iterator
						.next();
				
				adjacency.delete();
				
			}	
		

			
			claim.delete();

			FileSystemUtilities.deleteClaim(res.getClaimId());
			
			OpenTenureApplication.getLocalClaimsFragment()
					.refresh();

			break;

		case 100:

			

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					OpenTenureApplication.getContext().getResources().getString(R.string.message_error_withdraw_claim) + res.getMessage(), Toast.LENGTH_LONG);
			toast.show();


			break;
			
		case 400:

		

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					OpenTenureApplication.getContext().getResources().getString(R.string.message_error_withdraw_claim) + res.getMessage(), Toast.LENGTH_LONG);
			toast.show();


			break;	

		default:
			
			toast = Toast.makeText(OpenTenureApplication.getContext(),
					OpenTenureApplication.getContext().getResources().getString(R.string.message_error_withdraw_claim) + res.getMessage(), Toast.LENGTH_LONG);
			toast.show();


			break;
			
		}

	}

}
