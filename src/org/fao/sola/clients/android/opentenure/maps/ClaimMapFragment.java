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

import org.fao.sola.clients.android.opentenure.ClaimDispatcher;
import org.fao.sola.clients.android.opentenure.MapLabel;
import org.fao.sola.clients.android.opentenure.OpenTenurePreferencesActivity;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.model.Vertex;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

public class ClaimMapFragment extends Fragment {

	private static final int MAP_LABEL_FONT_SIZE = 16;
	private static final String OSM_MAPNIK_BASE_URL = "http://a.tile.openstreetmap.org/{z}/{x}/{y}.png";
	private static final String OSM_MAPQUEST_BASE_URL = "http://otile1.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png";
	private static final float CUSTOM_TILE_PROVIDER_Z_INDEX = 1.0f;

	private View mapView;
	private MapLabel label;
	private GoogleMap map;
	private PropertyBoundary propertyBoundary;
	private boolean saved = false;
	private LocationHelper lh;
	private TileOverlay tiles = null;
	private ClaimDispatcher claimActivity;
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
        	claimActivity = (ClaimDispatcher) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ClaimDispatcher");
        }
    }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.claim_map, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onResume() {
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
	public void onStart() {
		map = ((SupportMapFragment) getActivity().getSupportFragmentManager()
				.findFragmentById(R.id.claim_map_fragment)).getMap();
		super.onStart();

	}

	@Override
	public void onDestroyView() {
		lh.stop();
		Fragment map = getFragmentManager().findFragmentById(
				R.id.claim_map_fragment);
		Fragment label = getFragmentManager().findFragmentById(
				R.id.claim_map_provider_label);
		try {
			if (map.isResumed()) {
				FragmentTransaction ft = getActivity()
						.getSupportFragmentManager().beginTransaction();
				ft.remove(map);
				ft.commit();
			}
			if (label.isResumed()) {
				FragmentTransaction ft = getActivity()
						.getSupportFragmentManager().beginTransaction();
				ft.remove(label);
				ft.commit();
			}
		} catch (Exception e) {
		}
		super.onDestroyView();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		super.onCreateView(inflater, container, savedInstanceState);
		mapView = inflater.inflate(R.layout.fragment_claim_map, container,
				false);
		setHasOptionsMenu(true);
		label = (MapLabel) getActivity().getSupportFragmentManager()
				.findFragmentById(R.id.claim_map_provider_label);
		label.changeTextProperties(MAP_LABEL_FONT_SIZE, getActivity()
				.getResources().getString(R.string.map_provider_google_normal));
		map = ((SupportMapFragment) getActivity().getSupportFragmentManager()
				.findFragmentById(R.id.claim_map_fragment)).getMap();

		propertyBoundary = new PropertyBoundary(mapView.getContext(), map, claimActivity);
		
		propertyBoundary.drawBoundary();

		lh = new LocationHelper((LocationManager) getActivity()
				.getBaseContext().getSystemService(Context.LOCATION_SERVICE));
		lh.start();

		MapsInitializer.initialize(this.getActivity());

		LatLng center = propertyBoundary.getCenter();
		if(center.equals(Vertex.INVALID_POSITION)){
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(lh.getCurrentLocation(), 17));
		}else{
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(propertyBoundary.getCenter(), 17));
		}
		
		map.animateCamera(CameraUpdateFactory.zoomTo(16), 1000, null);

		return mapView;

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		Toast toast;
		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;
		case R.id.map_provider_google_normal:
			if (tiles != null) {
				tiles.remove();
				tiles = null;
			}
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_google_normal));
			return true;
		case R.id.map_provider_google_satellite:
			if (tiles != null) {
				tiles.remove();
				tiles = null;
			}
			map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_google_satellite));
			return true;
		case R.id.map_provider_google_hybrid:
			if (tiles != null) {
				tiles.remove();
				tiles = null;
			}
			map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_google_hybrid));
			return true;
		case R.id.map_provider_google_terrain:
			if (tiles != null) {
				tiles.remove();
				tiles = null;
			}
			map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_google_terrain));
			return true;
		case R.id.map_provider_osm_mapnik:
			if (tiles != null) {
				tiles.remove();
				tiles = null;
			}
			OsmTileProvider mapNikTileProvider = new OsmTileProvider(256, 256,
					OSM_MAPNIK_BASE_URL);
			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			tiles = map.addTileOverlay(new TileOverlayOptions().tileProvider(
					mapNikTileProvider).zIndex(CUSTOM_TILE_PROVIDER_Z_INDEX));
			propertyBoundary.drawBoundary();
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_osm_mapnik));
			return true;
		case R.id.map_provider_osm_mapquest:
			if (tiles != null) {
				tiles.remove();
				tiles = null;
			}
			OsmTileProvider mapQuestTileProvider = new OsmTileProvider(256,
					256, OSM_MAPQUEST_BASE_URL);
			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			tiles = map.addTileOverlay(new TileOverlayOptions().tileProvider(
					mapQuestTileProvider).zIndex(CUSTOM_TILE_PROVIDER_Z_INDEX));
			propertyBoundary.drawBoundary();
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_osm_mapquest));
			return true;
		case R.id.map_provider_local_tiles:
			if (tiles != null) {
				tiles.remove();
				tiles = null;
			}
			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			tiles = map.addTileOverlay(new TileOverlayOptions().tileProvider(
					new LocalMapTileProvider()).zIndex(
					CUSTOM_TILE_PROVIDER_Z_INDEX));
			propertyBoundary.drawBoundary();
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_local_tiles));
			return true;
		case R.id.map_provider_geoserver:
			if (tiles != null) {
				tiles.remove();
				tiles = null;
			}
			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			SharedPreferences OpenTenurePreferences = PreferenceManager
					.getDefaultSharedPreferences(mapView.getContext());
			String geoServerUrl = OpenTenurePreferences.getString(
					OpenTenurePreferencesActivity.GEOSERVER_URL_PREF, "xxxxxxx");
			String geoServerLayer = OpenTenurePreferences.getString(
					OpenTenurePreferencesActivity.GEOSERVER_LAYER_PREF, "xxxxxxx");
