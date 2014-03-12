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

import java.util.ArrayList;
import java.util.List;

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

public class Property {

	private static final float BOUNDARY_Z_INDEX = 2.0f;
	private List<Marker> vertices = new ArrayList<Marker>();
	private Polyline polyline = null;
	
	private GoogleMap map;

	Property(final Context context, final GoogleMap map) {
		
		this.map = map;

		this.map.setOnMapLongClickListener(new OnMapLongClickListener() {
			
			@Override
			public void onMapLongClick(final LatLng pix) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(context);
				dialog.setTitle(R.string.message_add_marker);
				dialog.setMessage("Lon: " + pix.longitude + ", lat: "
						+ pix.latitude);

				dialog.setPositiveButton(R.string.confirm, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						Marker marker = map.addMarker(new MarkerOptions()
						.position(new LatLng(pix.latitude,pix.longitude)).title(vertices.size()+"")
						.draggable(true));

						insertVertex(marker);
						drawBoundary();
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

						mark.remove();
						vertices.remove(mark);
						drawBoundary();
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
				drawBoundary();
				
			}

			@Override
			public void onMarkerDragEnd(Marker mark) {
				// TODO Restore default icon				
				drawBoundary();
			}

			@Override
			public void onMarkerDragStart(Marker mark) {
				// TODO Change icon to the one for dragging				
			}
			
		});
	
	}

	public void insertVertex(Marker newVertex) {

		double minDistance = Double.MAX_VALUE;
		int insertIndex = 0;

		if (vertices.size() < 3) {
			addVertex(newVertex);
			return;
		}

		for (int i = 0; i < vertices.size(); i++) {

			Marker from = vertices.get(i);
			Marker to = null;

			if (i == vertices.size() - 1) {
				to = vertices.get(0);
			} else {
				to = vertices.get(i + 1);
			}

			double currDistance = Math.sqrt(Math.pow(
					from.getPosition().latitude
							- newVertex.getPosition().latitude, 2.0)
					+ Math.pow(
							from.getPosition().longitude
									- newVertex.getPosition().longitude, 2.0))
					+ Math.sqrt(Math.pow(
							newVertex.getPosition().latitude
									- to.getPosition().latitude, 2.0)
							+ Math.pow(
									newVertex.getPosition().longitude
											- to.getPosition().longitude, 2.0));

			if (currDistance < minDistance) {
				minDistance = currDistance;
				insertIndex = i + 1;
			}

		}
		vertices.add(insertIndex, newVertex);
	}

	public void addVertex(Marker vertex) {

		vertices.add(vertex);

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
			polylineOptions.add(vertices.get(i).getPosition());
		}
		polylineOptions.add(vertices.get(0).getPosition()); // Needed in order to close the polyline
		polylineOptions.zIndex(BOUNDARY_Z_INDEX);
		polylineOptions.color(Color.RED);
		polyline = map.addPolyline(polylineOptions);
	}
}
