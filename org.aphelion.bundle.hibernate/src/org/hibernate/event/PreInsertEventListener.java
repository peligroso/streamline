//$Id: PreInsertEventListener.java 7581 2005-07-20 22:48:22Z oneovthafew $
package org.hibernate.event;

import java.io.Serializable;

/**
 * Called before inserting an item in the datastore
 * 
 * @author Gavin King
 */
public interface PreInsertEventListener extends Serializable {
	public boolean onPreInsert(PreInsertEvent event);
}
