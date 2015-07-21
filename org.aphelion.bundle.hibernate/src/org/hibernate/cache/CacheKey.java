//$Id: CacheKey.java 6753 2005-05-11 22:17:44Z oneovthafew $
package org.hibernate.cache;

import java.io.Serializable;

import org.hibernate.EntityMode;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.type.Type;

/**
 * Allows multiple entity classes / collection roles to be 
 * stored in the same cache region. Also allows for composite 
 * keys which do not properly implement equals()/hashCode().
 * 
 * @author Gavin King
 */
public class CacheKey implements Serializable {
	private final Serializable key;
	private final Type type;
	private final String entityOrRoleName;
	private final EntityMode entityMode;
	private final int hashCode;
	
	/**
	 * Construct a new key for a collection or entity instance.
	 * Note that an entity name should always be the root entity 
	 * name, not a subclass entity name.
	 */
	public CacheKey(
			final Serializable id, 
			final Type type, 
			final String entityOrRoleName, 
			final EntityMode entityMode, 
			final SessionFactoryImplementor factory
	) {
		this.key = id;
		this.type = type;
		this.entityOrRoleName = entityOrRoleName;
		this.entityMode = entityMode;
		hashCode = type.getHashCode(key, entityMode, factory);
	}

	//Mainly for OSCache
	public String toString() {
		return entityOrRoleName + '#' + key.toString();//"CacheKey#" + type.toString(key, sf);
	}

	public boolean equals(Object other) {
		if ( !(other instanceof CacheKey) ) return false;
		CacheKey that = (CacheKey) other;
		return type.isEqual(key, that.key, entityMode) && 
			entityOrRoleName.equals(that.entityOrRoleName);
	}

	public int hashCode() {
		return hashCode;
	}
	
	public Serializable getKey() {
		return key;
	}
	
	public String getEntityOrRoleName() {
		return entityOrRoleName;
	}

}
