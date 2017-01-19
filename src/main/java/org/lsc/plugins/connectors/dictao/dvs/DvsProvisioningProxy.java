/*
 ****************************************************************************
 * Ldap Synchronization Connector provides tools to synchronize
 * electronic identities from a list of data sources including
 * any database with a JDBC connector, another LDAP directory,
 * flat files...
 *
 *                  ==LICENSE NOTICE==
 * 
 * Copyright (c) 2008 - 2014 LSC Project 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of the LSC Project nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *                  ==LICENSE NOTICE==
 *
 *               (c) 2008 - 2014 LSC Project
 *         Sebastien Bahloul <seb@lsc-project.org>
 *         Thomas Chemineau <thomas@lsc-project.org>
 *         Jonathan Clarke <jon@lsc-project.org>
 *         Remy-Christophe Schermesser <rcs@lsc-project.org>
 ****************************************************************************
 */
package org.lsc.plugins.connectors.dictao.dvs;

import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.AddUserListRequest;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.AddUserListResponse;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.DeleteUserInformationListRequest;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.DeleteUserInformationListResponse;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.GetUserListRequest;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.GetUserListResponse;

/**
 * This class is used by the DACS LSC service to call the various DVS
 * provisioning web service
 * 
 * @author Sebastien Bahloul &lt;sbahloul@dictao.com&lt;
 */
public interface DvsProvisioningProxy {

	public AddUserListResponse addUserList(AddUserListRequest addUserListRequest);

	public GetUserListResponse getUserList(GetUserListRequest request);

	public DeleteUserInformationListResponse deleteUserInformationListRequest(DeleteUserInformationListRequest request);

	/**
	 * A class taken from the DVS code that details the various OpStatus
	 * returned by DVS provisioning web service
	 */
	public enum OpStatus {

		SUCCESS(0, "Sucess"), INVALID(11, "Invalid signature structure, certificate or invalid token"), FIELD_LENGTH_EXCEEDED(
				12, "Business fields exceeding authorized value specified in trust policy "), REQUEST_LENGTH_EXCEEDED(
				13, "Request size exceeding authorized value specified in trust policy"), INCORRECT_FORMAT(14,
				"Incorrection request format"), MISSING_DATA(15, "Missing data in request"), UNAUTHORIZED_APPLICATION(
				21, "Unauthorized application"), UNKNOWN_TRANSACTION(22, "Unknown transaction"), UNAUTHORIZED_REQUEST_TYPE(
				23, "Unauthorized request"), DVS_UNAVAILABLE(31, "DVS service unavailable"), DATABASE_ERROR(32,
				"Database access error"), MANAGEMENT_SERVICE_UNAVAILABLE(33, "Management service unavailable"), PROVISIONING_INTERNAL_ERROR(
				97, "Provisioning service internal error"), DVS_INTERNAL_ERROR(99, "Internal DVS error"), INVALID_CERTIFICATE(
				8193, "Invalid certificate"), INVALID_USER(8194, "Invalid user"), INVALID_GROUPID(8196, "Invalid group"), UNAUTHORIZED_ACCESS(
				8200, "Unauthorized access to the management service"), INVALID_CONFIGURATION(8208,
				"Invalid management service configuration"), COMPUTING_SOAP_ERROR(8224,
				"Treatment service connection error"), CLRSYNC_SOAP_ERROR(8240,
				"CRL synchronization service connection error"), COMPUTING_DATABASE_ERROR(8256,
				"Database connection error"), COMPUTING_COMMUNICATION_ERROR(8320, "Management service connection error"), LICENSE_ERROR(
				8352, "License error"), MANAGEMENT_INTERNAL_ERROR(10240, "Internal error in management service");

		/**
		 * Enumeration constructor
		 */
		OpStatus(int opStatusInt, String opStatusMessage) {
			_opStatusInt = opStatusInt;
			_opStatusString = opStatusMessage;
		}

		/**
		 * Get the integer value which is associate to the current enumeration
		 * 
		 * @return integer associate with the current enumeration
		 */
		public int getIntValue() {
			return _opStatusInt;
		}

		/**
		 * Get the string value which is associate to the current enumeration
		 * 
		 * @return string associate with the current enumeration
		 */
		public String getMessageValue() {
			return _opStatusString;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			final String DEBUT = "{";
			final String SEPARATEUR = "|";
			final String FIN = "}";
			StringBuilder buf = new StringBuilder(super.toString());
			try {
				buf.append(DEBUT);
				buf.append("name=");
				buf.append(name());
				buf.append(SEPARATEUR);
				buf.append("opStatusInt=");
				buf.append(getIntValue());
				buf.append(SEPARATEUR);
				buf.append("opStatusComment=");
				buf.append(getMessageValue());
				buf.append(FIN);
			} catch (Exception ex) {
				buf = new StringBuilder(super.toString());
			}
			return buf.toString();
		}

		/** Attribute which contains the integer part of the enumeration */
		private final int _opStatusInt;

		/** Attribute which contains the string part of the enumeration */
		private final String _opStatusString;

		public static OpStatus valueOf(int opStatus) {
			return INCORRECT_FORMAT;
		}
	}
}
