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
 * CCID IccPowerOn implementation.
 * 
 * @author Frank Cornelis
 * 
 */
public class IccPowerOn extends AbstractCCIDCommand {

	public IccPowerOn(UsbEndpoint bulkInUsbEndpoint,
			UsbEndpoint bulkOutUsbEndpoint,
			UsbDeviceConnection usbDeviceConnection) {
		super(bulkInUsbEndpoint, bulkOutUsbEndpoint, usbDeviceConnection);
	}

	public byte[] getATR() {
		byte[] iccPowerOn = new byte[] { 0x62, // bMessageType
				0x00, 0x00, 0x00, 0x00, // dwLength
				0x00, // bSlot
				0x00, // bSeq
				0x00, // bPowerSelect =
						// Automatic Voltage
						// Selection
				0x00, 0x00 // abRFU
		};
		int iccPowerOnResult = this.usbDeviceConnection.bulkTransfer(
				this.bulkOutUsbEndpoint, iccPowerOn, iccPowerOn.length, 2000);
		Log.d(LOG_TAG, "power on: " + iccPowerOnResult);
		if (iccPowerOnResult < 0) {
			return null;
		}
		// RDR_to_PC_DataBlock
		byte[] atrMessage = new byte[255];
		int inResult = this.usbDeviceConnection.bulkTransfer(
				this.bulkInUsbEndpoint, atrMessage, atrMessage.length, 2000);
		Log.d(LOG_TAG, "ATR result: " + inResult);
		if (inResult < 0) {
			return null;
		}
		if ((atrMessage[0] & 0xff) != 0x80) { // bMessageType
			return null;
		}
		if (atrMessage[7] != 0x00) { // status
			return null;
		}
		byte[] atr = new byte[inResult];
		System.arraycopy(atrMessage, 10, atr, 0, inResult - 10);
		return atr;
	}
}
