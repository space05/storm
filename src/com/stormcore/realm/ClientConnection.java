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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

import com.stormcore.realm.AuthCodes.AuthResult;
import com.stormcore.realm.AuthCodes.eAuthCmd;

public class ClientConnection extends Thread
{
	public static final BigNumber N = new BigNumber("894B645E89E1535BBDAD5B8B290650530801B18EBFBF5E8FAB3C82872A3E9BB7");
	public static final BigNumber g = new BigNumber("7");
	public static final BigNumber k = new BigNumber("3");
	
	static Logger _log = Logger.getLogger(ClientConnection.class.getName());
	Socket socket;
	int num;
	
	public ClientConnection(int num, Socket socket)
	{
		
		// копируем данные
		this.num = num;
		this.socket = socket;
		
		System.out.println("Подключение номер: " + num + " IP: "
		        + socket.getInetAddress().getHostAddress());
		
		// и запускаем новый вычислительный поток (см. ф-ю run())
		setDaemon(true);
		setPriority(NORM_PRIORITY);
		start();
	}
	
	public void run()
	{
		
		final int s_BYTE_SIZE = 32;
		
		try
		{
			// из сокета клиента берём поток входящих данных
			InputStream is = socket.getInputStream();
			// и оттуда же - поток данных от сервера к клиенту
			OutputStream os = socket.getOutputStream();
			
			// получение входящего пакета
			byte[] dataHead = new byte[4];
			is.read(dataHead, 0, 4);
			
			byte[] dataBody = new byte[dataHead[2] + 5];
			short[] shortDataPacked = new short[dataHead[2] + 5];
			is.read(dataBody, 4, dataHead[2]);
			int i = 0;
			for (byte data : dataBody)
			{
				shortDataPacked[i] = (short) (data & 0xff);
				i++;
			}
			
			byte[] outData = new byte[3];
			outData[0] = eAuthCmd.CMD_AUTH_LOGON_CHALLENGE;
			outData[1] = 0x00;
			
			int lenLogin = shortDataPacked[33];
			String login = "";
			for (i = 34; i < 34 + lenLogin; i++)
			{
				login = login + (char) shortDataPacked[i];
			}
			System.out.println("Login: " + login);
			short build = (short) (shortDataPacked[12] << 8 | shortDataPacked[11]);
			System.out.println("Build: " + build);
			
			// ip пользователя
			String address = socket.getInetAddress().getHostAddress();
			
			if (ipBan(address))
			{
				// пакет который возвращает что ip забанен
				outData[2] = AuthResult.WOW_FAIL_BANNED;
			}
			else
			{
				
				// проверка существует ли такой акк
				HashMap<String, String> hm = isAccount(login);
				if (!hm.isEmpty())
				{
					// Проверка заблокирован ли IP
					Boolean locked = false;
					if (Integer.valueOf(hm.get("locked")) == 1)
					{
						System.out.print(hm.get("locked"));
					}
					else
					{
						System.out.println("Пользователь " + login + " не привязан к IP");
					}
					
					// Если пользователь не привязан к IP
					if (!locked)
					{
						// Если учетная запись запрещена, отвергать попытки входа в систему
						
						// QueryResult *banresult =
						// LoginDatabase.PQuery("SELECT bandate,unbandate FROM account_banned WHERE "
						// "id = %u AND active = 1 AND (unbandate > UNIX_TIMESTAMP() OR unbandate = bandate)",
						// (*result)[1].GetUInt32());
						
						if (isBanResult(hm.get("id")))
						{
							// if((*banresult)[0].GetUInt64() == (*banresult)[1].GetUInt64())
							// {
							// pkt << (uint8) WOW_FAIL_BANNED;
							// BASIC_LOG("[AuthChallenge] Banned account %s tries to login!",_login.c_str
							// ());
							// }
							// else
							// {
							// pkt << (uint8) WOW_FAIL_SUSPENDED;
							// BASIC_LOG("[AuthChallenge] Temporarily banned account %s tries to login!",_login.c_str
							// ());
							// }
							//
							// delete banresult;
						}
						else
						{
							// Получить пароль от счета стол, верхняя его, и сделать расчет SRP6
							
							String rI = hm.get("sha_pass_hash");
							
							// Не рассчитывайте (V, S), если Есть уже кое-кто в базу данных
							String databaseV = hm.get("v");
							String databaseS = hm.get("s");
							
							// DEBUG_LOG("database authentication values: v='%s' s='%s'",
							// databaseV.c_str(),
							// databaseS.c_str());
							
							// multiply with 2, bytes are stored as hexstring
							if (databaseV.length() != s_BYTE_SIZE * 2
							        || databaseS.length() != s_BYTE_SIZE * 2)
							{
								_SetVSFields(rI);
							}
							else
							{
								s.setHexStr(databaseS);
								v.setHexStr(databaseV);
								System.out.println("s: " + s.asHexStr());
								System.out.println("v: " + v.asHexStr());
							}
							
							// BigNumber b = new BigNumber();
							b.setRand(19);
							// b.setHexStr("AC7407DAE495C6B239B288BFD13267EC87FC2F");
							System.out.println("b    = '" + b.asHexStr() + "'");
							// BigNumber B = new BigNumber();
							
							BigNumber gmod = g.modPow(b, N);
							System.out.println("gmod = '" + gmod.asHexStr() + "'");
							
							B = ((v.multiply(k)).add(gmod)).mod(N);
							System.out.println("B    = '" + B.asHexStr() + "'");
							
							// MANGOS_ASSERT(gmod.GetNumBytes() <= 32);
							
							BigNumber unk3 = new BigNumber();
							unk3.setRand(16);
							System.out.println("3: " + unk3.asHexStr());
							
							// ///- Fill the response packet with the result
							// pkt << uint8(WOW_SUCCESS);
							outData[2] = AuthResult.WOW_SUCCESS;
							
							ByteBuffer pkt = ByteBuffer.allocate(119);
							
							pkt.put(outData);
							pkt.put(B.asByteArray(32));
							pkt.put((byte) 1);
							pkt.put(g.asByteArray(1));
							pkt.put((byte) 32);
							pkt.put(N.asByteArray(32));
							pkt.put(s.asByteArray(32));
							pkt.put(unk3.asByteArray(16));
							pkt.put((byte) 0); // security flags (0x0...0x04)
							
							asd = pkt.array();
							System.out.print("");
							
						}
					}
				}
				else
				{
					// пакет который возвращает что такого аккаунта нет
					outData[2] = AuthResult.WOW_FAIL_UNKNOWN_ACCOUNT;
				}
			}
			
			os.write(asd, 0, 119);
			System.out.print("");
			byte[] data2 = new byte[75];
			is.read(data2, 0, 75);
			byte[] a = new byte[32];
			byte[] m1 = new byte[20];
			byte[] crc = new byte[20];
			System.arraycopy(data2, 1, a, 0, 32);
			System.arraycopy(data2, 33, m1, 0, 20);
			System.arraycopy(data2, 53, crc, 0, 20);
			System.out.print("");
			
			BigNumber M1 = new BigNumber();
			M1.setBinary(m1);
			System.out.println("M1: " + M1.asHexStr());
			BigNumber A = new BigNumber();
			A.setBinary(a);
			// A.setHexStr("01715C053C948504EA1C066B2D191207215A1053BF4A150103D29277034F767C");
			System.out.println("A: " + A.asHexStr());
			
			MessageDigest sha = null;
			try
			{
				sha = MessageDigest.getInstance("SHA-1");
			}
			catch (NoSuchAlgorithmException e)
			{
				e.printStackTrace();
				return;
			}
			
			sha.update(A.asByteArray(32));
			sha.update(B.asByteArray(32));
			BigNumber u = new BigNumber();
			u.setBinary(sha.digest());
			System.out.println("u: " + u.asHexStr());
			
			BigNumber S = new BigNumber();
			S = (A.multiply(v.modPow(u, N))).modPow(b, N);
			System.out.println("S: " + S.asHexStr());
			
			byte[] t = new byte[32];
			byte[] t1 = new byte[16];
			byte[] t2 = new byte[20];
			byte[] vK = new byte[40];
			
			t = S.asByteArray(32);
			for (i = 0; i < 16; ++i)
			{
				t1[i] = t[i * 2];
			}
			sha.update(t1);
			t2 = sha.digest();
			// BigNumber ut1 = new BigNumber();
			// ut1.setBinary(t1);
			// System.out.println("t1: " + ut1.asHexStr());
			// t1 = ut1.asByteArray(16);
			for (i = 0; i < 20; ++i)
			{
				vK[i * 2] = t2[i];
			}
			for (i = 0; i < 16; ++i)
			{
				t1[i] = t[i * 2 + 1];
			}
			sha.update(t1);
			t2 = sha.digest();
			// BigNumber ut2 = new BigNumber(sha.digest());
			// ut1.setBinary(ut2);
			// System.out.println("t2: " + ut2.asHexStr());
			for (i = 0; i < 20; ++i)
			{
				vK[i * 2 + 1] = t2[i];
			}
			BigNumber K = new BigNumber();
			K.setBinary(vK);
			System.out.println("K: " + K.asHexStr());
			
			byte[] hash = new byte[20];
			sha.update(N.asByteArray(32));
			hash = sha.digest();
			
			byte[] gH = new byte[20];
			sha.update(g.asByteArray(1));
			gH = sha.digest();
			for (i = 0; i < 20; ++i)
			{
				hash[i] ^= gH[i];
			}
			BigNumber t3 = new BigNumber();
			t3.setBinary(hash);
			System.out.println("t3: " + t3.asHexStr());
			
			byte[] t4 = new byte[20];
			sha.update(login.getBytes());
			t4 = sha.digest();
			
			sha.update(t3.asByteArray(20));
			sha.update(t4);
			sha.update(s.asByteArray(32));
			sha.update(A.asByteArray(32));
			sha.update(B.asByteArray(32));
			sha.update(K.asByteArray(40));
			
			BigNumber M = new BigNumber();
			
			M.setBinary(sha.digest());
			System.out.println("M: " + M.asHexStr());
			
			byte[] mx = M1.asByteArray(20);
			byte[] my = M.asByteArray(20);
			
			System.out.println(Arrays.toString(mx));
			System.out.println(Arrays.toString(my));
			System.out.println("");
			
			if (M1.asHexStr().equals(M.asHexStr()))
			{
				sha.update(A.asByteArray(32));
				sha.update(M.asByteArray(20));
				sha.update(K.asByteArray(40));
				
				BigNumber M2 = new BigNumber();
				M2.setBinary(sha.digest());
				
				byte[] proof = new byte[26];
				proof[0] = eAuthCmd.CMD_AUTH_LOGON_PROOF;
				proof[1] = 0;
				System.arraycopy(M2.asByteArray(20), 0, proof, 2, 20);
				proof[22] = 0;
				proof[23] = 0;
				proof[24] = 0;
				proof[25] = 0;
				
				os.write(proof, 0, 26);
				System.out.print("");
				byte[] data3 = new byte[1];
				is.read(data3, 0, 1);
				System.out.print("");
				
				byte[] realm = { 16, 43, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 2, 74, 97, 118, 97, 32, 87,
				        111, 87, 0, 49, 50, 55, 46, 48, 46, 48, 46, 49, 58, 56, 48, 56, 53, 0, 0,
				        0, 0, 0, 0, 1, 0, 2, 0 };
				
				os.write(realm, 0, 46);
				System.out.print("");
				byte[] data4 = new byte[100];
				is.read(data4, 0, 100);
				System.out.print("");
				
				// send((char *)&proof, sizeof(proof));
			}
			else
			{
				byte[] data = { eAuthCmd.CMD_AUTH_LOGON_PROOF, AuthResult.WOW_FAIL_UNKNOWN_ACCOUNT };
				os.write(data, 0, 2);
			}
			
			// завершаем соединение
			// s.close();
		}
		catch (Exception e)
		{
			// вывод исключений
			System.out.println("init error: " + e);
		}
	}
	
