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

import org.fao.sola.clients.android.opentenure.maps.ClaimMapFragment;
import org.fao.sola.clients.android.opentenure.model.Claim;

import com.astuetz.PagerSlidingTabStrip;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;

public class ClaimActivity extends FragmentActivity implements ClaimDispatcher, ModeDispatcher {

	public static final String CLAIM_ID_KEY = "claimId";
	public static final String MODE_KEY = "mode";
	public static final String CREATE_CLAIM_ID = "create";
	private ModeDispatcher.Mode mode;
	private String claimId = null;
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	PagerSlidingTabStrip tabs;

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
		outState.putString(CLAIM_ID_KEY, claimId);
		super.onSaveInstanceState(outState);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		mode = ModeDispatcher.Mode.valueOf(getIntent().getStringExtra(MODE_KEY));
		setContentView(R.layout.activity_claim);

		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mViewPager = (ViewPager) findViewById(R.id.claim_pager);
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		mViewPager.setAdapter(mSectionsPagerAdapter);
		tabs.setIndicatorColor(getResources().getColor(R.color.ab_tab_indicator_opentenure));
		tabs.setViewPager(mViewPager);
		
		String savedInstanceClaimId = null;

		if(savedInstanceState != null){
			savedInstanceClaimId = savedInstanceState.getString(CLAIM_ID_KEY);
		}

		String intentClaimId = getIntent().getExtras().getString(CLAIM_ID_KEY);
		
		if(savedInstanceClaimId != null){
			setClaimId(savedInstanceClaimId);
		}else if(intentClaimId != null && !intentClaimId.equalsIgnoreCase(CREATE_CLAIM_ID)){
			setClaimId(intentClaimId);
			
		}
	}

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
		public Fragment getItem(int position) {

			switch (position) {
			case 0:
				return new ClaimDetailsFragment();
			case 1:
				return new ClaimMapFragment();
			case 2:
				return new ClaimDocumentsFragment();
			case 3:
				return new ClaimAdditionalInfoFragments();
			case 4:
				return new AdjacentClaimsFragment();
			case 5:
				return new ChallengingClaimsFragment();
			case 6:
				return new OwnersFragment();
			}
			return null;
		}

		@Override
		public int getCount() {
			return 7;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_claim).toUpperCase(l);
			case 1:
				return getString(R.string.title_claim_map).toUpperCase(l);
			case 2:
				return getString(R.string.title_claim_documents).toUpperCase(l);				
			case 3:
				return getString(R.string.title_claim_additional_info).toUpperCase(l);
			case 4:
				return getString(R.string.title_claim_adjacencies).toUpperCase(l);
			case 5:
				return getString(R.string.title_claim_challenges).toUpperCase(l);
			case 6:
				return getString(R.string.title_claim_owners).toUpperCase(l);
			}
			return null;
		}
	}

	@Override
	public void setClaimId(String claimId) {
		this.claimId = claimId;
		if(claimId != null && !claimId.equalsIgnoreCase(CREATE_CLAIM_ID)){
			Claim claim = Claim.getClaim(claimId);
			setTitle(getResources().getString(R.string.app_name)+": "+claim.getName());
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
}
