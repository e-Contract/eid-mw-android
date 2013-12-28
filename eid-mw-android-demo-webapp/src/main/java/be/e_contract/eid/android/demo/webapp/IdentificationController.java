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

package be.e_contract.eid.android.demo.webapp;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Named("eIDIdentification")
public class IdentificationController {

	private static final Log LOG = LogFactory
			.getLog(IdentificationController.class);

	public String getUrl() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		String host = externalContext.getRequestServerName();
		String contextPath = externalContext.getRequestContextPath();
		HttpSession httpSession = (HttpSession) externalContext
				.getSession(true);
		String sessionId = httpSession.getId();
		String url = "beid:http://" + host + contextPath
				+ "/identification?jsessionid=" + sessionId;
		LOG.debug("URL: " + url);
		return url;
	}
}
