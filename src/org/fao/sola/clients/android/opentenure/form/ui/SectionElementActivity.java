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
package org.fao.sola.clients.android.opentenure.form.ui;

import java.util.Locale;

import org.fao.sola.clients.android.opentenure.ModeDispatcher;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.form.SectionElementPayload;
import org.fao.sola.clients.android.opentenure.form.SectionElementTemplate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.astuetz.PagerSlidingTabStrip;

public class SectionElementActivity extends FragmentActivity {

	public static final String SECTION_ELEMENT_TEMPLATE_KEY = "sectionElementTemplate";
	public static final String SECTION_ELEMENT_PAYLOAD_KEY = "sectionElementPayload";
	public static final String MODE_KEY = "mode";
	public static final int SECTION_ELEMENT_ACTIVITY_RESULT = 4321;
	private ModeDispatcher.Mode mode;
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private PagerSlidingTabStrip tabs;
	private SectionElementPayload originalElement;
	private SectionElementPayload editedElement;
	private SectionElementTemplate elementTemplate;
	private SectionElementFragment elementFragment;

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
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(SECTION_ELEMENT_PAYLOAD_KEY, editedElement.toJson());
		outState.putString(SECTION_ELEMENT_TEMPLATE_KEY, elementTemplate.toJson());
		super.onSaveInstanceState(outState);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		String savedElement = null;
		String savedElementTemplate = null;

		if (savedInstanceState != null) {
			savedElement = savedInstanceState
					.getString(SECTION_ELEMENT_PAYLOAD_KEY);
			savedElementTemplate = savedInstanceState
					.getString(SECTION_ELEMENT_TEMPLATE_KEY);
		}

		String intentElement = getIntent().getExtras().getString(
				SECTION_ELEMENT_PAYLOAD_KEY);
		if (intentElement != null) {
			originalElement = SectionElementPayload.fromJson(intentElement);
		}

		if (savedElement != null) {
			editedElement = SectionElementPayload
					.fromJson(savedElement);
		} else {
			editedElement = new SectionElementPayload(originalElement);
		}
		String intentElementTemplate = getIntent().getExtras().getString(
				SECTION_ELEMENT_TEMPLATE_KEY);
		if (savedElementTemplate != null) {
			elementTemplate = SectionElementTemplate
					.fromJson(savedElementTemplate);
		} else {
			elementTemplate = SectionElementTemplate
					.fromJson(intentElementTemplate);
		}

		elementFragment = new SectionElementFragment(editedElement,
				elementTemplate, mode);
		
		setContentView(R.layout.activity_field_group);

		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mViewPager = (ViewPager) findViewById(R.id.field_group_pager);
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());
		mode = ModeDispatcher.Mode
				.valueOf(getIntent().getStringExtra(MODE_KEY));

		mViewPager.setAdapter(mSectionsPagerAdapter);
		tabs.setIndicatorColor(getResources().getColor(
				R.color.ab_tab_indicator_opentenure));
		tabs.setViewPager(mViewPager);

	}

	@Override
	public void onBackPressed() {
		if (originalElement.toJson().equalsIgnoreCase(editedElement.toJson())) {
			Intent resultIntent = new Intent();
			resultIntent.putExtra(SECTION_ELEMENT_PAYLOAD_KEY,
					elementFragment.getEditedElement().toJson());
			setResult(SECTION_ELEMENT_ACTIVITY_RESULT,
					resultIntent);
			finish();
		} else {
			AlertDialog.Builder exitDialog = new AlertDialog.Builder(this);
			exitDialog.setTitle(R.string.title_save_claim_dialog);
			exitDialog.setMessage(getResources().getString(
					R.string.message_save_changes));

			exitDialog.setPositiveButton(R.string.confirm,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent resultIntent = new Intent();
							resultIntent.putExtra(SECTION_ELEMENT_PAYLOAD_KEY,
									elementFragment.getEditedElement().toJson());
							setResult(SECTION_ELEMENT_ACTIVITY_RESULT,
									resultIntent);
							finish();
						}
					});
			exitDialog.setNegativeButton(R.string.cancel,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent resultIntent = new Intent();
							resultIntent.putExtra(SECTION_ELEMENT_PAYLOAD_KEY,
									originalElement.toJson());
							setResult(SECTION_ELEMENT_ACTIVITY_RESULT,
									resultIntent);
							finish();
						}
					});
			exitDialog.show();

		}
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			switch (position) {
			case 0:
				return elementFragment;
			}
			return null;
		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return elementTemplate.getDisplayName().toUpperCase(l);
			}
			return null;
		}
	}
}
