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
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.Owner;
import org.fao.sola.clients.android.opentenure.model.Person;
import org.fao.sola.clients.android.opentenure.model.ShareProperty;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class ShareDetailsActivity extends FragmentActivity implements
		ModeDispatcher {

	public static final String SHARE_ID = "shareId";
	public static final String MODE_KEY = "mode";
	private static final int PERSON_RESULT = 100;
	private ModeDispatcher.Mode mode;

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

		if (intent.getStringExtra(MODE_KEY) != null)
			mode = ModeDispatcher.Mode.valueOf(intent.getStringExtra(MODE_KEY));
		else
			mode = ModeDispatcher.Mode.MODE_RW;

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
					&& !claim.getStatus()
							.equals(ClaimStatus._UPLOAD_INCOMPLETE)) {

				spinner.setFocusable(false);
				spinner.setEnabled(false);
			}

			update();

		} else {

			setContentView(R.layout.share_details);

			share = new ShareProperty();
			share.setClaimId(claimId);

		}

		ImageView newOwner = (ImageView) findViewById(R.id.action_new_owner);
		// Plus button to add a new Owner to the share
		newOwner.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Claim claim = Claim.getClaim(claimId);
				if (claim.getAvailableShares() >= 0) {

					AlertDialog.Builder dialog = new AlertDialog.Builder(
							((ViewGroup) getWindow().getDecorView())
									.getContext());

					dialog.setTitle(R.string.new_entity);
					dialog.setMessage(R.string.message_entity_type);

					dialog.setPositiveButton(R.string.person,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = new Intent(
											((ViewGroup) getWindow()
													.getDecorView())
													.getContext(),
											PersonActivity.class);
									intent.putExtra(
											PersonActivity.PERSON_ID_KEY,
											PersonActivity.CREATE_PERSON_ID);
									intent.putExtra(PersonActivity.ENTIY_TYPE,
											PersonActivity.TYPE_PERSON);
									intent.putExtra(PersonActivity.MODE_KEY,
											mode);
									startActivityForResult(intent,
											PERSON_RESULT);
								}
							});

					dialog.setNegativeButton(R.string.group,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = new Intent(
											((ViewGroup) getWindow()
													.getDecorView())
													.getContext(),
											PersonActivity.class);
									intent.putExtra(
											PersonActivity.PERSON_ID_KEY,
											PersonActivity.CREATE_PERSON_ID);
									intent.putExtra(PersonActivity.ENTIY_TYPE,
											PersonActivity.TYPE_GROUP);
									intent.putExtra(PersonActivity.MODE_KEY,
											mode);
									startActivityForResult(intent,
											PERSON_RESULT);

								}
							});

					dialog.show();

				} else {

					Toast toast = Toast.makeText(
							OpenTenureApplication.getContext(),
							R.string.message_no_available_shares,
							Toast.LENGTH_SHORT);
					toast.show();
				}
				return;

			}
		});

		Claim claim = Claim.getClaim(claimId);
		if (!claim.getStatus().equals(ClaimStatus._CREATED)
				&& !claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR)
				&& !claim.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE)) {

			newOwner.setClickable(false);
			newOwner.setEnabled(false);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
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
					this.ownerList, claimId, this);

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
			List<String> ownersId = new ArrayList<String>();

			if (shareId == null) {
				share.create();
			} else {
				share.updateShare();

				List<Owner> owners = Owner.getOwners(shareId);
				for (Iterator iterator = owners.iterator(); iterator.hasNext();) {
					Owner owner = (Owner) iterator.next();
					ownersId.add(owner.getPersonId());
					owner.delete();
				}

			}

			ListView ownerList = ((ListView) findViewById(R.id.owner_list));
			List<String> idsCreated = new ArrayList<String>();
			for (int i = 0; i <= ownerList.getLastVisiblePosition(); i++) {

				Owner owner = new Owner();
				owner.setPersonId(ownerList.getItemAtPosition(i).toString());
				owner.setShareId(share.getId());
				owner.create();
				idsCreated.add(owner.getPersonId());

			}

			// Here delete the no visible and no used Persons
			List<Owner> owners = Owner.getOwners(shareId);
			for (Iterator iterator = ownersId.iterator(); iterator.hasNext();) {
				String id = (String) iterator.next();

				if (!idsCreated.contains(id))
					Person.getPerson(id).delete();

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

	@Override
	public Mode getMode() {
		// TODO Auto-generated method stub
		return mode;
	}

}
