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


import java.util.List;

import org.lsc.plugins.connectors.dictao.configuration.generated.DvsProvisioningServiceSettings;
import org.lsc.plugins.connectors.dictao.dacs.DacsProvisioningProxyImpl.GlobalStatus;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.AddUserListRequest;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.AddUserListResponse;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.AddUserListResult;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.DeleteUserInformationListRequest;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.DeleteUserInformationListResponse;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.DeleteUserInformationListResult;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.GetUserListRequest;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.GetUserListResponse;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.GetUserListResult;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.GetUserListResult.UserList;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.UserInfoType;


/**
 * This class is used by the DACS LSC service to call the various DACS provisioning web service
 * 
 * @author Sebastien Bahloul &lt;sbahloul@dictao.com&lt;
 */
public class DvsFakeProvisioning implements DvsProvisioningProxy {

//	private DacsProvisioningServiceSettings dacsService;
	private DvsFakeUsersRepository fakeRepository;
	
	public DvsFakeProvisioning(DvsProvisioningServiceSettings dvsService, DvsFakeUsersRepository fakeRepository) {
//		this.dacsService = dacsService;
		this.fakeRepository = fakeRepository;
	}

	@Override
	public AddUserListResponse addUserList(
			AddUserListRequest addUserListRequest) {
		for(UserInfoType rUser: addUserListRequest.getUserInfoList().getUserInfo()) {
			UserInfoType eUser = fakeRepository.getUserByClientRef(rUser.getClientRef());
			if(eUser != null) {
				// Need to merge
			} else {
				fakeRepository.addUser(rUser);
			}
		}
		
		AddUserListResponse response = new AddUserListResponse();
		AddUserListResult listResult = new AddUserListResult();
		listResult.setOpStatus(OpStatus.SUCCESS.getIntValue());
		listResult.setGlobalStatus(GlobalStatus.OK.getIntValue());
		listResult.setRequestId(addUserListRequest.getHeader().getRequestId());
		response.setAddUserListResult(listResult);
		return response;
	}

	@Override
	public GetUserListResponse getUserList(GetUserListRequest request) {
		List<UserInfoType> users = fakeRepository.getUsers();
		GetUserListResponse response = new GetUserListResponse();
		GetUserListResult listResult = new GetUserListResult();
		listResult.setOpStatus(OpStatus.SUCCESS.getIntValue());
		listResult.setGlobalStatus(GlobalStatus.OK.getIntValue());
		listResult.setRequestId(request.getHeader().getRequestId());
		listResult.setNbResults(users.size());
		listResult.setNbResultsMax(users.size());
		listResult.setNbUsers(users.size());
		UserList userList = new UserList();
		userList.getUserInfo().addAll(users);
		listResult.setUserList(userList);
		response.setGetUserListResult(listResult);
		return response;
	}

	@Override
	public DeleteUserInformationListResponse deleteUserInformationListRequest(DeleteUserInformationListRequest request) {
		for(DeleteUserInformationListRequest.UserInformationList.UserInformation userToDelete: request.getUserInformationList().getUserInformation()) {
			fakeRepository.removeByUserAlias(userToDelete.getUserAlias());
		}
		
		DeleteUserInformationListResponse response = new DeleteUserInformationListResponse();
		DeleteUserInformationListResult listResult = new DeleteUserInformationListResult();
		listResult.setOpStatus(OpStatus.SUCCESS.getIntValue());
		listResult.setGlobalStatus(GlobalStatus.OK.getIntValue());
		listResult.setRequestId(request.getHeader().getRequestId());
		response.setDeleteUserInformationListResult(listResult);
		return response;
	}
}