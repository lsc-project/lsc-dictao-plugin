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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.xerces.jaxp.datatype.DatatypeFactoryImpl;
import org.lsc.LscDatasets;
import org.lsc.LscModificationType;
import org.lsc.LscModifications;
import org.lsc.beans.IBean;
import org.lsc.configuration.ConnectionType;
import org.lsc.configuration.PluginConnectionType;
import org.lsc.configuration.TaskType;
import org.lsc.exception.LscServiceConfigurationException;
import org.lsc.exception.LscServiceException;
import org.lsc.plugins.connectors.dictao.configuration.generated.DvsProvisioningConnectionSettings;
import org.lsc.plugins.connectors.dictao.configuration.generated.DvsProvisioningServiceSettings;
import org.lsc.plugins.connectors.dictao.dacs.DacsProvisioningService;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.ActivationStateType;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.AddUserListRequest;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.AddUserListResponse;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.CancellationStateType;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.CoordSimpleType;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.CoordType;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.DeleteUserInformationListRequest;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.DeleteUserInformationListRequest.UserInformationList;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.DeleteUserInformationListRequest.UserInformationList.UserInformation;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.DeleteUserInformationListResponse;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.GetUserListRequest;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.GetUserListResponse;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.HeaderType;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.PasswordType;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.PasswordTypeType;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.UserInfoListType;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.UserInfoType;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.UserInfoType.CoordAuthent;
import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.UserInfoType.CoordAuthent.CoordList;
import org.lsc.service.IWritableService;
import org.lsc.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a DVS Service destination service
 * 
 * @author Sebastien Bahloul &lt;sbahloul@dictao.com&lt;
 */
public class DvsProvisioningService implements IWritableService {

	private static Logger LOGGER = LoggerFactory.getLogger(DvsProvisioningService.class);

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

	public static final String SURNAME_NAME = "sn";
	public static final String GIVENNAME_NAME = "givenName";
	public static final String EMAIL_NAME = "mail";
	public static final String PASSWORD_NAME = "userPassword";
	public static final String PHONE_NAME = "mobile";
	public static final String BIRTHDATE_NAME = "birthdate";
	public static final String MODIFYTIMESTAMP_NAME = "modifyTimestamp";
    public static final String SSHA_PREFIX = "{SSHA}";
	public static final String CERTIFICATE_NAME = "userCertificate";
	public static final String OATH_SERIAL_NAME = "serialNumber";
	public static final String PASSWORD_ENCRYPTED_VALUE = "PASSWORD-IS-NOT-READABLE-BUT-IS-THERE";
	
	private DvsProvisioningConnectionSettings dvsConn;
	
	private DvsProvisioningServiceSettings dvsService;
	
	private DvsProvisioningProxy dvsProxy;

    private Class<IBean> beanClass;
	
    private String dateFormat;

    private Map<String, UserInfoType> cache;

    @SuppressWarnings("unchecked")
    public DvsProvisioningService(final TaskType task) throws LscServiceConfigurationException {
        try {
            this.beanClass = (Class<IBean>) Class.forName(task.getBean());
        } catch (ClassNotFoundException e) {
            throw new LscServiceConfigurationException(e);
        }
		
		List<Object> confs = ((PluginConnectionType)task.getPluginDestinationService().getConnection().getReference()).getAny();
		if(confs != null && confs.size() == 1) {
			if(task.getPluginDestinationService().getAny() != null && task.getPluginDestinationService().getAny().size() == 1) {
				dvsService = (DvsProvisioningServiceSettings)  task.getPluginDestinationService().getAny().get(0);
			} else {
				throw new LscServiceConfigurationException("Unable to find DVS agent service configuration !");
			}
			dvsConn = (DvsProvisioningConnectionSettings) confs.get(0);
		} else {
			throw new LscServiceConfigurationException("Unable to find DVS agent connection configuration !");
		}
		if(System.getProperty(DacsProvisioningService.DICTAO_LSC_MOCK_MODE) == null) {
			dvsProxy = new DvsProvisioningProxyImpl(dvsConn.getUrl(), dvsConn );
		} else {
			DvsFakeUsersRepository fakeRepository = new DvsFakeUsersRepository();
			dvsProxy = new DvsFakeProvisioning(dvsService, fakeRepository);
		}

        if(dvsService.getDateFormat() != null) {
            dateFormat = dvsService.getDateFormat(); 
        } else {
            dateFormat = "dd/MM/yyyy";
        }
	}

