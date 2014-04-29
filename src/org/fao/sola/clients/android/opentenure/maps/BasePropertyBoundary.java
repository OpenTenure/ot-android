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

import java.util.ArrayList;
import java.util.List;

import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.Vertex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class BasePropertyBoundary {

	protected static final float BOUNDARY_Z_INDEX = 2.0f;
	protected String name;
	protected List<Vertex> vertices = new ArrayList<Vertex>();

	public List<Vertex> getVertices() {
		return vertices;
	}

	protected Context context;
	protected Polyline polyline = null;
	protected GoogleMap map;
	protected LatLng center = null;
	protected Marker marker = null;
	protected LatLngBounds bounds = null;
	protected int color = Color.BLUE;

	public String getName() {
		return name;
	}

	public LatLng getCenter() {
		return center;
	}

	public Marker getMarker() {
		return marker;
	}

	public LatLngBounds getBounds() {
		return bounds;
	}

	public BasePropertyBoundary(final Context context, final GoogleMap map,
			final String claimId) {
		this.context = context;
		this.map = map;
		Claim claim = Claim.getClaim(claimId);
		if(claim != null){
			vertices = claim.getVertices();
			name = claim.getName()==null?context.getResources().getString(R.string.default_claim_name):claim.getName();
			String status = claim.getStatus();

			if(status != null){
			
				switch(Claim.Status.valueOf(status)){
				
				case unmoderated:
					color = Color.YELLOW;
					break;
				case moderated:
					color = Color.GREEN;
					break;
				case challenged:
					color = Color.RED;
					break;
				default:
					color = Color.BLUE;
					break;
				}
			}

			if(vertices != null && vertices.size() > 0){
				calculateCenterAndBounds();
				marker = createMarker(center, claim.getName()+"\n"+claim.getPerson().getFirstName()+" "+claim.getPerson().getLastName());
			}
		}
	}
	
	protected void calculateCenterAndBounds(){
		
		if(vertices == null  || vertices.size()<=0){
			return;
		}
	
		double minLat = Double.MAX_VALUE;
	    double minLong = Double.MAX_VALUE;
	    double maxLat = Double.MIN_VALUE;
	    double maxLong = Double.MIN_VALUE;

	    for (Vertex vertex : vertices) {
	    	minLat = Math.min(vertex.getMapPosition().latitude, minLat);
	    	minLong = Math.min(vertex.getMapPosition().longitude, minLong);
	    	maxLat = Math.max(vertex.getMapPosition().latitude, maxLat);
	    	maxLong = Math.max(vertex.getMapPosition().longitude, maxLong);
	    }

	    bounds = new LatLngBounds(new LatLng(minLat, minLong),new LatLng(maxLat, maxLong));
		center = new LatLng(minLat + ((maxLat - minLat) / 2), minLong
				+ ((maxLong - minLong) / 2));
	}

	private Marker createMarker(LatLng position, String title) {
		Rect boundsText = new Rect();
		Paint tf = new Paint();
		tf.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
		tf.setTextSize(20);
		tf.setTextAlign(Align.CENTER);
		tf.setAntiAlias(true);
		tf.setColor(color);
		tf.getTextBounds(name, 0, name.length(), boundsText);
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap bmpText = Bitmap.createBitmap(boundsText.width(),
		    boundsText.height(), conf);

		Canvas canvasText = new Canvas(bmpText);
		canvasText.drawText(name, canvasText.getWidth() / 2,
		            canvasText.getHeight(), tf);

		return map.addMarker(new MarkerOptions()
	    .position(position)
	    .title(title)
	    .icon(BitmapDescriptorFactory.fromBitmap(bmpText))
	    .anchor(0.5f, 1));
	}

	public void drawBoundary() {

		if (vertices.size() <= 0) {
			return;
		}
		if (polyline != null) {
			polyline.remove();
		}
		PolylineOptions polylineOptions = new PolylineOptions();
		for (int i = 0; i < vertices.size(); i++) {
			polylineOptions.add(vertices.get(i).getMapPosition());
		}
		polylineOptions.add(vertices.get(0).getMapPosition()); // Needed in
																// order to
																// close the
																// polyline
		polylineOptions.zIndex(BOUNDARY_Z_INDEX);
		polylineOptions.width(5);
		polylineOptions.color(color);
		polyline = map.addPolyline(polylineOptions);
	}
}
