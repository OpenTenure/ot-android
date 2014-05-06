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
import java.util.List;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.network.LoginActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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


public class LoggingFragment extends SeparatedListFragment implements
		OnTouchListener {
	private AlphabetListAdapter adapter = new AlphabetListAdapter();
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private boolean saved = false;
	private double x;
	private double y;
	private static final double TAP_THRESHOLD_DISTANCE = 10.0;
	

	public LoggingFragment() {
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.claim_photos, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"MyCameraApp");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.getDefault()).format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		Toast toast;
		switch (item.getItemId()) {
		case R.id.action_new:

			// create Intent to take a picture and return control to the calling
			// application
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(getOutputMediaFile(MEDIA_TYPE_IMAGE)));

			// start the image capture Intent
			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
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
				toast = Toast.makeText(rootView.getContext(), R.string.message_save_claim_before_submit, Toast.LENGTH_SHORT);
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
		
		if(!OpenTenureApplication.isLoggedin()){
    		
        	Context context = getActivity().getApplicationContext();
        	Intent intent = new Intent( context, LoginActivity.class );            	            	 
        	startActivity(intent);
        	
        	
         // Perform action on click
    	}
		
		
		rootView = inflater.inflate(R.layout.claim_photos_list, container,
				false);
		setHasOptionsMenu(true);

		List<String> photos = populateList();
		Collections.sort(photos);

		List<AlphabetListAdapter.Row> rows = getRows(photos);

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
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					String uri = "file://"
							+ Environment.getExternalStorageDirectory().getPath()
							+ "/opentenure/image.jpg";
					intent.setDataAndType(Uri.parse(uri), "image/*");
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
		List<String> photos = new ArrayList<String>();
		photos.add("Photo of this");
		photos.add("Photo of that");
		photos.add("Beautiful picture");
		photos.add("My face");
		photos.add("Snapshot of the area");
		photos.add("Passport image");
		photos.add("Scan of the certificate");
		return photos;
	}
}
