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
package org.fao.sola.clients.android.opentenure.form.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.fao.sola.clients.android.opentenure.form.FormTemplate;

import android.os.AsyncTask;
import android.os.Environment;

public class FormRetriever extends AsyncTask<Void, Void, Void> {

	private String formUrl;
	public void setFormUrl(String formUrl) {
		this.formUrl = formUrl;
	}

	private static final String file = "form.json";

	public FormRetriever() {
	}
	
	public static FormTemplate getTemplate(){
        StringBuffer buffer = new StringBuffer();
        String dir = Environment.getExternalStorageDirectory()+"/Open Tenure/forms/";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(dir+file));
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }
            return FormTemplate.fromJson(buffer.toString());
        } catch (IOException e) {
            return null;
        } catch (OutOfMemoryError e) {
            return null;
        } finally {
            if (br != null) try { br.close(); } catch (Exception ignored) {}
        }

	}

	protected void onPreExecute() {
	}

	protected Void doInBackground(Void... params) {

		FileOutputStream fos = null;
		InputStream is = null;

		String dir = Environment.getExternalStorageDirectory()+"/Open Tenure/forms/";
		File formDir = new File(dir);
		formDir.mkdirs();

		File outputFile = new File(dir, file);

		try {

			URL url = new URL(formUrl);
			HttpURLConnection c = (HttpURLConnection) url
					.openConnection();
			c.connect();
			fos = new FileOutputStream(outputFile);
			is = c.getInputStream();

			byte[] buffer = new byte[1024];
			int len1 = 0;
			while ((len1 = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len1);
			}
			fos.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fos!=null){
				try {fos.close();} catch (IOException ignore) {}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException ignore) {
				}
			}
		}
		return null;

	}

	protected void onPostExecute(String result) {
	}
}
