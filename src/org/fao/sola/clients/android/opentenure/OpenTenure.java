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

import org.fao.sola.clients.android.opentenure.maps.MainMapFragment;
import org.fao.sola.clients.android.opentenure.model.Configuration;

import com.astuetz.PagerSlidingTabStrip;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class OpenTenure extends FragmentActivity {


	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	PagerSlidingTabStrip tabs;

	@Override
	public void onPause() {
		OpenTenureApplication.getInstance().getDatabase().sync();
		super.onPause();
	};
	
	@Override
	public void onDestroy() {
		OpenTenureApplication.getInstance().getDatabase().close();
		super.onDestroy();
	};
	
	@Override
	public void onBackPressed() {
		AlertDialog.Builder exitDialog = new AlertDialog.Builder(this);
		exitDialog.setTitle(R.string.title_exit_dialog);
		exitDialog.setMessage(getResources().getString(R.string.message_exit_dialog));

		exitDialog.setPositiveButton(R.string.confirm, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				OpenTenureApplication.getInstance().getDatabase().close();
				OpenTenure.super.onBackPressed();
			}
		});
		exitDialog.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		exitDialog.show();
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
		
		OpenTenureApplication.getInstance().getDatabase().unlock(this);
		
		if(OpenTenureApplication.getInstance().getDatabase().isOpen()){
			// Check for pending upgrades
			if(OpenTenureApplication.getInstance().getDatabase().getUpgradePath().size() > 0){
				Toast.makeText(this,
						R.string.message_check_upgrade_db, Toast.LENGTH_LONG)
						.show();
				OpenTenureApplication.getInstance().getDatabase().performUpgrade();
			}
		}

		Log.d(this.getClass().getName(),
				"DB version is: " + Configuration.getConfigurationValue("DBVERSION"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.open_tenure, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent();
	        intent.setClass(OpenTenure.this, OpenTenurePreferencesActivity.class);
	        startActivityForResult(intent, 0); 
	        return true;
		default:
			return super.onOptionsItemSelected(item);
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
				return new NewsFragment();
			case 1:
				return new MainMapFragment();
			case 2:
				return new LocalClaimsFragment();
			case 3:
				return new PersonsFragment();
			}
			return null;
		}

		@Override
		public int getCount() {

			return 4;
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
				return getString(R.string.title_claims).toUpperCase(l);
			case 3:
				return getString(R.string.title_persons)
						.toUpperCase(l);
//			case 4:
//				return getString(R.string.title_challenged_claims).toUpperCase(
//						l);
			}
			return null;
		}
	}
}
