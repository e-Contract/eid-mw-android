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
 * CCID GetSlotStatus implementation.
 * 
 * @author Frank Cornelis
 * 
 */
public class GetSlotStatus extends AbstractCCIDCommand {

	public GetSlotStatus(UsbEndpoint bulkInUsbEndpoint,
			UsbEndpoint bulkOutUsbEndpoint,
			UsbDeviceConnection usbDeviceConnection) {
		super(bulkInUsbEndpoint, bulkOutUsbEndpoint, usbDeviceConnection);
	}

	public boolean check(int slot, boolean inserted) {
		byte[] getSlotStatus = new byte[] { 0x65, // bMessageType
				0x00, 0x00, 0x00, 0x00, // dwLength
				(byte) slot, // bSlot
				0x00, // bSeq
				0x00, 0x00, 0x00 // abRFU
		};
		int outResult = this.usbDeviceConnection.bulkTransfer(
				this.bulkOutUsbEndpoint, getSlotStatus, getSlotStatus.length,
				2000);
		Log.d(LOG_TAG, "get slot status: " + outResult);
		if (outResult != 10) {
			return false;
		}
		byte[] slotStatus = new byte[10];
		int inResult = this.usbDeviceConnection.bulkTransfer(
				this.bulkInUsbEndpoint, slotStatus, slotStatus.length, 1000);
		if (inResult <= 0) {
			return false;
		}
		byte bStatus = slotStatus[7];
		Log.d(LOG_TAG, "bStatus: " + bStatus);
		if (inserted) {
			return bStatus == 0;
		} else {
			return bStatus != 0;
		}
	}
}
