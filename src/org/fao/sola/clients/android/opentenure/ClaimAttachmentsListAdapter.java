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

import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.AttachmentStatus;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.network.GetAttachmentTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ClaimAttachmentsListAdapter extends ArrayAdapter<String> {
	private final Context context;
	private LayoutInflater inflater;
	private final List<String> slogans;
	private final List<String> ids;
	private String claimId;
	private boolean readOnly;

	public ClaimAttachmentsListAdapter(Context context, List<String> slogans,
			List<String> ids, String claimId, boolean readOnly) {
		super(context, R.layout.claim_attachments_list_item, slogans);
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
		this.slogans = slogans;
		this.ids = ids;
		this.claimId = claimId;
		this.readOnly = readOnly;
	}

	static class ViewHolder {
		TextView id;
		TextView slogan;
		TextView status;
		ImageView downloadIcon;
		ImageView removeIcon;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.claim_attachments_list_item,
					parent, false);
			vh = new ViewHolder();

			vh.id = (TextView) convertView.findViewById(R.id.attachment_id);
			vh.slogan = (TextView) convertView
					.findViewById(R.id.attachment_description);
			vh.status = (TextView) convertView
					.findViewById(R.id.attachment_status);
			vh.downloadIcon = (ImageView) convertView
					.findViewById(R.id.download_file);
			vh.removeIcon = (ImageView) convertView
					.findViewById(R.id.remove_icon);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		vh.slogan.setText(slogans.get(position));
		vh.id.setTextSize(8);
		vh.id.setText(ids.get(position));

		String attachmentId = vh.id.getText().toString();
		final Attachment att = Attachment.getAttachment(attachmentId);
		if (att.getStatus().equals(AttachmentStatus._UPLOADED)) {
			vh.status.setText(att.getStatus());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_unmoderated));
		} else if (att.getStatus().equals(AttachmentStatus._UPLOADING)) {
			vh.status.setText(att.getStatus());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_created));
		} else if (att.getStatus().equals(AttachmentStatus._CREATED)) {
			vh.status.setText(att.getStatus());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_created));
		} else if (att.getStatus().equals(AttachmentStatus._DOWNLOAD_FAILED)) {
			vh.status.setText(att.getStatus());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_challenged));
		} else if (att.getStatus().equals(AttachmentStatus._DOWNLOADING)) {
			vh.status.setText(att.getStatus());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_created));
		} else if (att.getStatus()
				.equals(AttachmentStatus._DOWNLOAD_INCOMPLETE)) {
			vh.status.setText(att.getStatus());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_created));
		}
		else if (att.getStatus()
				.equals(AttachmentStatus._UPLOAD_INCOMPLETE)) {
			vh.status.setText(att.getStatus());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_created));
		}
		else if (att.getStatus()
				.equals(AttachmentStatus._UPLOAD_ERROR)) {
			vh.status.setText(att.getStatus());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_challenged));
		}
		if (!readOnly) {
			vh.removeIcon.setOnClickListener(new OnClickListener() {

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

					confirmNewPasswordDialog.setPositiveButton(
							R.string.confirm,
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

		} else {
			((ViewManager)convertView).removeView(vh.removeIcon);
		}
		Claim claim = Claim.getClaim(claimId);

		if ((!claim.getStatus().equals(ClaimStatus._CREATED) && !claim
				.getStatus().equals(ClaimStatus._UPLOADING))
				&& (att.getPath() == null || att.getPath().equals(""))) {
			vh.downloadIcon.setVisibility(View.VISIBLE);
		}

		vh.downloadIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				String[] params = new String[2];
				params[0] = att.getClaimId();
				params[1] = att.getAttachmentId();

				GetAttachmentTask task = new GetAttachmentTask();
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);

				Toast toast = Toast.makeText(
						OpenTenureApplication.getContext(),
						R.string.message_downloading_attachment,
						Toast.LENGTH_SHORT);
				toast.show();

			}
		});

		return convertView;
	}
}