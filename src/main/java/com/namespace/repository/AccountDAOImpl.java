package com.namespace.repository;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyFactory;
import com.namespace.domain.Account;
import com.namespace.domain.UserGAE;

@Component
public class AccountDAOImpl implements AccountDAO {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ObjectifyFactory objectifyFactory;

	public AccountDAOImpl() {
	}

	public AccountDAOImpl(ObjectifyFactory objectifyFactory) {
		if (objectifyFactory != null)
			logger.info("objectifyFactory was injected succesfully to accountDao: " + objectifyFactory.toString());
		this.objectifyFactory = objectifyFactory;
	}

	@Override
	public List<Account> findAll() {
		logger.info("retrieving list of Accounts from the datastore");
		return objectifyFactory.begin().load().type(Account.class).list();
	}

	@Override
	public Account findByUsername(String username) {
		try {
			Key<UserGAE> userGaeKey = Key.create(UserGAE.class, username);
			logger.info("retrieving Account with username '{}' from the datastore", username);
			return objectifyFactory.begin().load().type(Account.class).ancestor(userGaeKey).first().now();
		} catch (Exception e) {
			logger.info(
					"cannot retrieve username='{}' Account from the datastore. Should be for two reasons: The account associated with this user doest'n exist, of they are not any accounts in the datastore",
					username);
			return null;
		}
	}

	@Override
	public void create(Account account) {
		objectifyFactory.begin().save().entity(account).now();
	}

	@Override
	public boolean update(Account account) {
		logger.info("update()");
		if (account == null)
			return false;
		final Account found = objectifyFactory.begin().load().type(Account.class).id(account.getId()).now();
		logger.info("verify if this account already exist " + "in the datastore: " + account.toString());
		final boolean thisAccountAlreadyExist = (found != null);

		if (thisAccountAlreadyExist) {
			logger.info("Confirmed: this account already exist.");
			objectifyFactory.begin().save().entity(account).now();
			return true;
		} else {
			logger.info("This account doesn't exist at the datastore or " + "something whas wrong (might be the ancestor reference");
			return false;
		}
	}

	@Override
	public boolean remove(Account item) {
		objectifyFactory.begin().delete().entity(item).now();
		return true;
	}

}
