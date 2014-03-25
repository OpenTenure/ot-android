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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.tools.RunScript;

import android.content.Context;
import android.util.Log;

public class Database {

	boolean open = false;
	private Connection connection;
	private Context context;

	private String DB_PATH;

	private String DB_NAME;

	private String DB_FILE_NAME;

	private String url;

	public Database(Context context) {
		this.context = context;
		DB_PATH = context.getFilesDir().getPath() + "/";

		DB_NAME = "opentenure";

		DB_FILE_NAME = "opentenure.h2.db";

		url = "jdbc:h2:" + DB_PATH + DB_NAME + ";FILE_LOCK=FS" + ";USER=sa"
				+ ";IFEXISTS=TRUE" + ";PAGE_SIZE=1024" + ";CACHE_SIZE=8192";
		try {
			if (init()) {
				open();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private boolean init() throws IOException {
		if (!checkDataBase()) {
			copyDataBase();
			executeScript("createvalues.sql");
		}
		return true;
	}

	public void open() {
		if (isOpen()) {
			return;
		}
		try {
			Log.d(this.getClass().getName(), "opening db ...");
			Class.forName("org.h2.Driver");
			connection = DriverManager.getConnection(url);
			Log.d(this.getClass().getName(), "... opened");

			open = true;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void close() {
		if (!isOpen()) {
			return;
		}
		try {
			Log.d(this.getClass().getName(), "closing db ...");
			connection.close();
			Log.d(this.getClass().getName(), "... closed");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		open = false;
	}

	public void sync() {
		if (!isOpen()) {
			return;
		}

		Log.d(this.getClass().getName(), "synching db ...");
		exec("CHECKPOINT SYNC");
		Log.d(this.getClass().getName(), "... synched");
	}

	public boolean isOpen() {
		return open;
	}

	private boolean checkDataBase() {

		open();
		if (isOpen()) {
			close();
			return true;
		}
		return false;
	}

	private boolean copyDataBase() {

		InputStream is = null;
		OutputStream os = null;
		try {
			is = context.getAssets().open(DB_FILE_NAME);
			String outFileName = DB_PATH + DB_FILE_NAME;

			os = new FileOutputStream(outFileName);

			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}

			os.flush();
			os.close();
			is.close();
			return true;

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		}
		return false;
	}

	public void exec(String command) {
		Connection localConnection = null;
		try {

			localConnection = DriverManager.getConnection(url);
			Statement statement = localConnection.createStatement();
			statement.execute(command);
			statement.close();

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
	}

	public Connection getConnection(){

		try {

			return DriverManager.getConnection(url);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int update(String command) {
		int result = 0;
		Connection localConnection = null;

		try {

			localConnection = DriverManager.getConnection(url);

			Statement statement = localConnection.createStatement();
			result = statement.executeUpdate(command);
			statement.close();

			localConnection.commit();
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

	public void executeScript(String script) {
		Connection localConnection = null;

		try {

			localConnection = DriverManager.getConnection(url);
			Log.d(this.getClass().getName(), "Executing script <" + script
					+ ">");
			InputStream scriptStream = context.getAssets().open(script);

			ResultSet rs = RunScript.execute(localConnection,
					new InputStreamReader(scriptStream));
			rs.close();
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
	}

}
