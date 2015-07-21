//$Id: StatisticsImpl.java 9319 2006-02-22 21:40:50Z steveebersole $
package org.hibernate.stat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.Cache;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.util.ArrayHelper;

/**
 * @see org.hibernate.stat.Statistics 
 *  
 * @author Gavin King
 */
public class StatisticsImpl implements Statistics, StatisticsImplementor {
	
	//TODO: we should provide some way to get keys of collection of statistics to make it easier to retrieve from a GUI perspective
	
	private static final Log log = LogFactory.getLog(StatisticsImpl.class);

	private SessionFactoryImplementor sessionFactory;

	private boolean isStatisticsEnabled;
	private long startTime;
	private long sessionOpenCount;
	private long sessionCloseCount;
	private long flushCount;
	private long connectCount;
	
	private long prepareStatementCount;
	private long closeStatementCount;
	
	private long entityLoadCount;
	private long entityUpdateCount;
	private long entityInsertCount;
	private long entityDeleteCount;
	private long entityFetchCount;
	private long collectionLoadCount;
	private long collectionUpdateCount;
	private long collectionRemoveCount;
	private long collectionRecreateCount;
	private long collectionFetchCount;
	
	private long secondLevelCacheHitCount;
	private long secondLevelCacheMissCount;
	private long secondLevelCachePutCount;
	
	private long queryExecutionCount;
	private long queryExecutionMaxTime;
	private String queryExecutionMaxTimeQueryString;
	private long queryCacheHitCount;
	private long queryCacheMissCount;
	private long queryCachePutCount;
	
	private long commitedTransactionCount;
	private long transactionCount;
	
	private long optimisticFailureCount;
	
	/** second level cache statistics per region */
	private final Map secondLevelCacheStatistics = new HashMap();
	/** entity statistics per name */
	private final Map entityStatistics = new HashMap();
	/** collection statistics per name */
	private final Map collectionStatistics = new HashMap();
	/** entity statistics per query string (HQL or SQL) */
	private final Map queryStatistics = new HashMap();

	public StatisticsImpl() {
		clear();
	}

