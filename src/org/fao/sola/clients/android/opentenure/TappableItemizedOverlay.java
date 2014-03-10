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
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class TappableItemizedOverlay extends ItemizedIconOverlay<OverlayItem> {
	private static final int MAX_TOUCH_OFFSET_TOLERANCE = 40;
	protected Context context;
	protected PathOverlay boundary;
	protected List<OverlayItem> markers;
	private OverlayItem inDrag = null;
	private ImageView dragImage = null;
	private int dragImageWidth = 0;
	private int dragImageHeight = 0;
	private int xTouchOffset = 0;
	private int yTouchOffset = 0;

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
	
	public void setDragImage(ImageView dragImage){
		this.dragImage = dragImage;
		dragImageWidth = dragImage.getDrawable().getIntrinsicWidth();
		dragImageHeight = dragImage.getDrawable().getIntrinsicHeight();
		Log.d(this.getClass().getName(), "Drag image size: " +  dragImageWidth + ", " + dragImageHeight);
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
		// Closing boundary by adding again the first point
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
		dialog.setTitle(R.string.message_add_marker);
		dialog.setMessage("Lon: " + pix.getLongitude() + ", lat: "
				+ pix.getLatitude());

		dialog.setPositiveButton(R.string.confirm, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				mapView.getOverlays().clear();
				mapView.invalidate();
				OverlayItem item = new OverlayItem("ID:" + markers.size(),
						", TITLE:" + markers.size(), pix);
				addItem(item);

				boundary.clearPath();

				for (int i = 0; i < size(); i++) {
					boundary.addPoint(getItem(i).getPoint());

				}
				boundary.addPoint(getItem(0).getPoint());

				mapView.getOverlays().add(boundary);
				mapView.getOverlays().add(getReference());
				mapView.invalidate();
			}
		});
		dialog.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		dialog.show();
		return true;
	};

	@Override
	protected boolean onSingleTapUpHelper(final int index,
			final OverlayItem item, final MapView mapView) {

		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(R.string.message_remove_marker);
		dialog.setMessage(item.getTitle() + ", " + item.getSnippet() + " at " + item.getPoint().getLongitude() + ", " + item.getPoint().getLatitude());
		dialog.setPositiveButton(R.string.confirm, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				mapView.getOverlays().clear();
				mapView.invalidate();
				removeItem(item);
				boundary.clearPath();

				for (int i = 0; i < size(); i++) {

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
		dialog.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		dialog.show();
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		final int action = event.getAction();
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		boolean result = false;

		if (action == MotionEvent.ACTION_DOWN) {

			for (OverlayItem item : markers) {

				Log.d(this.getClass().getName(), "Touched ...");

				Point t = mapView.getProjection().fromMapPixels(x, y, null);
				Point p = mapView.getProjection().toMapPixels(item.getPoint(), null);

				if ( Math.abs(t.x - p.x) < MAX_TOUCH_OFFSET_TOLERANCE
						&& Math.abs( t.y - p.y) < MAX_TOUCH_OFFSET_TOLERANCE) {

					Log.d(this.getClass().getName(), "Picking up item: " + item.getTitle() + ", " + item.getSnippet() + " at " + p.x + ", " + p.y + " by touching at " + t.x + ", " + t.y);
					result = true;
					inDrag = item;

					mapView.getOverlays().clear();
					mapView.invalidate();
					removeItem(item);
					boundary.clearPath();
					mapView.getOverlays().add(getReference());
					mapView.invalidate();

					xTouchOffset = t.x - p.x;
					yTouchOffset = t.y - p.y;

					dragImage.setVisibility(View.VISIBLE);
					setDragImagePosition(t.x - xTouchOffset, t.y - yTouchOffset);

					Log.d(this.getClass().getName(), "Touch offset is " + xTouchOffset + ", "+ yTouchOffset);
					break;
				}
			}
		} else if (action == MotionEvent.ACTION_MOVE && inDrag != null) {
			Log.d(this.getClass().getName(), "... Moving to " + x + ", " + y);
			setDragImagePosition(x - xTouchOffset, y - yTouchOffset);
			result = true;
		} else if (action == MotionEvent.ACTION_UP && inDrag != null) {
			dragImage.setVisibility(View.GONE);

			Log.d(this.getClass().getName(), "Released at " + x + ", "+ y);
			GeoPoint pt = (GeoPoint) mapView.getProjection().fromPixels(x - xTouchOffset + dragImageWidth/2, y - yTouchOffset);
			OverlayItem toDrop = new OverlayItem(inDrag.getTitle(),
					inDrag.getSnippet(), pt);

			mapView.getOverlays().clear();
			mapView.invalidate();
			addItem(toDrop);

			for (int i = 0; i < size(); i++) {
				boundary.addPoint(getItem(i).getPoint());
			}
			boundary.addPoint(getItem(0).getPoint());

			mapView.getOverlays().add(boundary);
			mapView.getOverlays().add(getReference());
			mapView.invalidate();

			inDrag = null;
			result = true;
		}

		return (result || super.onTouchEvent(event, mapView));
	}

	private void setDragImagePosition(int x, int y) {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dragImage
				.getLayoutParams();

		lp.setMargins(x, y, 0, 0);
		dragImage.setLayoutParams(lp);
	}
}