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

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class ClaimMapFragment extends Fragment {
	
		MapView mapView;
		private boolean saved = false;


		public ClaimMapFragment() {
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			inflater.inflate(R.menu.claim_map, menu);

			super.onCreateOptionsMenu(menu, inflater);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			setHasOptionsMenu(true);

			mapView = new MapView(getActivity(), 256);
			mapView.setClickable(true);
			mapView.setTileSource(TileSourceFactory.MAPNIK);
			mapView.setBuiltInZoomControls(true);
			mapView.setMultiTouchControls(true);
			mapView.getController().setZoom(17);
			mapView.getController().setCenter(new GeoPoint(41.882506, 12.488317));
//			mapView.setUseDataConnection(false);
			
			List<GeoPoint> boundaryPoints = new ArrayList<GeoPoint>();
			boundaryPoints.add(new GeoPoint(41.882267, 12.486804));
			boundaryPoints.add(new GeoPoint(41.881380, 12.488102));
			boundaryPoints.add(new GeoPoint(41.882778, 12.489889));
			boundaryPoints.add(new GeoPoint(41.883657, 12.488564));
			
			
			PathOverlay boundary = new PathOverlay(Color.RED, getActivity());
			TappableItemizedOverlay.addPoints(boundary, boundaryPoints);

			mapView.getOverlays().add(boundary);
			
			List<OverlayItem> markers = TappableItemizedOverlay.getMarkers(boundaryPoints);

			TappableItemizedOverlay boundaryMarkers = new TappableItemizedOverlay(
					getActivity(), boundary, markers);
			mapView.getOverlays().add(boundaryMarkers);
			mapView.invalidate();
			
			return mapView;
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// handle item selection
			Toast toast;
			switch (item.getItemId()) {
			case R.id.action_save:
				saved = true;
				toast = Toast.makeText(mapView.getContext(), R.string.message_saved, Toast.LENGTH_SHORT);
				toast.show();
				return true;
			case R.id.action_submit:
				if(saved){
					toast = Toast.makeText(mapView.getContext(), R.string.message_submitted, Toast.LENGTH_SHORT);
					toast.show();
				}else{
					toast = Toast.makeText(mapView.getContext(), R.string.message_save_before_submit, Toast.LENGTH_SHORT);
					toast.show();
				}
				return true;

			default:
				return super.onOptionsItemSelected(item);
			}
		}

	}