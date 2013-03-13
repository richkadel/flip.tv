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

package com.appeligo.search.actions.home;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

import com.appeligo.search.actions.BaseAction;
import com.appeligo.search.entity.Favorite;
import com.appeligo.search.entity.Friend;
import com.appeligo.search.entity.User;
import com.knowbout.hibernate.HibernateUtil;

public class FavoriteAction extends HomeAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2157613811695317607L;
	
	private long f;
	private Favorite deletedFavorite;
	
	public String up()  {
		user = getUser();
		if (user == null) {
			return super.execute();
		} else {
			Session session = HibernateUtil.currentSession();			
			Favorite fav = (Favorite)session.load(Favorite.class, f);
			if (fav == null || !fav.getUser().equals(user)) {
				return super.execute();
			}
			//Is it a show or a episode
			List<Favorite> favorites = null;
			if (fav.getProgramId().startsWith("EP")) {
				favorites = Favorite.getFavoriteEpisodes(user);				
			} else {
				favorites = Favorite.getFavoriteShows(user);				
			}
			int index = favorites.indexOf(fav);
			if (index > 0) {
				double startValue = 0.0;
				
				if (index > 1) {
					startValue = favorites.get(index -2).getRank();
				}
				double endValue = favorites.get(index -1).getRank();
				fav.setRank(((startValue+endValue)/2.0));
			}
			return super.execute();
		}
	}

	public String down()  {
		user = getUser();
		if (user == null) {
			return super.execute();
		} else {
			Session session = HibernateUtil.currentSession();			
			Favorite fav = (Favorite)session.load(Favorite.class, f);
			if (fav == null || !fav.getUser().equals(user)) {
				return super.execute();
			}
			//Is it a show or a episode
			List<Favorite> favorites = null;
			if (fav.getProgramId().startsWith("EP")) {
				favorites = Favorite.getFavoriteEpisodes(user);				
			} else {
				favorites = Favorite.getFavoriteShows(user);				
			}
			int index = favorites.indexOf(fav);
			int size = favorites.size();
			if (index >= 0 && index +1 < size) {
				double startValue = favorites.get(index +1).getRank();
				double endValue = 100.0;
				if (index+2 >= size) {
					double diff = startValue - fav.getRank();
					endValue = startValue+diff;
				} else {
					endValue = favorites.get(index + 2).getRank();
				}
				fav.setRank(((startValue+endValue)/2.0));
			}
			return super.execute();
		}
	}
	
	public String delete()  {
		user = getUser();
		if (user == null) {
			return super.execute();
		} else {
			Session session = HibernateUtil.currentSession();			
			Favorite fav = (Favorite)session.load(Favorite.class, f);
			if (fav == null || !fav.getUser().equals(user)) {
				return super.execute();
			}
			fav.setDeleted(true);
			deletedFavorite = fav;
			return super.execute();
		}
	}
	
	public String undoDelete() {
		user = getUser();
		if (user == null) {
			return super.execute();
		} else {
			Session session = HibernateUtil.currentSession();			
			Favorite fav = (Favorite)session.load(Favorite.class, f);
			if (fav == null || !fav.getUser().equals(user)) {
				return super.execute();
			}
			fav.setDeleted(false);
			return super.execute();
		}		
	}
	
	public User getCurrentUser() {
		return user;
	}

	/**
	 * @return Returns the f.
	 */
	public long getF() {
		return f;
	}

	/**
	 * @param f The f to set.
	 */
	public void setF(long f) {
		this.f = f;
	}

	/**
	 * @return Returns the deletedFavorite.
	 */
	public Favorite getDeletedFavorite() {
		return deletedFavorite;
	}

	/**
	 * @param deletedFavorite The deletedFavorite to set.
	 */
	public void setDeletedFavorite(Favorite deletedFavorite) {
		this.deletedFavorite = deletedFavorite;
	}
	

	
}
