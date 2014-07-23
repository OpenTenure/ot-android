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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class WmsMapTileCacher extends AsyncTask<Void, Void, Void> {

	private String url;
	private int zoom;
	private int levels;
	private long cacheTime;
	private int x;
	private int y;
	private boolean includeCurrentTile;
	
	private class Tile{
		public String getTileDir() {
			return tileDir;
		}
		public String getTileFile() {
			return tileFile;
		}
		public String getTileUrl() {
			return tileUrl;
		}
		private String tileDir;
		private String tileFile;
		private String tileUrl;
		
		public Tile(String url, int x, int y, int zoom){
			tileDir = Environment.getExternalStorageDirectory()+"/Open Tenure/tiles/" + zoom + "/" + x + "/";
			tileFile = y + ".png";
            double[] bbox = WmsMapTileProvider.getBoundingBox(x, y, zoom);
            tileUrl = String.format(Locale.US, url, bbox[WmsMapTileProvider.MINX], 
                    bbox[WmsMapTileProvider.MINY], bbox[WmsMapTileProvider.MAXX], bbox[WmsMapTileProvider.MAXY]);
		}
	}

	public WmsMapTileCacher(String url, int zoom, int levels, boolean includeCurrentTile, int x, int y, long cacheTime) {

		this.url = url;
		this.zoom = zoom;
		this.levels = levels;
		this.cacheTime = cacheTime;
		this.includeCurrentTile = includeCurrentTile;
		this.x = x;
		this.y = y;

	}

	protected void onPreExecute() {
	}

	protected Void doInBackground(Void... params) {

		FileOutputStream fos = null;
		InputStream is = null;
		
		if (zoom > 21) {
			return null;
		}

		try {

            List <Tile> tilesToDownload = new ArrayList<Tile>();

            if(includeCurrentTile){
                tilesToDownload.add(new Tile(url, x, y, zoom));
            }

            if(zoom < 21){
	            tilesToDownload.add(new Tile(url, 2*x, 2*y, zoom+1));
	            tilesToDownload.add(new Tile(url, 2*x+1, 2*y, zoom+1));
	            tilesToDownload.add(new Tile(url, 2*x, 2*y+1, zoom+1));
	            tilesToDownload.add(new Tile(url, 2*x+1, 2*y+1, zoom+1));
    		}


			for(Tile tileToDownload:tilesToDownload){

				File tileDirFile = new File(tileToDownload.getTileDir());
				tileDirFile.mkdirs();

				File outputFile = new File(tileToDownload.getTileDir(), tileToDownload.getTileFile());

				if(!outputFile.exists() || (outputFile.lastModified() < (System.currentTimeMillis() - cacheTime))){
					URL url = new URL(tileToDownload.getTileUrl());
					HttpURLConnection c = (HttpURLConnection) url.openConnection();
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
					Log.d(this.getClass().getName(), "cached/refreshed tile from url " + tileToDownload.getTileUrl() + " to file " + outputFile.getPath());
				}else{
					Log.d(this.getClass().getName(), "no need to refresh cached tile file " + outputFile.getPath());
				}
			}
				
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(fos!=null){
				try {fos.close();} catch (IOException ignore) {}
			}
			if(is!=null){
				try {is.close();} catch (IOException ignore) {}
			}
		}
		return null;

	}

	protected void onPostExecute(String result) {
		if(zoom < 21 && levels > 1){
			// Create a cacher for each tile on the next zoom level
			new WmsMapTileCacher(url, zoom+1, levels-1, false, 2*x, 2*y, cacheTime).execute();
			new WmsMapTileCacher(url, zoom+1, levels-1, false, 2*x+1, 2*y, cacheTime).execute();
			new WmsMapTileCacher(url, zoom+1, levels-1, false, 2*x, 2*y+1, cacheTime).execute();
			new WmsMapTileCacher(url, zoom+1, levels-1, false, 2*x+1, 2*y+1, cacheTime).execute();
		}
	}

}