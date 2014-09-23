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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.Owner;
import org.fao.sola.clients.android.opentenure.model.Person;
import org.fao.sola.clients.android.opentenure.model.ShareProperty;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ShareDetailsActivity extends FragmentActivity {

	public static final String SHARE_ID = "shareId";

	private String claimId;
	private String shareId;
	ShareProperty share;
	List<String> ownerList = new ArrayList<String>();
	View rootView;

	@Override
	public void onDestroy() {
		super.onDestroy();
		OpenTenureApplication.getInstance().getDatabase().sync();
	};

	@Override
	public void onPause() {
		super.onPause();
		OpenTenureApplication.getInstance().getDatabase().sync();
	};

	@Override
	public void onResume() {
		super.onResume();
		OpenTenureApplication.getInstance().getDatabase().open();
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Intent intent = getIntent();

		claimId = intent.getStringExtra("claimId");

		if (intent.getStringExtra(SHARE_ID) != null) {
			/* Load Share Property */
			shareId = intent.getStringExtra("shareId");
			setContentView(R.layout.share_details);

			share = ShareProperty.getShare(shareId);
			claimId = share.getClaimId();

			List<Owner> owners = Owner.getOwners(shareId);
			for (Iterator iterator = owners.iterator(); iterator.hasNext();) {
				Owner owner = (Owner) iterator.next();
				ownerList.add(owner.getPersonId());
			}

			Spinner spinner = (Spinner) findViewById(R.id.share_shares);
			spinner.setSelection(share.getShares() - 1);

			Claim claim = Claim.getClaim(claimId);

			if (!claim.getStatus().equals(ClaimStatus._CREATED)
					&& !claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR)
					&& !claim.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE)) {
				
				spinner.setFocusable(false);
				spinner.setEnabled(false);
			} 
			
			update();

		} else {

			setContentView(R.layout.share_details);

			share = new ShareProperty();
			share.setClaimId(claimId);

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.share_details, menu);
		
		Claim claim = Claim.getClaim(claimId);
		if (claim != null && !claim.isModifiable()) {
			menu.removeItem(R.id.action_new);
			menu.removeItem(R.id.action_save);
		}
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new:
			Claim claim = Claim.getClaim(claimId);
			if (claim.getAvailableShares() >= 0) {

				Intent intent = new Intent(OpenTenureApplication.getContext(),
						SelectPersonActivity.class);

				// SOLA DB cannot store the same person twice

				ArrayList<String> idsWithSharesOrClaims = Person.getIdsWithSharesOrClaims();

				intent.putStringArrayListExtra(
						SelectPersonActivity.EXCLUDE_PERSON_IDS_KEY,
						idsWithSharesOrClaims);

				startActivityForResult(intent,
						SelectPersonActivity.SELECT_PERSON_ACTIVITY_RESULT);
			} else {
				Toast toast = Toast.makeText(
						OpenTenureApplication.getContext(),
						R.string.message_no_available_shares,
						Toast.LENGTH_SHORT);
				toast.show();
			}
			return true;
		case R.id.action_save:

			save();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (data != null) { // No selection has been done

			switch (requestCode) {
			case SelectPersonActivity.SELECT_PERSON_ACTIVITY_RESULT:
				String personId = data
						.getStringExtra(PersonActivity.PERSON_ID_KEY);

				if (claimId != null) {

					if (Owner.getOwner(personId, share.getId()) != null
							|| ownerList.contains(personId)) {

						Toast.makeText(OpenTenureApplication.getContext(),
								R.string.message_already_owner,
								Toast.LENGTH_LONG).show();
					} else {
						ownerList.add(personId);
					}
				}

				update();
				break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	protected void update() {

		if (claimId != null) {

			Claim claim = Claim.getClaim(claimId);

			int i = 0;

			ArrayAdapter<String> adapter = null;

			adapter = new OwnersListAdapter(OpenTenureApplication.getContext(),
					this.ownerList, claimId);

			// adapter = new OwnersListAdapter(context, owners);
			ListView ownerList = (ListView) findViewById(R.id.owner_list);
			ownerList.setAdapter(adapter);
			adapter.notifyDataSetChanged();

		}
	}

	private int save() {

		try {
			Spinner spinner = (Spinner) findViewById(R.id.share_shares);
			int value = Integer.parseInt(spinner.getSelectedItem().toString());

			int delta = value - share.getShares();

			if (delta > 0
					&& Claim.getClaim(claimId).getAvailableShares() < delta) {

				Toast toast = Toast
						.makeText(OpenTenureApplication.getContext(),
								R.string.message_no_available_shares,
								Toast.LENGTH_LONG);
				toast.show();

				return 0;
			}

			share.setShares(value);

			if (shareId == null) {
				share.create();
			} else {
				share.updateShare();

				List<Owner> owners = Owner.getOwners(shareId);
				for (Iterator iterator = owners.iterator(); iterator.hasNext();) {
					Owner owner = (Owner) iterator.next();
					owner.delete();
				}

			}

			ListView ownerList = ((ListView) findViewById(R.id.owner_list));

			for (int i = 0; i <= ownerList.getLastVisiblePosition(); i++) {

				Owner owner = new Owner();
				owner.setPersonId(ownerList.getItemAtPosition(i).toString());
				owner.setShareId(share.getId());
				owner.create();

			}

			Toast toast = Toast.makeText(OpenTenureApplication.getContext(),
					R.string.message_share_saved, Toast.LENGTH_SHORT);
			toast.show();

		} catch (Exception e) {
			Toast toast = Toast.makeText(OpenTenureApplication.getContext(),
					R.string.message_share_not_saved, Toast.LENGTH_SHORT);
			toast.show();
		}

		return 0;
	}

}
