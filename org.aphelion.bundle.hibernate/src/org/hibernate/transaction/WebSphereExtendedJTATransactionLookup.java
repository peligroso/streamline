//$Id: WebSphereExtendedJTATransactionLookup.java 8047 2005-08-30 21:04:49Z oneovthafew $
package org.hibernate.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;

import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.hibernate.HibernateException;
import org.hibernate.util.NamingHelper;

/**
 * Support for proprietary interfaces for registering synchronizations in WebSphere 6.
 * @author Gavin King
 */
public class WebSphereExtendedJTATransactionLookup implements TransactionManagerLookup {
	
	public TransactionManager getTransactionManager(Properties props)
	throws HibernateException {
		return new TransactionManagerAdapter(props);
	}

	public String getUserTransactionName() {
		return "java:comp/UserTransaction";
	}
	
	public static class TransactionManagerAdapter implements TransactionManager {

		private final Properties properties;
		private final Class synchronizationCallbackClass;
		private final Method registerSynchronizationMethod;
		private final Method getLocalIdMethod;
		
		private TransactionManagerAdapter(Properties props) {
			this.properties = props;
			try {
				synchronizationCallbackClass = Class.forName("com.ibm.websphere.jtaextensions.SynchronizationCallback");
				Class extendedJTATransactionClass = Class.forName("com.ibm.websphere.jtaextensions.ExtendedJTATransaction");
				registerSynchronizationMethod = extendedJTATransactionClass.getMethod( 
						"registerSynchronizationCallbackForCurrentTran", 
						new Class[] { synchronizationCallbackClass } 
					);
				getLocalIdMethod = extendedJTATransactionClass.getMethod( "getLocalId", null );
				
			}
			catch (ClassNotFoundException cnfe) {
				throw new HibernateException(cnfe);
			}
			catch (NoSuchMethodException nsme) {
				throw new HibernateException(nsme);
			}
		}
		
		public void begin() throws NotSupportedException, SystemException {
			throw new UnsupportedOperationException();
		}

		public void commit() throws RollbackException, HeuristicMixedException,
				HeuristicRollbackException, SecurityException,
				IllegalStateException, SystemException {
			throw new UnsupportedOperationException();
		}

		public int getStatus() throws SystemException {
			throw new UnsupportedOperationException();
		}

		public Transaction getTransaction() throws SystemException {
			return new TransactionAdapter(properties);
		}

		public void resume(Transaction txn) throws  InvalidTransactionException, 
				IllegalStateException, SystemException {
			throw new UnsupportedOperationException();
		}

		public void rollback() throws IllegalStateException, SecurityException,
				SystemException {
			throw new UnsupportedOperationException();
		}

		public void setRollbackOnly() throws IllegalStateException,
				SystemException {
			throw new UnsupportedOperationException();
		}

		public void setTransactionTimeout(int i) throws SystemException {
			throw new UnsupportedOperationException();
		}

		public Transaction suspend() throws SystemException {
			throw new UnsupportedOperationException();
		}

		public class TransactionAdapter implements Transaction {
			
			private final Object extendedJTATransaction;
			
			private TransactionAdapter(Properties props) {
				try {
					extendedJTATransaction = NamingHelper.getInitialContext(props)
						.lookup("java:comp/websphere/ExtendedJTATransaction");
				}
				catch (NamingException ne) {
					throw new HibernateException(ne);
				}
			}

			public void registerSynchronization(final Synchronization synchronization)
					throws RollbackException, IllegalStateException,
					SystemException {
				
				final InvocationHandler ih = new InvocationHandler() {
					
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						if ( "afterCompletion".equals( method.getName() ) ) {
							int status = args[2].equals(Boolean.TRUE) ? 
									Status.STATUS_COMMITTED : 
									Status.STATUS_UNKNOWN;
							synchronization.afterCompletion(status);
						}
						else if ( "beforeCompletion".equals( method.getName() ) ) {
							synchronization.beforeCompletion();
						}
						else if ( "toString".equals( method.getName() ) ) {
							return synchronization.toString();
						}
						return null;
					}
					
				};
				
				final Object synchronizationCallback = Proxy.newProxyInstance( 
						getClass().getClassLoader(), 
						new Class[] { synchronizationCallbackClass }, 
						ih 
					);
				
				try {
					registerSynchronizationMethod.invoke( 
							extendedJTATransaction, 
							new Object[] { synchronizationCallback } 
						);
				}
				catch (Exception e) {
					throw new HibernateException(e);
				}

			}
			
			public int hashCode() {
				return getLocalId().hashCode();
			}
			
			public boolean equals(Object other) {
				if ( !(other instanceof TransactionAdapter) ) return false;
				TransactionAdapter that = (TransactionAdapter) other;
				return getLocalId().equals( that.getLocalId() );
			}

			private Object getLocalId() {
				try {
					return getLocalIdMethod.invoke(extendedJTATransaction, null);
				}
				catch (Exception e) {
					throw new HibernateException(e);
				}
			}

			public void commit() throws RollbackException, HeuristicMixedException,
					HeuristicRollbackException, SecurityException,
					IllegalStateException, SystemException {
				throw new UnsupportedOperationException();
			}
		
			public boolean delistResource(XAResource resource, int i)
					throws IllegalStateException, SystemException {
				throw new UnsupportedOperationException();
			}
		
			public boolean enlistResource(XAResource resource)
					throws RollbackException, IllegalStateException,
					SystemException {
				throw new UnsupportedOperationException();
			}
		
			public int getStatus() throws SystemException {
				return new Integer(0).equals( getLocalId() ) ? 
						Status.STATUS_NO_TRANSACTION : Status.STATUS_ACTIVE;
			}

			public void rollback() throws IllegalStateException, SystemException {
				throw new UnsupportedOperationException();
			}

			public void setRollbackOnly() throws IllegalStateException,
					SystemException {
				throw new UnsupportedOperationException();
			}
		}
			
	}
	
}
