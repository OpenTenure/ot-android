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

import java.util.List;
import java.util.Locale;

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.maps.MainMapFragment;
import org.fao.sola.clients.android.opentenure.model.ClaimType;
import org.fao.sola.clients.android.opentenure.model.Database;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

public class OpenTenureApplication extends Application {

	private static OpenTenureApplication sInstance;
	private Database database;
	private static Context context;
	private boolean checkedTypes = false;
	private boolean checkedDocTypes = false;
	private boolean checkedIdTypes = false;
	private boolean checkedLandUses = false;
	private boolean checkedCommunityArea = false;
	private boolean checkedForm = false;
	private boolean initialized = false;

	private static String localization;
	private static Locale locale;
	private static boolean loggedin;
	private static String username;
	private static Activity activity;
	private static List<ClaimType> claimTypes;
	private static AndroidHttpClient mHttpClient;
	private static CookieStore cookieStore;
	private static HttpContext http_context;
	private static MainMapFragment mapFragment;
	private static ClaimDocumentsFragment documentsFragment;

	private static View personsView;
	private static LocalClaimsFragment localClaimsFragment;
	private static FragmentActivity newsFragmentActivity;
	public static String _DEFAULT_COMMUNITY_SERVER = "https://ot.flossola.org";

	private static int downloadedClaims = 0;
	private static int TotalClaimsToDownload = 0;

	public static OpenTenureApplication getInstance() {
		return sInstance;
	}

	public Database getDatabase() {
		return database;
	}

	public static ClaimDocumentsFragment getDocumentsFragment() {
		return documentsFragment;
	}

