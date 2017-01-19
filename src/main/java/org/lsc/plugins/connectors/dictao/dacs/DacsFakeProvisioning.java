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

import java.util.List;

import org.lsc.plugins.connectors.dictao.configuration.generated.DacsProvisioningServiceSettings;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddIdentitiesToUserRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddIdentitiesToUserResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddOrUpdateUserInfoListRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddOrUpdateUserInfoListResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddUserListRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddUserListResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AttributeType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.CommandResponseType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.GetUserInfoRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.GetUserInfoResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.GetUserListRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.GetUserListResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.IdentityInformationListType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.IdentityInformationType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.IdentityLoginActionType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.IdentityLoginListType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.RemoveAuthPluginInfoFromUserRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.RemoveAuthPluginInfoFromUserResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.RemoveUserListRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.RemoveUserListResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.UserInfoListType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.UserInfoToUpdateType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.UserInfoType;


/**
 * This class is used by the DACS LSC service to call the various DACS provisioning web service
 * 
 * @author Sebastien Bahloul &lt;sbahloul@dictao.com&lt;
 */
public class DacsFakeProvisioning implements DacsProvisioningProxy {

//	private DacsProvisioningServiceSettings dacsService;
	private DacsFakeUsersRepository fakeRepository;
	
	public DacsFakeProvisioning(DacsProvisioningServiceSettings dacsService, DacsFakeUsersRepository fakeRepository) {
//		this.dacsService = dacsService;
		this.fakeRepository = fakeRepository;
	}

	@Override
	public AddOrUpdateUserInfoListResponse addOrUpdateUserInfoList(
			AddOrUpdateUserInfoListRequest addOrUpdateUserListRequest) {
		for(UserInfoToUpdateType user : addOrUpdateUserListRequest.getUserInfoToUpdateList().getUserInfoToUpdate()) {
			UserInfoType eUser = fakeRepository.getUserByClientRef(user.getClientRef());
			if(eUser != null) {
				if(eUser.getAttributeList() != null) {
					for(AttributeType attr : user.getAttributeList().getAttribute())  {
						if(attr.isRemoveExisting() != null && attr.isRemoveExisting()) {
							user.getAttributeList().getAttribute().remove(attr);
							break;
						} else {  
							AttributeType eAttr = findAttributeByName(eUser.getAttributeList().getAttribute(), attr.getAttributeName());
							if(eAttr == null) {
								eUser.getAttributeList().getAttribute().add(attr);
							} else {
								if(attr.isReplaceExisting() != null && attr.isReplaceExisting()) {
									eAttr.getAttributeValue().clear();
								}
								eAttr.getAttributeValue().addAll(attr.getAttributeValue());
							}
						}
					}
				} else {
					eUser.getAttributeList().getAttribute().addAll(user.getAttributeList().getAttribute());
				}
				if(eUser.getAuthPluginInfo() != null) {
					// Ignored
					// 
//					throw new UnsupportedOperationException();
				}
				if(eUser.getCardInformationList() != null) {
					throw new UnsupportedOperationException();
				}
				if(eUser.getIdentityInformationList() != null) {
					user.getIdentityInformationList().getIdentityInformation().clear();
					for(IdentityInformationType ii: user.getIdentityInformationList().getIdentityInformation()) {
						IdentityInformationType eIi = findIdentityInformationList(eUser.getIdentityInformationList(), ii.getApplicationName(), ii.getUserIdentity());
						if(eIi != null) {
							eIi.setActivated(ii.isActivated());
							eIi.setAuthorized(ii.isAuthorized());
							eIi.setFriendlyName(ii.getFriendlyName());
							eIi.setLastAccess(ii.getLastAccess());
							eIi.setLastAccessSuccess(ii.getLastAccessSuccess());
							if(ii.getIdentityLoginList().getAction() == IdentityLoginActionType.REPLACE) {
								eIi.getIdentityLoginList().getIdentityLogin().clear();
							}
							eIi.getIdentityLoginList().getIdentityLogin().addAll(ii.getIdentityLoginList().getIdentityLogin());
						} else {
							user.getIdentityInformationList().getIdentityInformation().add(ii);
						}
						user.getIdentityInformationList().getIdentityInformation().addAll(eUser.getIdentityInformationList().getIdentityInformation());
					}
				}
				if(eUser.getMetadataList() != null) {
					user.getMetadataList().getMetadata().clear();
					user.getMetadataList().getMetadata().addAll(eUser.getMetadataList().getMetadata());
				}
				if(eUser.getUserAliasList() != null) {
					
					user.getUserAliasList().getUserAlias().clear();
					user.getUserAliasList().getUserAlias().addAll(eUser.getUserAliasList().getUserAlias());
				}
			} else {
				fakeRepository.addUser(user);
			}
		}
		AddOrUpdateUserInfoListResponse response = new AddOrUpdateUserInfoListResponse();
		CommandResponseType responseType = new CommandResponseType();
		responseType.setOpStatus(DacsProvisioningProxyImpl.OpStatus.OK.getIntValue());
		responseType.setGlobalStatus(DacsProvisioningProxyImpl.GlobalStatus.OK.getIntValue());
		response.setResult(responseType);
		return response;
	}

