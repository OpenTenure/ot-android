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
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.model.Task;
import org.fao.sola.clients.android.opentenure.model.Tile;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class TileDownloadTask extends AsyncTask<Void, Double, Double> {

	private static final int TILES_PER_BATCH = 10;
	private static final long TILE_CACHE_TIME = 30 * 24 * 60 * 60 * 1000;
	private static final String TASK_ID = "TileDownloadTask";
	private Context context;

	public void setContext(Context context) {
		this.context = context;
	}


	public TileDownloadTask() {
	}
	

	protected void onPreExecute() {
		Task.deleteTask(new Task(TASK_ID));
		Task.createTask(new Task(TASK_ID));
	}

	protected Double doInBackground(Void... params) {

		List<Tile> tiles = Tile.getTilesToDownload(TILES_PER_BATCH);
		int originalTilesToDownload = Tile.getTilesToDownload();
		int downloadedTiles = 0;
		BigDecimal completion = BigDecimal.valueOf(0);

		Log.d(this.getClass().getName(), "Starting to download " + originalTilesToDownload + " tiles");

		while (tiles != null && tiles.size() >= 1) {
			Log.d(this.getClass().getName(), "Downloading a batch of " + tiles.size() + " tiles, tiles to go: " + (originalTilesToDownload - downloadedTiles));
			for (Tile tile : tiles) {

				File outputFile = new File(tile.getFileName());
				File dir = new File(outputFile.getParent());
				dir.mkdirs();
				InputStream is = null;
				FileOutputStream fos = null;

				if (!outputFile.exists()
						|| (outputFile.lastModified() < (System
								.currentTimeMillis() - TILE_CACHE_TIME))) {
					try{
					URL url = new URL(tile.getUrl());
					HttpURLConnection c = (HttpURLConnection) url
							.openConnection();
					c.setRequestMethod("GET");
					c.setDoOutput(true);
					c.connect();
					fos = new FileOutputStream(outputFile);

					is = c.getInputStream();

					byte[] buffer = new byte[1024];
					int len1 = 0;
					while ((len1 = is.read(buffer)) != -1) {

						fos.write(buffer, 0, len1);
					}
					fos.close();
					is.close();
					Log.d(this.getClass().getName(),
							"cached/refreshed tile from url " + tile.getUrl()
									+ " to file " + outputFile.getPath());
					downloadedTiles++;
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (is != null) {
							try {
								is.close();
							} catch (IOException ignore) {
							}
						}
						if (fos != null) {
							try {
								fos.close();
							} catch (IOException ignore) {
							}
						}
					}
				} else {
					Log.d(this.getClass().getName(),
							"no need to refresh cached tile file "
									+ outputFile.getPath());
					downloadedTiles++;
				}
				tile.delete();

			}
			completion = new BigDecimal((downloadedTiles/originalTilesToDownload)*100.0);
			Task task = Task.getTask(TASK_ID);
			task.updateCompletion(completion);
			Log.d(this.getClass().getName(), String.format(Locale.US, "Download task completion: %.2f percent", completion.doubleValue()/100.0));
			tiles = Tile.getTilesToDownload(TILES_PER_BATCH);
		}
		Log.d(this.getClass().getName(), "Done downloading tiles");
		return completion.doubleValue();

	}

	protected void onPostExecute(Double result) {
		if(result < 99.99){
			Toast.makeText(context, String.format(context.getResources().getString(R.string.not_all_tiles_downloaded), result), Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(context, context.getResources().getString(R.string.all_tiles_downloaded), Toast.LENGTH_SHORT).show();
		}
		Task.deleteTask(new Task(TASK_ID));
	}
}
