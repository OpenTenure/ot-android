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
import java.util.UUID;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;

public class Metadata {
	
	Database db = OpenTenureApplication.getInstance().getDatabase();

	public Metadata(){
		this.metadataId = UUID.randomUUID().toString();
	}

	public String getClaimId() {
		return claimId;
	}
	public void setClaimId(String claimId) {
		this.claimId = claimId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "Metadata ["
				+ "metadataId=" + metadataId
				+ ", uploaded=" + uploaded.toString()
				+ ", claimId=" + claimId
				+ ", name=" + name
				+ ", value=" + value
				+ "]";
	}

	public static int createMetadata(Metadata metadata) {

		int result = 0;
		Connection localConnection = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			PreparedStatement statement = localConnection
					.prepareStatement("INSERT INTO METADATA (METADATA_ID, UPLOADED, CLAIM_ID, NAME, VALUE) VALUES(?,?,?,?,?)");
			statement.setString(1, metadata.getMetadataId());
			statement.setBoolean(2, metadata.getUploaded()); 
			statement.setString(3, metadata.getClaimId()); 
			statement.setString(4, metadata.getName()); 
			statement.setString(5, metadata.getValue()); 
			result = statement.executeUpdate();
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
		return result;
	}

	public int create() {
		int result = 0;
		Connection localConnection = null;

		try {

			localConnection = db.getConnection();
			PreparedStatement statement = localConnection
					.prepareStatement("INSERT INTO METADATA (METADATA_ID, UPLOADED, CLAIM_ID, NAME, VALUE) VALUES(?,?,?,?,?)");
			statement.setString(1, getMetadataId());
			statement.setBoolean(2, getUploaded()); 
			statement.setString(3, getClaimId()); 
			statement.setString(4, getName()); 
			statement.setString(5, getValue()); 
			result = statement.executeUpdate();
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
		return result;
	}

	public int delete() {
		int result = 0;
		Connection localConnection = null;

		try {

			localConnection = db.getConnection();
			PreparedStatement statement = localConnection
					.prepareStatement("DELETE FROM METADATA WHERE METADATA_ID=?");
			statement.setString(1, getMetadataId());
			result = statement.executeUpdate();
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
		return result;
	}

	public static int deleteMetadata(Metadata metadata) {
		int result = 0;
		Connection localConnection = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			PreparedStatement statement = localConnection
					.prepareStatement("DELETE FROM METADATA WHERE METADATA_ID=?");
			statement.setString(1, metadata.getMetadataId());
			result = statement.executeUpdate();
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
		return result;
	}

	public static int updateMetadata(Metadata metadata) {

				int result = 0;
				Connection localConnection = null;

				try {

					localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
					PreparedStatement statement = localConnection
							.prepareStatement("UPDATE METADATA SET UPLOADED=?, CLAIM_ID=?, NAME=?, VALUE=? WHERE METADATA_ID=?");
					statement.setBoolean(1, metadata.getUploaded()); 
					statement.setString(2, metadata.getClaimId()); 
					statement.setString(3, metadata.getName()); 
					statement.setString(4, metadata.getValue()); 
					statement.setString(5, metadata.getMetadataId());
					result = statement.executeUpdate();
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
				return result;
	}

	public int updateMetadata() {
		int result = 0;
		Connection localConnection = null;

		try {

			localConnection = db.getConnection();
			PreparedStatement statement = localConnection
					.prepareStatement("UPDATE METADATA SET UPLOADED=?, CLAIM_ID=?, NAME=?, VALUE=? WHERE METADATA_ID=?");
			statement.setBoolean(1, getUploaded()); 
			statement.setString(2, getClaimId()); 
			statement.setString(3, getName()); 
			statement.setString(4, getValue()); 
			statement.setString(5, getMetadataId());
			result = statement.executeUpdate();
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
		return result;
	}

	public static Metadata getMetadata(String metadataId) {

		Metadata metadata = null;

		Connection localConnection = null;
		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			PreparedStatement statement = localConnection
					.prepareStatement("SELECT UPLOADED, CLAIM_ID, NAME, VALUE FROM METADATA WHERE METADATA_ID=?");
			statement.setString(1, metadataId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				metadata = new Metadata();
				metadata.setMetadataId(metadataId);
				metadata.setUploaded(rs.getBoolean(1));
				metadata.setClaimId(rs.getString(2));
				metadata.setName(rs.getString(3));
				metadata.setValue(rs.getString(4));
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
		return metadata;
	}

	public String getMetadata(String claimId, String name) {

		String value = null;
		Connection localConnection = null;

		try {

			localConnection = db.getConnection();
			PreparedStatement statement = localConnection
					.prepareStatement("SELECT VALUE FROM METADATA META WHERE META.CLAIM_ID=? AND META.NAME=?");
			statement.setString(1, claimId);
			statement.setString(2, name); 
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				value = rs.getString(1);
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
		return value;
	}

	public String getMetadataId() {
		return metadataId;
	}
	public void setMetadataId(String metadataId) {
		this.metadataId = metadataId;
	}

	public static List<Metadata> getClaimMetadata(String claimId) {

		List<Metadata> metadata = new ArrayList<Metadata>();

		Connection localConnection = null;
		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			PreparedStatement statement = localConnection
					.prepareStatement("SELECT METADATA_ID, UPLOADED, NAME, VALUE FROM METADATA META WHERE META.CLAIM_ID=?");
			statement.setString(1, claimId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				Metadata item = new Metadata();
				item.setClaimId(claimId);
				item.setMetadataId(rs.getString(1));
				item.setUploaded(rs.getBoolean(2));
				item.setName(rs.getString(3));
				item.setValue(rs.getString(4));
				metadata.add(item);
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
		return metadata;
	}

	public Boolean getUploaded() {
		return uploaded;
	}

	public void setUploaded(Boolean uploaded) {
		this.uploaded = uploaded;
	}

	String metadataId;
	Boolean uploaded = Boolean.valueOf(false);
	String claimId;
	String name;
	String value;

}
