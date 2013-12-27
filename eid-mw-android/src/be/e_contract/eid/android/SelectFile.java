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

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.util.Log;

/**
 * Select File APDU implementation.
 * 
 * @author Frank Cornelis
 * 
 */
public class SelectFile extends XfrBlock {

	public SelectFile(UsbEndpoint bulkInUsbEndpoint,
			UsbEndpoint bulkOutUsbEndpoint,
			UsbDeviceConnection usbDeviceConnection) {
		super(bulkInUsbEndpoint, bulkOutUsbEndpoint, usbDeviceConnection);
	}

	public boolean selectFile(byte[] fileIdentifier) {
		byte[] selectFileApdu = new byte[] { 0x00, // CLA
				(byte) 0xa4, // INS
				0x08, // P1 = direction selection by DF name
				0x0c, // P2 =
				(byte) fileIdentifier.length, // Lc
		};
		byte[] buffer = new byte[selectFileApdu.length + fileIdentifier.length];
		System.arraycopy(selectFileApdu, 0, buffer, 0, selectFileApdu.length);
		System.arraycopy(fileIdentifier, 0, buffer, selectFileApdu.length,
				fileIdentifier.length);
		byte[] response = super.sendApdu(buffer);
		if (null == response) {
			return false;
		}
		if (response.length < 2) {
			return false;
		}
		byte sw1 = response[0];
		byte sw2 = response[1];
		Log.d(LOG_TAG, "sw1: " + Integer.toHexString(sw1 & 0xff));
		Log.d(LOG_TAG, "sw2: " + Integer.toHexString(sw2 & 0xff));
		if (sw1 == (byte) 0x90 && sw2 == 0x00) {
			return true;
		}
		return false;
	}
}
