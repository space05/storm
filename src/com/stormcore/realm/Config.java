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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config
{
	
	/** Realm comfig */
	public static final String REALM_CONFIG = "./src/com/stormcore/realm/realm.properties";
	
	public static String LOGIN_DATABASE_INFO;
	public static String LOGS_DIR;
	public static int MAX_PING_TIME;
	public static int REALM_SERVER_PORT;
	public static String BIND_IP;
	public static String PID_FILE;
	public static int LOG_LEVEL;
	public static int LOG_TIME;
	public static String LOG_FILE;
	public static int LOG_TIMESTAMP;
	public static int LOG_FILE_LEVEL;
	public static String LOG_COLORS;
	public static int USE_PROCESSORS;
	public static int PROCESS_PRIORITY;
	public static int WAIT_AT_STARTUP_ERROR;
	public static int REALMS_STATE_UPDATE_DELAY;
	public static int WRONG_PASS_MAX_COUNT;
	public static int WRONG_PASS_BAN_TIME;
	public static int WRONG_PASS_BAN_TYPE;
	
	/** Datapack root directory */
	public static File DATAPACK_ROOT;
	
	public static void load()
	{
		
		InputStream is = null;
		Properties serverSettings = new Properties();
		try
		{
			is = new FileInputStream(new File(REALM_CONFIG));
			serverSettings.load(is);
			LOGIN_DATABASE_INFO = serverSettings.getProperty("LoginDatabaseInfo");
			LOGS_DIR = serverSettings.getProperty("LogsDir");
			MAX_PING_TIME = Integer.parseInt(serverSettings.getProperty("MaxPingTime"));
			REALM_SERVER_PORT = Integer.parseInt(serverSettings.getProperty("RealmServerPort"));
			BIND_IP = serverSettings.getProperty("BindIP");
			PID_FILE = serverSettings.getProperty("PidFile");
			LOG_LEVEL = Integer.parseInt(serverSettings.getProperty("LogLevel"));
			LOG_TIME = Integer.parseInt(serverSettings.getProperty("LogTime"));
			LOG_FILE = serverSettings.getProperty("LogFile");
			LOG_TIMESTAMP = Integer.parseInt(serverSettings.getProperty("LogTimestamp"));
			LOG_FILE_LEVEL = Integer.parseInt(serverSettings.getProperty("LogFileLevel"));
			LOG_COLORS = serverSettings.getProperty("LogColors");
			USE_PROCESSORS = Integer.parseInt(serverSettings.getProperty("UseProcessors"));
			PROCESS_PRIORITY = Integer.parseInt(serverSettings.getProperty("ProcessPriority"));
			WAIT_AT_STARTUP_ERROR = Integer.parseInt(serverSettings.getProperty("WaitAtStartupError"));
			REALMS_STATE_UPDATE_DELAY = Integer.parseInt(serverSettings.getProperty("RealmsStateUpdateDelay"));
			WRONG_PASS_MAX_COUNT = Integer.parseInt(serverSettings.getProperty("WrongPass.MaxCount"));
			WRONG_PASS_BAN_TIME = Integer.parseInt(serverSettings.getProperty("WrongPass.BanTime"));
			WRONG_PASS_BAN_TYPE = Integer.parseInt(serverSettings.getProperty("WrongPass.BanType"));
			
			DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
}
