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

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.fao.sola.clients.android.opentenure.model.Database;



import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;

public class OpenTenureApplication extends Application {

	    private static OpenTenureApplication sInstance;
	    private Database database;
	    
	    private static boolean loggedin ;
	    private static String username;
		
		private static AndroidHttpClient mHttpClient;
		private static CookieStore cookieStore;
		private static HttpContext http_context;

	    public static OpenTenureApplication getInstance() {
	      return sInstance;
	    }

	    public Database getDatabase(){
	        return database;
	    }
	    
		public boolean isOnline() {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnected()) {
				return true;
			}
			return false;
		}

	    @Override
	    public void onCreate() {
	      super.onCreate();  
	      sInstance = this;
	      sInstance.initializeInstance();
	    }

	    protected void initializeInstance() {
	        // try to open the DB without an encryption password
	    	database = new Database(getApplicationContext(),"");
	    }
	    
	    public static HttpContext getHttp_context() {
			return http_context;
		}


		public static void setHttp_context(HttpContext http_context) {
			OpenTenureApplication.http_context = http_context;
		}


		public static CookieStore getCoockieStore() {
			if( cookieStore != null)
				return cookieStore;
			else{			
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


		
		/*
		 * Return the single instance of the inizialized HttpClient
		 * that handle connection and session to the server 
		 *  
		 */
		public static synchronized AndroidHttpClient getHttpClient() {
			        	 
						if(mHttpClient != null)
			        	 return mHttpClient;
						else 
							return prepareClient();
			     }		
		
		
		/*
		 * Initialize the single istance of AndroidHttpClient that will 
		 * handle the connections to the server
		 *  
		 */
		private static AndroidHttpClient  prepareClient(){
			
			
			try {
				
				mHttpClient = AndroidHttpClient.newInstance("Android");
		        http_context = new BasicHttpContext(); 				
		         
			    cookieStore = new BasicCookieStore();
			    http_context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
				System.out.println("Finito di preparare il client");
				
			} catch (Throwable e) {
				e.printStackTrace();				
				
			}
			return mHttpClient;
			
		}
	    
}
