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

import java.math.BigDecimal;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationHelper {

	private static final long LOCATION_LISTENER_INTERVAL_FAST = 5*1000;
	private static final long LOCATION_LISTENER_INTERVAL_SLOW = 5*60*1000;

	LocationManager locationManager;

	LocationListener gpsLL = new LocationListener() {
		
		public void onLocationChanged(Location location) {
			OpenTenureApplication.getInstance().getDatabase().updateCurrentLocation(new BigDecimal(location.getLongitude()), new BigDecimal(location.getLatitude()));
		}

		public void onStatusChanged(String provider, int status,
				Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}
	};
	
	LocationHelper (LocationManager locationManager){
		this.locationManager=locationManager;
	}

	LocationListener networkLL = new LocationListener() {
		
		public void onLocationChanged(Location location) {
			OpenTenureApplication.getInstance().getDatabase().updateCurrentLocation(new BigDecimal(location.getLongitude()), new BigDecimal(location.getLatitude()));
		}

		public void onStatusChanged(String provider, int status,
				Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}
	};
	
	public void start(){
		locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, LOCATION_LISTENER_INTERVAL_FAST, 0, gpsLL);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, LOCATION_LISTENER_INTERVAL_FAST, 0, networkLL);
	}
	public void stop(){
		locationManager.removeUpdates(gpsLL);
		locationManager.removeUpdates(networkLL);
	}
	public void hurryUp(){
		locationManager.removeUpdates(gpsLL);
		locationManager.removeUpdates(networkLL);
		locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, LOCATION_LISTENER_INTERVAL_FAST, 0, gpsLL);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, LOCATION_LISTENER_INTERVAL_FAST, 0, networkLL);
	}
	public void slowDown(){
		locationManager.removeUpdates(gpsLL);
		locationManager.removeUpdates(networkLL);
		locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, LOCATION_LISTENER_INTERVAL_SLOW, 0, gpsLL);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, LOCATION_LISTENER_INTERVAL_SLOW, 0, networkLL);
	}
	public Location getCurrentLocation(){
		return OpenTenureApplication.getInstance().getDatabase().getCurrentLocation();
	}

}
