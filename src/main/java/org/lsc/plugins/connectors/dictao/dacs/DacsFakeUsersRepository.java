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

 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the LSC Project nor the names of its
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
 */package org.lsc.plugins.connectors.dictao.dacs;

import java.util.ArrayList;
import java.util.List;

import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.UserInfoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DacsFakeUsersRepository {
	
	private static Logger LOGGER = LoggerFactory.getLogger(DacsFakeUsersRepository.class);

	List<UserInfoType> users;
	
	public DacsFakeUsersRepository() {
		users = new ArrayList<UserInfoType>();
	}
	
	public List<UserInfoType> getUsers() {
		return users;
	}

	public UserInfoType getUserByClientRef(String clientRef) {
		for(UserInfoType user : users) {
			if(user.getClientRef().equals(clientRef)) {
				return user;
			}
		}
		return null;
	}

	public UserInfoType getUserByAlias(String userAlias) {
		for(UserInfoType user : users) {
			if(user.getUserAliasList().getUserAlias().contains(userAlias)) {
				return user;
			}
		}
		return null;
	}

	public void removeByUserAlias(String userAlias) {
		for(UserInfoType user : users) {
			if(user.getClientRef().equals(userAlias) || (user.getUserAliasList() != null && user.getUserAliasList().getUserAlias().contains(userAlias))) {
				users.remove(user);
				break;
			}
		}
	}

	public void addUser(UserInfoType user) {
		if(user != null) {
			users.add(user);
		} else {
			LOGGER.warn("Trying to add a null user !");
		}
	}
}
