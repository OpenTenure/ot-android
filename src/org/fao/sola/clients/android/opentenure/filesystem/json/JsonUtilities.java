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
package org.fao.sola.clients.android.opentenure.filesystem.json;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.util.Log;

import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.json.model.Person;
import org.fao.sola.clients.android.opentenure.filesystem.json.model.Vertex;
import org.fao.sola.clients.android.opentenure.filesystem.json.model.XMetadata;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.Metadata;


import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtilities {
	
	
 public static boolean createClaimJson(String claimID){
	 
	Log.d("CreateClaimJson","Calling data2json");
	 
	 String json = data2Json(claimID);	 
	 writeJsonTofile(claimID,json);
	 return true;
 }
 
 
 
 private static String data2Json(String claimId){
	 
	 org.fao.sola.clients.android.opentenure.filesystem.json.model.Claim tempClaim = new org.fao.sola.clients.android.opentenure.filesystem.json.model.Claim();
	 
	 Claim claim = Claim.getClaim(claimId); 
	 
	 
	 if(claim != null){
		 
		 
		// tempClaim.setChallengedClaim(null);
		 tempClaim.setName(claim.getName());
		 tempClaim.setChallenged_id_claim(claim.getChallengedClaim()!=null?claim.getChallengedClaim().getClaimId():null);
		 tempClaim.setId(claimId);
		 tempClaim.setStatus(claim.getStatus());
		 
		 Person person = new Person();
		 
		 
		 person.setContact_phone_number(claim.getPerson().getContactPhoneNumber());
		 person.setDate_of_birth(claim.getPerson().getDateOfBirth());
		 person.setEmail_address(claim.getPerson().getEmailAddress());
		 person.setFirst_name(claim.getPerson().getFirstName());
		 person.setId(claim.getPerson().getPersonId());
		 person.setLast_name(claim.getPerson().getLastName());
		 person.setMobile_phone_number(claim.getPerson().getMobilePhoneNumber());
		 person.setPlace_of_birth(claim.getPerson().getPlaceOfBirth());
		 person.setPostal_address(claim.getPerson().getPostalAddress());
		 person.setGender(claim.getPerson().getGender());
		 
		 tempClaim.setPerson(person);
		 
		 
		 List<Vertex> verteces = new ArrayList<Vertex>();

		 for (Iterator iterator = claim.getVertices().iterator(); iterator.hasNext();) {
			org.fao.sola.clients.android.opentenure.model.Vertex vert = (org.fao.sola.clients.android.opentenure.model.Vertex) iterator.next();
			
			Vertex v = new Vertex();	 

			v.setGPSPosition(vert.getGPSPosition());
			v.setMapPosition(vert.getMapPosition());
			v.setSequenceNumber(vert.getSequenceNumber());
			v.setVertexId(vert.getVertexId());
			
			
			verteces.add(v);
		}
		 
		 
		 List<org.fao.sola.clients.android.opentenure.filesystem.json.model.Attachment> attachments = new ArrayList<org.fao.sola.clients.android.opentenure.filesystem.json.model.Attachment>();
		 
		 for (Iterator iterator = claim.getAttachments().iterator(); iterator.hasNext();) {
			Attachment attachment = (Attachment) iterator.next();
			
			org.fao.sola.clients.android.opentenure.filesystem.json.model.Attachment attach = new org.fao.sola.clients.android.opentenure.filesystem.json.model.Attachment();
			
			attach.setAttachmentId(attachment.getAttachmentId());
			attach.setDescription(attachment.getDescription());
			attach.setFileName(attachment.getFileName());
			attach.setFileType(attachment.getFileType());
			attach.setMD5Sum(attachment.getMD5Sum());
			attach.setMimeType(attachment.getMimeType());
			attach.setStatus(attachment.getStatus());
			
			attachments.add(attach);
		}
		 
		 
		 List<XMetadata> xMetadata = new ArrayList<XMetadata>();
		 
		 for (Iterator iterator = claim.getMetadata().iterator(); iterator.hasNext();) {
			Metadata metadataO = (Metadata) iterator.next();
			
			XMetadata xm = new XMetadata();
			
			xm.setMetadataId(metadataO.getMetadataId());
			xm.setName(metadataO.getName());
			xm.setValue(metadataO.getValue());
			
			xMetadata.add(xm);
		}
		 
		 tempClaim.setVerteces(verteces);
		 tempClaim.setAttachments(attachments);
		 tempClaim.setMetadata(xMetadata);
		
		 
		 try {
			 Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
			 
			 String g = gson.toJson(tempClaim);			 
			 Log.d("CreateClaimJson",g);
			 
			 return g;
			
		} catch (Throwable e) {			
			
			Log.d("CreateClaimJson","An error has occurred" + e.getMessage());
			e.printStackTrace();
			
			return null;			
		}		 
	 }
	 else{ 
		 
		 Log.d("CreateClaimJson","The claim is null");
		 return null;		 
	 }
	 
 }
 
 
 
 private static boolean writeJsonTofile(String claimID, String json){
	 
	 
	 try {
		 
		// convert String into InputStream
		InputStream is = new ByteArrayInputStream(json.getBytes());
		 
		// read it with BufferedReader
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		
		
		File jFile = new File(FileSystemUtilities.getClaimFolder(claimID),"claim.json");
		
		
		if(jFile.exists())
			jFile.delete();
		
		jFile.createNewFile();
		
		BufferedWriter writer = null;		
		writer = new BufferedWriter( new FileWriter( jFile));
		
		char[] buffer = new char[1024];
		int x;
		while ((x = (br.read(buffer))) != -1) {
			
			
			writer.write(buffer,0,x);
			writer.flush();
			
		}

		writer.flush();
		writer.close();
		br.close();
				
		
	} catch (Exception e) {
		Log.d("CreateClaimJson","An error has occurred" + e.getMessage());
	}
	 
		return false;
		 
 }

}
