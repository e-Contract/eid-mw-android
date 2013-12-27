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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Datastructure for eID address data.
 * 
 * @author Frank Cornelis
 * 
 */
public class Address implements Parcelable {

	public String streetAndNumber;

	public String zip;

	public String municipality;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.streetAndNumber);
		dest.writeString(this.zip);
		dest.writeString(this.municipality);
	}

	public static final Parcelable.Creator<Address> CREATOR = new Creator<Address>() {

		@Override
		public Address createFromParcel(Parcel source) {
			Address address = new Address();
			address.streetAndNumber = source.readString();
			address.zip = source.readString();
			address.municipality = source.readString();
			return address;
		}

		@Override
		public Address[] newArray(int size) {
			return new Address[size];
		}
	};
}
