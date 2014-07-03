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
package org.fao.sola.clients.android.opentenure.maps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.os.Environment;

public class WmsMapTileCacher extends AsyncTask<Void, Void, Void> {

	private String url;
	private int zoom;
	private int x;
	private int y;

	public WmsMapTileCacher(String url, int zoom, int x, int y) {

		this.url = url;
		this.zoom = zoom;
		this.x = x;
		this.y = y;

	}

	protected void onPreExecute() {
	}

	protected Void doInBackground(Void... params) {

		FileOutputStream fos = null;
		InputStream is = null;

		try {

			URL url = new URL(this.url);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setRequestMethod("GET");
			c.setDoOutput(true);
			c.connect();
			String tileDir = Environment.getExternalStorageDirectory()+"/Open Tenure/tiles/" + zoom + "/" + x + "/";
			String tileFile = y + ".png";

			File file = new File(tileDir);
			file.mkdirs();

			File outputFile = new File(tileDir, tileFile);
			fos = new FileOutputStream(outputFile);

			is = c.getInputStream();

			byte[] buffer = new byte[1024];
			int len1 = 0;
			while ((len1 = is.read(buffer)) != -1) {

				fos.write(buffer, 0, len1);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(fos!=null){
				try {fos.close();} catch (IOException ignore) {}
			}
			if(is!=null){
				try {is.close();} catch (IOException ignore) {}
			}
		}
		return null;

	}

	protected void onPostExecute(String result) {
	}

}