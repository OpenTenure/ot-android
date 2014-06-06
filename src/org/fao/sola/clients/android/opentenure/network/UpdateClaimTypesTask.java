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
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;
import org.fao.sola.clients.android.opentenure.network.response.ClaimType;

import android.os.AsyncTask;
import android.util.Log;


/**
 * Task called to initialize the Application with the values of Types of claim
 * Retrieve all the types from the server
 * **/
public class UpdateClaimTypesTask extends
		AsyncTask<String, Void, List<ClaimType>> {

	@Override
	protected List<ClaimType> doInBackground(String... params) {
		List<ClaimType> types = CommunityServerAPI.getClaimTypes();

		// TODO Auto-generated method stub
		return types;
	}

	@Override
	protected void onPostExecute(List<ClaimType> types) {
		
		

		if (types != null && (types.size() > 0)) {

			for (Iterator iterator = types.iterator(); iterator.hasNext();) {
				ClaimType claimType = (ClaimType) iterator.next();

				org.fao.sola.clients.android.opentenure.model.ClaimType type = new org.fao.sola.clients.android.opentenure.model.ClaimType();

				
				type.setDescription(claimType.getDescription());
				type.setType(claimType.getCode());
				type.setDisplayValue(claimType.getDisplayValue());
				type.add();

			}
			
			OpenTenureApplication.getInstance().setCheckedTypes(true);			

		}
		

	}

}