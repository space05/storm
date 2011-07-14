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

import java.math.BigInteger;
import java.security.SecureRandom;

public class BigNumber
{
	
	public BigNumber()
	{
		this.bigInteger = BigInteger.ZERO;
	}
	
	public BigNumber(BigInteger bigInteger)
	{
		this.bigInteger = bigInteger.abs();
	}
	
	public BigNumber(String str)
	{
		this.bigInteger = new BigInteger(str, 16);
	}
	
	public BigNumber(byte[] array)
	
	{
		// Добавляем первый байт который обозначает знак
		if (array[0] < 0)
		{
			byte[] tmp = new byte[array.length + 1];
			System.arraycopy(array, 0, tmp, 1, array.length);
			array = tmp;
		}
		this.bigInteger = new BigInteger(array);
	}
	
	public BigNumber add(BigNumber val)
	{
		return new BigNumber(this.bigInteger.add(val.getBigInteger()));
	}
	
	public BigNumber multiply(BigNumber val)
	{
		return new BigNumber(this.bigInteger.multiply(val.getBigInteger()));
	}
	
	public void setHexStr(String str)
	{
		this.bigInteger = new BigInteger(str, 16);
	}
	
	public void setRand(int numBytes)
	{
		SecureRandom random = new SecureRandom();
		byte[] array = random.generateSeed(numBytes);
		this.bigInteger = new BigInteger(array).abs();
	}
	
	public void setBinary(byte[] array)
	{
		// Переварачиваем массив
		int length = array.length;
		for (int i = 0; i < length / 2; i++)
		{
			byte j = array[i];
			array[i] = array[length - 1 - i];
			array[length - 1 - i] = j;
		}
		
		// Добавляем первый байт который обозначает знак
		if (array[0] < 0)
		{
			byte[] tmp = new byte[array.length + 1];
			System.arraycopy(array, 0, tmp, 1, array.length);
			array = tmp;
		}
		
		this.bigInteger = new BigInteger(array);
	}
	
	public BigNumber mod(BigNumber m)
	{
		return new BigNumber(this.bigInteger.mod(m.getBigInteger()));
	}
	
	public BigNumber modPow(BigNumber exponent, BigNumber m)
	{
		return new BigNumber(this.bigInteger.modPow(exponent.getBigInteger(), m.getBigInteger()));
	}
	
	public byte[] asByteArray(int minSize)
	{
		
		// Убираем первый байт который обозначает знак
		byte[] array = this.bigInteger.toByteArray();
		if (array[0] == 0)
		{
			byte[] tmp = new byte[array.length - 1];
			System.arraycopy(array, 1, tmp, 0, tmp.length);
			array = tmp;
		}
		
		// Переварачиваем массив
		int length = array.length;
		for (int i = 0; i < length / 2; i++)
		{
			byte j = array[i];
			array[i] = array[length - 1 - i];
			array[length - 1 - i] = j;
		}
		
		// If we need more bytes than length of BigNumber set the rest to 0
		if (minSize > length)
		{
			byte[] newArray = new byte[minSize];
			System.arraycopy(array, 0, newArray, 0, length);
			
			return newArray;
		}
		
		return array;
	}
	
	// Передает последовательность без изменения
	public byte[] toByteArray()
	{
		return this.bigInteger.toByteArray();
	}
	
	public String asHexStr()
	{
		return this.bigInteger.toString(16).toUpperCase();
	}
	
	public BigInteger getBigInteger()
	{
		return this.bigInteger.abs();
	}
	
	private BigInteger bigInteger;
}
