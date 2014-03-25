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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.sola.clients.android.opentenure.model.Claim;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class LocalClaimsFragment extends SeparatedListFragment implements
		OnTouchListener {

	private AlphabetListAdapter adapter = new AlphabetListAdapter();
	private Map<String,String> claimIds = new HashMap<String,String>();
	private double x;
	private double y;
	private static final double TAP_THRESHOLD_DISTANCE = 10.0;

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
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			default:
				populateList();
				updateList();
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		reset();
		rootView = inflater.inflate(R.layout.local_claims_list, container,
				false);
		setHasOptionsMenu(true);

		List<String> localClaims = populateList();
		Collections.sort(localClaims);

		List<AlphabetListAdapter.Row> rows = getRows(localClaims);
		adapter.setRows(rows);
		adapter.setItemOnOnTouchListener(this);
		setListAdapter(adapter);

		updateList();
		return rootView;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {

			double distance = Math.sqrt(Math.pow(event.getX() - x, 2.0)
					+ Math.pow(event.getY() - y, 2.0));
			if (distance < TAP_THRESHOLD_DISTANCE) {
				Intent intent = new Intent(rootView.getContext(),
						ClaimActivity.class);
				intent.putExtra(ClaimActivity.CLAIM_ID_KEY, claimIds.get(((TextView)v).getText().toString()));
				intent.putExtra(ClaimActivity.MODE_KEY, ClaimActivity.MODE_RW);
				startActivity(intent);
				return true;
			} else {
				return false;
			}
		}
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			Log.d(this.getClass().getName(),"Down");
			x = event.getX();
			y = event.getY();
			return false;
		}
		return false;
	}

	protected List<String> populateList() {
		List<Claim> claims = Claim.getAllClaims();
		List<String> claimsList = new ArrayList<String>();
		claimIds = new HashMap<String,String>();
		
		for(Claim claim : claims){
			String slogan = claim.getName() + ", by: " + claim.getPerson().getFirstName()+ " " + claim.getPerson().getLastName();
			claimsList.add(slogan);
			claimIds.put(slogan, claim.getClaimId());
		}
		return claimsList;
	}
}
