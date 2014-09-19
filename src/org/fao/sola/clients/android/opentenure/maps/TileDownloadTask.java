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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.model.Task;
import org.fao.sola.clients.android.opentenure.model.Tile;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class TileDownloadTask extends AsyncTask<Void, Integer, Integer> {

	public static final String TASK_ID = "TileDownloadTask";
	private static final int TILES_PER_BATCH = 50;
	private static final long TILE_CACHE_TIME = 30 * 24 * 60 * 60 * 1000;
	private static final long TIMEOUT = 4000;
	private Context context;

	public void setContext(Context context) {
		this.context = context;
	}

	public TileDownloadTask() {
	}

	protected void onPreExecute() {
		Task.createTask(new Task(TASK_ID));
	}

	protected Integer doInBackground(Void... params) {

		List<Tile> tiles = Tile.getTilesToDownload(TILES_PER_BATCH);
		int originalTilesToDownload = Tile.getTilesToDownload();
		long startTime = System.currentTimeMillis();
		int failures = 0;
		int downloadedTiles = 0;
		BigDecimal completion = BigDecimal.valueOf(0);
//		Log.d(this.getClass().getName(),
//				"Nobody else is consuming tiles, let's go. Starting to download "
//						+ originalTilesToDownload + " tiles");

		while (tiles != null && tiles.size() >= 1) {
//			Log.d(this.getClass().getName(),
//					"Downloading a batch of " + tiles.size()
//							+ " tiles, tiles to go: "
//							+ (originalTilesToDownload - downloadedTiles));
			for (Tile tile : tiles) {

				File outputFile = new File(tile.getFileName());
				File dir = new File(outputFile.getParent());
				dir.mkdirs();
				InputStream is = null;
				FileOutputStream fos = null;

				long lastModified = outputFile.lastModified();
				boolean fileExists = outputFile.exists();

				if (!fileExists
						|| (fileExists && (lastModified > (System
								.currentTimeMillis() - TILE_CACHE_TIME)))) {
					try {
//						if (fileExists) {
//							Log.d(this.getClass().getName(),
//									outputFile.getPath()
//											+ " exists and has been modified on "
//											+ new Date(lastModified));
//
//						} else {
//							Log.d(this.getClass().getName(),
//									outputFile.getPath() + " does not exist");
//						}
						URL url = new URL(tile.getUrl());
						HttpURLConnection c = (HttpURLConnection) url
								.openConnection();
						c.setRequestMethod("GET");
						c.setDoOutput(true);
						c.setConnectTimeout((int) TIMEOUT / 2);
						c.setReadTimeout((int) TIMEOUT / 2);
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
//						Log.d(this.getClass().getName(),
//								"cached/refreshed tile from url "
//										+ tile.getUrl() + " to file "
//										+ outputFile.getPath());
						downloadedTiles++;
					} catch (IOException e) {
						failures++;
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
//					Log.d(this.getClass().getName(),
//							"no need to refresh cached tile file "
//									+ outputFile.getPath());
					downloadedTiles++;
				}
				tile.delete();

			}
			completion = new BigDecimal(
					((double) downloadedTiles / (double) originalTilesToDownload) * 100.0);
//			Log.d(this.getClass().getName(),
//					String.format(Locale.US,
//							"Download task completion: %.2f percent",
//							completion.doubleValue())
//							+ ", elapsed time: "
//							+ ((System.currentTimeMillis() - startTime) / 60000)
//							+ " min");
			tiles = Tile.getTilesToDownload(TILES_PER_BATCH);
		}
//		Log.d(this.getClass().getName(), "Done downloading tiles");
		return failures;

	}

	protected void onPostExecute(Integer failures) {

		Task.deleteTask(new Task(TASK_ID));
		// If many tasks like this were running only the one serving the last
		if (failures > 0) {
			Toast.makeText(
					context,
					String.format(
							context.getResources().getString(
									R.string.not_all_tiles_downloaded),
							failures), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(
					context,
					context.getResources().getString(
							R.string.all_tiles_downloaded), Toast.LENGTH_LONG)
					.show();
		}
	}
}
