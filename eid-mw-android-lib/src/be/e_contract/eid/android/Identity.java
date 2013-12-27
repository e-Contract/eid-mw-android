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

import java.util.GregorianCalendar;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Datastructure for eID identity data.
 * 
 * @author Frank Cornelis
 * 
 */
public class Identity implements Parcelable {

	public String name;

	public String firstName;

	public GregorianCalendar dateOfBirth;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.name);
		dest.writeString(this.firstName);
		dest.writeInt(this.dateOfBirth.get(GregorianCalendar.YEAR));
		dest.writeInt(this.dateOfBirth.get(GregorianCalendar.MONTH));
		dest.writeInt(this.dateOfBirth.get(GregorianCalendar.DAY_OF_MONTH));
	}

	public static final Parcelable.Creator<Identity> CREATOR = new Creator<Identity>() {

		@Override
		public Identity[] newArray(int size) {
			return new Identity[size];
		}

		@Override
		public Identity createFromParcel(Parcel source) {
			Identity identity = new Identity();
			identity.name = source.readString();
			identity.firstName = source.readString();
			int year = source.readInt();
			int month = source.readInt();
			int day = source.readInt();
			identity.dateOfBirth = new GregorianCalendar(year, month, day);
			return identity;
		}
	};
}