	@Override
	public IBean getBean(String pivotName, LscDatasets pivotAttributes,
			boolean fromSameService) throws LscServiceException {
        try {
            IBean bean = this.beanClass.newInstance();

            if(cache == null) {
                cache = new HashMap<String, UserInfoType>();
                fillCache();
            }
             
            if(!cache.containsKey(pivotName)) {
                return null;
            }
            UserInfoType userInfo  = cache.get(pivotName);

            if(userInfo.getClientRef().equalsIgnoreCase(pivotName)) {
                LscDatasets datasets = new LscDatasets();
                if(userInfo.getFirstName() != null) {
                    datasets.put(GIVENNAME_NAME, userInfo.getFirstName());
                }
                if(userInfo.getName() != null) {
                    datasets.put(SURNAME_NAME, userInfo.getName());
                }
                if(userInfo.getBirthday() != null) {
                    datasets.put(BIRTHDATE_NAME, DateUtils.format(userInfo.getBirthday().toGregorianCalendar().getTime()));
                }
                if(userInfo.getCoordAuthent() != null && userInfo.getCoordAuthent().getCoordList() != null) {
                    for(CoordType coord : userInfo.getCoordAuthent().getCoordList().getCoord()) {
                        if(coord.getCoordType() == CoordSimpleType.MAIL) {
                            datasets.put(EMAIL_NAME, coord.getCoordValue());
                        } else if (coord.getCoordType() == CoordSimpleType.SMS) {
                            datasets.put(PHONE_NAME, coord.getCoordValue());
                        } else {
                            throw new LscServiceException("Unknown personal data type: " + coord.getCoordType());
                        }
                    }
                }

                if(userInfo.getPasswordAuthent() != null && userInfo.getPasswordAuthent().getPasswordList() != null
                		&& userInfo.getPasswordAuthent().getPasswordList().getPassword() != null
                		&& userInfo.getPasswordAuthent().getPasswordList().getPassword().size() > 0) {
                	datasets.put(PASSWORD_NAME, PASSWORD_ENCRYPTED_VALUE);
                }

                bean.setDatasets(datasets);
                bean.setMainIdentifier(pivotName);
                return bean;
            }

            return null;
        } catch (InstantiationException e) {
            throw new LscServiceConfigurationException(e);
        } catch (IllegalAccessException e) {
            throw new LscServiceConfigurationException(e);
        }
	}

	private boolean fillCache() throws LscServiceException {
        GetUserListRequest request = new GetUserListRequest();
        HeaderType header = new HeaderType();
        header.setGroupIdentifier(dvsService.getGroupId());
        header.setRequestId(UUID.randomUUID().toString());
        header.setUserLevel(dvsService.getUserLevel().name());
        request.setHeader(header);

        GetUserListResponse response = dvsProxy.getUserList(request);

        int opStatus = response.getGetUserListResult().getOpStatus();
        int globalStatus = response.getGetUserListResult().getGlobalStatus();
        if (opStatus != DvsProvisioningProxy.OpStatus.SUCCESS.getIntValue() || globalStatus != 0) {
            LOGGER.error("The DVS provisioning has failed:");
            LOGGER.error("OpStatus: " + opStatus + ", message: "
                    + DvsProvisioningProxy.OpStatus.valueOf(opStatus).getMessageValue());
            LOGGER.error("Global status: " + DvsProvisioningProxyImpl.decodeGlobalStatus(globalStatus));
            throw new LscServiceException("The DVS provisioning has failed. OpStatus: " + opStatus + ", message: " + DvsProvisioningProxy.OpStatus.valueOf(opStatus).getMessageValue()
                 + ", Global status: " + DvsProvisioningProxyImpl.decodeGlobalStatus(globalStatus) + (response.getGetUserListResult().getFailureDetails() != null ? "FailureDetails = " + response.getGetUserListResult().getFailureDetails().toString() : ""));
        }

        cache.clear();
        if(response.getGetUserListResult() != null) {
            for(UserInfoType userInfo: response.getGetUserListResult().getUserList().getUserInfo()) {
                cache.put(userInfo.getClientRef(), userInfo);
            }
            return true;
        } else {
            return false;
        }
    }
	
