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
package org.fao.sola.clients.android.opentenure.network.API;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.filesystem.json.model.Claim;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.network.response.ApiResponse;
import org.fao.sola.clients.android.opentenure.network.response.GetAttachmentResponse;
import org.fao.sola.clients.android.opentenure.network.response.GetClaimTypesResponse;
import org.fao.sola.clients.android.opentenure.network.response.GetClaimsResponse;
import org.fao.sola.clients.android.opentenure.network.response.SaveAttachmentResponse;
import org.fao.sola.clients.android.opentenure.network.response.SaveClaimResponse;
import org.fao.sola.clients.android.opentenure.network.response.UploadChunkPayload;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.net.http.AndroidHttpClient;
import android.preference.PreferenceActivity.Header;
import android.util.Log;

public class CommunityServerAPI {

	/**
	 * 
	 * The login API
	 * 
	 * *
	 * 
	 * @return 200 in case of success, 401 in case of fail, 0 in case of generic
	 *         error, 80 in case of connection timed out error
	 * 
	 */
	public static int login(String username, String password) {

		try {
			/*
			 * Creating the url to call
			 */

			String url = String.format(CommunityServerAPIUtilities.HTTP_LOGIN,
					username, password);

			HttpGet request = new HttpGet(url);

			/* Preparing to store coockies */
			CookieStore CS = OpenTenureApplication.getCoockieStore();
			HttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, CS);

			AndroidHttpClient client = OpenTenureApplication.getHttpClient();

			/* Calling the Server.... */
			HttpResponse response = client.execute(request, context);

			Log.d("CommunityServerAPI",
					"Login Status line " + response.getStatusLine());

			// if (response.getStatusLine().getStatusCode() ==
			// (HttpStatus.SC_OK)) {
			//
			// String json = CommunityServerAPIUtilities.Slurp(response
			// .getEntity().getContent(), 1024);
			//
			// /* parsing the response in a Login object */
			// Gson gson = new Gson();
			// Login login = gson.fromJson(json, Login.class);

			switch (response.getStatusLine().getStatusCode()) {

			case 200:
				OpenTenureApplication.setCoockieStore(CS);
				Log.d("CommunityServerAPI", "Login status : 200");
				return 200;

			case 401:
				Log.d("CommunityServerAPI", "Login status : 401");
				return 401;

			default:
				Log.d("CommunityServerAPI", "Login status : default");
				return 0;
			}

		}

