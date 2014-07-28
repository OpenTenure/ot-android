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
import org.fao.sola.clients.android.opentenure.ClaimListener;
import org.fao.sola.clients.android.opentenure.MapLabel;
import org.fao.sola.clients.android.opentenure.ModeDispatcher;
import org.fao.sola.clients.android.opentenure.OpenTenurePreferencesActivity;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.Configuration;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
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

import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.GoogleMap.CancelableCallback;
import com.androidmapsextensions.GoogleMap.OnCameraChangeListener;
import com.androidmapsextensions.GoogleMap.OnMapLongClickListener;
import com.androidmapsextensions.GoogleMap.OnMarkerClickListener;
import com.androidmapsextensions.GoogleMap.OnMarkerDragListener;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.SupportMapFragment;
import com.androidmapsextensions.TileOverlay;
import com.androidmapsextensions.TileOverlayOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.vividsolutions.jts.algorithm.distance.DistanceToPoint;
import com.vividsolutions.jts.algorithm.distance.PointPairDistance;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class ClaimMapFragment extends Fragment implements
		OnCameraChangeListener, SensorEventListener, ClaimListener {

	private static final int MAP_LABEL_FONT_SIZE = 16;
	private static final String OSM_MAPNIK_BASE_URL = "http://a.tile.openstreetmap.org/{z}/{x}/{y}.png";
	private static final String OSM_MAPQUEST_BASE_URL = "http://otile1.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png";
	private static final float CUSTOM_TILE_PROVIDER_Z_INDEX = 1.0f;
	private static int CLAIM_MAP_SIZE = 800;
	private static int CLAIM_MAP_PADDING = 50;

	private View mapView;
	private MapLabel label;
	private GoogleMap map;
	private EditablePropertyBoundary currentProperty;
	private List<BasePropertyBoundary> visibleProperties;
	private List<Claim> allClaims;
	private MultiPolygon visiblePropertiesMultiPolygon;
	private boolean saved = false;
	private LocationHelper lh;
	private TileOverlay tiles = null;
	private ClaimDispatcher claimActivity;
	private ModeDispatcher modeActivity;
	private int mapType = R.id.map_provider_google_normal;
	private final static String MAP_TYPE = "__MAP_TYPE__";
	private double snapLat;
	private double snapLon;
	private Menu menu;
	private boolean isRotating = false;
	private boolean isFollowing = false;
	private Marker myLocation;
	private CameraPosition oldCameraPosition;
	private CameraPosition newCameraPosition;
	private boolean adjacenciesReset = false;
	private LocationListener myLocationListener = new LocationListener() {

		public void onLocationChanged(Location location) {
			if (isFollowing && myLocation != null) {
				myLocation.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
			}
		}
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}
	};
    // device sensor manager
    private SensorManager mSensorManager;


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
		super.onSaveInstanceState(outState);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.claim_map, menu);
		menu.findItem(R.id.action_stop_rotating).setVisible(false);
		if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
			menu.removeItem(R.id.action_new);
		}
		this.menu = menu;
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onResume() {
		super.onResume();
		if(isRotating){
	        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
	                SensorManager.SENSOR_DELAY_UI);
		}
		lh.hurryUp();
	}

	@Override
	public void onPause() {
		super.onPause();
	     // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
   		lh.slowDown();
	}

	@Override
	public void onStart() {
		super.onStart();
		lh.start();
	}
	
	@Override
	public void onClaimSaved(){
		currentProperty.reload();
	}

	@Override
	public void onStop() {
		super.onStop();
		lh.stop();
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
	
	private void centerMapOnCurrentProperty(CancelableCallback callback){
		if (currentProperty.getCenter() != null) {
			// A property exists for the claim
			// so we center on it
			LatLngBounds llb = currentProperty.getBounds();
			oldCameraPosition = map.getCameraPosition();
			newCameraPosition = map.getCameraPosition();
			map.animateCamera(CameraUpdateFactory.newLatLngBounds(
					llb, CLAIM_MAP_SIZE, CLAIM_MAP_SIZE, CLAIM_MAP_PADDING), callback);
			if(oldCameraPosition.equals(newCameraPosition) && callback != null){
				// NOTE: THIS IS A DIRTY HACK
				// animateCamera will not invoke callback.onFinish if there is no need to move the camera
				// so, if the map was never moved, we need to force it
				callback.onFinish();
			}
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
				.findFragmentById(R.id.claim_map_fragment)).getExtendedMap();
	    ClusteringSettings settings = new ClusteringSettings();
	    settings.clusterOptionsProvider(new OpenTenureClusterOptionsProvider(getResources()));
	    settings.addMarkersDynamically(true);
	    map.setClustering(settings);

		lh = new LocationHelper((LocationManager) getActivity()
				.getBaseContext().getSystemService(Context.LOCATION_SERVICE));
		lh.start();

		MapsInitializer.initialize(this.getActivity());
		this.map.setOnCameraChangeListener(this);

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

		hideVisibleProperties();

		if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
			currentProperty = new EditablePropertyBoundary(mapView.getContext(),
					map, Claim.getClaim(claimActivity.getClaimId()), claimActivity, visibleProperties, false);
		}else{
			currentProperty = new EditablePropertyBoundary(mapView.getContext(),
					map, Claim.getClaim(claimActivity.getClaimId()), claimActivity, visibleProperties, true);
		}
		
		centerMapOnCurrentProperty(null);
		reloadVisibleProperties();
		showVisibleProperties();

		if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RW) == 0) {
			
			// Allow adding, removing and dragging markers

			this.map.setOnMapLongClickListener(new OnMapLongClickListener() {

				@Override
				public void onMapLongClick(final LatLng position) {
					
					currentProperty.addMarker(position);

				}
			});
			this.map.setOnMarkerDragListener(new OnMarkerDragListener() {
				@Override
				public void onMarkerDrag(Marker mark) {
					PointPairDistance ppd = new PointPairDistance();
					DistanceToPoint.computeDistance(
							visiblePropertiesMultiPolygon,
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
					currentProperty.onMarkerDrag(mark);

				}

				@Override
				public void onMarkerDragEnd(Marker mark) {

					if (snapLat != 0.0 && snapLon != 0.0) {
						mark.setPosition(new LatLng(snapLat, snapLon));
					}
					currentProperty.onMarkerDragEnd(mark);
				}

				@Override
				public void onMarkerDragStart(Marker mark) {
					currentProperty.onMarkerDragStart(mark);
				}

			});

			this.map.setOnMarkerClickListener(new OnMarkerClickListener() {

				@Override
				public boolean onMarkerClick(final Marker mark) {
					return currentProperty.handleMarkerClick(mark);
				}
			});
		}
	    mSensorManager = (SensorManager) mapView.getContext().getSystemService(Context.SENSOR_SERVICE);
		return mapView;

	}
	
	private Polygon getPolygon(LatLngBounds bounds){
		GeometryFactory gf = new GeometryFactory();
		Coordinate[] coords = new Coordinate[5];

		coords[0] = new Coordinate(bounds.northeast.longitude, bounds.northeast.latitude);
		coords[1] = new Coordinate(bounds.northeast.longitude, bounds.southwest.latitude);
		coords[2] = new Coordinate(bounds.southwest.longitude, bounds.southwest.latitude);
		coords[3] = new Coordinate(bounds.southwest.longitude, bounds.northeast.latitude);
		coords[4] = new Coordinate(bounds.northeast.longitude, bounds.northeast.latitude);

		Polygon polygon = gf.createPolygon(coords);
		polygon.setSRID(Constants.SRID);
		return polygon;
	}
	
	private void reloadVisibleProperties(){

		LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
		Polygon boundsPoly = getPolygon(bounds);

		if(allClaims == null){
			allClaims = Claim.getAllClaims();
		}
		visibleProperties = new ArrayList<BasePropertyBoundary>();
		for (Claim claim : allClaims) {
			if (!claim.getClaimId()
					.equalsIgnoreCase(claimActivity.getClaimId())) {
				BasePropertyBoundary bpb = new BasePropertyBoundary(
						mapView.getContext(), map, claim);
				Polygon claimPoly = bpb.getPolygon();
				if(claimPoly != null && claimPoly.intersects(boundsPoly)){
					visibleProperties.add(bpb);
				}
			}
		}
		currentProperty.setOtherProperties(visibleProperties);

		List<Polygon> visiblePropertiesPolygonList = new ArrayList<Polygon>();

		for (BasePropertyBoundary visibleProperty : visibleProperties) {

			if (visibleProperty.getVertices() != null && visibleProperty.getVertices().size() > 0) {
				visiblePropertiesPolygonList.add(visibleProperty.getPolygon());
			}
		}
		Polygon[] visiblePropertiesPolygons = new Polygon[visiblePropertiesPolygonList.size()];
		visiblePropertiesPolygonList.toArray(visiblePropertiesPolygons);

		GeometryFactory gf = new GeometryFactory();
		visiblePropertiesMultiPolygon = gf.createMultiPolygon(visiblePropertiesPolygons);
		visiblePropertiesMultiPolygon.setSRID(Constants.SRID);

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
			redrawProperties();
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_osm_mapnik));
			break;
		case R.id.map_provider_osm_mapquest:

			OsmTileProvider mapQuestTileProvider = new OsmTileProvider(256,
					256, OSM_MAPQUEST_BASE_URL);
			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			tiles = map.addTileOverlay(new TileOverlayOptions().tileProvider(
					mapQuestTileProvider).zIndex(CUSTOM_TILE_PROVIDER_Z_INDEX));
			redrawProperties();
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_osm_mapquest));
			break;
		case R.id.map_provider_local_tiles:

			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			tiles = map.addTileOverlay(new TileOverlayOptions().tileProvider(
					new LocalMapTileProvider()).zIndex(
					CUSTOM_TILE_PROVIDER_Z_INDEX));
			redrawProperties();
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
					.tileProvider(new WmsMapTileProvider(256, 256,
							geoServerUrl, geoServerLayer)));
			redrawProperties();
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources()
					.getString(R.string.map_provider_geoserver));
			break;
		default:
			break;
		}
		mapType = type;
	}

	private void redrawProperties() {
		currentProperty.redrawBoundary();
		redrawVisibleProperties();
	}

	private void showVisibleProperties() {
		if(visibleProperties != null){
			for (BasePropertyBoundary visibleProperty : visibleProperties) {
				visibleProperty.showBoundary();
			}
		}
	}

	private void redrawVisibleProperties() {
		hideVisibleProperties();
		showVisibleProperties();
	}

	private void hideVisibleProperties() {
		if(visibleProperties != null){
			for (BasePropertyBoundary visibleProperty : visibleProperties) {
				visibleProperty.hideBoundary();
			}
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
			Log.d(this.getClass().getName(), "newPicture");
			centerMapOnCurrentProperty(new CancelableCallback() {
				
				@Override
				public void onFinish() {
					Log.d(this.getClass().getName(), "onFinish");
					currentProperty.saveSnapshot();
				}
				
				@Override
				public void onCancel() {
					// TODO Auto-generated method stub
					
				}
			});
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
		case R.id.action_center_and_follow:
			if(isFollowing){
				isFollowing = false;
				myLocation.remove();
				myLocation = null;
				lh.setCustomListener(null);
			}else{
				LatLng currentLocation = lh.getCurrentLocation();

				if (currentLocation != null && currentLocation.latitude != 0.0
						&& currentLocation.longitude != 0.0) {
					map.animateCamera(CameraUpdateFactory.newLatLngZoom(
							currentLocation, 18), 1000, null);
					myLocation = map.addMarker(new MarkerOptions()
					.position(currentLocation)
					.anchor(0.5f, 0.5f)
					.title(mapView.getContext().getResources().getString(R.string.title_i_m_here))
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.ic_menu_mylocation)));
					myLocation.setClusterGroup(Constants.MY_LOCATION_MARKERS_GROUP);
					lh.setCustomListener(myLocationListener);
					isFollowing = true;

				} else {
					Toast.makeText(getActivity().getBaseContext(),
							R.string.check_location_service, Toast.LENGTH_LONG)
							.show();
				}
			}
			return true;
		case R.id.action_new:
			LatLng newLocation = lh.getCurrentLocation();

			if (newLocation != null && newLocation.latitude != 0.0
					&& newLocation.longitude != 0.0) {
				Toast.makeText(getActivity().getBaseContext(),
						"onOptionsItemSelected - " + newLocation,
						Toast.LENGTH_SHORT).show();

				currentProperty.addMarker(newLocation, newLocation);

			} else {
				Toast.makeText(getActivity().getBaseContext(),
						R.string.check_location_service, Toast.LENGTH_LONG)
						.show();
			}
			return true;
		case R.id.action_rotate:

			if(!isRotating && lh.getLatestLocation() != null) {
				menu.findItem(R.id.action_rotate).setVisible(false);
				menu.findItem(R.id.action_stop_rotating).setVisible(true);
		        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
		                SensorManager.SENSOR_DELAY_UI);
				isRotating = true;
			}else{
				Toast.makeText(getActivity().getBaseContext(),
						R.string.check_location_service, Toast.LENGTH_LONG)
						.show();
			}
			return true;
		case R.id.action_stop_rotating:
			menu.findItem(R.id.action_rotate).setVisible(true);
			menu.findItem(R.id.action_stop_rotating).setVisible(false);
			mSensorManager.unregisterListener(this);
			isRotating = false;
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCameraChange(CameraPosition cameraPosition) {
		hideVisibleProperties();
		reloadVisibleProperties();
		showVisibleProperties();
		currentProperty.redrawBoundary();
		currentProperty.refreshMarkerEditControls();
		newCameraPosition = cameraPosition;
		if(!adjacenciesReset){
			currentProperty.resetAdjacency(visibleProperties);
			adjacenciesReset = true;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// nothing to do as of now
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		Location latestLocation = lh.getLatestLocation();

		if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR && latestLocation != null) {
		    GeomagneticField field = new GeomagneticField(
		            (float)latestLocation.getLatitude(),
		            (float)latestLocation.getLongitude(),
		            (float)latestLocation.getAltitude(),
		            System.currentTimeMillis()
		        );

		    // getDeclination returns degrees
		    float mDeclination = field.getDeclination();
			float[] mRotationMatrix = new float[16];
	        SensorManager.getRotationMatrixFromVector(
	        		mRotationMatrix , event.values);
	        float[] orientation = new float[3];
	        SensorManager.getOrientation(mRotationMatrix, orientation);
	        float bearing = (float) (Math.toDegrees(orientation[0]) + mDeclination);
	        CameraPosition currentPosition = map.getCameraPosition();

	        CameraPosition newPosition = new CameraPosition.Builder(currentPosition).bearing(bearing).build();
	         
	        map.moveCamera(CameraUpdateFactory.newCameraPosition(newPosition));
	    }
		
	}
}