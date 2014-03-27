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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;

import com.google.android.gms.maps.model.LatLng;

public class Vertex {
	
	public static final LatLng INVALID_POSITION = new LatLng(400.0, 400.0);
	
	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	Database db = OpenTenureApplication.getInstance().getDatabase();

	public Vertex(){
		this.vertexId = UUID.randomUUID().toString();
	}

	public Vertex(LatLng mapPosition){
		this.vertexId = UUID.randomUUID().toString();
		setMapPosition(mapPosition);
		setGPSPosition(INVALID_POSITION);
	}

	public Vertex(LatLng mapPosition,LatLng GPSPosition){
		this.vertexId = UUID.randomUUID().toString();
		setMapPosition(mapPosition);
		setGPSPosition(GPSPosition);
	}
	
	public LatLng getMapPosition(){
		return mapPosition;
	}

	public LatLng getGPSPosition(){
		return GPSPosition;
	}

	@Override
	public String toString() {
		return "Vertex ["
				+ "vertexId=" + vertexId
				+ ", claimId=" + claimId
				+ ", uploaded=" + uploaded
				+ ", sequenceNumber=" + sequenceNumber
				+ ", GPSLat=" + GPSPosition.latitude
				+ ", GPSLon=" + GPSPosition.longitude
				+ ", MapLat=" + mapPosition.latitude
				+ ", MapLon=" + mapPosition.longitude
				+ "]";
	}
	public void setGPSPosition(LatLng GPSPosition) {
		this.GPSPosition = GPSPosition;
	}

	public void setMapPosition(LatLng mapPosition) {
		this.mapPosition = mapPosition;
	}

	public static int markAsUploaded(Vertex vertex){
		vertex.setUploaded(true);
		return Vertex.updateVertex(vertex);
	}

	public int markAsUploaded(){
		setUploaded(true);
		return update();
	}

