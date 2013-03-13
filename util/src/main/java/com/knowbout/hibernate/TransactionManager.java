/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package com.knowbout.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;


/**
 * The <code>TransactionManager</code> class
 * @author almilli
 */
public class TransactionManager {
	
	private static final Log log = LogFactory.getLog(TransactionManager.class);

    private static final ThreadLocal<Boolean> rollbackOnly = new ThreadLocal<Boolean>();
    private static final ThreadLocal<Transaction> transaction = new ThreadLocal<Transaction>();

    /**
     * @return
     * @throws HibernateException
     */
    public static Transaction beginTransaction() throws HibernateException {
        Session session = HibernateUtil.currentSession();
        if (session == null) {
        	throw new HibernateException("No session found.");
        }
        if (currentTransaction() != null) {
        	throw new HibernateException("Transaction already started.");
        }
        Transaction t = session.beginTransaction();
        transaction.set(t);
        if (log.isDebugEnabled()) {
        	log.debug("vvvvvvvvvvvvvvvvvvvvv BEGIN TRANSACTION: Thread="+Thread.currentThread().getName()+" vvvvvvvvvvvvvvvvvvvvvvvvvvv");
        }
        return t;
    }

    public static Transaction currentTransaction() throws HibernateException {
        return transaction.get();
    }
    
    public static void setRollbackOnly() {
    	rollbackOnly.set(Boolean.TRUE);
    }
    
    public static boolean isRollbackOnly() {
    	Boolean isRollback = rollbackOnly.get();
    	if (isRollback != null) {
    		return isRollback.booleanValue();
    	} else {
    		return false;
    	}
    }

    /**
     * 
     * @throws HibernateException
     */
    public static void commitTransaction() throws HibernateException {
    	if (isRollbackOnly()) {
    		throw new HibernateException(
    				"The transaction cannot be committed because it is set to rollback only.");
    	}
    	Transaction t = currentTransaction();
        if (t != null) {
            t.commit();
	        if (log.isDebugEnabled()) {
	        	log.debug("^^^^^^^^^^^^^^^^^^^^^ COMMIT TRANSACTION: Thread="+Thread.currentThread().getName()+" ^^^^^^^^^^^^^^^^^^^^^^^^^^");
	        }
        }
        transaction.set(null);
    	rollbackOnly.set(null);
    }

    /**
     * 
     * @throws HibernateException
     */
    public static void rollbackTransaction() throws HibernateException {
    	Transaction t = currentTransaction();
        if (t != null) {
            t.rollback();
	        if (log.isDebugEnabled()) {
	        	log.debug("^^^^^^^^^^^^^^^^^^^^ ROLLBACK TRANSACTION: Thread="+Thread.currentThread().getName()+" ^^^^^^^^^^^^^^^^^^^^^^^^^");
	        }
        }
        transaction.set(null);
    	rollbackOnly.set(null);
    }
}
