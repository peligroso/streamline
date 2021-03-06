/**
 * Copyright (c) Rich Hickey. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 */

package com.trifork.clj_ds;

import java.util.Iterator;
import java.util.Map;


public interface IPersistentMap<K,V> extends Iterable<Map.Entry<K, V>>, Associative<K,V>, Counted{


IPersistentMap<K,V> assoc(K key, V val);

IPersistentMap<K,V> assocEx(K key, V val) throws Exception;

IPersistentMap<K,V> without(K key) throws Exception;

Iterator<Map.Entry<K, V>> iteratorFrom(K key);

Iterator<Map.Entry<K, V>> reverseIterator();

}
