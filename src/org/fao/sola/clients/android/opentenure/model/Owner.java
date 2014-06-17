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

public class Owner {

	@Override
	public String toString() {
		return "Owner [id=" + id + "claimId=" + claimId + ", personId="
				+ personId + ", shares=" + shares + "]";
	}

	public String getClaimId() {
		return claimId;
	}

	public void setClaimId(String claimId) {
		this.claimId = claimId;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public int getShares() {
		return shares;
	}

	public void setShares(int shares) {
		this.shares = shares;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	String claimId;
	String personId;
	String ownerId;
	int shares;
	String id;

	Database db = OpenTenureApplication.getInstance().getDatabase();

	public Owner(boolean isClaimant) {
		if (isClaimant) {
			this.id = UUID.randomUUID().toString();
			this.ownerId = UUID.randomUUID().toString();
		} else
			this.id = UUID.randomUUID().toString();
	}

	public static int createOwner(Owner own) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO OWNER(ID,CLAIM_ID, PERSON_ID, OWNER_ID, SHARES) VALUES (?,?,?,?,?)");
			statement.setString(1, own.getId());
			statement.setString(2, own.getClaimId());
			statement.setString(3, own.getPersonId());
			statement.setString(4, own.getOwnerId());
			statement.setBigDecimal(5, new BigDecimal(own.getShares()));
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
					.prepareStatement("INSERT INTO OWNER(ID, CLAIM_ID, PERSON_ID, OWNER_ID, SHARES) VALUES (?,?,?,?,?)");
			statement.setString(1, getId());
			statement.setString(2, getClaimId());
			statement.setString(3, getPersonId());
			statement.setString(4, getOwnerId());
			statement.setBigDecimal(5, new BigDecimal(getShares()));
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

	public static int updateOwner(Owner own) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE OWNER SET SHARES=? WHERE CLAIM_ID=? AND PERSON_ID=?");
			statement.setBigDecimal(1, new BigDecimal(own.getShares()));
			statement.setString(2, own.getClaimId());
			statement.setString(3, own.getPersonId());
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

	public int updateOwner() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE OWNER SET SHARES=? WHERE CLAIM_ID=? AND PERSON_ID=?");
			statement.setBigDecimal(1, new BigDecimal(shares));
			statement.setString(2, claimId);
			statement.setString(3, personId);
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

	public static int deleteOwner(Owner own) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("DELETE OWNER WHERE CLAIM_ID=? AND PERSON_ID=?");
			statement.setString(1, own.getClaimId());
			statement.setString(2, own.getPersonId());
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

	public static int deleteOwners(String claimId) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("DELETE OWNER WHERE CLAIM_ID=?");
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
					.prepareStatement("DELETE OWNER WHERE CLAIM_ID=? AND PERSON_ID=?");
			statement.setString(1, getClaimId());
			statement.setString(2, getPersonId());
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

	public static List<Owner> getOwners(String claimId) {
		List<Owner> ownList = new ArrayList<Owner>();
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT OWN.ID, OWN.PERSON_ID, OWN.OWNER_ID, OWN.SHARES FROM OWNER OWN WHERE OWN.CLAIM_ID=? ORDER BY OWN.PERSON_ID");
			statement.setString(1, claimId);
			rs = statement.executeQuery();
			while (rs.next()) {
				Owner own = new Owner(false);
				own.setId(rs.getString(1));
				own.setClaimId(claimId);
				own.setPersonId(rs.getString(2));
				own.setOwnerId(rs.getString(3));
				own.setShares(rs.getBigDecimal(4).intValue());
				ownList.add(own);
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
		return ownList;
	}

	public static Owner getOwner(String claimId, String personId) {

		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Owner own = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT OWN.ID, OWN.OWNER_ID, OWN.SHARES FROM OWNER OWN WHERE OWN.CLAIM_ID=? AND OWN.PERSON_ID=?");
			statement.setString(1, claimId);
			statement.setString(2, personId);
			rs = statement.executeQuery();
			while (rs.next()) {
				own = new Owner(false);
				own.setId(rs.getString(1));
				own.setClaimId(claimId);
				own.setPersonId(personId);
				own.setOwnerId(rs.getString(2));
				own.setShares(rs.getBigDecimal(3).intValue());
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
		return own;
	}

	public Owner getOwner() {

		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Owner own = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("SELECT OWN.ID, OWN.OWNER_ID, OWN.SHARES FROM OWNER OWN WHERE OWN.CLAIM_ID=? AND OWN.PERSON_ID=?");
			statement.setString(1, claimId);
			statement.setString(2, personId);
			rs = statement.executeQuery();
			while (rs.next()) {
				own = new Owner(false);
				own.setId(rs.getString(1));
				own.setClaimId(claimId);
				own.setPersonId(personId);
				own.setOwnerId(rs.getString(2));
				own.setShares(rs.getBigDecimal(3).intValue());
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
		return own;
	}
}
