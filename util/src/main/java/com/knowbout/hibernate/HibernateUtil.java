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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.event.DeleteEvent;
import org.hibernate.event.DeleteEventListener;
import org.hibernate.event.EventListeners;

/**
 * The <code>HibernateUtil</code> class
 * @author almilli
 */
public class HibernateUtil {
	private static final Log log = LogFactory.getLog(HibernateUtil.class);

    private static SessionFactory sessionFactory;
    private static Configuration config;
    private static LinkedList<DeleteHandler<?>> deleteHandlers;
    
    static {
        try {
            // Create the SessionFactory
        	config = new Configuration().configure();
        } catch (Throwable ex) {
            log.error("Initial SessionFactory creation failed:" + ex.getMessage(), ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private synchronized static SessionFactory getSessionFactory() {
    	if (sessionFactory == null) {
    		sessionFactory = config.buildSessionFactory();
    	}
    	return sessionFactory;
    }
    
    /**
     * This will add properties to the hibernate configuration for each entry that has 
     * a non null value and will remove properties for entries will a null value. It will also attempt 
     * to close the old SessionFactory and create a new one based on the new properties.
     * All sessions must be closed before this method is called.
     * @param properties
     */
    public synchronized static void setProperties(HashMap<String, String> properties) {
    	Properties currentProps = config.getProperties();
    	Set<Entry<String, String>> entries = properties.entrySet();
    	for (Entry<String, String> entry: entries) {
    		if (entry.getValue() == null) {
    			currentProps.remove(entry.getKey());    			
    		} else {
    			currentProps.setProperty(entry.getKey(), entry.getValue());
    		}
    	}
    	if (sessionFactory != null) {
	    	sessionFactory.close();
	    	sessionFactory = null;
    	}
    }

    public static final ThreadLocal<Session> session = new ThreadLocal<Session>();

    public static Session openSession() {
        Session s = session.get();
        if (s != null) {
        	throw new HibernateException("A session is already open.");
        }
        s = getSessionFactory().openSession();
        session.set(s);
        return s;
    }

    public static Session currentSession() {
        Session s = session.get();
        if (s == null) {
        	throw new HibernateException("No session is currently open.");
        }
        return s;
    }

    public static void closeSession() {
        Session s = session.get();
        if (s != null)
            s.close();
        session.set(null);
    }
    
    public static boolean isSessionOpen() {
    	return session.get() != null;
    }

	public static synchronized void addDeleteHandler(DeleteHandler<?> deleteHandler) {
		if (deleteHandlers == null) {
			deleteHandlers = new LinkedList<DeleteHandler<?>>();
			EventListeners els = config.getEventListeners();
			DeleteEventListener[] dels = els.getDeleteEventListeners();
			DeleteEventListener[] newDels = new DeleteEventListener[dels.length+1];
			System.arraycopy(dels, 0, newDels, 1, dels.length);
			newDels[0] = new DeleteEventListener() {

				private static final long serialVersionUID = 0L;

				public void onDelete(DeleteEvent event) throws HibernateException {
					Object deleted = event.getObject();
					for (DeleteHandler<?> dh : deleteHandlers) {
						Class<?> handledType = (Class)((ParameterizedType)dh.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
						if (handledType.isInstance(deleted)) {
							/* The following should work but won't compile, so I'll invoke through reflection
							dh.onDelete(handledType.cast(deleted));
							*/
							Class clazz = dh.getClass();
							try {
								Method method = clazz.getMethod("onDelete", new Class[]{handledType});
								method.invoke(dh, new Object[]{deleted});
							} catch (Exception e) {
								throw new Error("Bug in HibernateUtil.addDeleteHandler()");
							}
						}
					}
				}
				
			};
		}
		deleteHandlers.add(0, deleteHandler);
	}
}
