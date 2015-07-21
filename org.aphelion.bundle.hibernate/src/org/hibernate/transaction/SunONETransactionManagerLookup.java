//$Id: SunONETransactionManagerLookup.java 9192 2006-02-01 14:46:38Z epbernard $
package org.hibernate.transaction;

/**
 * TransactionManager lookup strategy for Sun ONE Application Server 7
 * @author Robert Davidson, Sanjeev Krishnan
 * @author Emmanuel Bernard
 */
public class SunONETransactionManagerLookup extends JNDITransactionManagerLookup {

	protected String getName() {
		return "java:appserver/TransactionManager";
	}

	public String getUserTransactionName() {
		return "java:comp/UserTransaction";
	}
	
}
