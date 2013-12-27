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

import java.io.UnsupportedEncodingException;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.util.Log;

/**
 * Belgian eID specific stuff.
 * 
 * @author Frank Cornelis
 * 
 */
public class BeID {

	public static final byte[] IDENTITY_FILE = new byte[] { 0x3F, 0x00,
			(byte) 0xDF, 0x01, 0x40, 0x31 };

	public static final byte[] ADDRESS_FILE = new byte[] { 0x3F, 0x00,
			(byte) 0xDF, 0x01, 0x40, 0x33 };

	public static final byte[] PHOTO_FILE = new byte[] { 0x3F, 0x00,
			(byte) 0xDF, 0x01, 0x40, 0x35 };

	private final UsbEndpoint bulkInUsbEndpoint;
	private final UsbEndpoint bulkOutUsbEndpoint;
	private final UsbDeviceConnection usbDeviceConnection;

	public BeID(UsbEndpoint bulkInUsbEndpoint, UsbEndpoint bulkOutUsbEndpoint,
			UsbDeviceConnection usbDeviceConnection) {
		this.bulkInUsbEndpoint = bulkInUsbEndpoint;
		this.bulkOutUsbEndpoint = bulkOutUsbEndpoint;
		this.usbDeviceConnection = usbDeviceConnection;
	}

	/**
	 * Generic TLV parser. You pass either identity or address.
	 * 
	 * @param tlvData
	 * @param identity
	 * @param address
	 * @throws UnsupportedEncodingException
	 */
	private void parse(byte[] tlvData, Identity identity, Address address)
			throws UnsupportedEncodingException {
		int idx = 0;
		while (idx < tlvData.length - 1) {
			final byte tag = tlvData[idx];
			idx++;
			byte lengthByte = tlvData[idx];
			int length = lengthByte & 0x7f;
			while ((lengthByte & 0x80) == 0x80) {
				idx++;
				lengthByte = tlvData[idx];
				length = (length << 7) + (lengthByte & 0x7f);
			}
			idx++;
			if (0 == tag) {
				idx += length;
				continue;
			}
			byte[] value = new byte[length];
			System.arraycopy(tlvData, idx, value, 0, length);
			if (null != identity) {
				switch (tag) {
				case 7:
					identity.name = new String(value, "UTF-8");
					break;
				case 8:
					identity.firstName = new String(value, "UTF-8");
					break;
				case 12:
					identity.dateOfBirth = DateOfBirthParser.convert(value);
					break;
				}
			} else {
				switch (tag) {
				case 1:
					address.streetAndNumber = new String(value, "UTF-8");
					break;
				case 2:
					address.municipality = new String(value, "UTF-8");
					break;
				case 3:
					address.zip = new String(value, "UTF-8");
					break;
				}
			}
			idx += length;
		}
	}

	private Identity parseIdentity(byte[] tlvData) {
		Identity identity = new Identity();
		try {
			parse(tlvData, identity, null);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
		return identity;
	}

	private Address parseAddress(byte[] tlvData) {
		Address address = new Address();
		try {
			parse(tlvData, null, address);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
		return address;
	}

	/**
	 * Reads out the eID identity file from the eID card.
	 * 
	 * @return can be <code>null</code>
	 */
	public Identity getIdentity() {
		SelectFile selectFile = new SelectFile(this.bulkInUsbEndpoint,
				this.bulkOutUsbEndpoint, this.usbDeviceConnection);
		boolean selectResult = selectFile.selectFile(BeID.IDENTITY_FILE);
		if (false == selectResult) {
			Log.e("BeID", "select error");
			return null;
		}
		ReadBinary readBinary = new ReadBinary(this.bulkInUsbEndpoint,
				this.bulkOutUsbEndpoint, this.usbDeviceConnection);
		byte[] identityData = readBinary.readBinary();
		if (null == identityData) {
			return null;
		}
		return parseIdentity(identityData);
	}

	/**
	 * Reads out the eID address file from the eID card.
	 * 
	 * @return can be <code>null</code>
	 */
	public Address getAddress() {
		SelectFile selectFile = new SelectFile(this.bulkInUsbEndpoint,
				this.bulkOutUsbEndpoint, this.usbDeviceConnection);
		boolean selectResult = selectFile.selectFile(BeID.ADDRESS_FILE);
		if (false == selectResult) {
			Log.e("BeID", "select error");
			return null;
		}
		ReadBinary readBinary = new ReadBinary(this.bulkInUsbEndpoint,
				this.bulkOutUsbEndpoint, this.usbDeviceConnection);
		byte[] addressData = readBinary.readBinary();
		if (null == addressData) {
			return null;
		}
		return parseAddress(addressData);
	}

	public byte[] getPhoto() {
		Log.d("BeID", "getPhoto");
		SelectFile selectFile = new SelectFile(this.bulkInUsbEndpoint,
				this.bulkOutUsbEndpoint, this.usbDeviceConnection);
		boolean selectResult = selectFile.selectFile(BeID.PHOTO_FILE);
		if (false == selectResult) {
			Log.e("BeID", "select error");
			return null;
		}
		ReadBinary readBinary = new ReadBinary(this.bulkInUsbEndpoint,
				this.bulkOutUsbEndpoint, this.usbDeviceConnection);
		byte[] photoData = readBinary.readBinary();
		return photoData;
	}
}
