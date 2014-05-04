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
import java.net.UnknownHostException;

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
import org.fao.sola.clients.android.opentenure.filesystem.json.model.Claim;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPIUtilities.Login;
import org.fao.sola.clients.android.opentenure.network.response.SaveClaimResponse;

import com.google.gson.Gson;

import android.net.http.AndroidHttpClient;
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

//			if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_OK)) {
//
//				String json = CommunityServerAPIUtilities.Slurp(response
//						.getEntity().getContent(), 1024);
//
//				/* parsing the response in a Login object */
//				Gson gson = new Gson();
//				Login login = gson.fromJson(json, Login.class);

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

//			if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_OK)) {
//
//				String json = CommunityServerAPIUtilities.Slurp(response
//						.getEntity().getContent(), 1024);
//
//				/* parsing the response */
//				Gson gson = new Gson();
//				Login login = gson.fromJson(json, Login.class);

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
				
				Log.d("CommunityServerAPI",
						"CLAIM JSON STRING " + json);

				Gson gson = new Gson();
				Claim claim = gson.fromJson(json, Claim.class);
				
				
				Log.d("CommunityServerAPI",
						"CLAIM JSON GPSGEOMETRY " + claim.getGpsGeometry());
				
				return claim;

			} else {
				
				Log.d("CommunityServerAPI",
						"CLAIM not retrieved ");
				return null;	
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return null;
		}


	}
	
	public static SaveClaimResponse saveClaim(String claim){
		
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
			Log.d("CommunityServerAPI",
					"saveClaim status line " + response.getStatusLine());
			
			
			String json = CommunityServerAPIUtilities.Slurp(response
					.getEntity().getContent(), 1024);
			
			Log.d("CommunityServerAPI",
					"SAVE CLAIM JSON RESPONSE " + json);
			
			Gson gson = new Gson();
			SaveClaimResponse saveResponse = gson.fromJson(json, SaveClaimResponse.class);
			
			saveResponse.setHttpStatusCode(response.getStatusLine().getStatusCode());
			
			return saveResponse;
			
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}		
		

	}

}
