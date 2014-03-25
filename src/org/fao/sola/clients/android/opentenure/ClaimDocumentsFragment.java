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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.MD5;

public class ClaimDocumentsFragment extends SeparatedListFragment implements
		OnTouchListener {

	private boolean saved = false;
	private static final int REQUEST_CHOOSER = 1234;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private AlphabetListAdapter adapter = new AlphabetListAdapter();
	private double x;
	private double y;
	private static final double TAP_THRESHOLD_DISTANCE = 10.0;
	private Uri uri;
	private String fileType;
	private String mimeType;
	private Map<String,String> attachmentIds = new HashMap<String,String>();

	private ClaimDispatcher claimActivity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			claimActivity = (ClaimDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ClaimDispatcher");
		}
	}

	public ClaimDocumentsFragment() {
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.claim_documents, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	private File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.
		String fileName;
		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"opentenure");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.e("opentenure", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.getDefault()).format(new Date());
		if (type == MEDIA_TYPE_IMAGE) {
			fileName = mediaStorageDir.getPath() + File.separator + "IMG_"
					+ timeStamp + ".jpg";
			fileType = "image";
			mimeType = "image/jpeg";
		} else if (type == MEDIA_TYPE_VIDEO) {
			fileName = mediaStorageDir.getPath() + File.separator + "VID_"
					+ timeStamp + ".mp4";
			fileType = "video";
			mimeType = "video/mp4";
		} else {
			return null;
		}

		return new File(fileName);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:

			Log.d(this.getClass().getName(),
					"Captured image: "
							+ FileUtils.getPath(rootView.getContext(), uri));
			AlertDialog.Builder snapshotDialog = new AlertDialog.Builder(rootView.getContext());
			snapshotDialog.setTitle(R.string.new_snapshot);
			final EditText snapshotDescription = new EditText(rootView.getContext());
			snapshotDescription.setInputType(InputType.TYPE_CLASS_TEXT);
			snapshotDialog.setView(snapshotDescription);
			snapshotDialog.setMessage(getResources().getString(R.string.add_description));

			snapshotDialog.setPositiveButton(R.string.confirm, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Attachment attachment = new Attachment();
					attachment.setClaimId(claimActivity.getClaimId());
					attachment.setDescription(snapshotDescription.getText().toString());
					attachment.setFileName(uri.getLastPathSegment());
					attachment.setFileType(fileType);
					attachment.setMimeType(mimeType);
					attachment.setMD5Sum(MD5.calculateMD5(FileUtils.getFile(
							rootView.getContext(), uri)));
					attachment.setPath(uri.getPath());
					attachment.create();
				}
			});
			snapshotDialog.setNegativeButton(R.string.cancel, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});

			snapshotDialog.show();
			
			break;
		case REQUEST_CHOOSER:
			if (resultCode == com.ipaulpro.afilechooser.FileChooserActivity.RESULT_OK) {

				uri = data.getData();

				Log.d(this.getClass().getName(),
						"Selected file: "
								+ FileUtils.getPath(rootView.getContext(), uri));

				fileType = "document";
				mimeType = FileUtils.getMimeType(rootView.getContext(), uri);

				AlertDialog.Builder fileDialog = new AlertDialog.Builder(rootView.getContext());
				fileDialog.setTitle(R.string.new_file);
				final EditText fileDescription = new EditText(rootView.getContext());
				fileDescription.setInputType(InputType.TYPE_CLASS_TEXT);
				fileDialog.setView(fileDescription);
				fileDialog.setMessage(getResources().getString(R.string.add_description));

				fileDialog.setPositiveButton(R.string.confirm, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Attachment attachment = new Attachment();
						attachment.setClaimId(claimActivity.getClaimId());
						attachment.setDescription(fileDescription.getText().toString());
						attachment.setFileName(uri.getLastPathSegment());
						attachment.setFileType("document");
						attachment.setMimeType(mimeType);
						attachment.setMD5Sum(MD5.calculateMD5(FileUtils.getFile(
								rootView.getContext(), uri)));
						attachment.setPath(uri.getPath());
						attachment.create();
					}
				});
				fileDialog.setNegativeButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

				fileDialog.show();
			}
			break;
		}
		populateList();
		updateList();
		super.onActivityResult(requestCode, resultCode, data);
	}

	static ClaimDocumentsFragment newInstance(String claimId, String mode) {
		ClaimDocumentsFragment fragment = new ClaimDocumentsFragment();
		Bundle args = new Bundle();
		args.putString(ClaimActivity.CLAIM_ID_KEY, claimId);
		args.putString(ClaimActivity.MODE_KEY, mode);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		Toast toast;
		Intent intent;
		switch (item.getItemId()) {

		case R.id.action_new_picture:
			if (claimActivity.getClaimId() == null) {
				toast = Toast
						.makeText(rootView.getContext(),
								R.string.message_create_before_edit,
								Toast.LENGTH_SHORT);
				toast.show();
				return true;
			}
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			uri = Uri.fromFile(getOutputMediaFile(MEDIA_TYPE_IMAGE));
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

			// start the image capture Intent
			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			return true;
		case R.id.action_new_attachment:
			if (claimActivity.getClaimId() == null) {
				toast = Toast
						.makeText(rootView.getContext(),
								R.string.message_create_before_edit,
								Toast.LENGTH_SHORT);
				toast.show();
				return true;
			}
			Intent getContentIntent = FileUtils.createGetContentIntent();

			intent = Intent.createChooser(getContentIntent, getResources()
					.getString(R.string.choose_file));
			startActivityForResult(intent, REQUEST_CHOOSER);
			return true;

		case R.id.action_save:
			saved = true;
			toast = Toast.makeText(rootView.getContext(),
					R.string.message_saved, Toast.LENGTH_SHORT);
			toast.show();
			return true;
		case R.id.action_submit:
			if (saved) {
				toast = Toast.makeText(rootView.getContext(),
						R.string.message_submitted, Toast.LENGTH_SHORT);
				toast.show();
			} else {
				toast = Toast
						.makeText(rootView.getContext(),
								R.string.message_save_before_submit,
								Toast.LENGTH_SHORT);
				toast.show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		reset();
		rootView = inflater.inflate(R.layout.claim_documents_list, container,
				false);
		setHasOptionsMenu(true);

		List<String> documents = populateList();
		Collections.sort(documents);

		List<AlphabetListAdapter.Row> rows = getRows(documents);
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
				String attachmentId = attachmentIds.get(((TextView)v).getText().toString());
				Attachment att = Attachment.getAttachment(attachmentId);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse("file://"
						+ att.getPath()), att.getMimeType());
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				startActivity(intent);
				return true;
			} else {
				return false;
			}
		}
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			x = event.getX();
			y = event.getY();
			return false;
		}
		return false;
	}

	protected List<String> populateList() {
		String claimId = claimActivity.getClaimId();
		List<Attachment> attachments;
		List<String> attachmentList = new ArrayList<String>();
		attachmentIds = new HashMap<String,String>();

		if (claimId != null) {
			Claim claim = Claim.getClaim(claimId);
			attachments = claim.getAttachments();
			for (Attachment attachment : attachments) {
				String slogan = attachment.getDescription() + ", Type: "
						+ attachment.getFileType() + " - "
						+ attachment.getMimeType();
				attachmentList.add(slogan);
				attachmentIds.put(slogan, attachment.getAttachmentId());
			}
		}
		return attachmentList;
	}
}