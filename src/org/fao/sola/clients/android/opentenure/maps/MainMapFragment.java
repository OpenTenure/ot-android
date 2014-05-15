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

import org.fao.sola.clients.android.opentenure.MapLabel;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.OpenTenurePreferencesActivity;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.network.GetAllClaimsTask;

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
import android.webkit.WebView.FindListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

public class MainMapFragment extends SupportMapFragment implements OnCameraChangeListener {

	public static final String MAIN_MAP_ZOOM = "__MAIN_MAP_ZOOM__";
	public static final String MAIN_MAP_LATITUDE = "__MAIN_MAP_LATITUDE__";
	public static final String MAIN_MAP_LONGITUDE = "__MAIN_MAP_LONGITUDE__";
	public static final String MAIN_MAP_TYPE = "__MAIN_MAP_PROVIDER__";
	private static final int MAP_LABEL_FONT_SIZE = 16;
	private static final String OSM_MAPNIK_BASE_URL = "http://a.tile.openstreetmap.org/{z}/{x}/{y}.png";
	private static final String OSM_MAPQUEST_BASE_URL = "http://otile1.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png";

	private View mapView;
	private MapLabel label;
	private GoogleMap map;
	private LocationHelper lh;
	private TileOverlay tiles = null;
	private List<BasePropertyBoundary> existingProperties;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mapView = inflater.inflate(R.layout.main_map, container, false);		
		setHasOptionsMenu(true);
		label = (MapLabel) getActivity().getSupportFragmentManager()
				.findFragmentById(R.id.main_map_provider_label);
		label.changeTextProperties(MAP_LABEL_FONT_SIZE, getActivity()
				.getResources().getString(R.string.map_provider_google_normal));
		map = ((SupportMapFragment) getActivity().getSupportFragmentManager()
				.findFragmentById(R.id.main_map_fragment)).getMap();
		MapsInitializer.initialize(this.getActivity());
		lh = new LocationHelper((LocationManager) getActivity()
				.getBaseContext().getSystemService(Context.LOCATION_SERVICE));
		lh.start();
		map.setOnCameraChangeListener(this);
		List<Claim> claims = Claim.getAllClaims();
		existingProperties = new ArrayList<BasePropertyBoundary>();
		for(Claim claim : claims){
				existingProperties.add(new BasePropertyBoundary(mapView.getContext(), map,
						claim));
		}

		drawProperties();

