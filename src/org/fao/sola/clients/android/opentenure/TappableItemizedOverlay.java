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
import java.util.Iterator;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class TappableItemizedOverlay extends ItemizedIconOverlay<OverlayItem> {
	protected Context context;
	protected PathOverlay boundary;
	protected List<OverlayItem> markers;

	public TappableItemizedOverlay(final Context context,
			final PathOverlay boundary, final List<OverlayItem> markers) {
		super(context, markers, new OnItemGestureListener<OverlayItem>() {
			@Override
			public boolean onItemSingleTapUp(final int index,
					final OverlayItem item) {
				return false;
			}

			@Override
			public boolean onItemLongPress(final int index,
					final OverlayItem item) {
				return false;
			}
		});

		this.context = context;
		this.boundary = boundary;
		this.markers = markers;
	}

	public static void addPoints(PathOverlay boundary, List<GeoPoint> points) {

		if (points == null || points.size() == 0) {
			return;
		}

		for (Iterator<GeoPoint> iterator = points.iterator(); iterator
				.hasNext();) {
			GeoPoint geoPoint = (GeoPoint) iterator.next();
			boundary.addPoint(geoPoint);

		}
		// Close boundary by adding again the first point
		boundary.addPoint(points.get(0));

	}

	public static List<OverlayItem> getMarkers(List<GeoPoint> points) {

		if (points == null || points.size() == 0) {
			return null;
		}
		List<OverlayItem> markers = new ArrayList<OverlayItem>();
		for (int i = 0; i < points.size(); i++) {
			GeoPoint geoPoint = (GeoPoint) points.get(i);
			markers.add(new OverlayItem("ID:" + i, "TITLE:" + i, geoPoint));
		}
		return markers;
	}

	public TappableItemizedOverlay getReference() {
		return this;
	}

	@Override
	public boolean onLongPress(final android.view.MotionEvent event,
			final MapView mapView) {

		Projection proj = mapView.getProjection();
		final GeoPoint pix = (GeoPoint) proj.fromPixels((int) event.getX(),
				(int) event.getY());

		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle("Long press");
		dialog.setMessage("At lon: " + pix.getLongitude() + ", lat: "
				+ pix.getLatitude());

		dialog.setPositiveButton("Add a marker here?", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				mapView.getOverlays().clear();
				mapView.invalidate();
				OverlayItem item = new OverlayItem("ID:" + markers.size(),
						"TITLE:" + markers.size(), pix);
				addItem(item);

				boundary.clearPath();

				for (int i = 0 ; i < size();i++) {
					boundary.addPoint(getItem(i).getPoint());

				}
				boundary.addPoint(getItem(0).getPoint());

				mapView.getOverlays().add(boundary);
				mapView.getOverlays().add(getReference());
				mapView.invalidate();
			}
		});
		dialog.show();
		return true;
	};

	@Override
	protected boolean onSingleTapUpHelper(final int index,
			final OverlayItem item, final MapView mapView) {

		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle("Single tap on: " + item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.setNegativeButton("Remove?", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				mapView.getOverlays().clear();
				mapView.invalidate();
				removeItem(item);
				boundary.clearPath();

				for (int i = 0 ; i < size() ; i++) {

					boundary.addPoint(getItem(i).getPoint());

				}
				if (size() > 0) {
					// Close boundary by adding again the first point
					boundary.addPoint(getItem(0).getPoint());
				}
				mapView.getOverlays().add(boundary);
				mapView.getOverlays().add(getReference());
				mapView.invalidate();
			}
		});
		dialog.show();
		return true;
	}
}