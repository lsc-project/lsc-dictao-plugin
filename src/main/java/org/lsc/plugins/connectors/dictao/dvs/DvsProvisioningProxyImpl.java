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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.lsc.Configuration;
import org.lsc.exception.LscConfigurationException;
import org.lsc.exception.LscServiceConfigurationException;
import org.lsc.plugins.connectors.dictao.configuration.generated.DvsProvisioningConnectionSettings;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.AddUserListRequest;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.AddUserListResponse;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.DVSProvisioningFrontEnd;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.DVSProvisioningFrontEnd_Service;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.DeleteUserInformationListRequest;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.DeleteUserInformationListResponse;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.GetUserListRequest;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.GetUserListResponse;
import org.lsc.plugins.connectors.dictao.utils.MutualSSLSocketFactory;

/**
 * This class is used by the DACS LSC service to call the various DVS
 * provisioning web service
 * 
 * @author Sebastien Bahloul &lt;sbahloul@dictao.com&lt;
 */
public class DvsProvisioningProxyImpl implements DvsProvisioningProxy {

	public static final String ADD_ONLY_MODE = "ADD_ONLY";
	public static final String ADD_OR_UPDATE_MODE = "ADD_OR_UPDATE";
	public static final String PASSWORD_FORMAT_PLAIN_TEXT = "PLAIN_TEXT";

	// private static Logger LOGGER =
	// LoggerFactory.getLogger(DvsProvisioningProxy.class);

	// web service port to call services
	private DVSProvisioningFrontEnd port;

	/**
	 * @param baseurl
	 *            The web service URL
	 * @param dacsConn
	 *            the various connection settings
	 * @throws LscConfigurationException
	 *             thrown if something goes wrong while opening the client
	 *             and/or server keystores
	 */
	public DvsProvisioningProxyImpl(String baseurl, DvsProvisioningConnectionSettings dvsConn)
			throws LscServiceConfigurationException {

		DVSProvisioningFrontEnd_Service service = new DVSProvisioningFrontEnd_Service(
				DVSProvisioningFrontEnd_Service.class.getClassLoader().getResource(
						"resources/dvs/wsdl/DVSProvisioning.wsdl"), new QName(
						"http://www.dictao.com/DVS/FrontEnd/Provisioning", "DVSProvisioningFrontEnd"));

        // get the port
		port = service.getDVSProvisioningFrontEndSOAP();
		

		try {
			setUpWSPort(new File(Configuration.getConfigurationDirectory(), dvsConn
					.getClientStore().getFile()).toURI().toURL(), dvsConn.getClientStore().getType(), dvsConn.getClientStore()
					.getPassword(), 
					new File(Configuration.getConfigurationDirectory(), dvsConn.getTrustStore().getFile()).toURI().toURL(), 
					dvsConn.getTrustStore().getType(), dvsConn.getTrustStore()
					.getPassword(), (BindingProvider) port, baseurl, (dvsConn.getProxySettings() != null ? dvsConn
					.getProxySettings().getHostname() : null), (dvsConn.getProxySettings() != null ? dvsConn
					.getProxySettings().getPort() : -1));

		} catch (IOException e) {
			throw new LscServiceConfigurationException("Unable to initialize DVS provisioning proxy ! (" + e.toString()
					+ ")", e);
		} catch (UnrecoverableKeyException e) {
			throw new LscServiceConfigurationException("Unable to initialize DVS provisioning proxy ! (" + e.toString()
					+ ")", e);
		} catch (KeyManagementException e) {
			throw new LscServiceConfigurationException("Unable to initialize DVS provisioning proxy ! (" + e.toString()
					+ ")", e);
		} catch (NoSuchAlgorithmException e) {
			throw new LscServiceConfigurationException("Unable to initialize DVS provisioning proxy ! (" + e.toString()
					+ ")", e);
		} catch (KeyStoreException e) {
			throw new LscServiceConfigurationException("Unable to initialize DVS provisioning proxy ! (" + e.toString()
					+ ")", e);
		} catch (CertificateException e) {
			throw new LscServiceConfigurationException("Unable to initialize DVS provisioning proxy ! (" + e.toString()
					+ ")", e);
		} catch (GeneralSecurityException e) {
			throw new LscServiceConfigurationException("Unable to initialize DVS provisioning proxy ! (" + e.toString()
					+ ")", e);
		}
	}

    public static void setUpWSPort(URL keystore, String keystoreType, String keystorePasswd,
            URL truststore, String truststoreType, String truststorePasswd, 
            BindingProvider port, String baseurl, String proxyHostname, int proxyPort) throws FileNotFoundException, IOException, GeneralSecurityException {

        MutualSSLSocketFactory sslFactory = new MutualSSLSocketFactory(new MutualSSLSocketFactory.SSLParameters(new FileInputStream(keystore.getFile()), keystoreType, keystorePasswd),
                new MutualSSLSocketFactory.SSLParameters(new FileInputStream(truststore.getFile()), truststoreType, truststorePasswd));

		((BindingProvider) port).getRequestContext().put("schema-validation-enabled", "true");

		Map<String, Object> context = ((BindingProvider) port).getRequestContext();
		if (baseurl != null) {
			context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseurl);
		}

//		if (proxyHostname != null && proxyHostname.length() > 0 && proxyPort > 0) {
//			sslFactory.setProxy(proxyHostname, proxyPort);
//		}
		// if (dvsConn.getProxySettings().getUsername() != null) {
		// String encoded =
		// LdifLayout.toBase64(dvsConn.getProxySettings().getUsername() + ":" +
		// dvsConn.getProxySettings().getPassword());
		// ((WSBindingProvider) port).setOutboundHeaders(new
		// Header("Proxy-Authorization", "Basic " + encoded));
		// }

		// use specific keyStore and trustStore, and not the default parameters
		// from JVM.
        context.put("com.sun.xml.internal.ws.transport.https.client.SSLSocketFactory", sslFactory);
//		context.put(JAXWSProperties.SSL_SOCKET_FACTORY, sslFactory);
	}

	public AddUserListResponse addUserList(AddUserListRequest addUserListRequest) {
		return port.addUserList(addUserListRequest);
	}

	public GetUserListResponse getUserList(GetUserListRequest request) {
		return port.getUserList(request);
	}

	public DeleteUserInformationListResponse deleteUserInformationListRequest(DeleteUserInformationListRequest request) {
		return port.deleteUserInformationList(request);
	}

	public static List<String> decodeGlobalStatus(int value) {
		byte c1 = new Integer(value / 256).byteValue();
		byte c2 = new Integer(value % 256).byteValue();
		List<String> errors = new ArrayList<String>();
		if ((c1 & new Integer(128).byteValue()) != 0) {
			errors.add("Provisioning error");
		}
		if ((c1 & new Integer(64).byteValue()) != 0) {
			errors.add("Management service error");
		}
		if ((c2 & new Integer(64).byteValue()) != 0) {
			errors.add("Error raised by DVSService");
		}
		if ((c2 & new Integer(32).byteValue()) != 0) {
			errors.add("Invalid parameters");
		}
		if ((c2 & new Integer(16).byteValue()) != 0) {
			errors.add("Unknown referenced object");
		}
		if ((c2 & new Integer(8).byteValue()) != 0) {
			errors.add("Mis-configured referenced object");
		}
		if ((c2 & new Integer(4).byteValue()) != 0) {
			errors.add("Invalid referenced token");
		}
		if ((c2 & new Integer(2).byteValue()) != 0) {
			errors.add("Provisioning computation failed");
		}
		if ((c2 & new Integer(1).byteValue()) != 0) {
			// RFU
		}
		return errors;
	}
}
