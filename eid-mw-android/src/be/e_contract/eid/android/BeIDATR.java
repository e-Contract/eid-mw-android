/*
 * Android eID Middleware Project.
 * Copyright (C) 2013 e-Contract.be BVBA.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see 
 * http://www.gnu.org/licenses/.
 */

package be.e_contract.eid.android;

/**
 * Belgian eID card detection based on the smart card ATR value.
 * 
 * @author Frank Cornelis
 * 
 */
public class BeIDATR {

	private final static byte[] ATR_PATTERN = new byte[] { 0x3b, (byte) 0x98,
			0x00, 0x40, 0x00, (byte) 0x00, 0x00, 0x00, 0x01, 0x01, (byte) 0xad,
			0x13, 0x10 };

	private final static byte[] ATR_MASK = new byte[] { (byte) 0xff,
			(byte) 0xff, 0x00, (byte) 0xff, 0x00, 0x00, 0x00, 0x00,
			(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xf0 };

	public static boolean isBeID(byte[] atr) {
		if (atr.length != (ATR_PATTERN.length + 10)) {
			return false;
		}
		for (int idx = 0; idx < ATR_PATTERN.length; idx++) {
			if ((atr[idx] & ATR_MASK[idx]) != ATR_PATTERN[idx]) {
				return false;
			}
		}
		return true;
	}
}
