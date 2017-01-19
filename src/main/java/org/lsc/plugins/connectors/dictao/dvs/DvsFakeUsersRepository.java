package org.lsc.plugins.connectors.dictao.dvs;

import java.util.ArrayList;
import java.util.List;

import org.lsc.plugins.connectors.dictao.dxs.provisioning.ws.jaxws.UserInfoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DvsFakeUsersRepository {
	
	private static Logger LOGGER = LoggerFactory.getLogger(DvsFakeUsersRepository.class);

	List<UserInfoType> users;
	
	public DvsFakeUsersRepository() {
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