	public static void setDocumentsFragment(
			ClaimDocumentsFragment documentsFragment) {
		OpenTenureApplication.documentsFragment = documentsFragment;
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public boolean isConnectedWifi(Context context) {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return (netInfo != null && netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI);
	}

	public boolean isConnectedMobile(Context context) {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return (netInfo != null && netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_MOBILE);
	}

	public static String getConnectionType(int type, int subType) {
		if (type == ConnectivityManager.TYPE_WIFI) {
			return "TYPE_WIFI";
		} else if (type == ConnectivityManager.TYPE_MOBILE) {
			switch (subType) {
			case TelephonyManager.NETWORK_TYPE_UNKNOWN:
				return "TYPE_UNKNOWN";
			case TelephonyManager.NETWORK_TYPE_1xRTT:
				return "TYPE_1XRTT"; // ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_CDMA:
				return "TYPE_CDMA"; // ~ 14-64 kbps
			case TelephonyManager.NETWORK_TYPE_EDGE:
				return "TYPE_EDGE"; // ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
				return "TYPE_EVDO_0"; // ~ 400-1000 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
				return "TYPE_EVDO_A"; // ~ 600-1400 kbps
			case TelephonyManager.NETWORK_TYPE_GPRS:
				return "TYPE_GPRS"; // ~ 100 kbps
			case TelephonyManager.NETWORK_TYPE_HSDPA:
				return "TYPE_HSDPA"; // ~ 2-14 Mbps
			case TelephonyManager.NETWORK_TYPE_HSPA:
				return "TYPE_HSPA"; // ~ 700-1700 kbps
			case TelephonyManager.NETWORK_TYPE_HSUPA:
				return "TYPE_HSUPA"; // ~ 1-23 Mbps
			case TelephonyManager.NETWORK_TYPE_UMTS:
				return "TYPE_UMTS"; // ~ 400-7000 kbps
			case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
				return "TYPE_IDEN"; // ~25 kbps

				// Unknown
			default:
				return "TYPE_UNKNOWN";
			}
		} else {
			return "TYPE UNKNOWN";
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
		sInstance.initializeInstance();
		context = getApplicationContext();
		locale = getResources().getConfiguration().locale;
		setLocalization(locale);

		FileSystemUtilities.createClaimsFolder();
		FileSystemUtilities.createClaimantsFolder();
		FileSystemUtilities.createOpenTenureFolder();

	}

	protected void initializeInstance() {
		// Start without a DB encryption password
		database = new Database(getApplicationContext(), "");

	}

	public static HttpContext getHttp_context() {
		return http_context;
	}

	public static void setHttp_context(HttpContext http_context) {
		OpenTenureApplication.http_context = http_context;
	}

	public static CookieStore getCoockieStore() {
		if (cookieStore != null)
			return cookieStore;
		else {
			cookieStore = new BasicCookieStore();
			return cookieStore;
		}

	}

	public static void setCoockieStore(CookieStore coockieStore) {
		OpenTenureApplication.cookieStore = coockieStore;
	}

	public static boolean isLoggedin() {
		return loggedin;
	}

	public static void setLoggedin(boolean loggedin) {
		OpenTenureApplication.loggedin = loggedin;
	}

	public static String getUsername() {
		return username;
	}

	public static void setUsername(String username) {
		OpenTenureApplication.username = username;
	}

	public static Context getContext() {
		return context;
	}

	public static void setContext(Context context) {
		OpenTenureApplication.context = context;
	}

	public boolean isCheckedDocTypes() {
		return checkedDocTypes;
	}

	public void setCheckedDocTypes(boolean checkedDocTypes) {
		this.checkedDocTypes = checkedDocTypes;
	}

	/*
	 * Return the single instance of the inizialized HttpClient that handle
	 * connection and session to the server
	 */
	public static synchronized AndroidHttpClient getHttpClient() {

		if (mHttpClient != null)
			return mHttpClient;
		else
			return prepareClient();
	}

	/*
	 * Return the single instance of the inizialized HttpClient that handle
	 * connection and session to the server
	 */
	public static synchronized void closeHttpClient() {

		mHttpClient.close();
		mHttpClient = null;

	}

	public static Activity getActivity() {
		return activity;
	}

	public static void setActivity(Activity activity) {
		OpenTenureApplication.activity = activity;
	}

	public static MainMapFragment getMapFragment() {
		return mapFragment;
	}

	public static void setMapFragment(MainMapFragment mapFragment) {
		OpenTenureApplication.mapFragment = mapFragment;
	}

	public boolean isCheckedTypes() {
		return checkedTypes;
	}

	public boolean isCheckedIdTypes() {
		return checkedIdTypes;
	}

	public void setCheckedIdTypes(boolean checkedIdTypes) {
		this.checkedIdTypes = checkedIdTypes;
	}

	public boolean isCheckedLandUses() {
		return checkedLandUses;
	}

	public void setCheckedLandUses(boolean checkedLandUses) {
		this.checkedLandUses = checkedLandUses;
	}

	public boolean isCheckedForm() {
		return checkedForm;
	}

	public void setCheckedForm(boolean checkedForm) {
		this.checkedForm = checkedForm;
	}

	public void setCheckedTypes(boolean checkedTypes) {
		this.checkedTypes = checkedTypes;
	}

	public static View getPersonsView() {
		return personsView;
	}

	public static void setPersonsView(View personsView) {
		OpenTenureApplication.personsView = personsView;
	}

	public static List<ClaimType> getClaimTypes() {
		return claimTypes;
	}

	public static void setClaimTypes(List<ClaimType> claimTypes) {
		OpenTenureApplication.claimTypes = claimTypes;
	}

	public boolean isCheckedCommunityArea() {
		return checkedCommunityArea;
	}

	public void setCheckedCommunityArea(boolean checkedCommunityArea) {
		this.checkedCommunityArea = checkedCommunityArea;
	}

	public static LocalClaimsFragment getLocalClaimsFragment() {
		return localClaimsFragment;
	}

	public static Locale getLocale() {
		return locale;
	}

	public static void setLocale(Locale locale) {
		OpenTenureApplication.locale = locale;
	}

	public static FragmentActivity getNewsFragment() {
		return newsFragmentActivity;
	}

	public static void setNewsFragment(FragmentActivity newsFragment) {
		OpenTenureApplication.newsFragmentActivity = newsFragment;
	}

	public static int getDownloadedClaims() {
		return downloadedClaims;
	}

	public static void setDownloadedClaims(int downloadedClaims) {
		synchronized (ACCESSIBILITY_SERVICE) {
			OpenTenureApplication.downloadedClaims = downloadedClaims;
		}
	}

	public static int getTotalClaimsToDownload() {
		return TotalClaimsToDownload;
	}

	public static void setTotalClaimsToDownload(int totalClaimsToDownload) {
		TotalClaimsToDownload = totalClaimsToDownload;
	}

	public static String getLocalization() {
		return localization;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public static void setLocalization(String localization) {
		OpenTenureApplication.localization = localization;
	}

	public static void setLocalization(Locale locale) {

		locale.getDisplayLanguage();

		OpenTenureApplication.localization = locale.getLanguage().toLowerCase()
				+ "-" + locale.getCountry().toLowerCase();

		/* to remove in the future */
		if (!OpenTenureApplication.localization.equals("en-us"))
			OpenTenureApplication.localization = "en-us";

	}

	public static void setLocalClaimsFragment(
			LocalClaimsFragment localClaimsFragment) {
		OpenTenureApplication.localClaimsFragment = localClaimsFragment;
	}

	/*
	 * Initialize the single istance of AndroidHttpClient that will handle the
	 * connections to the server
	 */
	private static AndroidHttpClient prepareClient() {

		try {

			mHttpClient = AndroidHttpClient.newInstance("Android", context);
			http_context = new BasicHttpContext();

			cookieStore = getCoockieStore();
			http_context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

			Log.d("OpenTEnureApplication", "Initialized HTTP Client");

		} catch (Throwable e) {
			e.printStackTrace();

		}
		return mHttpClient;

	}

}
