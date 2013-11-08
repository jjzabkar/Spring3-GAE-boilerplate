package com.namespace.repository;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.VoidWork;
import com.namespace.domain.Account;
import com.namespace.domain.UserGAE;

@Component
public class UserGaeDAOImpl implements UserGaeDAO {

	@Autowired
	private ObjectifyFactory objectifyFactory;

	private static final Logger logger = LoggerFactory.getLogger(UserGaeDAOImpl.class);

	public UserGaeDAOImpl() {
	}

	public UserGaeDAOImpl(ObjectifyFactory objectifyFactory) {
		this.objectifyFactory = objectifyFactory;
	}

	@Override
	public List<UserGAE> findAll() {
		return objectifyFactory.begin().load().type(UserGAE.class).list();
	}

	@Override
	public List<UserGAE> findAllEnabledUsers(boolean isEnabled) {
		try {
			logger.info("retrieving the users list from the datastore");
			List<UserGAE> users = objectifyFactory.begin().load().type(UserGAE.class).filterKey("enabled", isEnabled).list();
			return users;

		} catch (Exception e) {
			logger.info("cannot retrieve the users " + "from the datastore. Should be for two reasons: "
					+ "The account associated with this user doest'n exist, of " + "they are not any accounts in the datastore");
			return new ArrayList<UserGAE>();
		}

	}

	@Override
	public void create(UserGAE user) throws Exception {
		if (user != null) {
			objectifyFactory.begin().save().entity(user);
		} else {
			throw new Exception("You can't create a null user");
		}
	}

	@Override
	public boolean update(UserGAE user) {

		if (user == null)
			return false;

		UserGAE found = findByUsername(user.getUsername());

		boolean thisAccountAlreadyExist = (found != null);

		if (thisAccountAlreadyExist) {
			objectifyFactory.begin().save().entity(user);
			return true;
		} else {
			return false;
		}

	}

	@Override
	public boolean remove(UserGAE user) {
		objectifyFactory.begin().delete().entity(user).now();
		return true;
	}

	@Override
	public UserGAE findByUsername(String username) {
		return objectifyFactory.begin().load().type(UserGAE.class).filterKey("username", username).first().now();
	}

	// TODO:Eliminar esta clase
	@Override
	public void createUserAccount(final UserGAE user, final Account account) {

		objectifyFactory.begin().transact(new VoidWork() {
			public void vrun() {
				logger.info("ofy.put(user) will be realized now");
				objectifyFactory.begin().save().entity(user).now();
				logger.info("ofy.put(user) was realized sucessfully");
				Key<UserGAE> userGaeKey = Key.create(UserGAE.class, user.getUsername());
				logger.info("The username to be stored is:" + user.getUsername());
				account.setUser(userGaeKey);
				logger.info("ofy.put(account) will be realized now");
				objectifyFactory.begin().save().entity(account).now();
				logger.info("ofy.put(account) was realized sucessfully");

				// logger.info("ofy.put(scheduler) will be realized now");
				// ofy.put(scheduler);
				// logger.info("ofy.put(scheduler) was realized successfully");
			}
		});

	}

	@Override
	public UserGAE findByAccount(Account account) {
		Key<UserGAE> userAccount = account.getUser();
		if (userAccount != null) {
			return objectifyFactory.begin().load().type(UserGAE.class).id(userAccount.getId()).now();
		}
		return null;
	}

}
