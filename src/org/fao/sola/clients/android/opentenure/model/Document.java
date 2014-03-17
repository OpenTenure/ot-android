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
package org.fao.sola.clients.android.opentenure.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;

public class Document {
	
	Database db = OpenTenureApplication.getInstance().getDatabase();
	
	public String getClaimId() {
		return claimId;
	}

	public void setClaimId(String claimId) {
		this.claimId = claimId;
	}

	Document(){
		this.documentId = UUID.randomUUID().toString();
	}
	
	@Override
	public String toString() {
		return "Document [documentId=" + documentId + ", claimId=" + claimId
				+ ", fileName=" + fileName + ", MD5Sum=" + MD5Sum + ", path="
				+ path + "]";
	}
	public String getDocumentId() {
		return documentId;
	}
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getMD5Sum() {
		return MD5Sum;
	}
	public void setMD5Sum(String mD5Sum) {
		MD5Sum = mD5Sum;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	public static int createDocument(Document document) {
		return OpenTenureApplication.getInstance().getDatabase().update("INSERT INTO DOCUMENT(DOCUMENT_ID, CLAIM_ID, FILE_NAME, MD5SUM, PATH) VALUES ('"
				+ document.getDocumentId()
				+ "', '"
				+ document.getClaimId()
				+ "', '"
				+ document.getFileName()
				+ "', '"
				+ document.getMD5Sum()
				+ "', '" 
				+ document.getPath()
				+ "')");

	}

	public int create() {
		return OpenTenureApplication.getInstance().getDatabase().update("INSERT INTO DOCUMENT(DOCUMENT_ID, CLAIM_ID, FILE_NAME, MD5SUM, PATH) VALUES ('"
				+ getDocumentId()
				+ "', '"
				+ getClaimId()
				+ "', '"
				+ getFileName()
				+ "', '"
				+ getMD5Sum()
				+ "', '" 
				+ getPath()
				+ "')");
	}

	public static int updateDocument(Document document) {
		return OpenTenureApplication.getInstance().getDatabase().update("UPDATE DOCUMENT SET CLAIM_ID='"
				+ document.getClaimId()
				+ "', FILE_NAME='"
				+ document.getFileName()
				+ "', MD5SUM='"
				+ document.getMD5Sum() +
				"', PATH='"
				+ document.getPath()
				+ "' WHERE DOCUMENT_ID='"
				+ document.getDocumentId() + "'");
	}

	public int update() {
		return db.update("UPDATE DOCUMENT SET CLAIM_ID='"
				+ getClaimId()
				+ "', FILE_NAME='"
				+ getFileName()
				+ "', MD5SUM='"
				+ getMD5Sum() +
				"', PATH='"
				+ getPath()
				+ "' WHERE DOCUMENT_ID='"
				+ getDocumentId() + "'");
	}

	public static int deleteDocument(Document document) {
		return OpenTenureApplication.getInstance().getDatabase().update("DELETE FROM DOCUMENT WHERE DOCUMENT_ID='"
				+ document.getDocumentId() + "'");
	}

	public int delete() {
		return db.update("DELETE FROM DOCUMENT WHERE DOCUMENT_ID='"
				+ getDocumentId() + "'");
	}

	public Document getDocument(String documentId) {

		Document document = null;

		Connection localConnection = null;
		try {

			localConnection = db.getConnection();
			PreparedStatement statement = localConnection
					.prepareStatement("SELECT CLAIM_ID, FILE_NAME, MD5SUM, PATH FROM DOCUMENT DOC WHERE DOC.DOCUMENT_ID=?");
			statement.setString(1, documentId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				document = new Document();
				document.setDocumentId(documentId);
				document.setClaimId(rs.getString(1));
				document.setFileName(rs.getString(2));
				document.setMD5Sum(rs.getString(3));
				document.setPath(rs.getString(4));
			}
			rs.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return document;
	}

	String documentId;
	String claimId;
	String fileName;
	String MD5Sum;
	String path;

}
