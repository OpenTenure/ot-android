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

import org.fao.sola.clients.android.opentenure.ClaimDispatcher;
import org.fao.sola.clients.android.opentenure.MapLabel;
import org.fao.sola.clients.android.opentenure.ModeDispatcher;
import org.fao.sola.clients.android.opentenure.OpenTenurePreferencesActivity;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.Configuration;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.vividsolutions.jts.algorithm.distance.DistanceToPoint;
import com.vividsolutions.jts.algorithm.distance.PointPairDistance;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class ClaimMapFragment extends Fragment implements
		OnCameraChangeListener {

	private static final int MAP_LABEL_FONT_SIZE = 16;
	private static final String OSM_MAPNIK_BASE_URL = "http://a.tile.openstreetmap.org/{z}/{x}/{y}.png";
	private static final String OSM_MAPQUEST_BASE_URL = "http://otile1.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png";
	private static final float CUSTOM_TILE_PROVIDER_Z_INDEX = 1.0f;

	private View mapView;
	private MapLabel label;
	private GoogleMap map;
	private EditablePropertyBoundary currentProperty;
	private List<BasePropertyBoundary> existingProperties;
	private MultiPolygon existingPropertiesMultiPolygon;
	private boolean saved = false;
	private LocationHelper lh;
	private TileOverlay tiles = null;
	private ClaimDispatcher claimActivity;
	private ModeDispatcher modeActivity;
	private int mapType = R.id.map_provider_google_normal;
	private final static String MAP_TYPE = "__MAP_TYPE__";
	private double snapLat;
	private double snapLon;

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
		try {
			modeActivity = (ModeDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ModeDispatcher");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(MAP_TYPE, mapType);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.claim_map, menu);
		if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
			menu.removeItem(R.id.action_new);
			menu.removeItem(R.id.action_new_picture);
		}
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
		super.onStart();
		lh.start();
	}

	@Override
	public void onDestroyView() {
		Log.d(this.getClass().getName(), "onDestroyView");
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

		Log.d(this.getClass().getName(), "onCreateView");
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

		List<Claim> claims = Claim.getAllClaims();
		existingProperties = new ArrayList<BasePropertyBoundary>();
		for (Claim claim : claims) {
			if (!claim.getClaimId()
					.equalsIgnoreCase(claimActivity.getClaimId())) {
				BasePropertyBoundary bpb = new BasePropertyBoundary(
						mapView.getContext(), map, claim);
				existingProperties.add(bpb);
			}
		}

		ArrayList<Polygon> polygonList = new ArrayList<Polygon>();

		for (BasePropertyBoundary bpb : existingProperties) {

			if (bpb.getVertices() != null && bpb.getVertices().size() > 0) {
				polygonList.add(bpb.getPolygon());
			}
		}
		Polygon[] polygons = new Polygon[polygonList.size()];
		polygonList.toArray(polygons);

		GeometryFactory gf = new GeometryFactory();
		existingPropertiesMultiPolygon = gf.createMultiPolygon(polygons);
		existingPropertiesMultiPolygon.setSRID(3857);

		if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
			currentProperty = new EditablePropertyBoundary(mapView.getContext(),
					map, Claim.getClaim(claimActivity.getClaimId()), claimActivity, false);
		}else{
			currentProperty = new EditablePropertyBoundary(mapView.getContext(),
					map, Claim.getClaim(claimActivity.getClaimId()), claimActivity, true);
		}

		drawProperties();

		lh = new LocationHelper((LocationManager) getActivity()
				.getBaseContext().getSystemService(Context.LOCATION_SERVICE));
		lh.start();

		MapsInitializer.initialize(this.getActivity());

		if (savedInstanceState != null
				&& savedInstanceState.getInt(MAP_TYPE) != 0) {
			// probably an orientation change don't move the view but
			// restore the current type of the map
			setMapType(savedInstanceState.getInt(MAP_TYPE));
		} else {
			// restore the latest map type used on the main map
			String mapType = Configuration
					.getConfigurationValue(MainMapFragment.MAIN_MAP_TYPE);

			try {
				setMapType(Integer.parseInt(mapType));
			} catch (Exception e) {
			}
		}

		if (currentProperty.getCenter() != null) {
			// A property exists for the claim
			// so we center on it
			map.moveCamera(CameraUpdateFactory.newLatLngBounds(
					currentProperty.getBounds(), 800, 800, 50));
		} else {
			// No property exists for this claim
			// so we center where we left the main map
			String zoom = Configuration
					.getConfigurationValue(MainMapFragment.MAIN_MAP_ZOOM);
			String latitude = Configuration
					.getConfigurationValue(MainMapFragment.MAIN_MAP_LATITUDE);
			String longitude = Configuration
					.getConfigurationValue(MainMapFragment.MAIN_MAP_LONGITUDE);

			if (zoom != null && latitude != null && longitude != null) {
				try {
					map.moveCamera(CameraUpdateFactory.newLatLngZoom(
							new LatLng(Double.parseDouble(latitude), Double
									.parseDouble(longitude)), Float
									.parseFloat(zoom)));
				} catch (Exception e) {
				}
			}

		}
		
		if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) != 0) {
			
			this.map.setOnCameraChangeListener(this);
			// Allow adding, removing and dragging markers

			this.map.setOnMapLongClickListener(new OnMapLongClickListener() {

				@Override
				public void onMapLongClick(final LatLng position) {

					if (claimActivity.getClaimId() == null) {
						// Useless to add markers without a claim
						Toast toast = Toast.makeText(
								mapView.getContext(),
								R.string.message_save_claim_before_adding_content,
								Toast.LENGTH_SHORT);
						toast.show();
						return;
					}

					AlertDialog.Builder dialog = new AlertDialog.Builder(
							mapView.getContext());
					dialog.setTitle(R.string.message_add_marker);
					dialog.setMessage("Lon: " + position.longitude + ", lat: "
							+ position.latitude);

					dialog.setPositiveButton(R.string.confirm,
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									currentProperty.insertVertex(position);
									currentProperty
											.resetAdjacency(existingProperties);
								}
							});
					dialog.setNegativeButton(R.string.cancel,
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							});

					dialog.show();

				}
			});
			this.map.setOnMarkerDragListener(new OnMarkerDragListener() {
				@Override
				public void onMarkerDrag(Marker mark) {
					PointPairDistance ppd = new PointPairDistance();
					DistanceToPoint.computeDistance(
							existingPropertiesMultiPolygon,
							new Coordinate(mark.getPosition().longitude, mark
									.getPosition().latitude), ppd);

					if (ppd.getDistance() < BasePropertyBoundary.SNAP_THRESHOLD) {
						snapLat = ppd.getCoordinate(0).y;
						snapLon = ppd.getCoordinate(0).x;
						mark.setPosition(new LatLng(snapLat, snapLon));
					} else {
						snapLat = 0.0;
						snapLon = 0.0;
					}
					currentProperty.moveMarker(mark);
					currentProperty.drawBoundary();

				}

				@Override
				public void onMarkerDragEnd(Marker mark) {

					if (snapLat != 0.0 && snapLon != 0.0) {
						mark.setPosition(new LatLng(snapLat, snapLon));
					}
					currentProperty.moveMarker(mark);
					currentProperty.updateVertices();
					currentProperty.resetAdjacency(existingProperties);
					currentProperty.drawBoundary();
				}

				@Override
				public void onMarkerDragStart(Marker mark) {
					currentProperty.moveMarker(mark);
					currentProperty.drawBoundary();
				}

			});

			this.map.setOnMarkerClickListener(new OnMarkerClickListener() {

				@Override
				public boolean onMarkerClick(final Marker mark) {
					return currentProperty.handleMarkerClick(mark, existingProperties);
				}
			});
		}
		return mapView;

	}
	
	public void setMapType(int type) {

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
					mapNikTileProvider).zIndex(CUSTOM_TILE_PROVIDER_Z_INDEX));
			drawProperties();
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_osm_mapnik));
			break;
		case R.id.map_provider_osm_mapquest:

			OsmTileProvider mapQuestTileProvider = new OsmTileProvider(256,
					256, OSM_MAPQUEST_BASE_URL);
			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			tiles = map.addTileOverlay(new TileOverlayOptions().tileProvider(
					mapQuestTileProvider).zIndex(CUSTOM_TILE_PROVIDER_Z_INDEX));
			drawProperties();
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_osm_mapquest));
			break;
		case R.id.map_provider_local_tiles:

			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			tiles = map.addTileOverlay(new TileOverlayOptions().tileProvider(
					new LocalMapTileProvider()).zIndex(
					CUSTOM_TILE_PROVIDER_Z_INDEX));
			drawProperties();
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_local_tiles));
			break;
		case R.id.map_provider_geoserver:
			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			SharedPreferences OpenTenurePreferences = PreferenceManager
					.getDefaultSharedPreferences(mapView.getContext());
			String geoServerUrl = OpenTenurePreferences.getString(
					OpenTenurePreferencesActivity.GEOSERVER_URL_PREF,
					"http://192.168.56.1:8085/geoserver/nz");
			String geoServerLayer = OpenTenurePreferences.getString(
					OpenTenurePreferencesActivity.GEOSERVER_LAYER_PREF,
					"nz:orthophoto");
			tiles = map.addTileOverlay(new TileOverlayOptions()
					.tileProvider(new GeoserverMapTileProvider(256, 256,
							geoServerUrl, geoServerLayer)));
			drawProperties();
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_geoserver));
			break;
		default:
			break;
		}
		mapType = type;
	}

	private void drawProperties() {
		currentProperty.drawBoundary();
		for (BasePropertyBoundary existingProperty : existingProperties) {
			existingProperty.drawBoundary();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		Toast toast;
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
		case R.id.action_save:
			saved = true;
			toast = Toast.makeText(mapView.getContext(),
					R.string.message_saved, Toast.LENGTH_SHORT);
			toast.show();
			return true;
		case R.id.action_new_picture:
			currentProperty.saveSnapshot();
			return true;
		case R.id.action_submit:
			if (saved) {
				toast = Toast.makeText(mapView.getContext(),
						R.string.message_submitted, Toast.LENGTH_SHORT);
				toast.show();
			} else {
				toast = Toast.makeText(mapView.getContext(),
						R.string.message_save_claim_before_submit,
						Toast.LENGTH_SHORT);
				toast.show();
			}
			return true;
		case R.id.action_center:
			LatLng currentLocation = lh.getCurrentLocation();

			if (currentLocation != null && currentLocation.latitude != 0.0
					&& currentLocation.longitude != 0.0) {
				Toast.makeText(getActivity().getBaseContext(),
						"onOptionsItemSelected - " + currentLocation,
						Toast.LENGTH_SHORT).show();

				map.moveCamera(CameraUpdateFactory.newLatLngZoom(
						currentLocation, 12));

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
				Toast.makeText(getActivity().getBaseContext(),
						"onOptionsItemSelected - " + newLocation,
						Toast.LENGTH_SHORT).show();

				currentProperty.insertVertex(newLocation);

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

	@Override
	public void onCameraChange(CameraPosition cameraPosition) {
		currentProperty.refreshMarkerEditControls();
	}
}