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

import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddIdentitiesToUserRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddIdentitiesToUserResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddOrUpdateUserInfoListRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddOrUpdateUserInfoListResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddUserListRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddUserListResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.GetUserInfoRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.GetUserInfoResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.GetUserListRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.GetUserListResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.RemoveAuthPluginInfoFromUserRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.RemoveAuthPluginInfoFromUserResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.RemoveUserListRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.RemoveUserListResponse;

/**
 * This class is used by the DACS LSC service to call the various DACS provisioning web service
 * 
 * @author Sebastien Bahloul &lt;sbahloul@dictao.com&lt;
 */
public interface DacsProvisioningProxy {

    AddOrUpdateUserInfoListResponse addOrUpdateUserInfoList(AddOrUpdateUserInfoListRequest addOrUpdateUserListRequest);
    
    AddUserListResponse addUserInfoList(AddUserListRequest addUserListRequest);

    AddIdentitiesToUserResponse addIdentitiesToUser(AddIdentitiesToUserRequest request);

    RemoveAuthPluginInfoFromUserResponse removeAuthPluginInfoFromUser(RemoveAuthPluginInfoFromUserRequest removeUserRequest);
    
    RemoveUserListResponse remove(RemoveUserListRequest removeUserRequest);

    GetUserInfoResponse getUserInfo(GetUserInfoRequest getUserInfoRequest);
    
    GetUserListResponse getUsersList(GetUserListRequest getUserListRequest);
}