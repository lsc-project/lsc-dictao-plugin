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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.naming.NamingException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;
import org.lsc.LscDatasetModification;
import org.lsc.LscDatasetModification.LscDatasetModificationType;
import org.lsc.LscDatasets;
import org.lsc.LscModificationType;
import org.lsc.LscModifications;
import org.lsc.beans.IBean;
import org.lsc.configuration.ConnectionType;
import org.lsc.configuration.PluginConnectionType;
import org.lsc.configuration.TaskType;
import org.lsc.exception.LscServiceConfigurationException;
import org.lsc.exception.LscServiceException;
import org.lsc.plugins.connectors.dictao.configuration.generated.DacsProvisioningConnectionSettings;
import org.lsc.plugins.connectors.dictao.configuration.generated.DacsProvisioningServiceSettings;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddOrUpdateUserInfoListRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddOrUpdateUserInfoListResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddUserListRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AddUserListResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AttributeListType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AttributeType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AuthPluginInfoBirthdateType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AuthPluginInfoCertificateType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AuthPluginInfoEmailType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AuthPluginInfoOATHType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AuthPluginInfoPasswordType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AuthPluginInfoSMSType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.AuthPluginInfoType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.Birthdate;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.GetUserListRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.GetUserListResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.GroupIdListType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.IdentityInformationListType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.IdentityInformationType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.RemoveUserListRequest;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.RemoveUserListResponse;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.UserAliasListType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.UserInfoListType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.UserInfoToUpdateListType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.UserInfoToUpdateType;
import org.lsc.plugins.connectors.dictao.dacs.provisioning.ui.ws.jaxws.UserInfoType;
import org.lsc.service.IWritableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

/**
 * This class is a DACS Service destination service.
 * 
 * It supports both synchronization as source and as destination (but not as asynchronous source). It will synchronize
 * the available information from the user including :
 * <ul>
 * <li>the common name (as "cn")
 * <li>
 * <li>the email address (as "mail")
 * <li>
 * <li>the password (as "userPassword") in either clear or SSHA encrypted format
 * <li>
 * <li>the phone number used to send SMS OTP (as "telephoneNumber")
 * <li>
 * <li>the birth date (as "birthdate")
 * <li>
 * <li>the login (as "uid")
 * <li>
 * <li>the last time the object was updated (as "modifyTimestamp") in a LDAP format like 20120117233300Z
 * <li>
 * </ul>
 * Please notice that it only settings are changed (scope, application ids, ...) no update will be done by this service.
 * If you want to force an update, add the modifytimestamp attribute (because it's currently not returned by DACS).
 * 
 * 
 * @author Sebastien Bahloul &lt;sbahloul@dictao.com&gt;
 */
public class DacsProvisioningService implements IWritableService {

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

    /** The private static LOGGER used to record errors and notifications */
    private static Logger                       LOGGER               = LoggerFactory
                                                                             .getLogger(DacsProvisioningService.class);

    public static final String                  DICTAO_LSC_MOCK_MODE = "dictao.lsc.mock";

    public static final String                  COMMON_NAME          = "cn";
    public static final String                  EMAIL_NAME           = "mail";
    public static final String                  PASSWORD_NAME        = "userPassword";
    public static final String                  PHONE_NAME           = "mobile";
    public static final String                  BIRTHDATE_NAME       = "birthdate";
    public static final String                  LOGIN_NAME           = "uid";
    public static final String                  MODIFYTIMESTAMP_NAME = "modifyTimestamp";
    public static final String                  CERTIFICATE_NAME     = "userCertificate";
    public static final String                  OATH_SERIAL_NAME     = "serialNumber";

    /** The connection settings */
    private DacsProvisioningConnectionSettings  dacsConn;

    /** The service settings */
    private DacsProvisioningServiceSettings     dacsService;

    /** JAXB proxy class to access the provisioning web service */
    private DacsProvisioningProxy               dacsProxy;

    /** LSC bean class */
    private Class<IBean>                        beanClass;

    /** the format used to parse the birth date */
    private String                              dateFormat;

    /**
     * The cache used to avoid recalling the web service each time we look for an entry
     */
    private LoadingCache<String, Optional<UserInfoType>> cache;

    private DatatypeFactory                     datatypeFactory;

