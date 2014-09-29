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

import org.fao.sola.clients.android.opentenure.form.server.FormRetriever;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.network.AlertInitializationTask;
import org.fao.sola.clients.android.opentenure.network.UpdateClaimTypesTask;
import org.fao.sola.clients.android.opentenure.network.UpdateCommunityArea;
import org.fao.sola.clients.android.opentenure.network.UpdateDocumentTypesTask;
import org.fao.sola.clients.android.opentenure.network.UpdateIdTypesTask;
import org.fao.sola.clients.android.opentenure.network.UpdateLandUsesTask;
import org.fao.sola.clients.android.opentenure.network.response.GetClaimsInput;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class NewsFragment extends ListFragment {
	private View rootView;
	MenuItem alert;

	public NewsFragment() {
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.news, menu);

		alert = menu.getItem(2);
		alert.setVisible(false);

		OpenTenureApplication.setNewsFragment(getActivity());

		if (!Boolean.parseBoolean(Configuration.getConfigurationByName(
				"isInitialized").getValue())) {
			alert.setVisible(true);
		} else {
			alert.setVisible(false);

		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_news, container, false);
		setHasOptionsMenu(true);
		update();
		return rootView;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_alert:

			Log.d(this.getClass().getName(),
					"starting tasks for static data download");

			ProgressBar bar = (ProgressBar) rootView
					.findViewById(R.id.progress_bar);

			if (!OpenTenureApplication.getInstance().isCheckedTypes()) {
				Log.d(this.getClass().getName(),
						"starting tasks for claim type download");

				UpdateClaimTypesTask updateCT = new UpdateClaimTypesTask();
				updateCT.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

			}

			if (!OpenTenureApplication.getInstance().isCheckedDocTypes()) {
				Log.d(this.getClass().getName(),
						"starting tasks for document type download");

				UpdateDocumentTypesTask updateCT = new UpdateDocumentTypesTask();
				updateCT.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

			}

			if (!OpenTenureApplication.getInstance().isCheckedIdTypes()) {
				Log.d(this.getClass().getName(),
						"starting tasks for ID type download");

				UpdateIdTypesTask updateIdType = new UpdateIdTypesTask();
				updateIdType.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
			if (!OpenTenureApplication.getInstance().isCheckedLandUses()) {
				Log.d(this.getClass().getName(),
						"starting tasks for land use type download");

				UpdateLandUsesTask updateLu = new UpdateLandUsesTask();
				updateLu.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
			if (!OpenTenureApplication.getInstance().isCheckedCommunityArea()) {
				Log.d(this.getClass().getName(),
						"starting tasks for community area download");

				UpdateCommunityArea updateArea = new UpdateCommunityArea();
				updateArea.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
			if (!OpenTenureApplication.getInstance().isCheckedForm()) {
				Log.d(this.getClass().getName(),
						"starting tasks for form retrieval");

				SharedPreferences OpenTenurePreferences = PreferenceManager
						.getDefaultSharedPreferences(getActivity());
				String defaultFormUrl = OpenTenurePreferences
						.getString(OpenTenurePreferencesActivity.CS_URL_PREF,OpenTenureApplication._DEFAULT_COMMUNITY_SERVER);
				if(!defaultFormUrl.equalsIgnoreCase("")){
					defaultFormUrl += "/ws/en-us/claim/getDefaultFormTemplate";
				}
				String formUrl = OpenTenurePreferences
						.getString(OpenTenurePreferencesActivity.FORM_URL_PREF,
								defaultFormUrl);
				 FormRetriever formRetriever = new FormRetriever();
				 formRetriever.setFormUrl(formUrl);
				 formRetriever.execute();
			}

			GetClaimsInput input = new GetClaimsInput();
			input.setMapView(rootView);

			AlertInitializationTask fakeTask = new AlertInitializationTask(this.getActivity());
			fakeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, input);

			// AlertDialog.Builder alertDialog = new AlertDialog.Builder(
			// rootView.getContext());
			// alertDialog.setTitle(R.string.message_title_app_not_initialized);
			// alertDialog.setMessage(getResources().getString(
			// R.string.message_app_not_initialized));
			//
			// alertDialog.setPositiveButton(R.string.confirm,
			// new OnClickListener() {
			// @Override
			// public void onClick(DialogInterface dialog,
			// int which) {
			// AlertDialog.Builder newPasswordDialog = new AlertDialog.Builder(
			// rootView.getContext());
			//
			// return;
			//
			// }});
			//
			// alertDialog.show();
			return true;

		case R.id.action_lock:
			if (OpenTenureApplication.getInstance().getDatabase().isOpen()) {
				AlertDialog.Builder oldPasswordDialog = new AlertDialog.Builder(
						rootView.getContext());
				oldPasswordDialog.setTitle(R.string.title_lock_db);
				final EditText oldPasswordInput = new EditText(
						rootView.getContext());
				oldPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_VARIATION_PASSWORD);
				oldPasswordDialog.setView(oldPasswordInput);
				oldPasswordDialog.setMessage(getResources().getString(
						R.string.message_old_db_password));

				oldPasswordDialog.setPositiveButton(R.string.confirm,
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								AlertDialog.Builder newPasswordDialog = new AlertDialog.Builder(
										rootView.getContext());
								newPasswordDialog
										.setTitle(R.string.title_lock_db);
								final EditText newPasswordInput = new EditText(
										rootView.getContext());
								newPasswordInput
										.setInputType(InputType.TYPE_CLASS_TEXT
												| InputType.TYPE_TEXT_VARIATION_PASSWORD);
								newPasswordDialog.setView(newPasswordInput);
								newPasswordDialog
										.setMessage(getResources()
												.getString(
														R.string.message_new_db_password));

								newPasswordDialog.setPositiveButton(
										R.string.confirm,
										new OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												AlertDialog.Builder confirmNewPasswordDialog = new AlertDialog.Builder(
														rootView.getContext());
												confirmNewPasswordDialog
														.setTitle(R.string.title_lock_db);
												final EditText confirmNewPasswordInput = new EditText(
														rootView.getContext());
												confirmNewPasswordInput
														.setInputType(InputType.TYPE_CLASS_TEXT
																| InputType.TYPE_TEXT_VARIATION_PASSWORD);
												confirmNewPasswordDialog
														.setView(confirmNewPasswordInput);
												confirmNewPasswordDialog
														.setMessage(getResources()
																.getString(
																		R.string.message_confirm_new_db_password));

												confirmNewPasswordDialog
														.setPositiveButton(
																R.string.confirm,
																new OnClickListener() {

																	@Override
																	public void onClick(
																			DialogInterface dialog,
																			int which) {
																		String oldPassword = oldPasswordInput
																				.getText()
																				.toString();
																		String newPassword = newPasswordInput
																				.getText()
																				.toString();
																		String confirmNewPassword = confirmNewPasswordInput
																				.getText()
																				.toString();
																		if (!newPassword
																				.equals(confirmNewPassword)) {
																			Toast.makeText(
																					rootView.getContext(),
																					R.string.message_password_dont_match,
																					Toast.LENGTH_SHORT)
																					.show();

																		} else {
																			if ("".equalsIgnoreCase(newPassword)) {
																				newPassword = null;
																			}
																			OpenTenureApplication
																					.getInstance()
																					.getDatabase()
																					.changeEncryption(
																							oldPassword,
																							newPassword);
																			if (!OpenTenureApplication
																					.getInstance()
																					.getDatabase()
																					.isOpen()) {
																				Toast.makeText(
																						rootView.getContext(),
																						R.string.message_encryption_failed,
																						Toast.LENGTH_SHORT)
																						.show();
																			}
																		}
																	}
																});
												confirmNewPasswordDialog
														.setNegativeButton(
																R.string.cancel,
																new OnClickListener() {

																	@Override
																	public void onClick(
																			DialogInterface dialog,
																			int which) {
																	}
																});

												confirmNewPasswordDialog.show();

											}
										});
								newPasswordDialog.setNegativeButton(
										R.string.cancel, new OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
											}
										});

								newPasswordDialog.show();
							}
						});
				oldPasswordDialog.setNegativeButton(R.string.cancel,
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						});

				oldPasswordDialog.show();
			} else {
				OpenTenureApplication.getInstance().getDatabase()
						.unlock(rootView.getContext());
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(((TextView) v.findViewById(R.id.url)).getText()
							.toString()));
			startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(
					rootView.getContext(),
					getResources().getString(R.string.message_invalid_url)
							+ ": "
							+ ((TextView) v.findViewById(R.id.url)).getText()
									.toString(), Toast.LENGTH_SHORT).show();
		}
	}

	protected void update() {
		List<String> news = new ArrayList<String>();
		news.add("Community web site\nVisit the Open Tenure Community web site and tell us what you think about the new look and feel.");
		news.add("FLOSS SOLA news\nLook at the latest news on FLOSS SOLA web site.");
		news.add("Outage\nOpen Tenure service might be down next week due to planned maintenance.");
		List<String> urls = new ArrayList<String>();
		SharedPreferences OpenTenurePreferences = PreferenceManager
				.getDefaultSharedPreferences(rootView.getContext());

		String csUrl = OpenTenurePreferences.getString(
				OpenTenurePreferencesActivity.CS_URL_PREF,
				"http://ot.flossola.org");
		urls.add(csUrl);
		urls.add("http://www.flossola.org/home");
		urls.add("http://www.flossola.org/home");
		ArrayAdapter<String> adapter = new NewsListAdapter(
				rootView.getContext(), news.toArray(new String[news.size()]),
				urls.toArray(new String[urls.size()]));
		setListAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
}