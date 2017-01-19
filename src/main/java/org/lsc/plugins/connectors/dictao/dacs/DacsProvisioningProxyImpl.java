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
package org.lsc.plugins.connectors.dictao.dacs;

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
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.lsc.Configuration;
import org.lsc.exception.LscConfigurationException;
import org.lsc.exception.LscServiceConfigurationException;
import org.lsc.plugins.connectors.dictao.configuration.generated.DacsProvisioningConnectionSettings;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddIdentitiesToUserRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddIdentitiesToUserResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddOrUpdateUserInfoListRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddOrUpdateUserInfoListResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddUserListRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddUserListResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.DACSProvisioningFrontEnd;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.DACSProvisioningFrontEnd_Service;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.GetUserInfoRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.GetUserInfoResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.GetUserListRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.GetUserListResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.RemoveAuthPluginInfoFromUserRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.RemoveAuthPluginInfoFromUserResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.RemoveUserListRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.RemoveUserListResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.UpdateUserInfoListRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.UpdateUserInfoListResponse;
import org.lsc.plugins.connectors.dictao.utils.MutualSSLSocketFactory;

/**
 * This class is used by the DACS LSC service to call the various DACS provisioning web service
 * 
 * @author Sebastien Bahloul &lt;sbahloul@dictao.com&lt;
 */
public class DacsProvisioningProxyImpl implements DacsProvisioningProxy {

    /** web service port to call services */
    private DACSProvisioningFrontEnd port;


    /**
     * @param baseurl
     *            The web service URL
     * @param dacsConn
     *            the various connection settings
     * @throws LscConfigurationException
     *             thrown if something goes wrong while opening the client and/or server keystores
     */
    public DacsProvisioningProxyImpl(String baseurl, DacsProvisioningConnectionSettings dacsConn)
        throws LscServiceConfigurationException {
    	
        DACSProvisioningFrontEnd_Service service = new DACSProvisioningFrontEnd_Service(DACSProvisioningFrontEnd_Service.class.getClassLoader().getResource("resources/wsdl/DACSProvisioning.wsdl"), new QName("http://www.dictao.com/DACS/FrontEnd/Provisioning", "DACSProvisioningFrontEnd"));

        // get the port
        port = service.getDACSProvisioningFrontEndSOAP();

        try {
            setUpWSPort(new File(Configuration.getConfigurationDirectory(), dacsConn.getClientStore().getFile()).toURI().toURL(), dacsConn.getClientStore().getType(), dacsConn.getClientStore().getPassword(),
            		new File(Configuration.getConfigurationDirectory(), dacsConn.getTrustStore().getFile()).toURI().toURL(), dacsConn.getTrustStore().getType(), dacsConn.getTrustStore().getPassword(),
            		(BindingProvider) port, baseurl, 
            		(dacsConn.getProxySettings() != null ? dacsConn.getProxySettings().getHostname() : null), (dacsConn.getProxySettings() != null ? dacsConn.getProxySettings().getPort() : -1));
        } catch (IOException e) {
        	throw new LscServiceConfigurationException("Unable to initialize DACS provisioning proxy ! (" + e.toString() + ")", e);
        } catch (UnrecoverableKeyException e) {
        	throw new LscServiceConfigurationException("Unable to initialize DACS provisioning proxy ! (" + e.toString() + ")", e);
		} catch (KeyManagementException e) {
        	throw new LscServiceConfigurationException("Unable to initialize DACS provisioning proxy ! (" + e.toString() + ")", e);
		} catch (NoSuchAlgorithmException e) {
        	throw new LscServiceConfigurationException("Unable to initialize DACS provisioning proxy ! (" + e.toString() + ")", e);
		} catch (KeyStoreException e) {
        	throw new LscServiceConfigurationException("Unable to initialize DACS provisioning proxy ! (" + e.toString() + ")", e);
		} catch (CertificateException e) {
        	throw new LscServiceConfigurationException("Unable to initialize DACS provisioning proxy ! (" + e.toString() + ")", e);
		} catch (GeneralSecurityException e) {
        	throw new LscServiceConfigurationException("Unable to initialize DACS provisioning proxy ! (" + e.toString() + ")", e);
		}
    }


    public static void setUpWSPort(URL keystore, String keystoreType, String keystorePasswd,
            URL truststore, String truststoreType, String truststorePasswd, 
            BindingProvider port, String baseurl, String proxyHostname, int proxyPort) throws FileNotFoundException, IOException, GeneralSecurityException {

        MutualSSLSocketFactory sslFactory = new MutualSSLSocketFactory(new MutualSSLSocketFactory.SSLParameters(new FileInputStream(keystore.getFile()), keystoreType, keystorePasswd),
                new MutualSSLSocketFactory.SSLParameters(new FileInputStream(truststore.getFile()), truststoreType, truststorePasswd));
        
        Map<String, Object> context = ((BindingProvider) port).getRequestContext();
        
        context.put("schema-validation-enabled", "true");
        if (baseurl != null) {
            context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseurl);
        }

//        if (proxyHostname != null && proxyHostname.length() > 0 && proxyPort > 0) {
//            sslFactory.setProxy(proxyHostname, proxyPort);
//        }
//            if (dacsConn.getProxySettings().getUsername() != null) {
//                String encoded = LdifLayout.toBase64(dacsConn.getProxySettings().getUsername() + ":" + dacsConn.getProxySettings().getPassword());
//                ((WSBindingProvider) port).setOutboundHeaders(new Header("Proxy-Authorization", "Basic " + encoded));
//            }

//        context.put(JAXWSProperties.HOSTNAME_VERIFIER, new HostnameVerifier() {
//            
//            @Override
//            public boolean verify(String arg0, SSLSession arg1) {
//                return true;
//            }
//        });