	protected void flushCache() {
		cache = null;
	}

    @Override
	public Map<String, LscDatasets> getListPivots() throws LscServiceException {
	    Map<String, LscDatasets> users = new HashMap<String, LscDatasets>();
        GetUserListRequest request = new GetUserListRequest();
        HeaderType header = new HeaderType();
        header.setGroupIdentifier(dvsService.getGroupId());
        header.setRequestId(UUID.randomUUID().toString());
        header.setUserLevel(dvsService.getUserLevel().name());
        request.setHeader(header);
        GetUserListResponse response = dvsProxy.getUserList(request);

        if(response.getGetUserListResult().getOpStatus() != DvsProvisioningProxyImpl.OpStatus.SUCCESS.getIntValue()) {
            LOGGER.error("Operation status: " + DvsProvisioningProxyImpl.OpStatus.valueOf(response.getGetUserListResult().getOpStatus()).getMessageValue());
            throw new LscServiceException(response.getGetUserListResult().getFailureDetails().toString());
        }

        if(response.getGetUserListResult().getUserList() == null) {
            // No user from the repository web service
            return null;
        }
        
        for(UserInfoType userInfo: response.getGetUserListResult().getUserList().getUserInfo()) {
            LscDatasets datasets = new LscDatasets();
            if(userInfo.getFirstName() != null) {
                datasets.put(GIVENNAME_NAME, userInfo.getFirstName());
            }
            if(userInfo.getName() != null) {
                datasets.put(SURNAME_NAME, userInfo.getName());
            }
            if(userInfo.getBirthday() != null) {
                datasets.put(BIRTHDATE_NAME, DateUtils.format(userInfo.getBirthday().toGregorianCalendar().getTime()));
            }
            if(userInfo.getCoordAuthent() != null && userInfo.getCoordAuthent().getCoordList() != null) {
                for(CoordType coord : userInfo.getCoordAuthent().getCoordList().getCoord()) {
//                    if(coord.getCoordType() == CoordSimpleType.MAIL) {
//                        datasets.put(EMAIL_NAME, coord.getCoordValue());
//                    } else 
                    	if (coord.getCoordType() == CoordSimpleType.SMS) {
                        datasets.put(PHONE_NAME, coord.getCoordValue());
                    } else {
                        throw new LscServiceException("Unknown personal data type: " + coord.getCoordType());
                    }
                }
            }
            users.put(userInfo.getClientRef(), datasets);
        }
		return users;
	}