		catch (ConnectTimeoutException ct) {

			Log.d("CommunityServerAPI", ct.getMessage());
			ct.printStackTrace();
			return 80;

		} catch (Throwable ex) {

			Log.d("CommunityServerAPI", "An error has occurred during Login "
					+ ex.getMessage());
			ex.printStackTrace();
			return 0;
		}

	}

	/**
	 * 
	 * The logout API
	 * 
	 * *
	 * 
	 * @return 200 in case of success 401 in case of fail , 0 in case of generic
	 *         error 80 in case of connection timed out
	 */
	public static int logout() {

		try {

			HttpGet request = new HttpGet(
					CommunityServerAPIUtilities.HTTPS_LOGOUT);

			/* Preparing to store coockies */
			CookieStore CS = OpenTenureApplication.getCoockieStore();
			HttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, CS);

			AndroidHttpClient client = OpenTenureApplication.getHttpClient();

			/* Calling the Server.... */
			HttpResponse response = client.execute(request, context);

			Log.d("CommunityServerAPI", response.getStatusLine().toString());

			// if (response.getStatusLine().getStatusCode() ==
			// (HttpStatus.SC_OK)) {
			//
			// String json = CommunityServerAPIUtilities.Slurp(response
			// .getEntity().getContent(), 1024);
			//
			// /* parsing the response */
			// Gson gson = new Gson();
			// Login login = gson.fromJson(json, Login.class);

			switch (response.getStatusLine().getStatusCode()) {

			case 200:

				OpenTenureApplication.setCoockieStore(CS);

				return 200;

			case 401:

				return 401;

			default:

				return 0;
			}

		} catch (ConnectTimeoutException ct) {

			Log.d("CommunityServerAPI",
					"Logout ConnectTimeoutException" + ct.getMessage());
			ct.printStackTrace();
			return 80;

		}

		catch (UnknownHostException uhe) {

			Log.d("CommunityServerAPI",
					"Logout UnknownHostException" + uhe.getMessage());
			uhe.printStackTrace();
			return 1;
		} catch (Throwable ex) {

			Log.d("CommunityServerAPI", "Logout Exception " + ex.getMessage());
			ex.printStackTrace();
			return 0;
		}
	}

	public static List<org.fao.sola.clients.android.opentenure.network.response.Claim> getAllClaims() {

		/*
		 * Creating the url to call
		 */
		String url = String
				.format(CommunityServerAPIUtilities.HTTPS_GETALLCLAIMS);
		HttpGet request = new HttpGet(url);

		AndroidHttpClient client = OpenTenureApplication.getHttpClient();

		try {

			HttpResponse response = client.execute(request);

			String json = CommunityServerAPIUtilities.Slurp(response
					.getEntity().getContent(), 1024);

			if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_OK)) {

				Log.d("CommunityServerAPI", "GET ALL CLAIMS JSON RESPONSE "
						+ json);

				Type listType = new TypeToken<ArrayList<org.fao.sola.clients.android.opentenure.network.response.Claim>>() {
				}.getType();
				List<org.fao.sola.clients.android.opentenure.network.response.Claim> claimList = new Gson()
						.fromJson(json, listType);

				return claimList;

			} else {

				Log.d("CommunityServerAPI", "GET ALL CLAIMS JSON RESPONSE "
						+ json);
				return null;

			}

		} catch (Exception ex) {

			Log.d("CommunityServerAPI",
					"GET ALL CLAIMS error " + ex.getMessage());
			ex.printStackTrace();

			return null;

		}

	}

	public static List<org.fao.sola.clients.android.opentenure.network.response.Claim> getAllClaimsByBox(
			String[] coordinates) {

		/*
		 * Creating the url to call
		 */
		String url = String.format(
				CommunityServerAPIUtilities.HTTPS_GETALLCLAIMSBYBOX,
				coordinates[0], coordinates[1], coordinates[2], coordinates[3],
				"100");
		HttpGet request = new HttpGet(url);

		AndroidHttpClient client = OpenTenureApplication.getHttpClient();

		try {

			HttpResponse response = client.execute(request);

			String json = CommunityServerAPIUtilities.Slurp(response
					.getEntity().getContent(), 1024);

			if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_OK)) {

				Log.d("CommunityServerAPI",
						"GET ALL CLAIMS BY BOX JSON RESPONSE " + json);

				Type listType = new TypeToken<ArrayList<org.fao.sola.clients.android.opentenure.network.response.Claim>>() {
				}.getType();
				List<org.fao.sola.clients.android.opentenure.network.response.Claim> claimList = new Gson()
						.fromJson(json, listType);

				return claimList;

			} else {

				Log.d("CommunityServerAPI", "GET ALL CLAIMS JSON RESPONSE "
						+ json);
				return null;

			}

		} catch (Exception ex) {

			Log.d("CommunityServerAPI",
					"GET ALL CLAIMS error " + ex.getMessage());
			ex.printStackTrace();

			return null;

		}

	}

	public static Claim getClaim(String claimId) {

		/*
		 * Creating the url to call
		 */
		String url = String.format(CommunityServerAPIUtilities.HTTPS_GETCLAIM,
				claimId);
		HttpGet request = new HttpGet(url);

		AndroidHttpClient client = OpenTenureApplication.getHttpClient();

		/* Calling the Server.... */
		try {
			HttpResponse response = client.execute(request);

			Log.d("CommunityServerAPI",
					"GET Claim status line " + response.getStatusLine());

			if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_OK)) {

				String json = CommunityServerAPIUtilities.Slurp(response
						.getEntity().getContent(), 1024);

				Log.d("CommunityServerAPI", "CLAIM JSON STRING " + json);

				Gson gson = new Gson();
				Claim claim = gson.fromJson(json, Claim.class);

				Log.d("CommunityServerAPI",
						"CLAIM JSON GPSGEOMETRY " + claim.getGpsGeometry());

				return claim;

			} else {

				Log.d("CommunityServerAPI", "CLAIM not retrieved ");
				return null;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return null;
		}

	}

	public static GetAttachmentResponse getAttachment(String attachmentId,
			long start, long offset) {

		GetAttachmentResponse methodResponse = new GetAttachmentResponse();

		/*
		 * Creating the url to call
		 */
		String url = String.format(
				CommunityServerAPIUtilities.HTTPS_GETATTACHMENT, attachmentId);
		HttpGet request = new HttpGet(url);

		/* Retrieve the attachment partially */
		if (offset > start)
			request.setHeader("Range", "bytes=" + start + "-" + offset);

		Log.d("CommunityServerAPI", "bytes=" + start + "-" + offset);

		AndroidHttpClient client = OpenTenureApplication.getHttpClient();

		/* Calling the Server.... */
		try {
			HttpResponse response = client.execute(request);

			Log.d("CommunityServerAPI", "GET Attachment status line "
					+ response.getStatusLine());

			if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_OK)) {

				byte[] byteArray = CommunityServerAPIUtilities.slurp(response
						.getEntity().getContent(), 1024);

				Log.d("CommunityServerAPI", "ATTACHMENT RETRIEVED SIZE"
						+ byteArray.length);

				methodResponse.setArray(byteArray);
				methodResponse.setHttpStatusCode(response.getStatusLine()
						.getStatusCode());
				methodResponse.setMessage(response.getStatusLine()
						.getReasonPhrase());
				methodResponse
						.setMd5(response.getHeaders("ETag")[0].getValue());

				return methodResponse;

			} else if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_PARTIAL_CONTENT)) {

				org.apache.http.Header[] headers = response.getAllHeaders();

				for (int i = 0; i < headers.length; i++) {

					Log.d("CommunityServerAPI",
							"HEADER : " + headers[i].getName() + " "
									+ headers[i].getValue());
				}

				byte[] byteArray = CommunityServerAPIUtilities.slurp(response
						.getEntity().getContent(), 1024);

				Log.d("CommunityServerAPI",
						"ATTACHMENT partially retrieved. Size : "
								+ byteArray.length);

				methodResponse.setArray(byteArray);
				methodResponse.setHttpStatusCode(response.getStatusLine()
						.getStatusCode());
				methodResponse.setMessage(response.getStatusLine()
						.getReasonPhrase());
				methodResponse
						.setMd5(response.getHeaders("ETag")[0].getValue());
				return methodResponse;
			} else if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_NOT_FOUND)) {

				Log.d("CommunityServerAPI", "ATTACHMENT NOT FOUND. Size ");

				methodResponse.setArray(null);
				methodResponse.setHttpStatusCode(response.getStatusLine()
						.getStatusCode());
				methodResponse.setMessage(response.getStatusLine()
						.getReasonPhrase());
				return methodResponse;

			} else {

				Log.d("CommunityServerAPI", "ATTACHMENT NOT RETRIEVED.");

				methodResponse.setArray(null);
				methodResponse.setHttpStatusCode(response.getStatusLine()
						.getStatusCode());
				methodResponse.setMessage(response.getStatusLine()
						.getReasonPhrase());
				return methodResponse;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			methodResponse.setArray(null);
			methodResponse.setHttpStatusCode(400);
			methodResponse.setMessage("Error retrieving attachment");

			return methodResponse;
		}

	}

	public static List<org.fao.sola.clients.android.opentenure.network.response.ClaimType> getClaimTypes() {

		String url = String
				.format(CommunityServerAPIUtilities.HTTPS_GETCLAIMTYPES);

		HttpGet request = new HttpGet(url);

		AndroidHttpClient client = OpenTenureApplication.getHttpClient();

		try {

			HttpResponse response = client.execute(request);

			String json = CommunityServerAPIUtilities.Slurp(response
					.getEntity().getContent(), 1024);

			if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_OK)) {

				Log.d("CommunityServerAPI",
						"GET ALL CLAIM TYPES JSON RESPONSE " + json);

				Type listType = new TypeToken<ArrayList<org.fao.sola.clients.android.opentenure.network.response.ClaimType>>() {
				}.getType();
				List<org.fao.sola.clients.android.opentenure.network.response.ClaimType> claimTypeList = new Gson()
						.fromJson(json, listType);

				if (claimTypeList != null)
					Log.d("CommunityServerAPI",
							"RETRIEVED CLAIM TYPES LIST"
									+ claimTypeList.size());

				return claimTypeList;

			} else {

				Log.d("CommunityServerAPI",
						"GET ALL CLAIM TYPES NOT SUCCEDED : HTTP STATUS "
								+ response.getStatusLine().getStatusCode()
								+ "  "
								+ response.getStatusLine().getReasonPhrase());

				return null;

			}

		} catch (Exception ex) {

			Log.d("CommunityServerAPI",
					"GET ALL CLAIM TYPES ERROR " + ex.getMessage());
			ex.printStackTrace();
			return null;

		}

	}

	public static List<org.fao.sola.clients.android.opentenure.network.response.ClaimType> getdocumentTypes() {

		String url = String
				.format(CommunityServerAPIUtilities.HTTPS_GETDOCUMENTYPES);

		HttpGet request = new HttpGet(url);

		AndroidHttpClient client = OpenTenureApplication.getHttpClient();

		try {

			HttpResponse response = client.execute(request);

			String json = CommunityServerAPIUtilities.Slurp(response
					.getEntity().getContent(), 1024);

			if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_OK)) {

				Log.d("CommunityServerAPI",
						"GET ALL DOCUMENT TYPES JSON RESPONSE " + json);

				Type listType = new TypeToken<ArrayList<org.fao.sola.clients.android.opentenure.network.response.ClaimType>>() {
				}.getType();
				List<org.fao.sola.clients.android.opentenure.network.response.ClaimType> claimTypeList = new Gson()
						.fromJson(json, listType);

				if (claimTypeList != null)
					Log.d("CommunityServerAPI",
							"Ho recuperato la lista dei TYPES di dimensione"
									+ claimTypeList.size());

				return claimTypeList;

			} else {

				Log.d("CommunityServerAPI",
						"GET ALL DOCUMENT TYPES NOT SUCCEDED : HTTP STATUS "
								+ response.getStatusLine().getStatusCode()
								+ "  "
								+ response.getStatusLine().getReasonPhrase());

				return null;

			}

		} catch (Exception ex) {

			Log.d("CommunityServerAPI",
					"GET ALL CLAIM TYPES ERROR " + ex.getMessage());
			ex.printStackTrace();
			return null;

		}
	}

	public static SaveClaimResponse saveClaim(String claim) {

		String url = String.format(CommunityServerAPIUtilities.HTTPS_SAVECLAIM);

		HttpPost request = new HttpPost(url);

		StringEntity entity;
		try {
			entity = new StringEntity(claim, HTTP.UTF_8);
			entity.setContentType("application/json");
			request.setEntity(entity);

			AndroidHttpClient client = OpenTenureApplication.getHttpClient();

			CookieStore CS = OpenTenureApplication.getCoockieStore();

			HttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, CS);

			/* Calling the Server.... */
			HttpResponse response = client.execute(request, context);

			if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_OK)) {
				Log.d("CommunityServerAPI",
						"saveClaim status line " + response.getStatusLine());

				String json = CommunityServerAPIUtilities.Slurp(response
						.getEntity().getContent(), 1024);

				Log.d("CommunityServerAPI", "SAVE CLAIM JSON RESPONSE " + json);

				Gson gson = new Gson();
				SaveClaimResponse saveResponse = gson.fromJson(json,
						SaveClaimResponse.class);

				saveResponse.setHttpStatusCode(response.getStatusLine()
						.getStatusCode());

				return saveResponse;
			} else if (response.getStatusLine().getStatusCode() == 452) {
				Log.d("CommunityServerAPI",
						"saveClaim status line " + response.getStatusLine());

				String json = CommunityServerAPIUtilities.Slurp(response
						.getEntity().getContent(), 1024);

				Log.d("CommunityServerAPI", "SAVE CLAIM JSON RESPONSE " + json);

				Gson gson = new Gson();
				SaveClaimResponse saveResponse = gson.fromJson(json,
						SaveClaimResponse.class);

				saveResponse.setHttpStatusCode(response.getStatusLine()
						.getStatusCode());

				return saveResponse;
			}
			
			
			else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				Log.d("CommunityServerAPI",
						"saveClaim status line " + response.getStatusLine());

				String json = CommunityServerAPIUtilities.Slurp(response
						.getEntity().getContent(), 1024);

				

				SaveClaimResponse saveResponse = new SaveClaimResponse(); 

				saveResponse.setHttpStatusCode(response.getStatusLine()
						.getStatusCode());

				return saveResponse;
			}

			else {

				Log.d("CommunityServerAPI", "Error saving Claim :  "
						+ response.getStatusLine().getStatusCode());

				Log.d("CommunityServerAPI",
						"saveClaim status line " + response.getStatusLine());

				SaveClaimResponse saveResponse = new SaveClaimResponse();
				saveResponse.setHttpStatusCode(response.getStatusLine()
						.getStatusCode());
				saveResponse.setMessage("Error saving claim : "
						+ CommunityServerAPIUtilities.Slurp(response
								.getEntity().getContent(), 1024));
				return saveResponse;
			}

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			
			SaveClaimResponse saveResponse = new SaveClaimResponse();
			saveResponse.setHttpStatusCode(110);
			saveResponse.setMessage("Error saving claim " + e.getMessage());
			
			return saveResponse;
			
		}catch(UnknownHostException uhe){
			
			uhe.printStackTrace();
			
			SaveClaimResponse saveResponse = new SaveClaimResponse();
			saveResponse.setHttpStatusCode(100);
			saveResponse.setMessage("UnknownHostException");
			
			return saveResponse;			
		} 	
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			SaveClaimResponse saveResponse = new SaveClaimResponse();
			saveResponse.setHttpStatusCode(105);
			saveResponse.setMessage("Error saving claim " + e.getMessage());
			
			return saveResponse;
		}
		

	}

	public static SaveAttachmentResponse saveAttachment(String attachment,
			String attachmentId) {

		String url = String
				.format(CommunityServerAPIUtilities.HTTPS_SAVEATTACHMENT);

		HttpPost request = new HttpPost(url);
		SaveAttachmentResponse saveAttachmentResponse = null;

		StringEntity entity;
		try {

			Log.d("CommunityServerAPI", "saveAttachment payload " + attachment);

			entity = new StringEntity(attachment, HTTP.UTF_8);
			entity.setContentType("application/json");
			request.setEntity(entity);

			AndroidHttpClient client = OpenTenureApplication.getHttpClient();

			CookieStore CS = OpenTenureApplication.getCoockieStore();

			HttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, CS);

			/* Calling the Server.... */

			HttpResponse response = client.execute(request, context);

			Log.d("CommunityServerAPI", "saveAttachment HTTP status line "
					+ response.getStatusLine());
			
			
			String json = CommunityServerAPIUtilities.Slurp(response
					.getEntity().getContent(), 1024);

			Log.d("CommunityServerAPI", "SAVE ATTACHMENT JSON RESPONSE " + json);

			Gson gson = new Gson();
			saveAttachmentResponse = gson.fromJson(json,
					SaveAttachmentResponse.class);

			saveAttachmentResponse.setHttpStatusCode(response.getStatusLine()
					.getStatusCode());

			saveAttachmentResponse.setAttachmentId(attachmentId);

		} catch (UnknownHostException ex) {
			
			SaveAttachmentResponse sar = new SaveAttachmentResponse();
			sar.setHttpStatusCode(100);
			sar.setAttachmentId(attachmentId);
			sar.setMessage(ex.getMessage());

			Log.d("CommunityServerAPI",
					"saveAttachment UnknownHostException " + ex.getMessage());
			ex.printStackTrace();
			return sar;
		}catch (Throwable ex) {
			
			SaveAttachmentResponse sar = new SaveAttachmentResponse();
			sar.setHttpStatusCode(105);
			sar.setAttachmentId(attachmentId);
			sar.setMessage(ex.getMessage());

			Log.d("CommunityServerAPI",
					"saveAttachment Error " + ex.getMessage());
			ex.printStackTrace();
			return sar;
		}

		return saveAttachmentResponse;

	}

	public static ApiResponse uploadChunk(String payload, byte[] chunk) {

		Log.d("CommunityServerAPI", "chunk descriptor" + payload);

		String url = String
				.format(CommunityServerAPIUtilities.HTTPS_UPLOADCHUNK);

		HttpPost request = new HttpPost(url);
		ApiResponse apiResponse = null;

		MultipartEntityBuilder entity = MultipartEntityBuilder.create();
		try {

			entity.addTextBody("descriptor", payload);
			entity.addBinaryBody("chunk", chunk);
			entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			request.setEntity(entity.build());

			/* Preparing the client */
			AndroidHttpClient client = OpenTenureApplication.getHttpClient();
			CookieStore CS = OpenTenureApplication.getCoockieStore();
			HttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, CS);

			/* Calling the Server.... */

			HttpResponse response = client.execute(request, context);

			String json = CommunityServerAPIUtilities.Slurp(response
					.getEntity().getContent(), 1024);

			Log.d("CommunityServerAPI", "UPLOAD CHUNK JSON RESPONSE " + json);

			Gson gson = new Gson();
			apiResponse = gson.fromJson(json, ApiResponse.class);
			apiResponse.setHttpStatusCode(response.getStatusLine()
					.getStatusCode());

		} catch (Throwable ex) {
			apiResponse = new ApiResponse();
			apiResponse.setHttpStatusCode(100);
			apiResponse.setMessage("uploadChunk error :" + ex.getMessage());

			Log.d("CommunityServerAPI",
					"uploadChunk error : " + ex.getMessage());
			ex.printStackTrace();
			return apiResponse;
		}

		return apiResponse;

	}
}
