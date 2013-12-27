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
 * BeID action constants.
 * 
 * @author Frank Cornelis
 * 
 */
public class BeIDServiceConstants {

	/**
	 * Next eID action is reading out the identity file.
	 */
	public static final int READ_IDENTITY = 1;

	/**
	 * Next eID action is reading out the address file.
	 */
	public static final int READ_ADDRESS = 2;

	/**
	 * Next eID action is reading out the photo file.
	 */
	public static final int READ_PHOTO = 4;
}
