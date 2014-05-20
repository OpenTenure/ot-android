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
package org.fao.sola.clients.android.opentenure.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;
import org.fao.sola.clients.android.opentenure.network.response.GetAttachmentResponse;

import android.os.AsyncTask;
import android.util.Log;

public class GetAttachmentTask extends AsyncTask<String, Void, Boolean> {

	@Override
	protected Boolean doInBackground(String... params) {
		// TODO Auto-generated method stub

		/*
		 * Check if the file already exists
		 */

		Attachment att = Attachment.getAttachment(params[1]);

		File file = new File(
				FileSystemUtilities.getAttachmentFolder(params[0]),
				att.getFileName());

		if (file.exists()) {

			/*
			 * Here will be the implementation in case of a partial file
			 */

		}

		else {
			try {

				file.createNewFile();

				/* Here I need a cycle */

				GetAttachmentResponse res = CommunityServerAPI
						.getAttachment(att.getAttachmentId());

				if (res.getHttpStatusCode() == HttpStatus.SC_OK) {
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(res.getArray());
					fos.close();

				} else {

					Log.d("CommunityServerAPI",
							"ATTACHMENT DO NOT RETRIEVED : " + res.getMessage());
				}

				if (att.getSize() == file.length()) {

					att.setPath(file.getAbsolutePath());

					Attachment.updateAttachment(att);

				} else {

					att.setPath(file.getAbsolutePath());

					Attachment.updateAttachment(att);

				}

			} catch (IOException e) {
				
				
				Log.d("CommunityServerAPI",
						"ATTACHMENT DO NOT RETRIEVED : " + e.getMessage());
				
				System.out.println("IL file sarebbe dovuto esser creato qui : "+file.getAbsolutePath());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return null;
	}

}
