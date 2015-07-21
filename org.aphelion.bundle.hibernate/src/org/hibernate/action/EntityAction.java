//$Id: EntityAction.java 9673 2006-03-22 14:57:59Z steve.ebersole@jboss.com $
package org.hibernate.action;

import org.hibernate.AssertionFailure;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.util.StringHelper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Any action relating to insert/update/delete of an entity instance
 * @author Gavin King
 */
public abstract class EntityAction implements Executable, Serializable, Comparable {

	private final SessionImplementor session;
	private final Serializable id;
	private final Object instance;
	private final String entityName;

	private transient EntityPersister persister;

	protected EntityAction(SessionImplementor session, Serializable id, Object instance, EntityPersister persister) {
		this.session = session;
		this.id = id;
		this.persister = persister;
		this.instance = instance;
		this.entityName = persister.getEntityName();
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		persister = session.getFactory()
				.getEntityPersister( entityName );
	}

	public final Serializable[] getPropertySpaces() {
		return persister.getPropertySpaces();
	}

	protected final SessionImplementor getSession() {
		return session;
	}

	protected final Serializable getId() {
		if ( id instanceof DelayedPostInsertIdentifier ) {
			return session.getPersistenceContext().getEntry( instance ).getId();
		}
		return id;
	}

	protected final EntityPersister getPersister() {
		return persister;
	}

	protected final Object getInstance() {
		return instance;
	}

	public void beforeExecutions() {
		throw new AssertionFailure( "beforeExecutions() called for non-collection action" );
	}

	public boolean hasAfterTransactionCompletion() {
		return persister.hasCache() || hasPostCommitEventListeners();
	}
	
	protected abstract boolean hasPostCommitEventListeners();

	public String toString() {
		return StringHelper.unqualify( getClass().getName() ) + MessageHelper.infoString( entityName, id );
	}

	public int compareTo(Object other) {
		EntityAction action = ( EntityAction ) other;
		//sort first by entity name
		int roleComparison = entityName.compareTo( action.entityName );
		if ( roleComparison != 0 ) {
			return roleComparison;
		}
		else {
			//then by id
			return persister.getIdentifierType()
				.compare( id, action.id, session.getEntityMode() );
		}
	}
}