	private void _SetVSFields(String Ir)
	{
		BigNumber I = new BigNumber(Ir);
		byte[] hash = I.asByteArray(20);
		
		int length = hash.length;
		for (int i = 0; i < length / 2; i++)
		{
			byte j = hash[i];
			hash[i] = hash[length - 1 - i];
			hash[length - 1 - i] = j;
		}
		
		s.setRand(32);
		// s.setHexStr("E521E4D7B88ED3D5D6B7B9D3F7DCCC446C7618F8F0174634D263A380DE972E4F");
		System.out.println("s: " + s.asHexStr());
		
		MessageDigest sha = null;
		try
		{
			sha = MessageDigest.getInstance("SHA-1");
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			return;
		}
		
		sha.update(s.asByteArray(32));
		sha.update(hash);
		BigNumber x = new BigNumber();
		x.setBinary(sha.digest());
		System.out.println("x: " + x.asHexStr());
		
		BigNumber verifier = g.modPow(x, N);
		System.out.println("v: " + verifier.asHexStr());
		
		v = verifier;
	}
	
	private synchronized boolean isBanResult(String id)
	{
		boolean result = true;
		java.sql.Connection con = null;
		
		try
		{
			con = MysqlConnection.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT bandate, unbandate "
			        + "FROM account_banned " + "WHERE " + id
			        + " AND active = 1 AND (unbandate > UNIX_TIMESTAMP() OR unbandate = bandate);");
			ResultSet rset = statement.executeQuery();
			result = rset.next();
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning("could not check existing charname:" + e.getMessage());
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
		return result;
	}
	