	@Override
	public boolean apply(LscModifications lm) throws LscServiceException {
        HeaderType header = new HeaderType();
        header.setDateTime(new DatatypeFactoryImpl().newXMLGregorianCalendar(new GregorianCalendar()));
        header.setGroupIdentifier(dvsService.getGroupId());
        header.setRequestId(UUID.randomUUID().toString());
//        header.setUserLevel(dvsService.getUserLevel().toString());
	    if(lm.getOperation() == LscModificationType.CREATE_OBJECT || lm.getOperation() == LscModificationType.UPDATE_OBJECT) {
    		AddUserListRequest request = new AddUserListRequest();
    		request.setAddingMode(DvsProvisioningProxyImpl.ADD_OR_UPDATE_MODE);
    		request.setHeader(header);
    		
    		UserInfoListType userInfoList = new UserInfoListType();
    		userInfoList.getUserInfo().add(getDvsUserInfoFromLscModifications(lm));
    		request.setUserInfoList(userInfoList);
    		
    		AddUserListResponse response = dvsProxy.addUserList(request);
    		if(response.getAddUserListResult().getOpStatus() == DvsProvisioningProxy.OpStatus.SUCCESS.getIntValue()
    				&& response.getAddUserListResult().getGlobalStatus() == 0) {
    			LOGGER.debug("User successfully created or updated in DVS: " + lm.getMainIdentifier());
    			return true;
    		} else {
                LOGGER.error("Operation status: " + DvsProvisioningProxyImpl.OpStatus.valueOf(response.getAddUserListResult().getOpStatus()).getMessageValue() + " (" + response.getAddUserListResult().getOpStatus() + ")");
                LOGGER.error("Global status: " + DvsProvisioningProxyImpl.decodeGlobalStatus(response.getAddUserListResult().getGlobalStatus()));
                throw new LscServiceException("Unable to add or update DVS user=" + lm.getMainIdentifier() + " (" + response.getAddUserListResult().getFailureDetails() + ")");
    		}
        } else if(lm.getOperation() == LscModificationType.DELETE_OBJECT) {
            DeleteUserInformationListRequest request = new DeleteUserInformationListRequest();
            request.setDeletingMode("DELETE_ONLY");
            request.setHeader(header);
            UserInformationList userInformationList = new UserInformationList();
            UserInformation userInfo = new UserInformation();
            userInfo.setDropUser(true);
            userInfo.setUserAlias(lm.getMainIdentifier());
            userInformationList.getUserInformation().add(userInfo);
            request.setUserInformationList(userInformationList );
            DeleteUserInformationListResponse response = dvsProxy.deleteUserInformationListRequest(request);
            if(response.getDeleteUserInformationListResult().getOpStatus() == 0
    				&& response.getDeleteUserInformationListResult().getGlobalStatus() == 0) {
    			LOGGER.debug("User successfully delete in DVS: " + lm.getMainIdentifier());
                return true;
            } else {
                LOGGER.error("Operation status: " + DvsProvisioningProxyImpl.OpStatus.valueOf(response.getDeleteUserInformationListResult().getOpStatus()).getMessageValue() + " (" + response.getDeleteUserInformationListResult().getOpStatus() + ")");
                LOGGER.error("Global status: " + DvsProvisioningProxyImpl.decodeGlobalStatus(response.getDeleteUserInformationListResult().getGlobalStatus()));
                throw new LscServiceException("Unable to delete DVS user=" + lm.getMainIdentifier() + " (" + response.getDeleteUserInformationListResult().getFailureDetails() + ")");
            }
        } else {
            throw new LscServiceException("Unsupported operation type: " + lm.getOperation());
        }
    }