		return mapView;
	}

	private void drawProperties(){
		for(BasePropertyBoundary existingProperty : existingProperties){
			existingProperty.drawBoundary();
		}
	}

	@Override
	public void onStart() {
		map = ((SupportMapFragment) getActivity().getSupportFragmentManager()
				.findFragmentById(R.id.main_map_fragment)).getMap();
		super.onStart();

	}

	@Override
	public void onDestroyView() {
		lh.stop();
		Fragment map = getFragmentManager().findFragmentById(
				R.id.main_map_fragment);
		Fragment label = getFragmentManager().findFragmentById(
				R.id.main_map_provider_label);
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.map, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	public void setMapType(int type){

		if (tiles != null) {
			tiles.remove();
			tiles = null;
		}

		switch (type) {
		case R.id.map_provider_google_normal:

			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_google_normal));
			break;
		case R.id.map_provider_google_satellite:

			map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_google_satellite));
			break;
		case R.id.map_provider_google_hybrid:

			map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_google_hybrid));
			break;
		case R.id.map_provider_google_terrain:

			map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_google_terrain));
			break;
		case R.id.map_provider_osm_mapnik:

			OsmTileProvider mapNikTileProvider = new OsmTileProvider(256, 256,
					OSM_MAPNIK_BASE_URL);
			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			tiles = map.addTileOverlay(new TileOverlayOptions().tileProvider(
					mapNikTileProvider));
			drawProperties();
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_osm_mapnik));
			break;
		case R.id.map_provider_osm_mapquest:

			OsmTileProvider mapQuestTileProvider = new OsmTileProvider(256,
					256, OSM_MAPQUEST_BASE_URL);
			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			tiles = map.addTileOverlay(new TileOverlayOptions().tileProvider(
					mapQuestTileProvider));
			drawProperties();
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_osm_mapquest));
			break;
		case R.id.map_provider_local_tiles:

			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			tiles = map.addTileOverlay(new TileOverlayOptions().tileProvider(
					new LocalMapTileProvider()));
			drawProperties();
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_local_tiles));
			break;
		case R.id.map_provider_geoserver:
			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			SharedPreferences OpenTenurePreferences = PreferenceManager
					.getDefaultSharedPreferences(mapView.getContext());
			String geoServerUrl = OpenTenurePreferences.getString(
					OpenTenurePreferencesActivity.GEOSERVER_URL_PREF, "http://192.168.56.1:8085/geoserver/nz");
			String geoServerLayer = OpenTenurePreferences.getString(
					OpenTenurePreferencesActivity.GEOSERVER_LAYER_PREF, "nz:orthophoto");
			tiles = map.addTileOverlay(new TileOverlayOptions().tileProvider(
			new GeoserverMapTileProvider(256, 256, geoServerUrl, geoServerLayer)));
			drawProperties();
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_geoserver));
			break;
		default:
			break;
		}

		Configuration provider = Configuration.getConfigurationByName(MAIN_MAP_TYPE);

		if(provider != null){
			provider.setValue(""+type);
			provider.update();
		}else{
			provider = new Configuration();
			provider.setName(MAIN_MAP_TYPE);
			provider.setValue(""+type);
			provider.create();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;
		case R.id.map_provider_google_normal:
		case R.id.map_provider_google_satellite:
		case R.id.map_provider_google_hybrid:
		case R.id.map_provider_google_terrain:
		case R.id.map_provider_osm_mapnik:
		case R.id.map_provider_osm_mapquest:
		case R.id.map_provider_local_tiles:
		case R.id.map_provider_geoserver:
			setMapType(item.getItemId());
			return true;
		case R.id.action_center:
			LatLng location = lh.getCurrentLocation();

			if (location != null && location.latitude != 0.0
					&& location.longitude != 0.0) {
				Toast.makeText(
						getActivity().getBaseContext(),
						"onOptionsItemSelected - "
								+ location, Toast.LENGTH_SHORT)
						.show();

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));

				map.animateCamera(CameraUpdateFactory.zoomTo(16), 1000, null);

			} else {
				Toast.makeText(getActivity().getBaseContext(),
						R.string.check_location_service, Toast.LENGTH_LONG)
						.show();
			}
			return true;
			
		case R.id.action_download_claims:		
			
			
			OpenTenureApplication.setMapFragment(this);	
			
			LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
			
			GetAllClaimsTask task = new GetAllClaimsTask();
			task.execute(bounds);				
			
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCameraChange(CameraPosition cameraPosition) {

		Configuration zoom = Configuration.getConfigurationByName(MAIN_MAP_ZOOM);

		if(zoom != null){
			zoom.setValue(""+cameraPosition.zoom);
			zoom.update();
		}else{
			zoom = new Configuration();
			zoom.setName(MAIN_MAP_ZOOM);
			zoom.setValue(""+cameraPosition.zoom);
			zoom.create();
		}
		
		Configuration latitude = Configuration.getConfigurationByName(MAIN_MAP_LATITUDE);

		if(latitude != null){
			latitude.setValue(""+cameraPosition.target.latitude);
			latitude.update();
		}else{
			latitude = new Configuration();
			latitude.setName(MAIN_MAP_LATITUDE);
			latitude.setValue(""+cameraPosition.target.latitude);
			latitude.create();
		}
		
		Configuration longitude = Configuration.getConfigurationByName(MAIN_MAP_LONGITUDE);

		if(longitude != null){
			longitude.setValue(""+cameraPosition.target.longitude);
			longitude.update();
		}else{
			longitude = new Configuration();
			longitude.setName(MAIN_MAP_LONGITUDE);
			longitude.setValue(""+cameraPosition.target.longitude);
			longitude.create();
		}
	}
	
	
	public void refreshMap(){		
		
		List<Claim> claims = Claim.getAllClaims();
		existingProperties = new ArrayList<BasePropertyBoundary>();
		for(Claim claim : claims){
				existingProperties.add(new BasePropertyBoundary(mapView.getContext(), map,
						claim));
		}

		drawProperties();

		return ;
	}
	
	
	
}