	private IdentityInformationType findIdentityInformationList(IdentityInformationListType identityInformationList,
			String applicationName, String userIdentity) {
		for(IdentityInformationType iit: identityInformationList.getIdentityInformation()) {
			if(iit.getApplicationName().compareTo(applicationName) == 0
					&& iit.getUserIdentity().compareTo(userIdentity) == 0) {
				return iit;
			}
		}
		return null;
	}

	private AttributeType findAttributeByName(List<AttributeType> attribute, String attributeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AddUserListResponse addUserInfoList(AddUserListRequest addUserListRequest) {
		for(org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.UserInfoType user : addUserListRequest.getUserInfoList().getUserInfo()) {
			fakeRepository.addUser(user);
		}
		
		AddUserListResponse response = new AddUserListResponse();
		CommandResponseType responseType = new CommandResponseType();
		responseType.setOpStatus(DacsProvisioningProxyImpl.OpStatus.OK.getIntValue());
		responseType.setGlobalStatus(DacsProvisioningProxyImpl.GlobalStatus.OK.getIntValue());
		response.setResult(responseType);
		return response;
	}

	@Override
	public AddIdentitiesToUserResponse addIdentitiesToUser(AddIdentitiesToUserRequest request) {
		
		UserInfoType user = fakeRepository.getUserByAlias(request.getExistingUserAlias());
		for(IdentityInformationType iit : request.getIdentityInformationList().getIdentityInformation()) {
			IdentityInformationType iitToAdd = new IdentityInformationType();
			iitToAdd.setActivated(true);
			iitToAdd.setApplicationName(iit.getApplicationName());
			iitToAdd.setAuthorized(true);
			iitToAdd.setFriendlyName(iit.getFriendlyName());
			IdentityLoginListType illt = new IdentityLoginListType();
			illt.getIdentityLogin().addAll(iit.getIdentityLoginList().getIdentityLogin());
			illt.setAction(illt.getAction());
			iitToAdd.setIdentityLoginList(illt);
			iitToAdd.setUserIdentity(iit.getUserIdentity());
			user.getIdentityInformationList().getIdentityInformation().add(iitToAdd);
		}
		
		AddIdentitiesToUserResponse response = new AddIdentitiesToUserResponse();
		CommandResponseType responseType = new CommandResponseType();
		responseType.setOpStatus(DacsProvisioningProxyImpl.OpStatus.OK.getIntValue());
		responseType.setGlobalStatus(DacsProvisioningProxyImpl.GlobalStatus.OK.getIntValue());
		response.setResult(responseType);
		return response;
	}

	@Override
	public RemoveAuthPluginInfoFromUserResponse removeAuthPluginInfoFromUser(
			RemoveAuthPluginInfoFromUserRequest request) {
		
		UserInfoType user = fakeRepository.getUserByAlias(request.getExistingUserAlias());
		if(request.isRemoveAuthPluginInfoBirthdate()) {
			user.getAuthPluginInfo().getAuthPluginInfoBirthdate().clear();
		}
		if(request.isRemoveAuthPluginInfoCAP()) {
			user.getAuthPluginInfo().getAuthPluginInfoCap().clear();
		}
		if(request.isRemoveAuthPluginInfoCertificate()) {
			user.getAuthPluginInfo().getAuthPluginInfoCertificate().clear();
		}
		if(request.isRemoveAuthPluginInfoChallengeResponse()) {
			user.getAuthPluginInfo().getAuthPluginInfoChallengeResponse().clear();
		}
		if(request.isRemoveAuthPluginInfoEmail()) {
			user.getAuthPluginInfo().getAuthPluginInfoEmail().clear();
		}
		if(request.isRemoveAuthPluginInfoOATH()) {
			user.getAuthPluginInfo().getAuthPluginInfoOATH().clear();
		}
		if(request.isRemoveAuthPluginInfoPassword()) {
			user.getAuthPluginInfo().getAuthPluginInfoPassword().clear();
		}
		if(request.isRemoveAuthPluginInfoSIM()) {
			user.getAuthPluginInfo().getAuthPluginInfoSIM().clear();
		}
		if(request.isRemoveAuthPluginInfoSMS()) {
			user.getAuthPluginInfo().getAuthPluginInfoSMS().clear();
		}
			
		RemoveAuthPluginInfoFromUserResponse response = new RemoveAuthPluginInfoFromUserResponse();
		CommandResponseType responseType = new CommandResponseType();
		responseType.setOpStatus(DacsProvisioningProxyImpl.OpStatus.OK.getIntValue());
		responseType.setGlobalStatus(DacsProvisioningProxyImpl.GlobalStatus.OK.getIntValue());
		response.setResult(responseType);
		return response;
	}

	@Override
	public RemoveUserListResponse remove(RemoveUserListRequest removeUserRequest) {
		for(String userAlias : removeUserRequest.getExistingUserAliasList().getUserAlias()) {
			fakeRepository.removeByUserAlias(userAlias);
		}
		
		RemoveUserListResponse response = new RemoveUserListResponse();
		CommandResponseType responseType = new CommandResponseType();
		responseType.setOpStatus(DacsProvisioningProxyImpl.OpStatus.OK.getIntValue());
		responseType.setGlobalStatus(DacsProvisioningProxyImpl.GlobalStatus.OK.getIntValue());
		response.setResult(responseType);
		return response;
	}

	/**
	 * Request is ignored, return all users each time
	 */
	@Override
	public GetUserListResponse getUsersList(GetUserListRequest getUserListRequest) {
		GetUserListResponse response = new GetUserListResponse();
		response.setNbUsers(fakeRepository.getUsers().size());
		CommandResponseType responseType = new CommandResponseType();
		responseType.setOpStatus(DacsProvisioningProxyImpl.OpStatus.OK.getIntValue());
		responseType.setGlobalStatus(DacsProvisioningProxyImpl.GlobalStatus.OK.getIntValue());
		// responseType.getFailureDetails()
		response.setResult(responseType);
		UserInfoListType userInfoList = new UserInfoListType();
		for(UserInfoType rUser : fakeRepository.getUsers()) {
			userInfoList.getUserInfo().add(rUser);
		}
		response.setUserInfoList(userInfoList);
		return response;
	}

	@Override
	public GetUserInfoResponse getUserInfo(GetUserInfoRequest getUserInfoRequest) {
		GetUserInfoResponse response = new GetUserInfoResponse();
		CommandResponseType responseType = new CommandResponseType();
		responseType.setOpStatus(DacsProvisioningProxyImpl.OpStatus.OK.getIntValue());
		responseType.setGlobalStatus(DacsProvisioningProxyImpl.GlobalStatus.OK.getIntValue());
		// responseType.getFailureDetails()
		response.setResult(responseType);
		response.setUserInfo(fakeRepository.getUserByAlias(getUserInfoRequest.getUserIdentity()));
		return response;
	}
}