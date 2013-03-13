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

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.appeligo.epg.DefaultEpg;
import com.knowbout.epg.service.Program;
import com.knowbout.hibernate.model.PersistentObject;

public class Favorite  extends PersistentObject {
	
	private long id;
	private User user;
	private Date created;
	private String programId;
	private boolean deleted;
	private String label;
	private double rank;
	private Program program;
	
	
	public Favorite() {		
	}


	/**
	 * @return Returns the created.
	 */
	public Date getCreated() {
		return created;
	}


	/**
	 * @param created The created to set.
	 */
	public void setCreated(Date created) {
		this.created = created;
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
	 * @return Returns the programId.
	 */
	public String getProgramId() {
		return programId;
	}


	/**
	 * @param programId The programId to set.
	 */
	public void setProgramId(String programId) {
		this.programId = programId;
	}


	/**
	 * @return Returns the user.
	 */
	public User getUser() {
		return user;
	}


	/**
	 * @param user The user to set.
	 */
	public void setUser(User user) {
		this.user = user;
	}


	/**
	 * @return Returns the deleted.
	 */
	public boolean isDeleted() {
		return deleted;
	}


	/**
	 * @param deleted The deleted to set.
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}


	/**
	 * @return Returns the rank.
	 */
	public double getRank() {
		return rank;
	}


	/**
	 * @param rank The rank to set.
	 */
	public void setRank(double rank) {
		this.rank = rank;
	}


	/*
	public static List<Favorite> findFavorites(User user) {
    	Permissions.checkUser(user);
		Session session = getSession();
		Query query = session.getNamedQuery("Favorite.getUserFavorites");
		query.setEntity("user", user);
		return query.list();
	}
	*/
	
    public static Favorite getTopFavoriteShow(User user) {
    	Permissions.checkUser(user);
        Session session = getSession();
        Query query = session.getNamedQuery("Favorite.getFavoriteNonEpisodes");
        query.setMaxResults(1);
        query.setEntity("user", user);
        return (Favorite)query.uniqueResult();
    }

    public static Favorite getTopFavoriteEpisode(User user) {
    	Permissions.checkUser(user);
        Session session = getSession();
        Query query = session.getNamedQuery("Favorite.getFavoriteEpisodes");
        query.setMaxResults(1);
        query.setEntity("user", user);
        return (Favorite)query.uniqueResult();
    }	
	
    public static List<Favorite> getFavoriteShows(User user) {
    	Permissions.checkUser(user);
        Session session = getSession();
        Query query = session.getNamedQuery("Favorite.getFavoriteNonEpisodes");
        query.setEntity("user", user);
        return query.list();
    }

    public static List<Favorite> getFavoriteEpisodes(User user) {
    	Permissions.checkUser(user);
        Session session = getSession();
        Query query = session.getNamedQuery("Favorite.getFavoriteEpisodes");
        query.setEntity("user", user);
        return query.list();
    }	
	
	public static Favorite findFavoriteProgram(User user, String programId) {
    	Permissions.checkUser(user);
		Session session = getSession();
		Query query = session.getNamedQuery("Favorite.getProgram");
		query.setEntity("user", user);
		query.setString("programId", programId);
		return (Favorite)query.uniqueResult();
	}


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}

	public Program getProgram() {
		if (program == null) {
    		if (programId != null) {
    			program = DefaultEpg.getInstance().getProgram(programId);
    		}
		}
		return program;
	}

	
}
