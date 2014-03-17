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
package org.fao.sola.clients.android.opentenure.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;

public class Vertex {
	
	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	Database db = OpenTenureApplication.getInstance().getDatabase();

	Vertex(){
	}

	Vertex(double mapLat, double mapLon){
		setMapLat(mapLat);
		setMapLon(mapLon);
	}

	Vertex(double GPSLat, double GPSLon, double mapLat, double mapLon){
		setGPSLat(GPSLat);
		setGPSLon(GPSLon);
		setMapLat(mapLat);
		setMapLon(mapLon);
	}

	@Override
	public String toString() {
		return "Vertex [sequenceNumber="
				+ sequenceNumber + ", GPSLat=" + String.format("%.8f", GPSLat) + ", GPSLon=" + String.format("%.8f", GPSLon)
				+ ", MapLat=" + String.format("%.8f", MapLat) + ", MapLon=" + String.format("%.8f", MapLon) + "]";
	}
	public double getGPSLat() {
		return GPSLat;
	}
	public void setGPSLat(double GPSLat) {
		this.GPSLat = GPSLat;
	}
	public double getGPSLon() {
		return GPSLon;
	}
	public void setGPSLon(double GPSLon) {
		this.GPSLon = GPSLon;
	}
	public double getMapLat() {
		return MapLat;
	}
	public void setMapLat(double mapLat) {
		this.MapLat = mapLat;
	}
	public double getMapLon() {
		return MapLon;
	}
	public void setMapLon(double mapLon) {
		this.MapLon = mapLon;
	}

	public static int createVertex(String boundaryId, Vertex vertex) {
		return OpenTenureApplication.getInstance().getDatabase().update("INSERT INTO BOUNDARY(BOUNDARY_ID, SEQUENCE_NUMBER, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON) VALUES ('"
				+ boundaryId
				+ "', "
				+ vertex.getSequenceNumber()
				+ ", "
				+ String.format("%.8f", vertex.getGPSLat())
				+ ", "
				+ String.format("%.8f", vertex.getGPSLon())
				+ ", " 
				+ String.format("%.8f", vertex.getMapLat())
				+ ", " 
				+ String.format("%.8f", vertex.getMapLon())
				+ ")");

	}

	public int delete(String boundaryId) {
		return OpenTenureApplication.getInstance().getDatabase().update("DELETE FROM BOUNDARY WHERE BOUNDARY_ID='"
				+ boundaryId
				+ "' AND SEQUENCE_NUMBER="
				+ getSequenceNumber()
				);

	}

	public static int deleteVertex(String boundaryId, Vertex vertex) {
		return OpenTenureApplication.getInstance().getDatabase().update("DELETE FROM BOUNDARY WHERE BOUNDARY_ID='"
				+ boundaryId
				+ "' AND SEQUENCE_NUMBER="
				+ vertex.getSequenceNumber()
				);

	}

	public int create(String boundaryId) {
		return OpenTenureApplication.getInstance().getDatabase().update("INSERT INTO BOUNDARY(BOUNDARY_ID, SEQUENCE_NUMBER, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON) VALUES ('"
				+ boundaryId
				+ "', "
				+ getSequenceNumber()
				+ ", "
				+ String.format("%.8f", getGPSLat())
				+ ", "
				+ String.format("%.8f", getGPSLon())
				+ ", " 
				+ String.format("%.8f", getMapLat())
				+ ", " 
				+ String.format("%.8f", getMapLon())
				+ ")");

	}

	public static int updateVertex(String boundaryId, Vertex vertex) {
		return OpenTenureApplication.getInstance().getDatabase().update("UPDATE BOUNDARY SET GPS_LAT="
				+ String.format("%.8f", vertex.getGPSLat())
				+ ", GPS_LON="
				+ String.format("%.8f", vertex.getGPSLon())
				+ ", MAP_LAT="
				+ String.format("%.8f", vertex.getMapLat())
				+ ", MAP_LON="
				+ String.format("%.8f", vertex.getMapLon())
				+ " WHERE BOUNDARY_ID='"
				+ boundaryId
				+ "' AND SEQUENCE_NUMBER="
				+ vertex.getSequenceNumber()
				);
	}

	public int updateVertex(String boundaryId) {
		return OpenTenureApplication.getInstance().getDatabase().update("UPDATE BOUNDARY SET GPS_LAT="
				+ String.format("%.8f", getGPSLat())
				+ ", GPS_LON="
				+ String.format("%.8f", getGPSLon())
				+ ", MAP_LAT="
				+ String.format("%.8f", getMapLat())
				+ ", MAP_LON="
				+ String.format("%.8f", getMapLon())
				+ " WHERE BOUNDARY_ID='"
				+ boundaryId
				+ "' AND SEQUENCE_NUMBER="
				+ getSequenceNumber()
				);
	}

	public Vertex getVertex(String boundaryId, int sequenceNumber) {

		Vertex vertex = null;

		Connection localConnection = null;
		try {

			localConnection = db.getConnection();
			PreparedStatement statement = localConnection
					.prepareStatement("SELECT GPS_LAT, GPS_LON, MAP_LAT, MAP_LON FROM BOUNDARY BOUND WHERE BOUND.BOUNDARY_ID=? AND BOUND.SEQUENCE_NUMBER=?");
			statement.setString(1, boundaryId);
			statement.setInt(2, sequenceNumber);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				vertex = new Vertex();
				vertex.setSequenceNumber(sequenceNumber);
				vertex.setGPSLat(rs.getBigDecimal(1).doubleValue());
				vertex.setGPSLon(rs.getBigDecimal(2).doubleValue());
				vertex.setMapLat(rs.getBigDecimal(3).doubleValue());
				vertex.setMapLon(rs.getBigDecimal(4).doubleValue());
			}
			rs.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return vertex;
	}

	public static List<Vertex> getVertices(String boundaryId) {

		List<Vertex> vertices = new ArrayList<Vertex>();

		Connection localConnection = null;
		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			PreparedStatement statement = localConnection
					.prepareStatement("SELECT SEQUENCE_NUMBER, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON FROM BOUNDARY BOUND WHERE BOUND.BOUNDARY_ID=? ORDER BY SEQUENCE_NUMBER");
			statement.setString(1, boundaryId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				Vertex vertex = new Vertex();
				vertex.setSequenceNumber(rs.getInt(1));
				vertex.setGPSLat(rs.getBigDecimal(2).doubleValue());
				vertex.setGPSLon(rs.getBigDecimal(3).doubleValue());
				vertex.setMapLat(rs.getBigDecimal(4).doubleValue());
				vertex.setMapLon(rs.getBigDecimal(5).doubleValue());
			}
			rs.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return vertices;
	}

	int sequenceNumber;
	double GPSLat;
	double GPSLon;
	double MapLat;
	double MapLon;

}
