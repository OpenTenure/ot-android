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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class OpenTenurePreferencesFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		SharedPreferences OpenTenurePreferences = PreferenceManager
				.getDefaultSharedPreferences(OpenTenureApplication.getContext());

		if (OpenTenurePreferences.getBoolean("albanian_language", true)) {

			findPreference("default_language").setEnabled(true);
			findPreference("khmer_language").setEnabled(true);
			findPreference("albanian_language").setEnabled(false);
			findPreference("albanian_language").setSelectable(false);
			System.out.println("isSelectable() "
					+ findPreference("albanian_language").isSelectable());
		}
		if (OpenTenurePreferences.getBoolean("default_language", true)) {

			findPreference("albanian_language").setEnabled(true);
			findPreference("khmer_language").setEnabled(true);
			findPreference("default_language").setEnabled(false);
			findPreference("default_language").setSelectable(false);
			System.out.println("isSelectable() "
					+ findPreference("default_language").isSelectable());
		}
		if (OpenTenurePreferences.getBoolean("khmer_language", true)) {

			findPreference("albanian_language").setEnabled(true);
			findPreference("default_language").setEnabled(true);
			findPreference("khmer_language").setEnabled(false);
			findPreference("khmer_language").setSelectable(false);
			System.out.println("isSelectable() "
					+ findPreference("khmer_language").isSelectable());
		}

		OpenTenurePreferences
				.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
					public void onSharedPreferenceChanged(
							SharedPreferences prefs, String key) {

						if (key.equals("default_language")) {
							System.out.println("Clicco default");
							// Reset other items

							if (prefs.getBoolean("default_language", true)) {

								SharedPreferences.Editor editor = prefs.edit();

								editor.putBoolean("khmer_language", false);
								editor.putBoolean("albanian_language", false);

								editor.commit();

								synchronized (this) {
									setPreferenceScreen(null);
									addPreferencesFromResource(R.xml.preferences);

								}

								Intent i = OpenTenureApplication
										.getContext()
										.getPackageManager()
										.getLaunchIntentForPackage(
												OpenTenureApplication
														.getContext()
														.getPackageName());
								i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								System.exit(2);
								startActivity(i);

							}
						}

						if (key.equals("khmer_language")) {
							// Reset other items
							System.out.println("Clicco khmer");

							if (prefs.getBoolean("khmer_language", true)) {

								SharedPreferences.Editor editor = prefs.edit();
								editor.putBoolean("default_language", false);
								editor.putBoolean("albanian_language", false);

								editor.commit();

								synchronized (this) {
									setPreferenceScreen(null);
									addPreferencesFromResource(R.xml.preferences);

								}

								Intent i = OpenTenureApplication
										.getContext()
										.getPackageManager()
										.getLaunchIntentForPackage(
												OpenTenureApplication
														.getContext()
														.getPackageName());
								i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								System.exit(2);
								startActivity(i);
							}

						}

						if (key.equals("albanian_language")) {

							System.out.println("Clicco albanese");
							// Reset other items

							if (prefs.getBoolean("albanian_language", true)) {

								SharedPreferences.Editor editor = prefs.edit();
								editor.putBoolean("default_language", false);
								editor.putBoolean("khmer_language", false);

								editor.commit();

								synchronized (this) {
									setPreferenceScreen(null);
									addPreferencesFromResource(R.xml.preferences);

								}

								Intent i = OpenTenureApplication
										.getContext()
										.getPackageManager()
										.getLaunchIntentForPackage(
												OpenTenureApplication
														.getContext()
														.getPackageName());
								i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

								System.exit(2);
								startActivity(i);
							}

						}
					}
				});

	}
}
