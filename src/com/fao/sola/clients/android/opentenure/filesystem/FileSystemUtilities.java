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
package com.fao.sola.clients.android.opentenure.filesystem;

import java.io.File;
import java.io.IOException;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

public class FileSystemUtilities {


	private static String _CLAIMS_FOLDER = "claims";
	private static String _CLAIM_PREFIX = "claim_";
	private static String _CLAIM_METADATA = "metadata";
	private static String _SIGNED_CLAIM = "signed_claim";
	private static String _TENURE_IMAGERY = "tenure_imagery";
	private static String _OWNERS = "owners";
	private static String _OWNERS_ID = "owners//id";
	private static String _OWNERS_PHOTOGRAPHIES = "owners//photograpies";


	public static boolean createClaimsFolder(){

		
		if(isExternalStorageWritable()){		

			try {
				Context context = OpenTenureApplication.getContext();	
				File appFolder = context.getExternalFilesDir(null);				
				new File(appFolder, _CLAIMS_FOLDER).mkdir();				
				File claimsFolder = new File(appFolder.getAbsoluteFile()+File.separator+_CLAIMS_FOLDER);

				if(claimsFolder.exists() && claimsFolder.isDirectory())
					return true;
				else
					return false;

			} catch (Exception e) {					
				System.err.println("Error creating Claims folder" + e.getMessage());
				return false;
			}			
		}
		else {

			System.out.println("External Storage not Writable");
			return false;			
		}		

	}



	public static boolean createClaimFileSystem(String claimID){

		File claimFolder = null;
		File claimsFolder = null;		

		try {

			claimsFolder = getClaimsFolder();

			new File(claimsFolder,_CLAIM_PREFIX+claimID).mkdir();

			claimFolder = new File(claimsFolder,_CLAIM_PREFIX+claimID);

			new File(claimFolder, _CLAIM_METADATA).mkdir();
			new File(claimFolder, _SIGNED_CLAIM).mkdir();
			new File(claimFolder, _TENURE_IMAGERY).mkdir();
			new File(claimFolder, _OWNERS_ID).mkdir();
			new File(claimFolder, _OWNERS_PHOTOGRAPHIES).mkdir();

		} catch (Exception e) {
			System.out.println("Error creating the file system of the claim");
			return false ;
		}

		return(new File(claimFolder,_CLAIM_METADATA).exists() && 
				new File(claimFolder,_SIGNED_CLAIM).exists() &&  
				new File(claimFolder,_TENURE_IMAGERY).exists() && 
				new File(claimFolder,_OWNERS_ID).exists() &&
				new File(claimFolder,_OWNERS_PHOTOGRAPHIES).exists()
				);		
	}


	public static File getClaimsFolder(){

		Context context = OpenTenureApplication.getContext();	
		File appFolder = context.getExternalFilesDir(null);				
		return new File(appFolder, _CLAIMS_FOLDER);

	}


	public static File getClaimFolder(String claimID){
		getClaimsFolder();
		return new File(getClaimsFolder(), _CLAIM_PREFIX + claimID);		

	}

	public static File getMetadataFolder(String claimID){
		getClaimsFolder();
		return new File(getClaimFolder(claimID), _CLAIM_METADATA);
	}

	public static File getSignedClaim(String claimID){
		getClaimsFolder();
		return new File(getClaimFolder(claimID), _SIGNED_CLAIM);
	}

	public static File getTenureImageryFolder(String claimID){
		getClaimsFolder();
		return new File(getClaimFolder(claimID), _TENURE_IMAGERY);
	}

	public static File getOwnersIDFolder(String claimID){
		getClaimsFolder();
		return new File(getClaimFolder(claimID), _OWNERS_ID);
	}

	public static File getOwnersPhotograpiesFolder(String claimID){
		getClaimsFolder();
		return new File(getClaimFolder(claimID), _OWNERS_PHOTOGRAPHIES);
	}




	protected static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}




}
