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


import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MapFragment extends Fragment {
		MapView mapView;
		LocationHelper lh;

		public MapFragment() {
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			inflater.inflate(R.menu.map, menu);

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
			mapView.getController().setCenter(new GeoPoint(41.825508, 12.603604));
//			mapView.setUseDataConnection(false);
			lh = new LocationHelper((LocationManager)mapView.getContext().getSystemService(Context.LOCATION_SERVICE));
			lh.start();
			return mapView;
		}

		@Override
		public void onResume(){
			super.onResume();
			lh.hurryUp();
		}

		@Override
		public void onPause() {
			super.onPause();
			lh.slowDown();
		}

		@Override
		public void onStop() {
			super.onStop();
			lh.stop();
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {

			switch (item.getItemId()) {
			case R.id.action_center:
				Location location = OpenTenureApplication.getInstance().getDatabase().getCurrentLocation();
				if(location != null && location.getLatitude() != 0.0 && location.getLongitude() != 0){
					Toast.makeText(mapView.getContext(),
							"onOptionsItemSelected - lon: " + location.getLongitude() + ", lat: " + location.getLatitude(), Toast.LENGTH_SHORT)
							.show();

					mapView.getController().animateTo(new GeoPoint(location.getLatitude(), location.getLongitude()));
				}else{
					Toast.makeText(mapView.getContext(),
							R.string.check_location_service, Toast.LENGTH_LONG)
							.show();
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}
	}