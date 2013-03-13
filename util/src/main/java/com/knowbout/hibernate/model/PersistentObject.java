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

package com.knowbout.hibernate.model;

import org.hibernate.LockMode;
import org.hibernate.Session;

import com.knowbout.hibernate.HibernateUtil;

/**
 * Provides a simple parent class for persistent objects to subclass when desired
 * in order to easily implement basic persistence methods.
 * @author fear
 */
public class PersistentObject {

	public void insert() {
		this.insert(this);
	}
	
	public void delete() {
		this.delete(this);
	}
	
	public void save() {
		this.save(this);
	}
	
	public void reattach() {
		this.reattach(this);
	}
	
	public boolean isAttached() {
		return HibernateUtil.currentSession().contains(this);
	}
	
	protected void save(Object object) {
		HibernateUtil.currentSession().saveOrUpdate(object);
	}
	
	protected void insert(Object object) {
		HibernateUtil.currentSession().save(object);
	}
	
	protected void delete(Object object) {
		HibernateUtil.currentSession().delete(object);
	}
	
	protected void reattach(Object object) {
		Session session = HibernateUtil.currentSession();
		if (!session.contains(object)) {
			session.lock(object, LockMode.NONE);
		}
	}
	
	protected static Session getSession() {
		return HibernateUtil.currentSession();
	}
}
