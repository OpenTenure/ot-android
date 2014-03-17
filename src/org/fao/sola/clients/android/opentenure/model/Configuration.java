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
import java.util.HashMap;
import java.util.Map;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;

public class Configuration {

	public static int addConfiguration(String key, String value) {
		return OpenTenureApplication.getInstance().getDatabase().update("INSERT INTO CONFIGURATION(NAME, VALUE) VALUES ('" + key
				+ "','" + value + "')");
	}

	public static int updateConfiguration(String key, String value) {
		return OpenTenureApplication.getInstance().getDatabase().update("UPDATE CONFIGURATION CFG SET CFG.VALUE='" + value
				+ "' WHERE CFG.NAME='" + key + "'");
	}

	public static String getConfiguration(String key) {

		String value = null;
		Connection localConnection = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			PreparedStatement statement = localConnection
					.prepareStatement("SELECT CFG.VALUE FROM CONFIGURATION CFG WHERE CFG.NAME=?");
			statement.setString(1, key);
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

	public static Map<String, String> loadConfiguration() {

		Map<String, String> cfg = new HashMap<String, String>();

		Connection localConnection = null;
		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			PreparedStatement statement = localConnection
					.prepareStatement("SELECT CFG.NAME, CFG.VALUE FROM CONFIGURATION CFG");
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				cfg.put(rs.getString(1), rs.getString(2));
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
		return cfg;
	}

}
