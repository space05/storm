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

import java.net.ServerSocket;
import org.apache.log4j.Logger;

public class Realm
{
	
	@SuppressWarnings("unused")
	private static Realm _realm;
	public static final Logger _log = Logger.getLogger(Realm.class);
	
	public static void main(String[] args)
	{
		_log.info("Hello World!");
		_realm = new Realm();
	}
	
	public Realm()
	{
		// Загрузка конфига
		Config.load();
		
		System.out.println("Welcome to Server side");
		
		try
		{
			// счётчик подключений
			int i = 0;
			
			// привинтить сокет на локалхост, порт
			ServerSocket server = new ServerSocket(Config.REALM_SERVER_PORT);
			
			System.out.println("server is started");
			
			// слушаем порт
			while (true)
			{
				// ждём нового подключения, после чего запускаем обработку клиента
				// в новый вычислительный поток и увеличиваем счётчик на единичку
				new ClientConnection(i, server.accept());
				i++;
			}
		}
		catch (Exception e)
		{
			// вывод исключений
			_log.info("Couldn't listen to port " + Config.REALM_SERVER_PORT);
		}
		
	}
	
}
