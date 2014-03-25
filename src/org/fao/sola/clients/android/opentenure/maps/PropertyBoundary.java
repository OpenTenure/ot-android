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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.fao.sola.clients.android.opentenure.ClaimDispatcher;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.model.Vertex;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class PropertyBoundary {

	private static final float BOUNDARY_Z_INDEX = 2.0f;
	private List<Vertex> vertices = new ArrayList<Vertex>();
	private Map<String, Vertex> verticesMap = new HashMap<String, Vertex>();
	private ClaimDispatcher claimActivity;

	private Polyline polyline = null;
	private GoogleMap map;

	public PropertyBoundary(final Context context, final GoogleMap map, final ClaimDispatcher claimActivity) {
		this.claimActivity = claimActivity;
		this.map = map;
		vertices = Vertex.getVertices(claimActivity.getClaimId());
		
		for(Vertex vertex : vertices){
			Marker mark = createMarker(vertex.getMapPosition());
			verticesMap.put(mark.getId(), vertex);
		}

		this.map.setOnMapLongClickListener(new OnMapLongClickListener() {
			
			@Override
			public void onMapLongClick(final LatLng position) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(context);
				dialog.setTitle(R.string.message_add_marker);
				dialog.setMessage("Lon: " + position.longitude + ", lat: "
						+ position.latitude);

				dialog.setPositiveButton(R.string.confirm, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						Marker mark = createMarker(position);
						Vertex vert = new Vertex(position);
						vert.setClaimId(claimActivity.getClaimId());
						verticesMap.put(mark.getId(), vert);
						insertVertex(vert);
						drawBoundary();
						updateClaimBoundary(claimActivity.getClaimId());
					}
				});
				dialog.setNegativeButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

				dialog.show();
				
			}
		});
		this.map.setOnMarkerClickListener(new OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(final Marker mark) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(context);
				dialog.setTitle(R.string.message_remove_marker);
				dialog.setMessage("Marker " + mark.getTitle() + ", at lon: " + mark.getPosition().longitude + ", lat: " + mark.getPosition().latitude);
				dialog.setPositiveButton(R.string.confirm, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						vertices.remove(verticesMap.remove(mark.getId()));
						mark.remove();
						drawBoundary();
						updateClaimBoundary(claimActivity.getClaimId());
					}
				});
				dialog.setNegativeButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				dialog.show();
				return false;
			}
		});
		this.map.setOnMarkerDragListener(new OnMarkerDragListener() {
			@Override
			public void onMarkerDrag(Marker mark) {
				mark.setTitle(mark.getId() + String.format(Locale.getDefault(), " - lat: %+.6f, long: %+.6f", mark.getPosition().latitude, mark.getPosition().longitude));
				mark.showInfoWindow();
				verticesMap.get(mark.getId()).setMapPosition(mark.getPosition());
				drawBoundary();
				
			}

			@Override
			public void onMarkerDragEnd(Marker mark) {
				mark.hideInfoWindow();
				mark.setTitle(mark.getId());
				verticesMap.get(mark.getId()).setMapPosition(mark.getPosition());
				updateClaimBoundary(claimActivity.getClaimId());
				drawBoundary();
			}

			@Override
			public void onMarkerDragStart(Marker mark) {
				mark.setTitle(mark.getId() + String.format(Locale.getDefault(), " - lat: %+.6f, long: %+.6f", mark.getPosition().latitude, mark.getPosition().longitude));
				mark.showInfoWindow();
				verticesMap.get(mark.getId()).setMapPosition(mark.getPosition());
				drawBoundary();
			}
			
		});
	
	}

	public LatLng getCenter(){
		
		if(vertices.size()<=0){
			return Vertex.INVALID_POSITION;
		}
		double minLat = Double.MAX_VALUE, minLon = Double.MAX_VALUE;
		double maxLat = Double.MIN_VALUE, maxLon = Double.MIN_VALUE;

		for (Vertex vertex : vertices){
			if(vertex.getMapPosition().latitude > maxLat){
				maxLat = vertex.getMapPosition().latitude;
			}
			if(vertex.getMapPosition().latitude < minLat){
				minLat = vertex.getMapPosition().latitude;
			}
			if(vertex.getMapPosition().longitude > maxLon){
				maxLon = vertex.getMapPosition().longitude;
			}
			if(vertex.getMapPosition().longitude < minLon){
				minLon = vertex.getMapPosition().longitude;
			}
		}
		
		return new LatLng(minLat +((maxLat-minLat)/2),minLon +((maxLon-minLon)/2));
	}

	public void updateClaimBoundary(String claimId){

		Vertex.deleteVertices(claimId);

		for(int i = 0 ; i < vertices.size() ; i++){
			Vertex vertex = vertices.get(i);
			vertex.setSequenceNumber(i);
			Vertex.createVertex(vertex);
		}
		
	}

	public void insertVertexFromGPS(LatLng position) {
		Marker mark = createMarker(position);
		Vertex vert = new Vertex(position);
		vert.setClaimId(claimActivity.getClaimId());
		verticesMap.put(mark.getId(), vert);
		insertVertex(vert);
		drawBoundary();
		updateClaimBoundary(claimActivity.getClaimId());
	}

	private void insertVertex(Vertex newVertex) {

		double minDistance = Double.MAX_VALUE;
		int insertIndex = 0;

		if (vertices.size() < 3) {
			addVertex(newVertex);
			return;
		}

		for (int i = 0; i < vertices.size(); i++) {

			Vertex from = vertices.get(i);
			Vertex to = null;

			if (i == vertices.size() - 1) {
				to = vertices.get(0);
			} else {
				to = vertices.get(i + 1);
			}

			double currDistance = Math.sqrt(Math.pow(
					from.getMapPosition().latitude
							- newVertex.getMapPosition().latitude, 2.0)
					+ Math.pow(
							from.getMapPosition().longitude
									- newVertex.getMapPosition().longitude, 2.0))
					+ Math.sqrt(Math.pow(
							newVertex.getMapPosition().latitude
									- to.getMapPosition().latitude, 2.0)
							+ Math.pow(
									newVertex.getMapPosition().longitude
											- to.getMapPosition().longitude, 2.0));

			if (currDistance < minDistance) {
				minDistance = currDistance;
				insertIndex = i + 1;
			}

		}
		vertices.add(insertIndex, newVertex);
	}

	private void addVertex(Vertex vertex) {

		vertices.add(vertex);
	}

	private Marker createMarker(LatLng position){
		return map.addMarker(new MarkerOptions()
		.position(position).title(vertices.size()+"")
		.draggable(true));
	}

	public void drawBoundary(){
		
		if(vertices.size() <= 0){
			return;
		}
		if(polyline != null){
			polyline.remove();
		}
		PolylineOptions polylineOptions = new PolylineOptions();
		for (int i = 0 ; i < vertices.size() ; i++) {
			polylineOptions.add(vertices.get(i).getMapPosition());
		}
		polylineOptions.add(vertices.get(0).getMapPosition()); // Needed in order to close the polyline
		polylineOptions.zIndex(BOUNDARY_Z_INDEX);
		polylineOptions.width(5);
		polylineOptions.color(Color.RED);
		polyline = map.addPolyline(polylineOptions);
	}
}
