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
 * CCID XfrBlock implementation.
 * 
 * @author Frank Cornelis
 * 
 */
public class XfrBlock extends AbstractCCIDCommand {

	public XfrBlock(UsbEndpoint bulkInUsbEndpoint,
			UsbEndpoint bulkOutUsbEndpoint,
			UsbDeviceConnection usbDeviceConnection) {
		super(bulkInUsbEndpoint, bulkOutUsbEndpoint, usbDeviceConnection);
	}

	byte[] sendApdu(byte[] apdu) {
		byte[] xfrBlock = new byte[] { 0x6f, // bMessageType
				(byte) apdu.length, 0, 0, 0, // dwLength
				0, // bSlot
				0, // bSeq
				0, // bBWI
				0, 0 // wLevelParameter: 0 = short APDU
		};
		byte[] buffer = new byte[xfrBlock.length + apdu.length];
		System.arraycopy(xfrBlock, 0, buffer, 0, xfrBlock.length);
		System.arraycopy(apdu, 0, buffer, xfrBlock.length, apdu.length);
		int outResult = this.usbDeviceConnection.bulkTransfer(
				this.bulkOutUsbEndpoint, buffer, buffer.length, 2000);
		if (outResult < 0) {
			return null;
		}
		Log.d(LOG_TAG, "out result: " + outResult);
		byte[] dataBlock = new byte[512]; // 268
		int inResult = this.usbDeviceConnection.bulkTransfer(
				this.bulkInUsbEndpoint, dataBlock, dataBlock.length, 1000);
		Log.d(LOG_TAG, "in result: " + inResult);
		if (inResult < 0) {
			return null;
		}
		byte bMessageType = dataBlock[0];
		if (0x80 != bMessageType) {
			Log.d(LOG_TAG, "no data received");
		}
		byte[] abData = new byte[inResult - 10];
		System.arraycopy(dataBlock, 10, abData, 0, abData.length);
		return abData;
	}
}
