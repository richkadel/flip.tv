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

package com.knowbout.epg.service;

import java.io.Serializable;

public class Credit implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1965760685524994521L;
	private long creditId;
	private String firstName;
	private String lastName;
	private String roleDescription;
			
	/**
	 * @param creditId
	 * @param firstName
	 * @param lastName
	 * @param roleDescription
	 */
	public Credit(long creditId, String firstName, String lastName, String roleDescription) {
		// TODO Auto-generated constructor stub
		this.creditId = creditId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.roleDescription = roleDescription;
	}
	/**
	 * @return Returns the creditId.
	 */
	public long getCreditId() {
		return creditId;
	}
	/**
	 * @param creditId The creditId to set.
	 */
	public void setCreditId(long creditId) {
		this.creditId = creditId;
	}
	/**
	 * @return Returns the firstName.
	 */
	public String getFirstName() {
		return firstName;
	}
	/**
	 * @param firstName The firstName to set.
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	/**
	 * @return Returns the lastName.
	 */
	public String getLastName() {
		return lastName;
	}
	/**
	 * @param lastName The lastName to set.
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	/**
	 * @return Returns the roleDescription.
	 */
	public String getRoleDescription() {
		return roleDescription;
	}
	/**
	 * @param roleDescription The roleDescription to set.
	 */
	public void setRoleDescription(String roleDescription) {
		this.roleDescription = roleDescription;
	}

	
}
