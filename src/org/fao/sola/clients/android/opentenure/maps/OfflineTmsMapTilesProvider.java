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

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.OpenTenurePreferencesActivity;
import org.fao.sola.clients.android.opentenure.model.Tile;

import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.UrlTileProvider;

public class OfflineTmsMapTilesProvider extends UrlTileProvider implements OfflineTilesProvider{

	// array indexes for array to hold bounding boxes.
	public static final int MINX = 0;
	public static final int MINY = 1;
	public static final int MAXX = 2;
	public static final int MAXY = 3;

	// array indexes for array to hold tile x y.
	public static final int X = 0;
	public static final int Y = 1;

	// array indexes for array to hold tile x y.
	public static final int NORTH_EAST_X = 0;
	public static final int NORTH_EAST_Y = 1;
	public static final int SOUTH_WEST_X = 2;
	public static final int SOUTH_WEST_Y = 3;

    private String URL_STRING;
    
    public OfflineTmsMapTilesProvider(int width, int height, SharedPreferences preferences) {
    	super(width, height);
		URL_STRING = preferences.getString(
				OpenTenurePreferencesActivity.TMS_URL_PREF,
				"http://khm1.google.com/kh/v=152");
		URL_STRING += "&x=%d&y=%d&z=%d";
	}
    
    private String getUrl(int x, int y, int zoom){
        return String.format(Locale.US, URL_STRING, x, y, zoom);
    }

	@Override
	public URL getTileUrl(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	private static double mercatorFromLatitude(double latitude) {
	    double radians = Math.log(Math.tan(Math.toRadians(latitude+90.0)/2));
	    double mercator = Math.toDegrees(radians);
	    return mercator;
	}
	
	private static int[] tileOfCoordinate(LatLng coord, int zoom) {
	    int[] result = new int[2];
		int noTiles = (1 << zoom);
	    double longitudeSpan = 360.0 / noTiles;
	    result[X] = BigDecimal.valueOf((coord.longitude +180.0)/longitudeSpan).intValue();
	    result[Y] = -(BigDecimal.valueOf(((noTiles * (mercatorFromLatitude(coord.latitude) - 180.0)))/360.0).intValue());

	    return result;
	}
	
	public List<Tile> getTilesForLatLngBounds(LatLngBounds llb, int startZoom, int endZoom){
		List<Tile> tiles = new ArrayList<Tile>();
		
		int[] northeast = tileOfCoordinate(llb.northeast, startZoom);
		int[] southwest = tileOfCoordinate(llb.southwest, startZoom);
		
		for(int zoom = startZoom ; zoom <= endZoom; zoom++){
			
			for(int x = southwest[X] ; x <= northeast[X] ; x++){
				for(int y = northeast[Y] ; y <= southwest[Y] ; y++){
					String fileName = getBaseStorageDir() + zoom + "/" + x + "/" + y + getTilesSuffix();
					Tile tile = new Tile();
					tile.setUrl(getUrl(x, y, zoom));
					tile.setFileName(fileName);
					tiles.add(tile);
				}
			}

			// At each subsequent level of zoom, tiles indexes double

			northeast[X] *= 2;
			northeast[Y] *= 2;
			southwest[X] *= 2;
			southwest[Y] *= 2;
		}
		
		return tiles;
	}

	@Override
	public TilesProviderType getType() {
		return TilesProviderType.TMS;
	}

	@Override
	public String getBaseStorageDir() {
		return OpenTenureApplication.getContext().getExternalFilesDir(null).getAbsolutePath() + "/tiles/" + getType() + "/" ;
	}

	@Override
	public String getTilesSuffix() {
		return ".jpg";
	}

}