	public synchronized boolean ipBan(String address)
	{
		boolean result = true;
		java.sql.Connection con = null;
		
		try
		{
			con = MysqlConnection.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT unbandate "
			        + "FROM ip_banned "
			        + "WHERE (unbandate = bandate OR unbandate > UNIX_TIMESTAMP()) AND ip = '"
			        + address + "';");
			
			ResultSet rset = statement.executeQuery();
			result = rset.next();
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning("could not check existing charname:" + e.getMessage());
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
		return result;
	}
	
	public synchronized HashMap<String, String> isAccount(String account)
	{
		HashMap<String, String> hm = null;
		java.sql.Connection con = null;
		
		try
		{
			con = MysqlConnection.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT sha_pass_hash,id,locked,last_ip,gmlevel,v,s FROM account WHERE username = '"
			        + account + "';");
			
			ResultSet rset = statement.executeQuery();
			
			if (rset.next())
			{
				hm = new HashMap<String, String>();
				hm.put("sha_pass_hash", rset.getString(1));
				hm.put("id", rset.getString(2));
				hm.put("locked", rset.getString(3));
				hm.put("last_ip", rset.getString(4));
				hm.put("gmlevel", rset.getString(5));
				hm.put("v", rset.getString(6));
				hm.put("s", rset.getString(7));
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning("could not check existing charname:" + e.getMessage());
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
		return hm;
	}
	
	public static byte[] hexStringToByteArray(String s)
	{
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2)
		{
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
	
	static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1', (byte) '2', (byte) '3',
	        (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) 'a',
	        (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f' };
	
	public static String getHexString(byte[] raw) throws UnsupportedEncodingException
	{
		byte[] hex = new byte[2 * raw.length];
		int index = 0;
		for (byte b : raw)
		{
			int v = b & 0xFF;
			hex[index++] = HEX_CHAR_TABLE[v >>> 4];
			hex[index++] = HEX_CHAR_TABLE[v & 0xF];
		}
		return new String(hex, "ASCII");
	}
	byte[] asd = new byte[119];
	
	private BigNumber v = new BigNumber();
	private BigNumber s = new BigNumber();
	private BigNumber b = new BigNumber();
	private BigNumber B = new BigNumber();
	
}