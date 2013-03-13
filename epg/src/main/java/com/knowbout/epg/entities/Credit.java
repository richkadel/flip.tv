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

package com.knowbout.epg.entities;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;

import com.knowbout.hibernate.HibernateUtil;

public class Credit {

	
	private long id;
	private CreditType type;
	private String firstName;
	private String lastName;
	private String roleDescription;
	private Set<Program> programs;
	
	public Credit() {		
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
	 * @return Returns the id.
	 */
	public long getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(long id) {
		this.id = id;
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
	/**
	 * @return Returns the type.
	 */
	public CreditType getType() {
		return type;
	}
	/**
	 * @param type The type to set.
	 */
	public void setType(CreditType type) {
		this.type = type;
	}
	
	
	
	/**
	 * @return Returns the programs.
	 */
	public Set<Program> getPrograms() {
		if (programs == null) {
			programs = new HashSet<Program>();
		}
		return programs;
	}
	
	public void addProgram(Program program) {
		Set<Program> programs = getPrograms();
		if (!programs.contains(program)) {
			getPrograms().add(program);
			Set<Credit> credits = program.getCredits();
			if (!credits.contains(this)){
				credits.add(this);
			}
		}
	}
	
	public void removeProgram(Program program){
		getPrograms().remove(program);
		program.getCredits().remove(this);
	}
	
	/**
	 * @param programs The programs to set.
	 */
	public void setPrograms(Set<Program> programs) {
		this.programs = programs;
	}
	
	public void insert() {
		Session session = HibernateUtil.currentSession();
		session.saveOrUpdate(this);
	}
	
	public void delete() {
		Session session = HibernateUtil.currentSession();
		session.delete(this);
	}
	
	public static Credit selectById(String creditId) {
		Session session = HibernateUtil.currentSession();
		return (Credit)session.get(Credit.class, creditId);
	}
	
	public static Credit selectByValues(CreditType type, String firstName, String lastName, String roleDescription) {
		Session session = HibernateUtil.currentSession();
		Query query = session.getNamedQuery("Credit.getByValues");
		query.setInteger("type", type.ordinal());
		query.setString("firstName", firstName);
		query.setString("lastName", lastName);
		query.setString("roleDescription", roleDescription);
		return (Credit)query.uniqueResult();
	}		
	
	
	public boolean equals(Object obj) {
		boolean equal = true;
		if (obj instanceof Credit) {
			Credit credit = (Credit)obj;
			if (!type.equals(credit.type)) {
				equal = false;
			} else if ((firstName != null && !firstName.equals(credit.firstName)) || (firstName == null && credit.firstName != null)) {
				equal = false;
			} else if ((lastName != null && !lastName.equals(credit.firstName)) || (lastName == null && credit.lastName != null)) {
				equal = false;
			} else if ((roleDescription != null && !roleDescription.equals(credit.roleDescription)) || (roleDescription == null && credit.roleDescription != null)) {
				equal = false;
			} 
		} else {
			equal = false;
		}
		return equal;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {		
		return firstName.hashCode() ^ lastName.hashCode() ^ roleDescription.hashCode() ^ type.hashCode();
	}
	
	
}
