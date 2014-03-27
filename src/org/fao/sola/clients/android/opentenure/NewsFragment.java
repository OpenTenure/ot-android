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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Toast;

public class NewsFragment extends SeparatedListFragment implements
		OnTouchListener {
	private double x;
	private double y;
	private static final double TAP_THRESHOLD_DISTANCE = 10.0;
	private AlphabetListAdapter adapter = new AlphabetListAdapter();

	public NewsFragment() {
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.news, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		reset();
		rootView = inflater.inflate(R.layout.fragment_news, container,
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
			Log.d(this.getClass().getName(), "Action up");

			double distance = Math.sqrt(Math.pow(event.getX() - x, 2.0)
					+ Math.pow(event.getY() - y, 2.0));
			if (distance < TAP_THRESHOLD_DISTANCE) {
				Intent intent = new Intent(Intent.ACTION_VIEW,
						Uri.parse("http://www.flossola.org/home"));
				startActivity(intent);
				Log.d(this.getClass().getName(), "Moved by " + distance
						+ " while tapping");
				return true;
			} else {
				Log.d(this.getClass().getName(), "Moved by " + distance
						+ " while tapping. That's too much.");
				return false;
			}
		}
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			Log.d(this.getClass().getName(), "Action down");
			x = event.getX();
			y = event.getY();
			return false;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_lock:
			if(OpenTenureApplication.getInstance().getDatabase().isOpen()){
				AlertDialog.Builder oldPasswordDialog = new AlertDialog.Builder(rootView.getContext());
				oldPasswordDialog.setTitle(R.string.title_lock_db);
				final EditText oldPasswordInput = new EditText(rootView.getContext());
				oldPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT);
				oldPasswordDialog.setView(oldPasswordInput);
				oldPasswordDialog.setMessage(getResources().getString(R.string.message_old_db_password));

				oldPasswordDialog.setPositiveButton(R.string.confirm, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						AlertDialog.Builder newPasswordDialog = new AlertDialog.Builder(rootView.getContext());
						newPasswordDialog.setTitle(R.string.title_lock_db);
						final EditText newPasswordInput = new EditText(rootView.getContext());
						newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT);
						newPasswordDialog.setView(newPasswordInput);
						newPasswordDialog.setMessage(getResources().getString(R.string.message_new_db_password));

						newPasswordDialog.setPositiveButton(R.string.confirm, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								AlertDialog.Builder confirmNewPasswordDialog = new AlertDialog.Builder(rootView.getContext());
								confirmNewPasswordDialog.setTitle(R.string.title_lock_db);
								final EditText confirmNewPasswordInput = new EditText(rootView.getContext());
								confirmNewPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT);
								confirmNewPasswordDialog.setView(confirmNewPasswordInput);
								confirmNewPasswordDialog.setMessage(getResources().getString(R.string.message_confirm_new_db_password));

								confirmNewPasswordDialog.setPositiveButton(R.string.confirm, new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										String oldPassword = oldPasswordInput.getText().toString();
										String newPassword = newPasswordInput.getText().toString();
										String confirmNewPassword = confirmNewPasswordInput.getText().toString();
										if(!newPassword.equals(confirmNewPassword)){
											Toast
													.makeText(rootView.getContext(),
															R.string.message_password_dont_match,
															Toast.LENGTH_SHORT).show();

										}else{
											OpenTenureApplication.getInstance().getDatabase().changeEncryption(oldPassword, newPassword);
											if(!OpenTenureApplication.getInstance().getDatabase().isOpen()){
												Toast
												.makeText(rootView.getContext(),
														R.string.message_encryption_failed,
														Toast.LENGTH_SHORT).show();
											}
										}
									}
								});
								confirmNewPasswordDialog.setNegativeButton(R.string.cancel, new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
									}
								});

								confirmNewPasswordDialog.show();

							}
						});
						newPasswordDialog.setNegativeButton(R.string.cancel, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						});

						newPasswordDialog.show();
					}
				});
				oldPasswordDialog.setNegativeButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

				oldPasswordDialog.show();
			}else{
				OpenTenureApplication.getInstance().getDatabase().unlock(rootView.getContext());
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	protected List<String> populateList() {
		List<String> news = new ArrayList<String>();
		news.add("New version available\nA new version of the Open Tenure mobile client has been released for enhanced operation.");
		news.add("Service outage\nOpen Tenure service might be down next week due to planned maintenance.");
		news.add("Community web site\nVisit the Open Tenure Community web site and tell us what you think about the new look and feel.");
		news.add("News 1\nLorem ipsum dolor sit amet.");
		news.add("News 2\nLorem ipsum dolor sit amet.");
		news.add("News 3\nLorem ipsum dolor sit amet.");
		news.add("News 4\nLorem ipsum dolor sit amet.");
		news.add("News 5\nLorem ipsum dolor sit amet.");
		news.add("News 6\nLorem ipsum dolor sit amet.");
		news.add("News 7\nLorem ipsum dolor sit amet.");
		news.add("News 8\nLorem ipsum dolor sit amet.");
		news.add("News 9\nLorem ipsum dolor sit amet.");
		news.add("News 10\nLorem ipsum dolor sit amet.");
		news.add("News 11\nLorem ipsum dolor sit amet.");
		return news;
	}
}