	private UserInfoType getDvsUserInfoFromLscModifications(
			LscModifications lm) throws LscServiceException {
		UserInfoType userInfo = new UserInfoType();
		userInfo.setActivationState(ActivationStateType.ACTIVE);
		if(lm.getModificationsItemsByHash().get(BIRTHDATE_NAME) != null) {
			GregorianCalendar calendar = new GregorianCalendar();
			try {
                calendar.setTime(new SimpleDateFormat(dateFormat).parse(lm.getModificationsItemsByHash().get(BIRTHDATE_NAME).get(0).toString()));
            } catch (ParseException e) {
                throw new LscServiceException("Unable to parse the birthdate: " 
                        + lm.getModificationsItemsByHash().get(BIRTHDATE_NAME).get(0).toString() 
                        + ". It should respect the format '" + dateFormat + "' !");
            }
			userInfo.setBirthday(new DatatypeFactoryImpl().newXMLGregorianCalendar(calendar));
		} else {
			// TODO: Check that it does really the job !
			userInfo.setBirthday(null);
		}
		
		userInfo.setClientRef(lm.getMainIdentifier());
		if(lm.getModificationsItemsByHash().containsKey(GIVENNAME_NAME)
				&& lm.getModificationsItemsByHash().get(GIVENNAME_NAME).size() > 0) {
	        userInfo.setFirstName(lm.getModificationsItemsByHash().get(GIVENNAME_NAME).get(0).toString());
		}
        if(lm.getModificationsItemsByHash().containsKey(SURNAME_NAME)
        		&& lm.getModificationsItemsByHash().get(SURNAME_NAME).size() > 0) {
            userInfo.setName(lm.getModificationsItemsByHash().get(SURNAME_NAME).get(0).toString());
        }
		UserInfoType.PasswordAuthent passwordAuthn = new UserInfoType.PasswordAuthent();
		if(lm.getModificationsItemsByHash().get(PASSWORD_NAME) != null
				&& lm.getModificationsItemsByHash().get(PASSWORD_NAME).size() > 0) {
			UserInfoType.PasswordAuthent.PasswordList passwords = new UserInfoType.PasswordAuthent.PasswordList();
			PasswordType password = new PasswordType();
//			password.setIdentifier(lm.getMainIdentifier());//+"_PASSWD");
			password.setPasswordType(PasswordTypeType.LOGIN);
			password.setScope(dvsService.getScope());
			String passwordStr = null;
			if(lm.getModificationsItemsByHash().get(PASSWORD_NAME).get(0) instanceof byte[]) {
				passwordStr = new String((byte[])(lm.getModificationsItemsByHash().get(PASSWORD_NAME).get(0)));
			} else if (lm.getModificationsItemsByHash().get(PASSWORD_NAME).get(0) instanceof String){
				passwordStr = (String)lm.getModificationsItemsByHash().get(PASSWORD_NAME).get(0);
			} else {
				passwordStr = (String)lm.getModificationsItemsByHash().get(PASSWORD_NAME).get(0).toString();
			}
			
			if(passwordStr.startsWith("{")) {
                password.setPasswordFormat("LDAP_FORMAT");
                password.setPasswordValue(passwordStr);
			} else {
	            password.setPasswordFormat("PLAIN_TEXT");
	            password.setPasswordValue(passwordStr);
			}

			passwords.getPassword().add(password);
			passwordAuthn.setPasswordList(passwords);
			passwordAuthn.setActivationState(ActivationStateType.ACTIVE);
		} else {
			passwordAuthn.setActivationState(ActivationStateType.SUSPENDED);
		}
		userInfo.setPasswordAuthent(passwordAuthn);
        CoordAuthent coordAuthent = new CoordAuthent();
		if(lm.getModificationsItemsByHash().get(PHONE_NAME) != null && lm.getModificationsItemsByHash().get(PHONE_NAME).size() > 0) {
	        CoordList coordList = new CoordList();
	        CoordType coordSms = new CoordType();
			coordSms.setCoordType(CoordSimpleType.SMS);
			coordSms.setCoordValue(lm.getModificationsItemsByHash().get(PHONE_NAME).get(0).toString());
			coordSms.setCancellationState(CancellationStateType.VALID);
	        coordList.getCoord().add(coordSms);
	        coordAuthent.setCoordList(coordList);
	        coordAuthent.setActivationState(ActivationStateType.ACTIVE);
		} else {
	        coordAuthent.setActivationState(ActivationStateType.SUSPENDED);
		}
        userInfo.setCoordAuthent(coordAuthent);

		return userInfo;
	}  

	@Override
	public List<String> getWriteDatasetIds() {
		return Arrays.asList(new String[] {SURNAME_NAME, GIVENNAME_NAME, PASSWORD_NAME,
				PHONE_NAME, BIRTHDATE_NAME, MODIFYTIMESTAMP_NAME, CERTIFICATE_NAME, OATH_SERIAL_NAME}); //EMAIL_NAME, 
	}

    /**
     * @see org.lsc.service.IService.getSupportedConnectionType()
     */
    public Collection<Class<? extends ConnectionType>> getSupportedConnectionType() {
        Collection<Class<? extends ConnectionType>> list = new ArrayList<Class<? extends ConnectionType>>();
        list.add(PluginConnectionType.class);
        return list;
    }

    public Collection<Class<?>> getSupportedPluginConnectionType() {
        Collection<Class<?>> list = new ArrayList<Class<?>>();
        list.add(DvsProvisioningConnectionSettings.class);
        return list;
    }
}