    @SuppressWarnings("unchecked")
    public DacsProvisioningService(final TaskType task) throws LscServiceConfigurationException {
        try {
            this.beanClass = (Class<IBean>) Class.forName(task.getBean());
        } catch (ClassNotFoundException e) {
            throw new LscServiceConfigurationException(e);
        }

        List<Object> confs = null;
        if (task.getPluginDestinationService() != null && task.getPluginDestinationService().getConnection() != null) {
            confs = ((PluginConnectionType) task.getPluginDestinationService().getConnection().getReference()).getAny();
        } else if (task.getPluginSourceService() != null && task.getPluginSourceService().getConnection() != null) {
            confs = ((PluginConnectionType) task.getPluginSourceService().getConnection().getReference()).getAny();
        }
        if (confs != null && confs.size() == 1) {
            if (task.getPluginDestinationService() != null && task.getPluginDestinationService().getAny() != null
                    && task.getPluginDestinationService().getAny().size() == 1) {
                dacsService = (DacsProvisioningServiceSettings) task.getPluginDestinationService().getAny().get(0);
            } else if (task.getPluginSourceService() != null && task.getPluginSourceService().getAny() != null
                    && task.getPluginSourceService().getAny().size() == 1) {
                dacsService = (DacsProvisioningServiceSettings) task.getPluginSourceService().getAny().get(0);
            } else {
                throw new LscServiceConfigurationException("Unable to find DACS agent service configuration !");
            }
            dacsConn = (DacsProvisioningConnectionSettings) confs.get(0);
        } else {
            throw new LscServiceConfigurationException("Unable to find DACS agent connection configuration !");
        }
        if (System.getProperty(DICTAO_LSC_MOCK_MODE) == null) {
            dacsProxy = new DacsProvisioningProxyImpl(dacsConn.getProvisioningUrl(), dacsConn);
        } else {
            DacsFakeUsersRepository fakeRepository = new DacsFakeUsersRepository();
            dacsProxy = new DacsFakeProvisioning(dacsService, fakeRepository);
        }

        // Initiate variables
        if (dacsService.getDateFormat() != null) {
            dateFormat = dacsService.getDateFormat();
        } else {
            dateFormat = "dd/MM/yyyy";
        }

        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new LscServiceConfigurationException("Internal error (" + e.toString() + ")", e);
        }
    }

    @Override
    public IBean getBean(String pivotName, LscDatasets pivotAttributes, boolean fromSameService)
            throws LscServiceException {
        try {
            IBean bean = this.beanClass.newInstance();
            Optional<UserInfoType> userInfo;
            if (cache == null) {
                refillCache();
            }

            userInfo = cache.get(pivotName);
            if (!userInfo.isPresent()) {
                return null;
            }

            LscDatasets datasets = new LscDatasets();
            if (userInfo.get().getAttributeList() != null) {
                for (AttributeType attr : userInfo.get().getAttributeList().getAttribute()) {
                    datasets.put(attr.getAttributeName(), attr.getAttributeValue());
                }
            }
            bean.setDatasets(datasets);
            bean.setMainIdentifier(pivotName);

            return bean;
        } catch (InstantiationException e) {
            throw new LscServiceConfigurationException(e);
        } catch (IllegalAccessException e) {
            throw new LscServiceConfigurationException(e);
        } catch (ExecutionException e) {
            throw new LscServiceException(e);
        }
    }

    private synchronized boolean refillCache() throws LscServiceException {

        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(dacsService.getEntryCacheExpiracy(), TimeUnit.SECONDS)
                .maximumSize(dacsService.getEntryCacheSize()).build(new CacheLoader<String, Optional<UserInfoType>>() {

                    public Optional<UserInfoType> load(String id) throws LscServiceException {
                        GetUserListRequest request = new GetUserListRequest();
                        GroupIdListType groupIdList = new GroupIdListType();
                        groupIdList.getGroupId().add(dacsService.getGroupId());
                        request.setGroupIdList(groupIdList);
                        request.setClientRef(id);
                        request.setFirstIdx(0);
                        request.setLastIdx(1);
                        final GetUserListResponse response = dacsProxy.getUsersList(request);

                        if (response.getResult().getOpStatus() == DacsProvisioningProxyImpl.OpStatus.OK.getIntValue()) {
                            if (response.getNbUsers() == 1) {
                                return Optional.of(response.getUserInfoList().getUserInfo().get(0));
                            } else if (response.getNbUsers() == 0) {
                                return Optional.absent();
                            } else { // if (response.getNbUsers() > 1) {
                                throw new LscServiceException("Found too many users for id=" + id + " ("
                                        + response.getNbUsers() + ")");
                            }
                        } else {
                            LOGGER.error("Operation status: "
                                    + DacsProvisioningProxyImpl.OpStatus.valueOf(response.getResult().getOpStatus())
                                            .getMessageValue());
                            throw new LscServiceException((response.getResult().getFailureDetails() != null ? response
                                    .getResult().getFailureDetails().toString() : ""));
                        }
                    }

                    @Override
                    public Map<String, Optional<UserInfoType>> loadAll(Iterable<? extends String> keys)
                            throws LscServiceException {
                        Map<String, Optional<UserInfoType>> temp = new HashMap<String, Optional<UserInfoType>>(dacsService
                                .getEntryCacheSize().intValue());

                        GetUserListRequest request = new GetUserListRequest();
                        GroupIdListType groupIdList = new GroupIdListType();
                        groupIdList.getGroupId().add(dacsService.getGroupId());
                        request.setGroupIdList(groupIdList);
                        request.setFirstIdx(0);
                        request.setLastIdx(dacsService.getEntryCacheSize().intValue());
                        GetUserListResponse response = dacsProxy.getUsersList(request);

                        if (response.getResult().getOpStatus() == DacsProvisioningProxyImpl.OpStatus.OK.getIntValue()) {
                            for (UserInfoType userInfo : response.getUserInfoList().getUserInfo()) {
                                temp.put(userInfo.getClientRef(), Optional.of(userInfo));
                            }
                        } else {
                            LOGGER.error("Operation status: "
                                    + DacsProvisioningProxyImpl.OpStatus.valueOf(response.getResult().getOpStatus())
                                            .getMessageValue());
                            throw new LscServiceException((response.getResult().getFailureDetails() != null ? response
                                    .getResult().getFailureDetails().toString() : ""));
                        }
                        return temp;
                    }
                });

        return true;

        // GetUserListRequest request = new GetUserListRequest();
        // GroupIdListType groupIdList = new GroupIdListType();
        // groupIdList.getGroupId().add(dacsService.getGroupId());
        // request.setGroupIdList(groupIdList);
        // request.setFirstIdx(0);
        // request.setLastIdx(dacsService.getEntryCacheSize().intValue());
        // final GetUserListResponse response = dacsProxy.getUsersList(request);
        //
        // if (response.getResult().getOpStatus() != DacsProvisioningProxyImpl.OpStatus.OK.getIntValue()) {
        // LOGGER.error("Operation status: "
        // + DacsProvisioningProxyImpl.OpStatus.valueOf(response.getResult().getOpStatus()).getMessageValue());
        // throw new LscServiceException((response.getResult().getFailureDetails() != null ? response.getResult()
        // .getFailureDetails().toString() : ""));
        // }
        // if (response.getUserInfoList() != null) {
        // for (UserInfoType userInfo : response.getUserInfoList().getUserInfo()) {
        // cache.put(userInfo.getClientRef(), userInfo);
        // }
        // return true;
        // } else {
        // return false;
        // }
    }

    protected void flushCache() {
        cache = null;
    }

    @Override
    public Map<String, LscDatasets> getListPivots() throws LscServiceException {
        Map<String, LscDatasets> users = new HashMap<String, LscDatasets>();

        if (cache == null) {
            refillCache();
        }

        for (Optional<UserInfoType> userInfo : cache.asMap().values()) {
            LscDatasets datasets = new LscDatasets();
            if (userInfo.get().getAttributeList() != null) {
                for (AttributeType attr : userInfo.get().getAttributeList().getAttribute()) {
                    datasets.put(attr.getAttributeName(), attr.getAttributeValue());
                }
            }
            users.put(userInfo.get().getClientRef(), datasets);
        }
        return users;
    }

    @Override
    public boolean apply(LscModifications lm) throws LscServiceException {
        String friendlyName = getFriendlyName(lm);

        if (lm.getOperation() == LscModificationType.CREATE_OBJECT) {
            AddUserListRequest request = new AddUserListRequest();
            UserInfoListType userInfoList = new UserInfoListType();
            UserInfoToUpdateType user = getDacsUserInfoFromLscModifications(lm);
            userInfoList.getUserInfo().add(user);

            IdentityInformationListType identities = new IdentityInformationListType();
            for (String applicationId : dacsService.getApplicationId()) {
                IdentityInformationType identity = new IdentityInformationType();
                identity.setActivated(true);
                identity.setApplicationName(applicationId);
                identity.setFriendlyName(friendlyName);
                identity.setUserIdentity(lm.getModificationsItemsByHash().get(LOGIN_NAME).get(0).toString());
                identities.getIdentityInformation().add(identity);
            }
            user.setIdentityInformationList(identities);

            request.setUserInfoList(userInfoList);

            AddUserListResponse response = dacsProxy.addUserInfoList(request);
            if (response.getResult().getOpStatus() != DacsProvisioningProxyImpl.OpStatus.OK.getIntValue()) {
                LOGGER.error("Operation status: "
                        + DacsProvisioningProxyImpl.OpStatus.valueOf(response.getResult().getOpStatus())
                                .getMessageValue());
                throw new LscServiceException(response.getResult().getFailureDetails().toString());
            }
            LOGGER.debug("User successfully created in DACS: " + lm.getMainIdentifier());
            return true;
        } else if (lm.getOperation() == LscModificationType.UPDATE_OBJECT) {
            AddOrUpdateUserInfoListRequest request = new AddOrUpdateUserInfoListRequest();
            UserInfoToUpdateListType userInfoList = new UserInfoToUpdateListType();
            UserInfoToUpdateType user = getDacsUserInfoFromLscModifications(lm);
            userInfoList.getUserInfoToUpdate().add(user);

            try {
                user.setFriendlyName(friendlyName);

                String userIdentity = null;
                if (lm.getModificationsItemsByHash().containsKey(LOGIN_NAME)) {
                    userIdentity = lm.getModificationsItemsByHash().get(LOGIN_NAME).get(0).toString();
                } else {
                    userIdentity = lm.getDestinationBean().getDatasetFirstValueById(LOGIN_NAME);
                }

                IdentityInformationListType identities = new IdentityInformationListType();
                for (String applicationId : dacsService.getApplicationId()) {
                    IdentityInformationType identity = new IdentityInformationType();
                    identity.setActivated(true);
                    identity.setApplicationName(applicationId);
                    identity.setFriendlyName(friendlyName);
                    identity.setUserIdentity(userIdentity);
                    identities.getIdentityInformation().add(identity);
                }
                user.setIdentityInformationList(identities);
            } catch (NamingException ne) {
                throw new LscServiceException("Unable to prepare DACS updated user=" + lm.getMainIdentifier() + " ("
                        + ne.getExplanation() + ")");
            }
            request.setUserInfoToUpdateList(userInfoList);

            AddOrUpdateUserInfoListResponse response = dacsProxy.addOrUpdateUserInfoList(request);
            if (response.getResult().getOpStatus() != DacsProvisioningProxyImpl.OpStatus.OK.getIntValue()) {
                LOGGER.error("Operation status: "
                        + DacsProvisioningProxyImpl.OpStatus.valueOf(response.getResult().getOpStatus())
                                .getMessageValue());
                throw new LscServiceException(response.getResult().getFailureDetails().toString());
            }
            LOGGER.debug("User successfully updated in DACS: " + lm.getMainIdentifier());
            return true;
        } else if (lm.getOperation() == LscModificationType.DELETE_OBJECT) {
            RemoveUserListRequest request = new RemoveUserListRequest();
            UserAliasListType userAliasListType = new UserAliasListType();
            userAliasListType.getUserAlias().add(lm.getMainIdentifier());
            request.setExistingUserAliasList(userAliasListType);
            RemoveUserListResponse response = dacsProxy.remove(request);
            if (response.getResult().getOpStatus() == DacsProvisioningProxyImpl.OpStatus.OK.getIntValue()) {
                LOGGER.debug("User successfully delete in DACS: " + lm.getMainIdentifier());
                return true;
            } else {
                LOGGER.error("Operation status: "
                        + DacsProvisioningProxyImpl.OpStatus.valueOf(response.getResult().getOpStatus())
                                .getMessageValue());
                throw new LscServiceException("Unable to delete DACS user=" + lm.getMainIdentifier() + " ("
                        + response.getResult().getFailureDetails() + ")");
            }
        } else {
            throw new LscServiceException("Unsupported operation type: " + lm.getOperation());
        }
    }

    public static LscDatasetModification getLscAttributeModificationByName(LscModifications lm, String name) {
        for(LscDatasetModification modification: lm.getLscAttributeModifications()) {
            if(modification.getAttributeName().equalsIgnoreCase(name)) {
                return modification;
            }
        }
        return null;
    }


    private String getFriendlyName(LscModifications lm) {
        LscDatasetModification commonNameUpdate = getLscAttributeModificationByName(lm, COMMON_NAME);
        if (commonNameUpdate != null && commonNameUpdate.getOperation() != LscDatasetModificationType.DELETE_VALUES) {
            return (String) commonNameUpdate.getValues().get(0);
        } else if (lm.getDestinationBean() != null) {
            Set<Object> destCN = lm.getDestinationBean().getDatasetById(COMMON_NAME);
            if (destCN != null && !destCN.isEmpty()) {
                return (String) destCN.iterator().next();
            } else {
                return lm.getMainIdentifier();
            }
        } else {
            return null;
        }
    }

    private UserInfoToUpdateType getDacsUserInfoFromLscModifications(LscModifications lm) throws LscServiceException {
        UserInfoToUpdateType userInfo = new UserInfoToUpdateType();
        String friendlyName = getFriendlyName(lm);
        userInfo.setClientRef(lm.getMainIdentifier());
        userInfo.setFriendlyName(friendlyName);

        userInfo.setGroupId(dacsService.getGroupId());
        IdentityInformationListType iil = new IdentityInformationListType();

        if (lm.getModificationsItemsByHash().containsKey(LOGIN_NAME)) {
            for (String applicationId : dacsService.getApplicationId()) {
                IdentityInformationType ii = new IdentityInformationType();
                ii.setActivated(true);
                ii.setApplicationName(applicationId);
                ii.setFriendlyName(friendlyName);
                ii.setUserIdentity(lm.getModificationsItemsByHash().get(LOGIN_NAME).get(0).toString());

                iil.getIdentityInformation().add(ii);
            }
            userInfo.setIdentityInformationList(iil);
        }
        AttributeListType attributes = new AttributeListType();
        for (Entry<String, List<Object>> update : lm.getModificationsItemsByHash().entrySet()) {
            AttributeType attribute = new AttributeType();
            attribute.setAttributeName(update.getKey());
            for (Object value : update.getValue()) {
                attribute.getAttributeValue().add(value.toString());
            }
            attributes.getAttribute().add(attribute);
        }
        userInfo.setAttributeList(attributes);

        return populatePluginsInfo(lm, userInfo);
    }

    public UserInfoToUpdateType populatePluginsInfo(LscModifications lm, UserInfoToUpdateType userInfo)
            throws LscServiceException {
        AuthPluginInfoType pluginsInfo = new AuthPluginInfoType();

        LscDatasetModification birthdateUpdate = getLscAttributeModificationByName(lm, BIRTHDATE_NAME);
        if (birthdateUpdate != null) {
            AuthPluginInfoBirthdateType birthdateInfo = new AuthPluginInfoBirthdateType();
            birthdateInfo.setScope(dacsService.getScope());
            if (birthdateUpdate.getOperation() == LscDatasetModificationType.ADD_VALUES
                    || birthdateUpdate.getOperation() == LscDatasetModificationType.REPLACE_VALUES) {
                birthdateInfo.setActivated(true);
                birthdateInfo.setAliasBirthdate(lm.getMainIdentifier());
                Birthdate birthdate = new Birthdate();

                GregorianCalendar calendar = new GregorianCalendar();
                try {
                    calendar.setTime(new SimpleDateFormat(dateFormat)
                            .parse((String) birthdateUpdate.getValues().get(0)));
                } catch (ParseException e) {
                    throw new LscServiceException("Unable to parse the birthdate: "
                            + lm.getModificationsItemsByHash().get(BIRTHDATE_NAME).get(0).toString()
                            + ". It should respect the format '" + dateFormat + "' !");
                }
                birthdate.setValue(datatypeFactory.newXMLGregorianCalendar(calendar));
                birthdateInfo.setBirthdate(birthdate);
                birthdateInfo.setAvailable(true);
                birthdateInfo.setEnrolled(true);
            } else {
                birthdateInfo.setActivated(false);
            }
            pluginsInfo.getAuthPluginInfoBirthdate().add(birthdateInfo);

        }

        LscDatasetModification certUpdate = getLscAttributeModificationByName(lm, CERTIFICATE_NAME);
        if (certUpdate != null) {
            AuthPluginInfoCertificateType x509Info = new AuthPluginInfoCertificateType();
            x509Info.setScope(dacsService.getScope());
            if (certUpdate.getOperation() == LscDatasetModificationType.ADD_VALUES
                    || certUpdate.getOperation() == LscDatasetModificationType.REPLACE_VALUES) {
                x509Info.setActivated(true);
                x509Info.setAliasCertificate(lm.getMainIdentifier());
                x509Info.setAvailable(true);
                x509Info.setEnrolled(true);
            } else {
                x509Info.setAvailable(false);
            }
            pluginsInfo.getAuthPluginInfoCertificate().add(x509Info);
        }

        LscDatasetModification emailUpdate = getLscAttributeModificationByName(lm, EMAIL_NAME);
        if (emailUpdate != null) {
            AuthPluginInfoEmailType emailInfo = new AuthPluginInfoEmailType();
            emailInfo.setScope(dacsService.getScope());
            if (emailUpdate.getOperation() == LscDatasetModificationType.ADD_VALUES
                    || emailUpdate.getOperation() == LscDatasetModificationType.REPLACE_VALUES) {
                emailInfo.setActivated(true);
                emailInfo.setAliasEmail(lm.getMainIdentifier());
                emailInfo.setAvailable(true);
                emailInfo.setEnrolled(true);
            } else {
                emailInfo.setAvailable(false);
            }
            pluginsInfo.getAuthPluginInfoEmail().add(emailInfo);
        }

        LscDatasetModification passwordUpdate = getLscAttributeModificationByName(lm, PASSWORD_NAME);
        if (passwordUpdate != null) {
            AuthPluginInfoPasswordType passwordInfo = new AuthPluginInfoPasswordType();
            passwordInfo.setScope(dacsService.getScope());
            if (passwordUpdate.getOperation() == LscDatasetModificationType.ADD_VALUES
                    || passwordUpdate.getOperation() == LscDatasetModificationType.REPLACE_VALUES) {
                passwordInfo.setActivated(true);
                passwordInfo.setAliasPassword(lm.getMainIdentifier());
                passwordInfo.setAvailable(true);
                passwordInfo.setEnrolled(true);
            } else {
                passwordInfo.setActivated(false);
            }
            pluginsInfo.getAuthPluginInfoPassword().add(passwordInfo);
        }

        LscDatasetModification smsUpdate = getLscAttributeModificationByName(lm, PHONE_NAME);
        if (smsUpdate != null) {
            AuthPluginInfoSMSType smsInfo = new AuthPluginInfoSMSType();
            smsInfo.setScope(dacsService.getScope());
            if (smsUpdate.getOperation() == LscDatasetModificationType.ADD_VALUES
                    || smsUpdate.getOperation() == LscDatasetModificationType.REPLACE_VALUES) {
                smsInfo.setActivated(true);
                smsInfo.setAliasSMS(lm.getMainIdentifier());
                smsInfo.setAvailable(true);
                smsInfo.setEnrolled(true);
                // The phone number is only provided to DVS
            } else {
                smsInfo.setActivated(false);
            }
            pluginsInfo.getAuthPluginInfoSMS().add(smsInfo);
        }

        LscDatasetModification oathUpdate = getLscAttributeModificationByName(lm, OATH_SERIAL_NAME);
        if (oathUpdate != null) {
            AuthPluginInfoOATHType oathInfo = new AuthPluginInfoOATHType();
            oathInfo.setScope(dacsService.getScope());
            if (oathUpdate.getOperation() == LscDatasetModificationType.ADD_VALUES
                    || oathUpdate.getOperation() == LscDatasetModificationType.REPLACE_VALUES) {
                oathInfo.setActivated(true);
                oathInfo.setAliasOATH(lm.getMainIdentifier());
                oathInfo.setAvailable(true);
                oathInfo.setEnrolled(true);
            } else {
                oathInfo.setActivated(false);
            }
            pluginsInfo.getAuthPluginInfoOATH().add(oathInfo);
        }

        userInfo.setAuthPluginInfo(pluginsInfo);
        return userInfo;
    }

    @Override
    public List<String> getWriteDatasetIds() {
        return Arrays.asList(new String[] { LOGIN_NAME, COMMON_NAME, EMAIL_NAME, PASSWORD_NAME, PHONE_NAME,
                BIRTHDATE_NAME, MODIFYTIMESTAMP_NAME, CERTIFICATE_NAME });
    }

    public Collection<Class<? extends ConnectionType>> getSupportedConnectionType() {
        Collection<Class<? extends ConnectionType>> list = new ArrayList<Class<? extends ConnectionType>>();
        list.add(PluginConnectionType.class);
        return list;
    }

    public Collection<Class<?>> getSupportedPluginConnectionType() {
        Collection<Class<?>> list = new ArrayList<Class<?>>();
        list.add(DacsProvisioningConnectionSettings.class);
        return list;
    }
}
