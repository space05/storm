/*
 * Copyright (C) 2011 StormCore
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package com.stormcore.realm;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import com.mysql.jdbc.Connection;

public class MysqlConnection
{
	
	static Logger _log = Logger.getLogger(MysqlConnection.class.getName());
	
	private static MysqlConnection _instance;
	private static Properties connInfo;
	
	public MysqlConnection() throws SQLException
	{
		
		try
		{
			connInfo = new Properties();
			
			connInfo.put("characterEncoding", "UTF8");
			connInfo.put("user", "javawow");
			connInfo.put("password", "javawow");
			
			System.out.println("MySQL: start");
			
		}
		catch (Exception e)
		{
			if (true)
				_log.fine("Database Connection FAILED");
			throw new SQLException("could not init DB connection:" + e);
		}
	}
	
	// Property - Public
	public static MysqlConnection getInstance() throws SQLException
	{
		if (_instance == null)
		{
			_instance = new MysqlConnection();
		}
		return _instance;
	}
	
	public Connection getConnection() // throws SQLException
	{
		Connection con = null;
		
		while (con == null)
		{
			try
			{
				con = (Connection) DriverManager.getConnection("jdbc:mysql://localhost/realm", connInfo);
			}
			catch (SQLException e)
			{
				_log.warning("MysqlConnection: getConnection() failed, trying again " + e);
			}
		}
		return con;
	}
	
}
