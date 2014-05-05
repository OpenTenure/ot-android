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

import org.fao.sola.clients.android.opentenure.model.AttachmentStatus;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ClaimAttachmentsListAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final List<String> slogans;
	private final List<String> ids;
	private String claimId;

	public ClaimAttachmentsListAdapter(Context context, List<String> slogans,
			List<String> ids, String claimId) {
		super(context, R.layout.local_claims_list_item, slogans);
		this.context = context;
		this.slogans = slogans;
		this.ids = ids;
		this.claimId = claimId;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.claim_attachments_list_item,
				parent, false);
		TextView slogan = (TextView) rowView
				.findViewById(R.id.attachment_description);
		
		TextView id = (TextView) rowView.findViewById(R.id.attachment_id);
		slogan.setText(slogans.get(position));
		id.setTextSize(8);
		id.setText(ids.get(position));
		
		
		String attachmentId = id.getText().toString();
		Attachment att = Attachment.getAttachment(attachmentId);
		
		TextView status = (TextView) rowView
				.findViewById(R.id.attachment_status);
		if(att.getStatus().equals(AttachmentStatus._UPLOADED) ){
			status.setText(att.getStatus());
			status.setTextColor(context.getResources().getColor(R.color.status_uploaded));
			}
		if(att.getStatus().equals(AttachmentStatus._UPLOADING) ){
			status.setText(att.getStatus());
			status.setTextColor(context.getResources().getColor(R.color.status_uploading));
			}
		else{			
			status.setVisibility(View.INVISIBLE);
		}
		
		
		ImageView picture = (ImageView) rowView.findViewById(R.id.remove_icon);
		picture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder confirmNewPasswordDialog = new AlertDialog.Builder(
						context);
				confirmNewPasswordDialog
						.setTitle(R.string.action_remove_attachment);
				confirmNewPasswordDialog.setMessage(slogans.get(position)
						+ ": "
						+ context.getResources().getString(
								R.string.message_remove_attachment));

				confirmNewPasswordDialog.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Attachment.getAttachment(ids.get(position))
										.delete();
								Toast.makeText(context,
										R.string.attachment_removed,
										Toast.LENGTH_SHORT).show();
								slogans.remove(position);
								ids.remove(position);
								notifyDataSetChanged();
							}
						});
				confirmNewPasswordDialog.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						});

				confirmNewPasswordDialog.show();

			}
		});

		ImageView downloadPic = (ImageView) rowView
				.findViewById(R.id.download_file);

		Claim claim = Claim.getClaim(claimId);

		if (claim.getStatus().equals(ClaimStatus._CREATED)
				|| claim.getStatus().equals(ClaimStatus._UPLOADING)) {
			downloadPic.setVisibility(View.INVISIBLE);
		}

		downloadPic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Toast toast = Toast.makeText(
						OpenTenureApplication.getContext(),
						R.string.message_downloading_attachment,
						Toast.LENGTH_SHORT);
				toast.show();

			}
		});

		return rowView;
	}
}