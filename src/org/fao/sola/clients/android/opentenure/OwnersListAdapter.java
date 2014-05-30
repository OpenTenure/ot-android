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

import java.util.List;

import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.Owner;
import org.fao.sola.clients.android.opentenure.model.Person;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class OwnersListAdapter extends ArrayAdapter<OwnersListTO> {
	private final Context context;
	private List<OwnersListTO> owners;
	private LayoutInflater inflater;
	private int availableShares;
	private boolean readOnly;
	private String claimId;

	public OwnersListAdapter(Context context, List<OwnersListTO> owners, String claimId, boolean readOnly) {
		super(context, R.layout.owners_list_item, owners);
		this.context = context;
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.owners = owners;
		this.readOnly = readOnly;
		this.claimId = claimId;
		setAvailableShares();
	}
	
	private void setAvailableShares(){
		this.availableShares = Claim.MAX_SHARES_PER_CLAIM;
		for(OwnersListTO owner: owners){
			availableShares -= owner.getShares();
		}
	}
	
	static class ViewHolder {
		TextView id;
		TextView slogan;
		Spinner shares;
		ImageView picture;
		ImageView removeIcon;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder vh;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.owners_list_item,
					parent, false);
			vh = new ViewHolder();
			vh.slogan = (TextView) convertView.findViewById(R.id.owner_slogan);
			vh.id = (TextView) convertView.findViewById(R.id.owner_id);
			vh.picture = (ImageView) convertView
					.findViewById(R.id.owner_picture);
			vh.shares = (Spinner) convertView.findViewById(R.id.owner_shares);
			vh.removeIcon = (ImageView) convertView
					.findViewById(R.id.remove_icon);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		vh.slogan.setText(owners.get(position).getSlogan());
		vh.slogan.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context,
						PersonActivity.class);
				intent.putExtra(PersonActivity.PERSON_ID_KEY, owners.get(position).getId());
				intent.putExtra(PersonActivity.MODE_KEY, ModeDispatcher.Mode.MODE_RO.toString());
				context.startActivity(intent);
			}
			
		});

		int shares = owners.get(position).getShares();
		String personId = owners.get(position).getId();

		if(shares >= 1){
			vh.shares.setSelection(shares-1);
		}

		vh.shares.setEnabled(!readOnly);
		vh.shares.setFocusable(!readOnly);
		vh.shares.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
				int shares = pos+1;
				owners.get(position).setShares(shares);
				Owner own = Owner.getOwner(claimId, owners.get(position).getId());
				if(shares > own.getShares() + availableShares){
					Toast.makeText(context,
							R.string.message_no_available_shares,
							Toast.LENGTH_SHORT).show();
					((Spinner)view.getParent()).setSelection(own.getShares()-1);
					
				}else{
					own.setShares(shares);
					own.updateOwner();
					setAvailableShares();
					notifyDataSetChanged();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// Do nothing
			}});

		vh.id.setTextSize(8);
		vh.id.setText(personId);
		vh.picture.setImageBitmap(Person.getPersonPicture(
				context,
				Person.getPersonPictureFile(personId), 96));
		vh.removeIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder removeOwnerDialog = new AlertDialog.Builder(
						context);
				removeOwnerDialog
						.setTitle(R.string.action_remove_owner);
				removeOwnerDialog.setMessage(owners.get(position).getSlogan()
						+ ": "
						+ context.getResources().getString(
								R.string.message_remove_owner));

				removeOwnerDialog.setPositiveButton(
						R.string.confirm,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Owner.getOwner(claimId, owners.get(position).getId())
										.delete();
								owners.remove(position);
								Toast.makeText(context,
										R.string.owner_removed,
										Toast.LENGTH_SHORT).show();
								setAvailableShares();
								notifyDataSetChanged();
							}
						});
				removeOwnerDialog.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						});

				removeOwnerDialog.show();

			}
		});

		return convertView;
	}
}