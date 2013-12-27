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

/**
 * CCID NotifySlotChange implementation.
 * 
 * @author Frank Cornelis
 * 
 */
public class NotifySlotChange extends AbstractCCIDCommand {

	private final UsbEndpoint intUsbEndpoint;

	public NotifySlotChange(UsbEndpoint bulkInUsbEndpoint,
			UsbEndpoint bulkOutUsbEndpoint,
			UsbDeviceConnection usbDeviceConnection, UsbEndpoint intUsbEndpoint) {
		super(bulkInUsbEndpoint, bulkOutUsbEndpoint, usbDeviceConnection);
		this.intUsbEndpoint = intUsbEndpoint;
	}

	public boolean check(boolean inserted) {
		byte[] notifySlotChange = new byte[4];
		int result = this.usbDeviceConnection.bulkTransfer(this.intUsbEndpoint,
				notifySlotChange, notifySlotChange.length, 1000);
		if (result < 0) {
			return false;
		}
		if (0x50 != notifySlotChange[0]) {
			return false;
		}
		if (inserted) {
			return (notifySlotChange[1] & 0x01) == 0x01;
		} else {
			return (notifySlotChange[1] & 0x01) != 0x01;
		}
	}
}
