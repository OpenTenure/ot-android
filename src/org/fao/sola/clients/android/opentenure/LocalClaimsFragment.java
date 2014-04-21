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
package org.fao.sola.clients.android.opentenure;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class LocalClaimsFragment extends ListFragment {

	private View rootView;
	private static final int CLAIM_RESULT = 100;

	public LocalClaimsFragment() {
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.local_claims, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		switch (item.getItemId()) {
		case R.id.action_new:
			Intent intent = new Intent(rootView.getContext(),
					ClaimActivity.class);
			intent.putExtra(ClaimActivity.CLAIM_ID_KEY, ClaimActivity.CREATE_CLAIM_ID);
			intent.putExtra(ClaimActivity.MODE_KEY, ClaimActivity.MODE_RW);
			startActivityForResult(intent, CLAIM_RESULT);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		default:
			update();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.local_claims_list, container,
				false);
		setHasOptionsMenu(true);

		update();

		return rootView;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(rootView.getContext(),
				ClaimActivity.class);
		intent.putExtra(ClaimActivity.CLAIM_ID_KEY, ((TextView)v.findViewById(R.id.claim_id)).getText());
		intent.putExtra(ClaimActivity.MODE_KEY, ClaimActivity.MODE_RW);
		startActivityForResult(intent, CLAIM_RESULT);
	}

	protected void update() {
		List<Claim> claims = Claim.getAllClaims();
		List<String> ids = new ArrayList<String>();
		List<String> slogans = new ArrayList<String>();
		List<String> status = new ArrayList<String>();

		for(Claim claim : claims){
			String slogan = claim.getName() + ", by: " + claim.getPerson().getFirstName()+ " " + claim.getPerson().getLastName();
			slogans.add(slogan);
			ids.add(claim.getClaimId());
			if(claim.getStatus().equals(ClaimStatus._UPLOADING)) 
				status.add(claim.getStatus());
			else status.add(" ");


		}
		ArrayAdapter<String> adapter = new LocalClaimsListAdapter(rootView.getContext(), slogans.toArray(new String[slogans.size()]), ids.toArray(new String[ids.size()]), status.toArray(new String[status.size()]));
		setListAdapter(adapter);
		adapter.notifyDataSetChanged();

	}
}
