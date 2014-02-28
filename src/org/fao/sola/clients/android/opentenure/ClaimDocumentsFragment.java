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
import java.util.Collections;
import java.util.List;

import com.ipaulpro.afilechooser.utils.FileUtils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class ClaimDocumentsFragment extends SeparatedListFragment implements
		OnTouchListener {

	private boolean saved = false;
	private static final int REQUEST_CHOOSER = 1234;
	private AlphabetListAdapter adapter = new AlphabetListAdapter();
	private double x;
	private double y;
	private static final double TAP_THRESHOLD_DISTANCE = 10.0;

	public ClaimDocumentsFragment() {
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.claim_documents, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CHOOSER:
			if (resultCode == com.ipaulpro.afilechooser.FileChooserActivity.RESULT_OK) {

				final Uri uri = data.getData();

				String path = FileUtils.getPath(rootView.getContext(), uri);
				
				Log.d(this.getClass().getName(), "Selected file: " + path);
				
				//TODO Actually do something meaningful
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		Toast toast;
switch (item.getItemId()) {

		case R.id.action_new:
			// Create the ACTION_GET_CONTENT Intent
			Intent getContentIntent = FileUtils.createGetContentIntent();

			Intent intent = Intent.createChooser(getContentIntent,
					"Select a file");
			startActivityForResult(intent, REQUEST_CHOOSER);
			return true;

		case R.id.action_save:
			saved = true;
			toast = Toast.makeText(rootView.getContext(), R.string.message_saved, Toast.LENGTH_SHORT);
			toast.show();
			return true;
		case R.id.action_submit:
			if(saved){
				toast = Toast.makeText(rootView.getContext(), R.string.message_submitted, Toast.LENGTH_SHORT);
				toast.show();
			}else{
				toast = Toast.makeText(rootView.getContext(), R.string.message_save_before_submit, Toast.LENGTH_SHORT);
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
		 if (event.getAction() == MotionEvent.ACTION_UP)
		 {

			 double distance = Math.sqrt(Math.pow(event.getX() - x, 2.0)+Math.pow(event.getY() - y, 2.0));
			 if(distance < TAP_THRESHOLD_DISTANCE){
			        Intent intent = new Intent(Intent.ACTION_VIEW);
					String uri = "file://" + Environment.getExternalStorageDirectory().getPath() + "/opentenure/document.pdf";
					intent.setDataAndType(Uri.parse(uri), "application/pdf");
			        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					
					startActivity(intent);
				 return true;
			 }else{
				 return false;
			 }
		 }
		 if (event.getAction() == MotionEvent.ACTION_DOWN)
		 {
			 x = event.getX();
			 y = event.getY();
			 return false;
		 }
		return false;
	}

	protected List<String> populateList() {
		List<String> documents = new ArrayList<String>();
		documents.add("Act of those");
		documents.add("Certificate of what");
		documents.add("Outcome of that");
		documents.add("Declaration of this");
		documents.add("Doc 1");
		documents.add("Doc 2");
		documents.add("Doc 3");
		documents.add("Statement 1");
		documents.add("Statement 2");
		documents.add("Interview");
		return documents;
	}
}