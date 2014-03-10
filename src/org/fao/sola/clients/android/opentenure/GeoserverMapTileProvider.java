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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import android.util.Log;

import com.google.android.gms.maps.model.UrlTileProvider;

public class GeoserverMapTileProvider extends UrlTileProvider{

	// Web Mercator n/w corner of the map.
	private static final double[] TILE_ORIGIN = {-20037508.34789244, 20037508.34789244};
	//array indexes for that data
	private static final int ORIG_X = 0; 
	private static final int ORIG_Y = 1; // "

	// Size of square world map in meters, using WebMerc projection.
	private static final double MAP_SIZE = 20037508.34789244 * 2;

	// array indexes for array to hold bounding boxes.
	protected static final int MINX = 0;
	protected static final int MINY = 1;
	protected static final int MAXX = 2;
	protected static final int MAXY = 3;

    String baseURL = "mytileserver.com";
    String version = "1.3.0";
    String request = "GetMap";
    String format = "image/png";
    String srs = "EPSG:900913";
    String service = "WMS";
    String width = "256";
    String height = "256";
    String styles = "";
    String layers = "wtx:road_hazards";

    final String URL_STRING = baseURL + 
            "&LAYERS=" + layers + 
            "&VERSION=" + version + 
            "&SERVICE=" + service + 
            "&REQUEST=" + request + 
            "&TRANSPARENT=TRUE&STYLES=" + styles + 
            "&FORMAT=" + format + 
            "&SRS=" + srs + 
            "&BBOX=%f,%f,%f,%f" + 
            "&WIDTH=" + width + 
            "&HEIGHT=" + height;
    
    public GeoserverMapTileProvider(int width, int height) {
	    super(width, height);
	    // TODO Auto-generated constructor stub
	}

    @Override
    public synchronized URL getTileUrl(int x, int y, int zoom) {
        try {       

            double[] bbox = getBoundingBox(x, y, zoom);

            String s = String.format(Locale.getDefault(), URL_STRING, bbox[MINX], 
                    bbox[MINY], bbox[MAXX], bbox[MAXY]);

            Log.d("GeoServerTileURL", s);

            URL url = null;

            try {
                url = new URL(s);
            } 
            catch (MalformedURLException e) {
                throw new AssertionError(e);
            }

            return url;
        }
        catch (RuntimeException e) {
            Log.d("GeoServerTileException", "getTile x=" + x + ", y=" + y + ", zoomLevel=" + zoom + " raised an exception", e);
            throw e;
        }

    }
    
    // Return a web Mercator bounding box given tile x/y indexes and a zoom
	// level.
	protected double[] getBoundingBox(int x, int y, int zoom) {
	    double tileSize = MAP_SIZE / Math.pow(2, zoom);
	    double minx = TILE_ORIGIN[ORIG_X] + x * tileSize;
	    double maxx = TILE_ORIGIN[ORIG_X] + (x+1) * tileSize;
	    double miny = TILE_ORIGIN[ORIG_Y] - (y+1) * tileSize;
	    double maxy = TILE_ORIGIN[ORIG_Y] - y * tileSize;

	    double[] bbox = new double[4];
	    bbox[MINX] = minx;
	    bbox[MINY] = miny;
	    bbox[MAXX] = maxx;
	    bbox[MAXY] = maxy;

	    return bbox;
	}

	}