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

import android.app.ActivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class OpenTenure extends FragmentActivity {


	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	PagerSlidingTabStrip tabs;
	private long lastPress;

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
	public void onBackPressed() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastPress > 4000) {
			Toast.makeText(getBaseContext(),
					R.string.message_press_again_to_exit, Toast.LENGTH_LONG)
					.show();
			lastPress = currentTime;
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open_tenure);
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

		Log.d(this.getClass().getName(),
				"Starting with " + activityManager.getMemoryClass()
						+ "MB of memory for the application.");

		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mViewPager = (ViewPager) findViewById(R.id.open_tenure_pager);
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		mViewPager.setAdapter(mSectionsPagerAdapter);
		tabs.setIndicatorColor(getResources().getColor(R.color.ab_tab_indicator_opentenure));
		tabs.setViewPager(mViewPager);

		Log.d(this.getClass().getName(),
				"DB version is: " + OpenTenureApplication.getInstance().getDatabase().getConfiguration("DBVERSION"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.open_tenure, menu);
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
				return new NewsFragment();
			case 1:
				return new MainMapFragment();
			case 2:
				return new LocalClaimsFragment();
			case 3:
				return new SubmittedClaimsFragment();
			case 4:
				return new ChallengedClaimsFragment();
			}
			return null;
		}

		@Override
		public int getCount() {

			return 5;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_news).toUpperCase(l);
			case 1:
				return getString(R.string.title_map).toUpperCase(l);
			case 2:
				return getString(R.string.title_local_claims).toUpperCase(l);
			case 3:
				return getString(R.string.title_submitted_claims)
						.toUpperCase(l);
			case 4:
				return getString(R.string.title_challenged_claims).toUpperCase(
						l);
			}
			return null;
		}
	}
}