        // use specific keyStore and trustStore, and not the default parameters from JVM.
        context.put("com.sun.xml.internal.ws.transport.https.client.SSLSocketFactory", sslFactory);
//        context.put(JAXWSProperties.SSL_SOCKET_FACTORY, sslFactory);
    }

    
	/**
     * Call the corresponding webservice method
     * 
     * @param addOrUpdateUserListRequest
     *            the parameter
     * @return the webservice result
     */
    public AddOrUpdateUserInfoListResponse addOrUpdateUserInfoList(AddOrUpdateUserInfoListRequest addOrUpdateUserListRequest) {
        return port.addOrUpdateUserInfoList(addOrUpdateUserListRequest);
    }
    
    public AddUserListResponse addUserInfoList(AddUserListRequest addUserListRequest) {
    	return port.addUserList(addUserListRequest);
    }

    public AddIdentitiesToUserResponse addIdentitiesToUser(AddIdentitiesToUserRequest request) {
        return port.addIdentitiesToUser(request);
    }

    public RemoveAuthPluginInfoFromUserResponse removeAuthPluginInfoFromUser(RemoveAuthPluginInfoFromUserRequest removeUserRequest) {
        return port.removeAuthPluginInfoFromUser(removeUserRequest);
    }
    
    public RemoveUserListResponse remove(RemoveUserListRequest removeUserRequest) {
        return port.removeUserList(removeUserRequest);
    }
    
    public UpdateUserInfoListResponse updateUserInfoList(UpdateUserInfoListRequest updateUserInfoListRequest) {
    	return port.updateUserInfoList(updateUserInfoListRequest);
    }

    
    public GetUserInfoResponse getUserInfo(GetUserInfoRequest getUserInfoRequest) {
        return port.getUserInfo(getUserInfoRequest);
    }
    
    public GetUserListResponse getUsersList(GetUserListRequest getUserListRequest) {
        return port.getUserList(getUserListRequest);
    }

    /**
     * A class taken from the DACS code that details the various GlobalStatus returned by DACS provisioning web service
     */
    public enum GlobalStatus {

        OK(0, "GLOBALSTATUS_OK"),

        INTERNAL_ERROR(11, "INTERNAL ERROR"),

        /** The connection has not used a client certificate */
        MISSING_CERTIF_CLIENT(12, "MISSING CLIENT CERTIF"),
        // Error while get ending certificate
        READ_ERROR_CERTIF_CLIENT(13, "ENCODING CERTIF ERROR"),
        // Error while get ending certificate
        BAD_CERTIF_CLIENT(14, "BAD CERTIF CLIENT"),
        // Error while get ending certificate
        WS_REQUEST_ERROR(15, "WEB SERVICE REQUEST ERROR");

        GlobalStatus(int globalStatusInt, String globalMessage) {
            _globalStatusInt = globalStatusInt;
            _globalStatusString = globalMessage;
        }

        /**
         * Get the integer value which is associate to the current enumeration
         * 
         * @return integer associate with the current enumeration
         */
        public int getIntValue() {
            return _globalStatusInt;
        }

        /**
         * Get the string value which is associate to the current enumeration
         * 
         * @return string associate with the current enumeration
         */
        public String getMessageValue() {
            return _globalStatusString;
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
                buf.append("globalStatusInt=");
                buf.append(getIntValue());
                buf.append(SEPARATEUR);
                buf.append("globalStatusComment=");
                buf.append(getMessageValue());
                buf.append(FIN);
            }
            catch (Exception ex) {
                buf = new StringBuilder(super.toString());
            }
            return buf.toString();
        }

        /** Attribute which contains the integer part of the enumeration */
        private final int    _globalStatusInt;

        /** Attribute which contains the string part of the enumeration */
        private final String _globalStatusString;

    }

    /**
     * A class taken from the DACS code that details the various OpStatus returned by DACS provisioning web service
     */
    public enum OpStatus {

        OK(0, "STATUS OK"),

        DATABASE_ERROR(32, "DATABASE ERROR"),

        INTERNAL_ERROR(99, "INTERNAL ERROR"),

        /** Unauthorized client certificate */
        UNAUTHORIZED_WS_CLIENT(101, "UNAUTHORIZED WEB SERVICE CLIENT"),

        MALFORMED_REQUEST_ERROR(102, "MALFORMED REQUEST ERROR");

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
            }
            catch (Exception ex) {
                buf = new StringBuilder(super.toString());
            }
            return buf.toString();
        }

        /** Attribute which contains the integer part of the enumeration */
        private final int    _opStatusInt;

        /** Attribute which contains the string part of the enumeration */
        private final String _opStatusString;

        public static OpStatus valueOf(int opStatus) {
            if (opStatus == OK._opStatusInt) {
                return OK;
            }
            else if (opStatus == DATABASE_ERROR._opStatusInt) {
                return DATABASE_ERROR;
            }
            else if (opStatus == MALFORMED_REQUEST_ERROR._opStatusInt) {
                return MALFORMED_REQUEST_ERROR;
            }
            else if (opStatus == UNAUTHORIZED_WS_CLIENT._opStatusInt) {
                return UNAUTHORIZED_WS_CLIENT;
            }
            else if (opStatus == INTERNAL_ERROR._opStatusInt) {
                return INTERNAL_ERROR;
            }
            else {
                return INTERNAL_ERROR;
            }
        }
    }
}