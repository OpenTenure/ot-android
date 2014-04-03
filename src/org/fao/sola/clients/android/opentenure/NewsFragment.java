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
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NewsFragment extends ListFragment {
	private View rootView;


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
		rootView = inflater.inflate(R.layout.fragment_news, container,
				false);
		setHasOptionsMenu(true);
		update();
		return rootView;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_lock:
			if(OpenTenureApplication.getInstance().getDatabase().isOpen()){
				AlertDialog.Builder oldPasswordDialog = new AlertDialog.Builder(rootView.getContext());
				oldPasswordDialog.setTitle(R.string.title_lock_db);
				final EditText oldPasswordInput = new EditText(rootView.getContext());
				oldPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				oldPasswordDialog.setView(oldPasswordInput);
				oldPasswordDialog.setMessage(getResources().getString(R.string.message_old_db_password));

				oldPasswordDialog.setPositiveButton(R.string.confirm, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						AlertDialog.Builder newPasswordDialog = new AlertDialog.Builder(rootView.getContext());
						newPasswordDialog.setTitle(R.string.title_lock_db);
						final EditText newPasswordInput = new EditText(rootView.getContext());
						newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
						newPasswordDialog.setView(newPasswordInput);
						newPasswordDialog.setMessage(getResources().getString(R.string.message_new_db_password));

						newPasswordDialog.setPositiveButton(R.string.confirm, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								AlertDialog.Builder confirmNewPasswordDialog = new AlertDialog.Builder(rootView.getContext());
								confirmNewPasswordDialog.setTitle(R.string.title_lock_db);
								final EditText confirmNewPasswordInput = new EditText(rootView.getContext());
								confirmNewPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
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

	  @Override
	  public void onListItemClick(ListView l, View v, int position, long id) {
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(((TextView)v.findViewById(R.id.url)).getText().toString()));
			startActivity(intent);
	  }
	  
	protected void update() {
		List<String> news = new ArrayList<String>();
		news.add("Community web site\nVisit the Open Tenure Community web site and tell us what you think about the new look and feel.");
		news.add("FLOSS SOLA news\nLook at the latest news on FLOSS SOLA web site.");
		news.add("Outage\nOpen Tenure service might be down next week due to planned maintenance.");
		List<String> urls = new ArrayList<String>();
		urls.add("http://192.168.137.1:8080/sola/opentenure/index.xhtml");
		urls.add("http://www.flossola.org/home");
		urls.add("http://www.flossola.org/home");
		ArrayAdapter<String> adapter = new NewsListAdapter(rootView.getContext(), news.toArray(new String[news.size()]), urls.toArray(new String[urls.size()]));
	    setListAdapter(adapter);
	    adapter.notifyDataSetChanged();
	}
}