	public StatisticsImpl(SessionFactoryImplementor sessionFactory) {
		clear();
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * reset all statistics
	 */
	public synchronized void clear() {
		secondLevelCacheHitCount = 0;
		secondLevelCacheMissCount = 0;
		secondLevelCachePutCount = 0;
		
		sessionCloseCount = 0;
		sessionOpenCount = 0;
		flushCount = 0;
		connectCount = 0;
		
		prepareStatementCount = 0;
		closeStatementCount = 0;
		
		entityDeleteCount = 0;
		entityInsertCount = 0;
		entityUpdateCount = 0;
		entityLoadCount = 0;
		entityFetchCount = 0;
		
		collectionRemoveCount = 0;
		collectionUpdateCount = 0;
		collectionRecreateCount = 0;
		collectionLoadCount = 0;
		collectionFetchCount = 0;
		
		queryExecutionCount = 0;
		queryCacheHitCount = 0;
		queryExecutionMaxTime = 0;
		queryExecutionMaxTimeQueryString = null;
		queryCacheMissCount = 0;
		queryCachePutCount = 0;
		
		transactionCount = 0;
		commitedTransactionCount = 0;
		
		optimisticFailureCount = 0;
		
		secondLevelCacheStatistics.clear();
		entityStatistics.clear();
		collectionStatistics.clear();
		queryStatistics.clear();
		
		startTime = System.currentTimeMillis();
	}
	
	public synchronized void openSession() {
		sessionOpenCount++;
	}
	
	public synchronized void closeSession() {
		sessionCloseCount++;
	}
	
	public synchronized void flush() {
		flushCount++;
	}
	
	public synchronized void connect() {
		connectCount++;
	}
	
	public synchronized void loadEntity(String entityName) {
		entityLoadCount++;
		getEntityStatistics(entityName).loadCount++;
	}

	public synchronized void fetchEntity(String entityName) {
		entityFetchCount++;
		getEntityStatistics(entityName).fetchCount++;
	}

	/**
	 * find entity statistics per name
	 * 
	 * @param entityName entity name
	 * @return EntityStatistics object
	 */
	public synchronized EntityStatistics getEntityStatistics(String entityName) {
		EntityStatistics es = (EntityStatistics) entityStatistics.get(entityName);
		if (es==null) {
			es = new EntityStatistics(entityName);
			entityStatistics.put(entityName, es);
		}
		return es;
	}
	
	public synchronized void updateEntity(String entityName) {
		entityUpdateCount++;
		EntityStatistics es = getEntityStatistics(entityName);
		es.updateCount++;
	}

	public synchronized void insertEntity(String entityName) {
		entityInsertCount++;
		EntityStatistics es = getEntityStatistics(entityName);
		es.insertCount++;
	}

	public synchronized void deleteEntity(String entityName) {
		entityDeleteCount++;
		EntityStatistics es = getEntityStatistics(entityName);
		es.deleteCount++;
	}

	/**
	 * Get collection statistics per role
	 * 
	 * @param role collection role
	 * @return CollectionStatistics
	 */
	public synchronized CollectionStatistics getCollectionStatistics(String role) {
		CollectionStatistics cs = (CollectionStatistics) collectionStatistics.get(role);
		if (cs==null) {
			cs = new CollectionStatistics(role);
			collectionStatistics.put(role, cs);
		}
		return cs;
	}
	
	public synchronized void loadCollection(String role) {
		collectionLoadCount++;
		getCollectionStatistics(role).loadCount++;
	}

	public synchronized void fetchCollection(String role) {
		collectionFetchCount++;
		getCollectionStatistics(role).fetchCount++;
	}

	public synchronized void updateCollection(String role) {
		collectionUpdateCount++;
		getCollectionStatistics(role).updateCount++;
	}

	public synchronized void recreateCollection(String role) {
		collectionRecreateCount++;
		getCollectionStatistics(role).recreateCount++;
	}

	public synchronized void removeCollection(String role) {
		collectionRemoveCount++;
		getCollectionStatistics(role).removeCount++;
	}
	
	/**
	 * Second level cache statistics per region
	 * 
	 * @param regionName region name
	 * @return SecondLevelCacheStatistics
	 */
	public synchronized SecondLevelCacheStatistics getSecondLevelCacheStatistics(String regionName) {
		SecondLevelCacheStatistics slcs = (SecondLevelCacheStatistics) secondLevelCacheStatistics.get(regionName);
		if (slcs==null) {
			if (sessionFactory == null) return null;
			Cache cache = sessionFactory.getSecondLevelCacheRegion(regionName);
			if (cache==null) return null;
			slcs = new SecondLevelCacheStatistics(cache);
			secondLevelCacheStatistics.put(regionName, slcs);
		}
		return slcs;
	}

	public synchronized void secondLevelCachePut(String regionName) {
		secondLevelCachePutCount++;
		getSecondLevelCacheStatistics(regionName).putCount++;
	}

	public synchronized void secondLevelCacheHit(String regionName) {
		secondLevelCacheHitCount++;
		getSecondLevelCacheStatistics(regionName).hitCount++;
	}

	public synchronized void secondLevelCacheMiss(String regionName) {
		secondLevelCacheMissCount++;
		getSecondLevelCacheStatistics(regionName).missCount++;
	}

	public synchronized void queryExecuted(String hql, int rows, long time) {
		queryExecutionCount++;
		if (queryExecutionMaxTime<time) {
			queryExecutionMaxTime=time;
			queryExecutionMaxTimeQueryString = hql;
		}
		if (hql!=null) {
			QueryStatistics qs = getQueryStatistics(hql);
			qs.executed(rows, time);
		}
	}
	
	public synchronized void queryCacheHit(String hql, String regionName) {
		queryCacheHitCount++;
		if (hql!=null) {
			QueryStatistics qs = getQueryStatistics(hql);
			qs.cacheHitCount++;
		}
		SecondLevelCacheStatistics slcs = getSecondLevelCacheStatistics(regionName);
		slcs.hitCount++;
	}

	public synchronized void queryCacheMiss(String hql, String regionName) {
		queryCacheMissCount++;
		if (hql!=null) {
			QueryStatistics qs = getQueryStatistics(hql);
			qs.cacheMissCount++;
		}
		SecondLevelCacheStatistics slcs = getSecondLevelCacheStatistics(regionName);
		slcs.missCount++;
	}

	public synchronized void queryCachePut(String hql, String regionName) {
		queryCachePutCount++;
		if (hql!=null) {
			QueryStatistics qs = getQueryStatistics(hql);
			qs.cachePutCount++;
		}
		SecondLevelCacheStatistics slcs = getSecondLevelCacheStatistics(regionName);
		slcs.putCount++;
	}

	/**
	 * Query statistics from query string (HQL or SQL)
	 * 
	 * @param queryString query string
	 * @return QueryStatistics
	 */
	public synchronized QueryStatistics getQueryStatistics(String queryString) {
		QueryStatistics qs = (QueryStatistics) queryStatistics.get(queryString);
		if (qs==null) {
			qs = new QueryStatistics(queryString);
			queryStatistics.put(queryString, qs);
		}
		return qs;
	}

	/**
	 * @return entity deletion count
	 */
	public long getEntityDeleteCount() {
		return entityDeleteCount;
	}
	
	/**
	 * @return entity insertion count
	 */
	public long getEntityInsertCount() {
		return entityInsertCount;
	}
	
	/**
	 * @return entity load (from DB)
	 */
	public long getEntityLoadCount() {
		return entityLoadCount;
	}
	
	/**
	 * @return entity fetch (from DB)
	 */
	public long getEntityFetchCount() {
		return entityFetchCount;
	}

	/**
	 * @return entity update
	 */
	public long getEntityUpdateCount() {
		return entityUpdateCount;
	}

	public long getQueryExecutionCount() {
		return queryExecutionCount;
	}
	
	public long getQueryCacheHitCount() {
		return queryCacheHitCount;
	}
	
	public long getQueryCacheMissCount() {
		return queryCacheMissCount;
	}
	
	public long getQueryCachePutCount() {
		return queryCachePutCount;
	}
	
	/**
	 * @return flush
	 */
	public long getFlushCount() {
		return flushCount;
	}
	
	/**
	 * @return session connect
	 */
	public long getConnectCount() {
		return connectCount;
	}

	/**
	 * @return second level cache hit
	 */
	public long getSecondLevelCacheHitCount() {
		return secondLevelCacheHitCount;
	}

	/**
	 * @return second level cache miss
	 */
	public long getSecondLevelCacheMissCount() {
		return secondLevelCacheMissCount;
	}
	
	/**
	 * @return second level cache put
	 */
	public long getSecondLevelCachePutCount() {
		return secondLevelCachePutCount;
	}

	/**
	 * @return session closing
	 */
	public long getSessionCloseCount() {
		return sessionCloseCount;
	}
	
	/**
	 * @return session opening
	 */
	public long getSessionOpenCount() {
		return sessionOpenCount;
	}

	/**
	 * @return collection loading (from DB)
	 */
	public long getCollectionLoadCount() {
		return collectionLoadCount;
	}

	/**
	 * @return collection fetching (from DB)
	 */
	public long getCollectionFetchCount() {
		return collectionFetchCount;
	}
	
	/**
	 * @return collection update
	 */
	public long getCollectionUpdateCount() {
		return collectionUpdateCount;
	}

	/**
	 * @return collection removal
	 * FIXME: even if isInverse="true"?
	 */
	public long getCollectionRemoveCount() {
		return collectionRemoveCount;
	}
	/**
	 * @return collection recreation
	 */
	public long getCollectionRecreateCount() {
		return collectionRecreateCount;
	}

	/**
	 * @return start time in ms (JVM standards {@link System#currentTimeMillis()})
	 */
	public long getStartTime() {
		return startTime;
	}
	
	/**
	 * log in info level the main statistics
	 */
	public void logSummary() {
		log.info("Logging statistics....");
		log.info("start time: " + startTime);
		log.info("sessions opened: " + sessionOpenCount);
		log.info("sessions closed: " + sessionCloseCount);
		log.info("transactions: " + transactionCount);
		log.info("successful transactions: " + commitedTransactionCount);
		log.info("optimistic lock failures: " + optimisticFailureCount);
		log.info("flushes: " + flushCount);
		log.info("connections obtained: " + connectCount);
		log.info("statements prepared: " + prepareStatementCount);
		log.info("statements closed: " + closeStatementCount);
		log.info("second level cache puts: " + secondLevelCachePutCount);
		log.info("second level cache hits: " + secondLevelCacheHitCount);
		log.info("second level cache misses: " + secondLevelCacheMissCount);
		log.info("entities loaded: " + entityLoadCount);
		log.info("entities updated: " + entityUpdateCount);
		log.info("entities inserted: " + entityInsertCount);
		log.info("entities deleted: " + entityDeleteCount);
		log.info("entities fetched (minimize this): " + entityFetchCount);
		log.info("collections loaded: " + collectionLoadCount);
		log.info("collections updated: " + collectionUpdateCount);
		log.info("collections removed: " + collectionRemoveCount);
		log.info("collections recreated: " + collectionRecreateCount);
		log.info("collections fetched (minimize this): " + collectionFetchCount);
		log.info("queries executed to database: " + queryExecutionCount);
		log.info("query cache puts: " + queryCachePutCount);
		log.info("query cache hits: " + queryCacheHitCount);
		log.info("query cache misses: " + queryCacheMissCount);
		log.info("max query time: " + queryExecutionMaxTime + "ms");
	}
	
	/**
	 * Are statistics logged
	 */
	public boolean isStatisticsEnabled() {
		return isStatisticsEnabled;
	}
	
	/**
	 * Enable statistics logs (this is a dynamic parameter)
	 */
	public void setStatisticsEnabled(boolean b) {
		isStatisticsEnabled = b;
	}

	/**
	 * @return Returns the max query execution time,
	 * for all queries
	 */
	public long getQueryExecutionMaxTime() {
		return queryExecutionMaxTime;
	}
	
	/**
	 * Get all executed query strings
	 */
	public String[] getQueries() {
		return ArrayHelper.toStringArray( queryStatistics.keySet() );
	}
	
	/**
	 * Get the names of all entities
	 */
	public String[] getEntityNames() {
		if (sessionFactory==null) {
			return ArrayHelper.toStringArray( entityStatistics.keySet() );
		}
		else {
			return ArrayHelper.toStringArray( sessionFactory.getAllClassMetadata().keySet() );
		}
	}

	/**
	 * Get the names of all collection roles
	 */
	public String[] getCollectionRoleNames() {
		if (sessionFactory==null) {
			return ArrayHelper.toStringArray( collectionStatistics.keySet() );
		}
		else {
			return ArrayHelper.toStringArray( sessionFactory.getAllCollectionMetadata().keySet() );
		}
	}
	
	/**
	 * Get all second-level cache region names
	 */
	public String[] getSecondLevelCacheRegionNames() {
		if (sessionFactory==null) {
			return ArrayHelper.toStringArray( secondLevelCacheStatistics.keySet() );
		}
		else {
			return ArrayHelper.toStringArray( sessionFactory.getAllSecondLevelCacheRegions().keySet() );
		}
	}

	public void endTransaction(boolean success) {
		transactionCount++;
		if (success) commitedTransactionCount++;
	}
	
	public long getSuccessfulTransactionCount() {
		return commitedTransactionCount;
	}
	
	public long getTransactionCount() {
		return transactionCount;
	}

	public void closeStatement() {
		closeStatementCount++;
	}

	public void prepareStatement() {
		prepareStatementCount++;
	}

	public long getCloseStatementCount() {
		return closeStatementCount;
	}

	public long getPrepareStatementCount() {
		return prepareStatementCount;
	}

	public void optimisticFailure(String entityName) {
		optimisticFailureCount++;
		getEntityStatistics(entityName).optimisticFailureCount++;
	}

	public long getOptimisticFailureCount() {
		return optimisticFailureCount;
	}
	public String toString() {
		return new StringBuffer()
			.append("Statistics[")
			.append("start time=").append(startTime)
			.append(",sessions opened=").append(sessionOpenCount)
			.append(",sessions closed=").append(sessionCloseCount)
			.append(",transactions=").append(transactionCount)
			.append(",successful transactions=").append(commitedTransactionCount)
			.append(",optimistic lock failures=").append(optimisticFailureCount)
			.append(",flushes=").append(flushCount)
			.append(",connections obtained=").append(connectCount)
			.append(",statements prepared=").append(prepareStatementCount)
			.append(",statements closed=").append(closeStatementCount)
			.append(",second level cache puts=").append(secondLevelCachePutCount)
			.append(",second level cache hits=").append(secondLevelCacheHitCount)
			.append(",second level cache misses=").append(secondLevelCacheMissCount)
			.append(",entities loaded=").append(entityLoadCount)
			.append(",entities updated=").append(entityUpdateCount)
			.append(",entities inserted=").append(entityInsertCount)
			.append(",entities deleted=").append(entityDeleteCount)
			.append(",entities fetched=").append(entityFetchCount)
			.append(",collections loaded=").append(collectionLoadCount)
			.append(",collections updated=").append(collectionUpdateCount)
			.append(",collections removed=").append(collectionRemoveCount)
			.append(",collections recreated=").append(collectionRecreateCount)
			.append(",collections fetched=").append(collectionFetchCount)
			.append(",queries executed to database=").append(queryExecutionCount)
			.append(",query cache puts=").append(queryCachePutCount)
			.append(",query cache hits=").append(queryCacheHitCount)
			.append(",query cache misses=").append(queryCacheMissCount)
			.append(",max query time=").append(queryExecutionMaxTime)
			.append(']')
			.toString();
	}

	public String getQueryExecutionMaxTimeQueryString() {
		return queryExecutionMaxTimeQueryString;
	}
	
}