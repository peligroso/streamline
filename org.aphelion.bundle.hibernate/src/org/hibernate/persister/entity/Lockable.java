package org.hibernate.persister.entity;

/**
 * Contract for things that can be locked via a {@link org.hibernate.dialect.lock.LockingStrategy}
 *
 * @author Steve Ebersole
 * @since 3.2
 */
public interface Lockable extends EntityPersister {
	/**
	 * Locks are always applied to the "root table".
	 *
	 * @return The root table name
	 */
	public String getRootTableName();

	/**
	 * Get the names of columns on the root table used to persist the identifier.
	 *
	 * @return The root table identifier column names.
	 */
	public String[] getRootTableIdentifierColumnNames();

	/**
	 * For versioned entities, get the name of the column (again, expected on the
	 * root table) used to store the version values.
	 *
	 * @return The version column name.
	 */
	public String getVersionColumnName();
}
