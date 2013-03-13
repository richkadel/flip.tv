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

package com.appeligo.acegi;

import java.sql.Timestamp;
import java.util.Date;

import org.acegisecurity.event.authentication.AuthenticationSuccessEvent;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import com.appeligo.search.entity.User;
import com.knowbout.hibernate.HibernateUtil;
import com.knowbout.hibernate.TransactionManager;

/**
 * WTF do I do?
 */
public class AcegiApplicationListener implements ApplicationListener {

	private static final Logger log = Logger.getLogger(AcegiApplicationListener.class);
	
	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof AuthenticationSuccessEvent) {
			org.acegisecurity.userdetails.User source = (org.acegisecurity.userdetails.User)((AuthenticationSuccessEvent)event).getAuthentication().getPrincipal();
			String name = source.getUsername();
			
			// We need to be a good citizen here and not open a session if one is already available.
			boolean opened = false;
			if (!HibernateUtil.isSessionOpen()) {
				HibernateUtil.openSession();
				opened = true;
			}
			boolean startedTransaction = false;
			if (TransactionManager.currentTransaction() == null) {
				TransactionManager.beginTransaction();
				startedTransaction = true;
			}
			try {
				User user = User.findByUsername(name);
				user.setLastLogin(new Timestamp(System.currentTimeMillis()));
				user.save();
				if (log.isDebugEnabled()) {
					log.debug("Updating last login for user: " + name);
				}
				if (startedTransaction) {
					TransactionManager.commitTransaction();
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				if (startedTransaction) {
					TransactionManager.rollbackTransaction();
				}
			} finally {
				if (opened) {
					HibernateUtil.closeSession();
				}
			}
		}
		
	}

}
