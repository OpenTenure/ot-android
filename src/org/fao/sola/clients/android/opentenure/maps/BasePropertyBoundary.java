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
import org.fao.sola.clients.android.opentenure.model.Adjacency.CardinalDirection;
import org.fao.sola.clients.android.opentenure.model.Adjacency;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimType;
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
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class BasePropertyBoundary {

	protected static final float BOUNDARY_Z_INDEX = 2.0f;
	public static final double SNAP_THRESHOLD = 0.0001;

	protected String name;
	protected String claimId;
	protected List<Vertex> vertices = new ArrayList<Vertex>();

	public List<Vertex> getVertices() {
		return vertices;
	}

	protected Context context;
	protected Polyline polyline = null;
	protected Polygon polygon = null;
	protected GoogleMap map;
	protected LatLng center = null;
	protected Marker marker = null;
	protected LatLngBounds bounds = null;
	protected int color = Color.BLUE;

	public String getName() {
		return name;
	}

	public String getClaimId() {
		return claimId;
	}

	public LatLng getCenter() {
		return center;
	}

	public Polygon getPolygon() {
		return polygon;
	}

	public Marker getMarker() {
		return marker;
	}

	public LatLngBounds getBounds() {
		return bounds;
	}

	public BasePropertyBoundary(final Context context, final GoogleMap map,
			final Claim claim) {
		this.context = context;
		this.map = map;
		if (claim != null) {
			vertices = claim.getVertices();
			name = claim.getName() == null || claim.getName().equalsIgnoreCase("") ? context.getResources().getString(
					R.string.default_claim_name) : claim.getName();
			String status = claim.getStatus();
			claimId = claim.getClaimId();

			if (status != null) {

				switch (Claim.Status.valueOf(status)) {

				case unmoderated:
					color = context.getResources().getColor(
							R.color.status_unmoderated);
					break;
				case moderated:
					color = context.getResources().getColor(
							R.color.status_moderated);
					break;
				case challenged:
					color = context.getResources().getColor(
							R.color.status_challenged);
					break;
				default:
					color =context.getResources().getColor(
							R.color.status_created);
					break;
				}
			}

			if (vertices != null && vertices.size() > 0) {
				calculateGeometry();
				ClaimType ct = new ClaimType();
				String claimName = claim.getName().equalsIgnoreCase("")? context.getString(R.string.default_claim_name):claim.getName();
				marker = createMarker(center, claimName + ", "
						+ context.getString(R.string.by) + ": "
						+ claim.getPerson().getFirstName() + " "
						+ claim.getPerson().getLastName() + ", "
						+ context.getString(R.string.type) + ": "
						+ ct.getDisplayValueByType(claim.getType()));
			}
		}
	}

	protected void resetAdjacency(List<BasePropertyBoundary> existingProperties){

		List<BasePropertyBoundary> adjacentProperties = findAdjacentProperties(existingProperties);
		Adjacency.deleteAdjacencies(claimId);

		if(adjacentProperties != null){

			for (BasePropertyBoundary adjacentProperty : adjacentProperties) {
				
				Adjacency adj = new Adjacency();
				adj.setSourceClaimId(claimId);
				adj.setDestClaimId(adjacentProperty.getClaimId());
				adj.setCardinalDirection(getCardinalDirection(adjacentProperty));
				adj.create();
			}
		}
	}

	protected void calculateGeometry() {
		
		int fakeCoords = 1;

		if (vertices == null || vertices.size() <= 0) {
			return;
		}
		if (vertices.size() <= 1) {
			center = vertices.get(0).getMapPosition();
			bounds = new LatLngBounds(center, center);
			return;
		}

		GeometryFactory gf = new GeometryFactory();

		if (vertices.size() <= 2) {
			// need at least four coordinates for a closed polygon with three vertices
			fakeCoords = 2;
		}

		Coordinate[] coords = new Coordinate[vertices.size() + fakeCoords];

		int i = 0;

		for (Vertex vertex : vertices) {
			coords[i++] = new Coordinate(vertex.getMapPosition().longitude,
					vertex.getMapPosition().latitude);
		}

		if (vertices.size() <= 2) {
			coords[i++] = new Coordinate(vertices.get(1).getMapPosition().longitude,
					vertices.get(1).getMapPosition().latitude);
		}

		coords[i] = new Coordinate(vertices.get(0).getMapPosition().longitude,
				vertices.get(0).getMapPosition().latitude);

		polygon = gf.createPolygon(coords);
		polygon.setSRID(3857);

		bounds = new LatLngBounds(new LatLng(polygon.getEnvelope().getCoordinates()[0].y,
						polygon.getEnvelope().getCoordinates()[0].x),
				new LatLng(polygon.getEnvelope().getCoordinates()[2].y,
						polygon.getEnvelope().getCoordinates()[2].x));
		center = new LatLng(polygon.getCentroid().getCoordinate().y, polygon
				.getCentroid().getCoordinate().x);
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
				boundsText.height() - boundsText.bottom, conf);

		Canvas canvasText = new Canvas(bmpText);
		canvasText.drawText(name, canvasText.getWidth() / 2,
				canvasText.getHeight(), tf);

		return map.addMarker(new MarkerOptions().position(position)
				.title(title).icon(BitmapDescriptorFactory.fromBitmap(bmpText))
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
	
	public List<BasePropertyBoundary> findAdjacentProperties(List<BasePropertyBoundary> properties){
		List<BasePropertyBoundary> adjacentProperties = null;
		for(BasePropertyBoundary property : properties){
			if(polygon != null && property.getPolygon() != null && polygon.distance(property.getPolygon()) < SNAP_THRESHOLD){
				if(adjacentProperties == null){
					adjacentProperties = new ArrayList<BasePropertyBoundary>();
				}
				adjacentProperties.add(property);
			}
		}
		return adjacentProperties;
	}

	public CardinalDirection getCardinalDirection(BasePropertyBoundary dest){
		double deltaX = dest.getCenter().longitude - center.longitude;
		double deltaY = dest.getCenter().latitude - center.latitude;
		if(deltaX == 0){
			return deltaY > 0 ? CardinalDirection.NORTH : CardinalDirection.SOUTH;
		}
		double slope = deltaY/deltaX;
		if(slope >= -1.0/3.0 && slope < 1.0/3.0){
			return deltaX > 0 ? CardinalDirection.EAST : CardinalDirection.WEST;
		}else if(slope >= 1.0/3.0 && slope < 3.0){
			return deltaY > 0 ? CardinalDirection.NORTHEAST : CardinalDirection.SOUTHWEST;
		}else if(slope >= 3.0 || slope <= -3.0){
			return deltaY > 0 ? CardinalDirection.NORTH : CardinalDirection.SOUTH;
		}else{
			return deltaY > 0 ? CardinalDirection.NORTHWEST : CardinalDirection.SOUTHEAST;
		}
	}

}
