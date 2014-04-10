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

import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.ZipUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.json.JsonUtilities;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;	
import android.util.Log;


public class ExporterTask extends AsyncTask<String, Void, Boolean >{


	protected ProgressDialog progressDialog;
	private Context mContext;

	public ExporterTask(Context context){
		super();
		mContext = context;

	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();	

		progressDialog = ProgressDialog.show(mContext,mContext.getString(R.string.title_export),
				mContext.getString(R.string.message_export),
				true, false);
	}

	@Override
	protected Boolean doInBackground(String... params) {


		try {				
			JsonUtilities.createClaimJson((String) params[1]);				
			FileSystemUtilities.deleteCompressedClaim((String) params[1]);

			ZipUtilities.AddFilesWithAESEncryption((String)params[0],(String) params[1]);

			progressDialog.dismiss();

			return true;				
		} catch (Exception e) {
			Log.d("ExporterTask","And error has occured creating the compressed claim ");
			return false;
		}

	}




}




