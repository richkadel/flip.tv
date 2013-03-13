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

package com.appeligo.search.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.knowbout.hibernate.model.PersistentObject;

/**
 * 
 * @author fear
 *
 */
public class ToNotify extends PersistentObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -367748476543917690L;

	private static final Logger log = Logger.getLogger(ToNotify.class);
	
	private long id;
	
	private String email;
	
	private Timestamp created = new Timestamp(System.currentTimeMillis());
	
	public ToNotify() {
		
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	/**
	 * 
	 * @param email
	 * @return
	 */
	public static ToNotify findByEmail(String email) {
		Session session = getSession();
		Query query = session.getNamedQuery("ToNotify.findByEmail");
		query.setString("email", email);
		return (ToNotify)query.uniqueResult();
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}
}
	
