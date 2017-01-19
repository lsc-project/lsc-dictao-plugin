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
package org.lsc.plugins.connectors.dictao.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Factory to create SSLv3 with client and server certificate based
 * authentication
 */
public class MutualSSLSocketFactory extends SSLSocketFactory {

//	private static final Logger LOGGER = LoggerFactory.getLogger(MutualSSLSocketFactory.class);

	static {
		if (System.getProperties().containsKey("INSECURE_SSL_CONNECTION")) {
			javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {

				@Override
				public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
					return true;
				}
			});
		}
	}

	public static class SSLParameters {

		private final InputStream keystore;
		private final String keystoreType;
		private final char[] keystorePassword;

		public SSLParameters(InputStream keystore, String keystoreType, String keystorePassword) {
			super();
			this.keystore = keystore;
			this.keystoreType = keystoreType;
			this.keystorePassword = keystorePassword.toCharArray();
		}

		@Override
		public String toString() {
			return "SSLPaarameters [keystore=" + keystore.toString() + ", keystore_type=" + keystoreType + "]";
		}
	}

	private String proxyHost = null;
	private int proxyPort = -1;
	private SSLSocketFactory factory = null;
	private SSLContext ctx = null;

	public MutualSSLSocketFactory() {
	}

	public MutualSSLSocketFactory(SSLParameters client, SSLParameters server) throws NoSuchAlgorithmException,
			KeyStoreException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException,
			KeyManagementException {
		ctx = SSLContext.getInstance("TLS");

		KeyManagerFactory kmf = null;

		try {
			kmf = KeyManagerFactory.getInstance("SunX509");
		} catch (NoSuchAlgorithmException noSuchAlgorithmException) {
			kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		}
		if (client != null) {
			KeyStore ks = KeyStore.getInstance(client.keystoreType);
			ks.load(client.keystore, client.keystorePassword);
			kmf.init(ks, client.keystorePassword);
		} else {
			// set client key store to null
			kmf.init(null, null);
		}

		KeyStore ts = KeyStore.getInstance(server.keystoreType);
		ts.load(server.keystore, server.keystorePassword);

		TrustManagerFactory tmf;
		try {
			tmf = TrustManagerFactory.getInstance("SunX509");
		} catch (NoSuchAlgorithmException noSuchAlgorithmException) {
			tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		}
		tmf.init(ts);

		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextInt();
		ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), secureRandom);

		factory = ctx.getSocketFactory();
	}

	public void setProxy(String proxyHost, int proxyPort) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
	}

	public SSLSocketFactory getSSLSocketFactory() {
		return this.factory;
	}

	public SSLContext getSSLContext() {
		return this.ctx;
	}
	
	public static SocketFactory getDefault() {
		return new MutualSSLSocketFactory();
	}

	@Override
	public Socket createSocket() throws IOException {
		return factory.createSocket();
	}
	
	@Override
	public Socket createSocket(InetAddress remoteHost, int remotePort, InetAddress localHost, int localPort)
			throws IOException {
		SSLSocket sslSocket;

		if (proxyHost != null) {
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)); // This
																									// returns
																									// the
																									// proxy
																									// object
			sslSocket = (SSLSocket) factory.createSocket(new Socket(proxy), remoteHost.getHostAddress(), remotePort,
					true);
		} else {
			sslSocket = (SSLSocket) factory.createSocket(remoteHost, remotePort, localHost, localPort);
		}
		if (sslSocket != null) {
			sslSocket.startHandshake();
		}
		return sslSocket;
	}

	@Override
	public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
		return factory.createSocket(s, host, port, autoClose);
	}

	@Override
	public Socket createSocket(String remoteHost, int remotePort) throws IOException, UnknownHostException {
		return createSocket(Inet4Address.getByName(remoteHost), remotePort);
	}

	@Override
	public Socket createSocket(String remoteHost, int remotePort, InetAddress localHost, int localPort)
			throws IOException, UnknownHostException {
		return createSocket(Inet4Address.getByName(remoteHost), remotePort, localHost, localPort);
	}

	@Override
	public Socket createSocket(InetAddress remoteHost, int remotePort) throws IOException {
		return createSocket(remoteHost, remotePort, null, -1);
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return factory.getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return factory.getSupportedCipherSuites();
	}
}