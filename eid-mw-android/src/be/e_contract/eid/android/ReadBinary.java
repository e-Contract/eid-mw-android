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

import java.io.ByteArrayOutputStream;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.util.Log;

/**
 * Read Binary APDU implementation.
 * 
 * @author Frank Cornelis
 * 
 */
public class ReadBinary extends XfrBlock {

	public ReadBinary(UsbEndpoint bulkInUsbEndpoint,
			UsbEndpoint bulkOutUsbEndpoint,
			UsbDeviceConnection usbDeviceConnection) {
		super(bulkInUsbEndpoint, bulkOutUsbEndpoint, usbDeviceConnection);
	}

	byte[] readBinary() {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		int offset = 0;
		byte length = 0;
		while (true) {
			byte[] readBinaryApdu = new byte[] { 0x00, // CLA
					(byte) 0xb0, // INS
					(byte) (offset >> 8), // P1 = offset high
					(byte) (offset & 0xff), // P2 = offset low
					length // Le
			};
			byte[] response = super.sendApdu(readBinaryApdu);
			if (null == response) {
				Log.e(LOG_TAG, "woops");
				return null;
			}
			if (response.length < 2) {
				Log.e(LOG_TAG, "no SW");
				return null;
			}
			byte sw1 = response[response.length - 2];
			byte sw2 = response[response.length - 1];
			Log.d(LOG_TAG, "sw1: " + Integer.toHexString(sw1 & 0xff));
			Log.d(LOG_TAG, "sw2: " + Integer.toHexString(sw2 & 0xff));
			if (sw1 == 0x6c) {
				length = sw2;
				continue;
			}
			if (sw1 == 0x6b) {
				break;
			}
			data.write(response, 0, response.length - 2);
			offset += response.length - 2;
		}
		return data.toByteArray();
	}
}
