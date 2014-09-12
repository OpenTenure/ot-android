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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.fao.sola.clients.android.opentenure.form.FormPayload;
import org.fao.sola.clients.android.opentenure.model.Claim;

import android.os.AsyncTask;

public class FormSender extends AsyncTask<Void, Void, Void> {

	private static final String url = "http://192.168.1.101:8080/DynamicFormGeneration/payloadServlet";
	private static final String charset = "UTF-8";

	private String id;

	public void setId(String id) {
		this.id = id;
	}

	public FormSender(String id) {
		this.id = id;
	}

	public FormSender() {
	}

	public static FormPayload getPayload(String id) {
		
		Claim claim = Claim.getClaim(id);
		return claim.getSurveyForm();
	}

	public static void savePayload(FormPayload payload, String id) {
		Claim claim = Claim.getClaim(id);
		claim.setSurveyForm(payload);
		claim.update();
	}

	protected void onPreExecute() {
	}

	protected Void doInBackground(Void... params) {

		try {

			MultipartUtility multipart = new MultipartUtility(url, charset);
			
			multipart.addFormField("id", id);
			FormPayload payload = getPayload(id);
			if(payload != null){
				multipart.addJsonFormField("form", payload.toJson());
			}

			List<String> response = multipart.finish();
			
			System.out.println("SERVER REPLIED:");
			
			for (String line : response) {
				System.out.println(line);
			}		
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	protected void onPostExecute(String result) {
	}

	public class MultipartUtility {
		private final String boundary;
		private static final String LINE_FEED = "\r\n";
		private HttpURLConnection httpConn;
		private String charset;
		private OutputStream outputStream;
		private PrintWriter writer;

		/**
		 * This constructor initializes a new HTTP POST request with content type
		 * is set to multipart/form-data
		 * @param requestURL
		 * @param charset
		 * @throws IOException
		 */
		public MultipartUtility(String requestURL, String charset)
				throws IOException {
			this.charset = charset;
			
			// creates a unique boundary based on time stamp
			boundary = "===" + System.currentTimeMillis() + "===";
			
			URL url = new URL(requestURL);
			httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setUseCaches(false);
			httpConn.setDoOutput(true);	// indicates POST method
			httpConn.setDoInput(true);
			httpConn.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + boundary);
			httpConn.setRequestProperty("User-Agent", "OpenTenure");
			outputStream = httpConn.getOutputStream();
			writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
					true);
		}

		/**
		 * Adds a form field to the request
		 * @param name field name
		 * @param value field value
		 */
		public void addFormField(String name, String value) {
			writer.append("--" + boundary).append(LINE_FEED);
			writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
					.append(LINE_FEED);
			writer.append("Content-Type: text/plain; charset=" + charset).append(
					LINE_FEED);
			writer.append(LINE_FEED);
			writer.append(value).append(LINE_FEED);
			writer.flush();
		}

		/**
		 * Adds a json form field to the request
		 * @param name field name
		 * @param value field value
		 */
		public void addJsonFormField(String name, String value) {
			writer.append("--" + boundary).append(LINE_FEED);
			writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
					.append(LINE_FEED);
			writer.append("Content-Type: application/json; charset=" + charset).append(
					LINE_FEED);
			writer.append(LINE_FEED);
			writer.append(value).append(LINE_FEED);
			writer.flush();
		}

		/**
		 * Adds a upload file section to the request 
		 * @param fieldName name attribute in <input type="file" name="..." />
		 * @param uploadFile a File to be uploaded 
		 * @throws IOException
		 */
		public void addFilePart(String fieldName, File uploadFile)
				throws IOException {
			String fileName = uploadFile.getName();
			writer.append("--" + boundary).append(LINE_FEED);
			writer.append(
					"Content-Disposition: form-data; name=\"" + fieldName
							+ "\"; filename=\"" + fileName + "\"")
					.append(LINE_FEED);
			writer.append(
					"Content-Type: "
							+ URLConnection.guessContentTypeFromName(fileName))
					.append(LINE_FEED);
			writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
			writer.append(LINE_FEED);
			writer.flush();

			FileInputStream inputStream = new FileInputStream(uploadFile);
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			outputStream.flush();
			inputStream.close();
			
			writer.append(LINE_FEED);
			writer.flush();		
		}

		/**
		 * Adds a upload file section to the request 
		 * @param fieldName name attribute in <input type="file" name="..." />
		 * @param uploadFile a File to be uploaded 
		 * @throws IOException
		 */
		public void addFilePart(String fieldName, File uploadFile, String contentType)
				throws IOException {
			String fileName = uploadFile.getName();
			writer.append("--" + boundary).append(LINE_FEED);
			writer.append(
					"Content-Disposition: form-data; name=\"" + fieldName
							+ "\"; filename=\"" + fileName + "\"")
					.append(LINE_FEED);
			writer.append(
					"Content-Type: "
							+ contentType)
					.append(LINE_FEED);
			writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
			writer.append(LINE_FEED);
			writer.flush();

			FileInputStream inputStream = new FileInputStream(uploadFile);
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			outputStream.flush();
			inputStream.close();
			
			writer.append(LINE_FEED);
			writer.flush();		
		}

		/**
		 * Adds a header field to the request.
		 * @param name - name of the header field
		 * @param value - value of the header field
		 */
		public void addHeaderField(String name, String value) {
			writer.append(name + ": " + value).append(LINE_FEED);
			writer.flush();
		}
		
		/**
		 * Completes the request and receives response from the server.
		 * @return a list of Strings as response in case the server returned
		 * status OK, otherwise an exception is thrown.
		 * @throws IOException
		 */
		public List<String> finish() throws IOException {
			List<String> response = new ArrayList<String>();

			writer.append(LINE_FEED).flush();
			writer.append("--" + boundary + "--").append(LINE_FEED);
			writer.close();

			// checks server's status code first
			int status = httpConn.getResponseCode();
			if (status == HttpURLConnection.HTTP_OK) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						httpConn.getInputStream()));
				String line = null;
				while ((line = reader.readLine()) != null) {
					response.add(line);
				}
				reader.close();
				httpConn.disconnect();
			} else {
				throw new IOException("Server returned non-OK status: " + status);
			}

			return response;
		}
	}
}
