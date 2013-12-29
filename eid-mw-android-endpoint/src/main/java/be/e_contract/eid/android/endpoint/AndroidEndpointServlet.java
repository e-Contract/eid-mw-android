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

package be.e_contract.eid.android.endpoint;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The server-side eID Middleware for Android endpoint.
 * 
 * @author Frank Cornelis
 * 
 */
public abstract class AndroidEndpointServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(AndroidEndpointServlet.class);

	@Override
	protected void doPost(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletresponse) throws ServletException,
			IOException {
		LOG.debug("doPost");
		BeIDOperation operation = getOperation();
		if (BeIDOperation.READ_IDENTITY == operation) {
			Response response = new Response();
			response.action = "identity";
			PrintWriter printWriter = httpServletresponse.getWriter();
			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();
			printWriter.write(gson.toJson(response));
		}
	}

	private static class Response {
		String action;
	}

	/**
	 * Gives back the eID operation to be performed.
	 * 
	 * @return
	 */
	protected abstract BeIDOperation getOperation();
}
