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

import java.util.Locale;

import org.fao.sola.clients.android.opentenure.form.FormPayload;
import org.fao.sola.clients.android.opentenure.form.FormTemplate;
import org.fao.sola.clients.android.opentenure.form.SectionElementPayload;
import org.fao.sola.clients.android.opentenure.form.SectionTemplate;
import org.fao.sola.clients.android.opentenure.form.ui.SectionElementFragment;
import org.fao.sola.clients.android.opentenure.form.ui.SectionFragment;
import org.fao.sola.clients.android.opentenure.maps.ClaimMapFragment;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.model.SurveyFormTemplate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;
import com.github.amlcurran.showcaseview.ApiUtils;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

public class ClaimActivity extends FragmentActivity implements ClaimDispatcher,
		ModeDispatcher, ClaimListener, OnShowcaseEventListener,
		View.OnClickListener, FormDispatcher {

	public static final String CLAIM_ID_KEY = "claimId";
	public static final String MODE_KEY = "mode";
	public static final String CREATE_CLAIM_ID = "create";
	private ModeDispatcher.Mode mode;
	private String claimId = null;
	FormPayload originalFormPayload;
	FormPayload editedFormPayload;
	FormTemplate formTemplate;

	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	PagerSlidingTabStrip tabs;
	SparseArray<Fragment> fragmentReferences = new SparseArray<Fragment>();

	// SHOWCASE Variables
	ShowcaseView sv;
	private int counter = 0;
	private final ApiUtils apiUtils = new ApiUtils();
	public static final String FIRST_RUN_CLAIM_ACTIVITY = "__FIRST_RUN_CLAIM_ACTIVITY__";

	// END SHOW CASE

	@Override
	public void onDestroy() {
		super.onDestroy();
		OpenTenureApplication.getInstance().getDatabase().sync();
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			final ClaimDetailsFragment fragment = (ClaimDetailsFragment) fragmentReferences
					.get(0);

			if (fragment != null) {
				if (fragment.checkChanges()) {
					return true;
				} else {
					return super.onKeyDown(keyCode, event);
				}
			} else
				return super.onKeyDown(keyCode, event);
		} else
			return super.onKeyDown(keyCode, event);
	}

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
		outState.putString(CLAIM_ID_KEY, claimId);
		super.onSaveInstanceState(outState);

	}

	private String getFirstRun() {
		String result = "False";
		Configuration firstRun = Configuration
				.getConfigurationByName(FIRST_RUN_CLAIM_ACTIVITY);

		if (firstRun != null) {
			result = firstRun.getValue();
			firstRun.setValue("False");
			firstRun.update();
		} else {
			firstRun = new Configuration();
			firstRun.setName(FIRST_RUN_CLAIM_ACTIVITY);
			firstRun.setValue("False");
			firstRun.create();
			result = "True";
		}
		return result;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		mode = ModeDispatcher.Mode
				.valueOf(getIntent().getStringExtra(MODE_KEY));
		setContentView(R.layout.activity_claim);

		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mViewPager = (ViewPager) findViewById(R.id.claim_pager);
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		mViewPager.setAdapter(mSectionsPagerAdapter);
		tabs.setIndicatorColor(getResources().getColor(
				R.color.ab_tab_indicator_opentenure));
		tabs.setViewPager(mViewPager);

		String savedInstanceClaimId = null;

		if (savedInstanceState != null) {
			savedInstanceClaimId = savedInstanceState.getString(CLAIM_ID_KEY);
		}

		String intentClaimId = getIntent().getExtras().getString(CLAIM_ID_KEY);

		if (savedInstanceClaimId != null) {
			setClaimId(savedInstanceClaimId);
		} else if (intentClaimId != null
				&& !intentClaimId.equalsIgnoreCase(CREATE_CLAIM_ID)) {
			setClaimId(intentClaimId);

		}

		formTemplate = SurveyFormTemplate.getDefaultSurveyFormTemplate();
		originalFormPayload = new FormPayload(formTemplate,getClaimId());
		editedFormPayload = new FormPayload(originalFormPayload);

		// ShowCase Main
		if (getFirstRun().contentEquals("True")) {
			sv = new ShowcaseView.Builder(this, true)
					.setTarget(
							new ViewTarget(tabs.getTabsContainer()
									.getChildAt(0)))
					.setContentTitle(getString(R.string.showcase_claim_title))
					.setContentText(getString(R.string.showcase_claim_message))
					.setStyle(R.style.CustomShowcaseTheme)
					.setOnClickListener(this).build();
			sv.setButtonText(getString(R.string.next));
			sv.setSkipButtonText(getString(R.string.skip));
			setAlpha(0.2f, tabs.getTabsContainer().getChildAt(0), tabs
					.getTabsContainer().getChildAt(1), tabs.getTabsContainer()
					.getChildAt(2), tabs.getTabsContainer().getChildAt(3), tabs
					.getTabsContainer().getChildAt(4), tabs.getTabsContainer()
					.getChildAt(5), tabs.getTabsContainer().getChildAt(6),
					mViewPager);
		}

	}

	private void setAlpha(float alpha, View... views) {
		if (apiUtils.isCompatWithHoneycomb()) {
			for (View view : views) {
				view.setAlpha(alpha);
			}
		}
	}

	@Override
	public void onClick(View v) {

		if (v.toString().indexOf("skip") > 0) {
			counter = 0;
			sv.hide();
			setAlpha(1.0f, tabs.getTabsContainer().getChildAt(0), tabs
					.getTabsContainer().getChildAt(1), tabs.getTabsContainer()
					.getChildAt(2), tabs.getTabsContainer().getChildAt(3), tabs
					.getTabsContainer().getChildAt(4), tabs.getTabsContainer()
					.getChildAt(5), tabs.getTabsContainer().getChildAt(6),
					tabs, mViewPager);
			mViewPager.setCurrentItem(0);
			counter = 0;
			return;
		}

		switch (counter) {
		// case 0:
		// sv.setShowcase(
		// new ViewTarget(tabs.getTabsContainer().getChildAt(0)), true);
		// sv.setContentTitle(getString(R.string.title_claim).toUpperCase());
		// sv.setContentText(getString(R.string.showcase_claim_message));
		// mViewPager.setCurrentItem(0);
		// setAlpha(1.0f, tabs.getTabsContainer().getChildAt(0));
		// break;

		case 0:
			sv.setShowcase(new ViewTarget(findViewById(R.id.action_export)),
					true);
			sv.setContentTitle("  ");
			sv.setContentText(getString(R.string.showcase_actionClaimDetails_message));
			break;
		case 1:
			sv.setShowcase(
					new ViewTarget(tabs.getTabsContainer().getChildAt(1)), true);
			sv.setContentTitle(getString(R.string.title_claim_map)
					.toUpperCase(Locale.getDefault()));
			sv.setContentText(getString(R.string.showcase_claim_map_message));
			setAlpha(1.0f, tabs.getTabsContainer().getChildAt(1));
			mViewPager.setCurrentItem(1);
			break;
		case 2:
			sv.setShowcase(new ViewTarget(mViewPager), true);
			sv.setContentTitle("  ");
			sv.setContentText(getString(R.string.showcase_claim_mapdraw_message));
			mViewPager.setCurrentItem(1);
			break;

		case 3:
			sv.setShowcase(new ViewTarget(
					findViewById(R.id.action_center_and_follow)), true);
			sv.setContentTitle("  ");
			sv.setContentText(getString(R.string.showcase_actionClaimMap_message));
			break;
		case 4:
			sv.setShowcase(
					new ViewTarget(tabs.getTabsContainer().getChildAt(2)), true);
			sv.setContentTitle(getString(R.string.title_claim_documents)
					.toUpperCase(Locale.getDefault()));
			sv.setContentText(getString(R.string.showcase_claim_document_message));
			setAlpha(1.0f, tabs.getTabsContainer().getChildAt(2));
			mViewPager.setCurrentItem(2);
			break;
		// case 5:
		// sv.setShowcase(new ViewTarget(
		// findViewById(R.id.action_new_attachment)), true);
		// sv.setContentTitle("  ");
		// sv.setContentText(getString(R.string.showcase_claim_documentAttach_message));
		// break;
		// case 6:
		// sv.setShowcase(
		// new ViewTarget(tabs.getTabsContainer().getChildAt(3)), true);
		// sv.setContentTitle(getString(R.string.title_claim_additional_info)
		// .toUpperCase());
		// sv.setContentText("ECCO TERZO TAB");
		// setAlpha(1.0f, tabs.getTabsContainer().getChildAt(3));
		// mViewPager.setCurrentItem(3);
		// break;
		case 5:
			sv.setShowcase(
					new ViewTarget(tabs.getTabsContainer().getChildAt(4)), true);
			sv.setContentTitle(getString(R.string.title_claim_adjacencies)
					.toUpperCase(Locale.getDefault()));
			sv.setContentText(getString(R.string.showcase_claim_adjacencies_message));
			setAlpha(1.0f, tabs.getTabsContainer().getChildAt(4));
			mViewPager.setCurrentItem(4);
			break;
		case 6:
			sv.setShowcase(
					new ViewTarget(tabs.getTabsContainer().getChildAt(5)), true);
			sv.setContentTitle(getString(R.string.title_claim_challenges)
					.toUpperCase(Locale.getDefault()));
			sv.setContentText(getString(R.string.showcase_claim_challenges_message));
			setAlpha(1.0f, tabs.getTabsContainer().getChildAt(5));
			mViewPager.setCurrentItem(5);
			break;
		case 7:
			sv.setShowcase(
					new ViewTarget(tabs.getTabsContainer().getChildAt(6)), true);
			sv.setContentTitle(getString(R.string.title_claim_owners)
					.toUpperCase(Locale.getDefault()));
			sv.setContentText(getString(R.string.showcase_claim_shares_message));
			setAlpha(1.0f, tabs.getTabsContainer().getChildAt(6));
			mViewPager.setCurrentItem(6);
			break;
		case 8:
			sv.setShowcase(
					new ViewTarget(tabs.getTabsContainer().getChildAt(0)), true);
			sv.setContentTitle(("  "));
			sv.setContentText(getString(R.string.showcase_end_message));
			setAlpha(0.6f, tabs.getTabsContainer().getChildAt(0), tabs
					.getTabsContainer().getChildAt(1), tabs.getTabsContainer()
					.getChildAt(2), tabs.getTabsContainer().getChildAt(3), tabs
					.getTabsContainer().getChildAt(4), tabs.getTabsContainer()
					.getChildAt(5), tabs.getTabsContainer().getChildAt(6), tabs);
			sv.setButtonText(getString(R.string.close));
			mViewPager.setCurrentItem(0);
			break;
		case 9:
			sv.hide();
			setAlpha(1.0f, tabs, mViewPager);
			counter = 0;
			break;
		}

		counter++;
	}

	@Override
	public void onShowcaseViewHide(ShowcaseView showcaseView) {
		// if (apiUtils.isCompatWithHoneycomb()) {
		// listView.setAlpha(1f);
		// }
		// buttonBlocked.setText(R.string.button_show);
		// //buttonBlocked.setEnabled(false);
	}

	@Override
	public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
	}

	@Override
	public void onShowcaseViewShow(ShowcaseView showcaseView) {
		// dimView(listView);
		// buttonBlocked.setText(R.string.button_hide);
		// //buttonBlocked.setEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_claim_showcase:
			// ShowCase Tutorial
			counter = 0;
			sv = new ShowcaseView.Builder(this, true)
					.setTarget(
							new ViewTarget(tabs.getTabsContainer()
									.getChildAt(0)))
					.setContentTitle(getString(R.string.showcase_claim_title))
					.setContentText(getString(R.string.showcase_claim_message))
					.setStyle(R.style.CustomShowcaseTheme)
					.setOnClickListener(this).build();
			sv.setButtonText(getString(R.string.next));
			sv.setSkipButtonText(getString(R.string.skip));
			setAlpha(0.2f, tabs.getTabsContainer().getChildAt(0), tabs
					.getTabsContainer().getChildAt(1), tabs.getTabsContainer()
					.getChildAt(2), tabs.getTabsContainer().getChildAt(3), tabs
					.getTabsContainer().getChildAt(4), tabs.getTabsContainer()
					.getChildAt(5), tabs.getTabsContainer().getChildAt(6),
					mViewPager);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// END SHOWCASE

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.claim, menu);
		return true;
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public void destroyItem(android.view.ViewGroup container, int position,
				Object object) {

			fragmentReferences.remove(position);

		}

		@Override
		public Fragment getItem(int position) {

			Fragment fragment;
			int sectionPosition = position - 7;

			switch (position) {
			case 0:
				fragment = new ClaimDetailsFragment();
				break;
			case 1:
				fragment = new ClaimMapFragment();
				break;
			case 2:
				fragment = new ClaimDocumentsFragment();
				break;
			case 3:
				fragment = new ClaimAdditionalInfoFragments();
				break;
			case 4:
				fragment = new AdjacentClaimsFragment();
				break;
			case 5:
				fragment = new ChallengingClaimsFragment();
				break;
			case 6:
				fragment = new OwnersFragment();
				break;
			default:
				SectionTemplate sectionTemplate = formTemplate.getSections().get(sectionPosition);
				if(sectionTemplate == null){
					return null;
				}
				if(sectionTemplate.getMaxOccurrences() > 1){
					fragment = new SectionFragment(editedFormPayload.getSections().get(sectionPosition), sectionTemplate, mode);
				}else{
					if(editedFormPayload.getSections().get(sectionPosition).getElements().size()==0){
						editedFormPayload.getSections().get(sectionPosition).getElements().add(new SectionElementPayload(sectionTemplate));
					}
					fragment = new SectionElementFragment(editedFormPayload.getSections().get(sectionPosition).getElements().get(0), sectionTemplate, mode);
				}
			}
			fragmentReferences.put(position, fragment);
			return fragment;
		}

		@Override
		public int getCount() {
				return 7 + getNumberOfSections();
		}
		
		private int getNumberOfSections(){
			if(editedFormPayload != null){
				return editedFormPayload.getSections().size();
			}else if(formTemplate != null){
				return formTemplate.getSections().size();
			}else{
				return 0;
			}
		}

		private String getSectionTitle(int position){
			if(editedFormPayload != null){
				return editedFormPayload.getSections().get(position).getDisplayName().toUpperCase(Locale.US);
			}else if(formTemplate != null){
				return formTemplate.getSections().get(position).getDisplayName().toUpperCase(Locale.getDefault());
			}else{
				return "";
			}
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();

			int sectionPosition = position - 7;

			switch (position) {
			case 0:
				return getString(R.string.title_claim).toUpperCase(l);
			case 1:
				return getString(R.string.title_claim_map).toUpperCase(l);
			case 2:
				return getString(R.string.title_claim_documents).toUpperCase(l);
			case 3:
				return getString(R.string.title_claim_additional_info)
						.toUpperCase(l);
			case 4:
				return getString(R.string.title_claim_adjacencies).toUpperCase(
						l);
			case 5:
				return getString(R.string.title_claim_challenges)
						.toUpperCase(l);
			case 6:
				return getString(R.string.title_claim_owners).toUpperCase(l);
			default:
				return getSectionTitle(sectionPosition);
			}
		}
	}

	@Override
	public void setClaimId(String claimId) {
		this.claimId = claimId;
		if (claimId != null && !claimId.equalsIgnoreCase(CREATE_CLAIM_ID)) {
			Claim claim = Claim.getClaim(claimId);
			setTitle(getResources().getString(R.string.app_name) + ": "
					+ claim.getName());
			if(claim.getSurveyForm()!=null){
				originalFormPayload = claim.getSurveyForm();
				if(originalFormPayload != null){
					editedFormPayload = new FormPayload(originalFormPayload);
				}else{
					originalFormPayload = new FormPayload();
					originalFormPayload.setTemplate(new FormTemplate());
					editedFormPayload = new FormPayload();
					editedFormPayload.setTemplate(new FormTemplate());
				}
			}
		}
	}

	@Override
	public String getClaimId() {
		return claimId;
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public void onClaimSaved() {
		ClaimMapFragment claimMapFragment = (ClaimMapFragment) fragmentReferences
				.get(1);
		if (claimMapFragment != null)
			 claimMapFragment.onClaimSaved();
	}

	@Override
	public FormPayload getEditedFormPayload() {
		return editedFormPayload;
	}

	@Override
	public FormTemplate getFormTemplate() {
		return formTemplate;
	}

	@Override
	public FormPayload getOriginalFormPayload() {
		return originalFormPayload;
	}

}