	public static int createVertex(Vertex vertex) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO VERTEX(VERTEX_ID, UPLOADED, CLAIM_ID, SEQUENCE_NUMBER, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON) VALUES(?,?,?,?,?,?,?,?)");
			statement.setString(1, vertex.getVertexId());
			statement.setBoolean(2, vertex.getUploaded());
			statement.setString(3, vertex.getClaimId());
			statement.setInt(4, vertex.getSequenceNumber());
			statement.setBigDecimal(5, new BigDecimal(vertex.getGPSPosition().latitude));
			statement.setBigDecimal(6, new BigDecimal(vertex.getGPSPosition().longitude));
			statement.setBigDecimal(7, new BigDecimal(vertex.getMapPosition().latitude));
			statement.setBigDecimal(8, new BigDecimal(vertex.getMapPosition().longitude));
			result = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public int create() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null; 

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO VERTEX(VERTEX_ID, UPLOADED, CLAIM_ID, SEQUENCE_NUMBER, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON) VALUES(?,?,?,?,?,?,?,?)");
			statement.setString(1, getVertexId());
			statement.setBoolean(2, getUploaded());
			statement.setString(3, getClaimId());
			statement.setInt(4, getSequenceNumber());
			statement.setBigDecimal(5, new BigDecimal(getGPSPosition().latitude));
			statement.setBigDecimal(6, new BigDecimal(getGPSPosition().longitude));
			statement.setBigDecimal(7, new BigDecimal(getMapPosition().latitude));
			statement.setBigDecimal(8, new BigDecimal(getMapPosition().longitude));
			result = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public static int deleteVertex(Vertex vertex) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			statement = localConnection
					.prepareStatement("DELETE FROM VERTEX WHERE VERTEX_ID=?");
			statement.setString(1, vertex.getVertexId());
			result = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public static int deleteVertices(String claimId) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			statement = localConnection
					.prepareStatement("DELETE FROM VERTEX WHERE CLAIM_ID=?");
			statement.setString(1, claimId);
			result = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public int delete() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("DELETE FROM VERTEX WHERE VERTEX_ID=?");
			statement.setString(1, getVertexId());
			result = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public static int updateVertex(Vertex vertex) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			statement = localConnection
					.prepareStatement("UPDATE VERTEX SET UPLOADED=?, CLAIM_ID=?, SEQUENCE_NUMBER=?, GPS_LAT=?, GPS_LON=?, MAP_LAT=?, MAP_LON=? WHERE VERTEX_ID=?");
			statement.setBoolean(1, vertex.getUploaded());
			statement.setString(2, vertex.getClaimId());
			statement.setInt(3, vertex.getSequenceNumber());
			statement.setBigDecimal(4, new BigDecimal(vertex.getGPSPosition().latitude));
			statement.setBigDecimal(5, new BigDecimal(vertex.getGPSPosition().longitude));
			statement.setBigDecimal(6, new BigDecimal(vertex.getMapPosition().latitude));
			statement.setBigDecimal(7, new BigDecimal(vertex.getMapPosition().longitude));
			statement.setString(8, vertex.getVertexId());
			result = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public int update() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE VERTEX SET UPLOADED=?, CLAIM_ID=?, SEQUENCE_NUMBER=?, GPS_LAT=?, GPS_LON=?, MAP_LAT=?, MAP_LON=? WHERE VERTEX_ID=?");
			statement.setBoolean(1, getUploaded());
			statement.setString(2, getClaimId());
			statement.setInt(3, getSequenceNumber());
			statement.setBigDecimal(4, new BigDecimal(getGPSPosition().latitude));
			statement.setBigDecimal(5, new BigDecimal(getGPSPosition().longitude));
			statement.setBigDecimal(6, new BigDecimal(getMapPosition().latitude));
			statement.setBigDecimal(7, new BigDecimal(getMapPosition().longitude));
			statement.setString(8, getVertexId());
			result = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public static Vertex getVertex(String vertexId) {
		Vertex vertex = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			statement = localConnection
					.prepareStatement("SELECT UPLOADED, CLAIM_ID, SEQUENCE_NUMBER, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON FROM VERTEX VERT WHERE VERT.VERTEX_ID=?");
			statement.setString(1, vertexId);
			rs = statement.executeQuery();
			while (rs.next()) {
				vertex = new Vertex();
				vertex.setVertexId(vertexId);
				vertex.setUploaded(rs.getBoolean(1));
				vertex.setClaimId(rs.getString(2));
				vertex.setSequenceNumber(rs.getInt(3));
				vertex.setGPSPosition(new LatLng(rs.getBigDecimal(4).doubleValue(),rs.getBigDecimal(5).doubleValue()));
				vertex.setMapPosition(new LatLng(rs.getBigDecimal(6).doubleValue(),rs.getBigDecimal(7).doubleValue()));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return vertex;
	}

	public static List<Vertex> getVertices(String claimId) {
		List<Vertex> vertices = new ArrayList<Vertex>();
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			statement = localConnection
					.prepareStatement("SELECT VERTEX_ID, UPLOADED, SEQUENCE_NUMBER, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON FROM VERTEX VERT WHERE VERT.CLAIM_ID=? ORDER BY SEQUENCE_NUMBER");
			statement.setString(1, claimId);
			rs = statement.executeQuery();
			while (rs.next()) {
				Vertex vertex = new Vertex();
				vertex.setVertexId(rs.getString(1));
				vertex.setUploaded(rs.getBoolean(2));
				vertex.setClaimId(claimId);
				vertex.setSequenceNumber(rs.getInt(3));
				vertex.setGPSPosition(new LatLng(rs.getBigDecimal(4).doubleValue(),rs.getBigDecimal(5).doubleValue()));
				vertex.setMapPosition(new LatLng(rs.getBigDecimal(6).doubleValue(),rs.getBigDecimal(7).doubleValue()));
				vertices.add(vertex);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return vertices;
	}

	public String getVertexId() {
		return vertexId;
	}

	public void setVertexId(String vertexId) {
		this.vertexId = vertexId;
	}

	public String getClaimId() {
		return claimId;
	}

	public void setClaimId(String claimId) {
		this.claimId = claimId;
	}

	public Boolean getUploaded() {
		return uploaded;
	}

	public void setUploaded(Boolean uploaded) {
		this.uploaded = uploaded;
	}

	String vertexId;
	Boolean uploaded = Boolean.valueOf(false);
	String claimId;
	int sequenceNumber;
	LatLng GPSPosition;
	LatLng mapPosition;

}