//			tiles = map.addTileOverlay(new TileOverlayOptions().tileProvider(
//			new GeoserverMapTileProvider(256, 256, "http://192.168.56.1:8085/geoserver/nz", "nz:orthophoto")));
			tiles = map.addTileOverlay(new TileOverlayOptions().tileProvider(
			new GeoserverMapTileProvider(256, 256, geoServerUrl, geoServerLayer)));
			propertyBoundary.drawBoundary();
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_geoserver));
			return true;
		case R.id.action_save:
			saved = true;
			toast = Toast.makeText(mapView.getContext(),
					R.string.message_saved, Toast.LENGTH_SHORT);
			toast.show();
			return true;
		case R.id.action_submit:
			if (saved) {
				toast = Toast.makeText(mapView.getContext(),
						R.string.message_submitted, Toast.LENGTH_SHORT);
				toast.show();
			} else {
				toast = Toast
						.makeText(mapView.getContext(),
								R.string.message_save_before_submit,
								Toast.LENGTH_SHORT);
				toast.show();
			}
			return true;
		case R.id.action_center:
			LatLng currentLocation = lh.getCurrentLocation();

			if (currentLocation != null && currentLocation.latitude != 0.0
					&& currentLocation.longitude != 0.0) {
				Toast.makeText(
						getActivity().getBaseContext(),
						"onOptionsItemSelected - "
								+ currentLocation, Toast.LENGTH_SHORT)
						.show();

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));

				map.animateCamera(CameraUpdateFactory.zoomTo(16), 1000, null);

			} else {
				Toast.makeText(getActivity().getBaseContext(),
						R.string.check_location_service, Toast.LENGTH_LONG)
						.show();
			}
			return true;
		case R.id.action_new:
			LatLng newLocation = lh.getCurrentLocation();

			if (newLocation != null && newLocation.latitude != 0.0
					&& newLocation.longitude != 0.0) {
				Toast.makeText(
						getActivity().getBaseContext(),
						"onOptionsItemSelected - "
								+ newLocation, Toast.LENGTH_SHORT)
						.show();

				propertyBoundary.insertVertexFromGPS(newLocation);
				propertyBoundary.drawBoundary();

			} else {
				Toast.makeText(getActivity().getBaseContext(),
						R.string.check_location_service, Toast.LENGTH_LONG)
						.show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}