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

package com.appeligo.search.actions;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.appeligo.search.entity.User;
import com.appeligo.search.util.InstantiableURLEncoder;
import com.opensymphony.webwork.interceptor.ServletRequestAware;
import com.opensymphony.webwork.interceptor.ServletResponseAware;
import com.opensymphony.webwork.interceptor.SessionAware;
import com.opensymphony.xwork.ActionSupport;
import org.apache.commons.configuration.Configuration;
import com.appeligo.search.util.ConfigUtils;

/**
 * 
 * @author fear
 */
@SuppressWarnings("serial")
public class BaseAction extends ActionSupport implements ServletRequestAware, ServletResponseAware, SessionAware {
	
	private static final Log log = LogFactory.getLog(BaseAction.class);
	private static final String USER_ID = "appeligo.userId";
	private static final String LINEUP_ID = "appeligo.lineupId";
	public static final String COOKIE_ID = "flip.tv-user-cookie";
	public static final String DEFAULT_LINEUP = "P-DC";
	public static final String TIMEZONE_ID  = "appeligo.timezone";
	public static final String DEFAULT_TIMEZONE_ID  = "America/Los_Angeles";
	
    private static InstantiableURLEncoder urlEncoder = new InstantiableURLEncoder();
    
	private static Date NEW_EPG_DATE = null;
	
	private Configuration config;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private Map session;
    private String baseUrl;
    private String page;
    private String pagedir;
    private String pageType;
    private boolean noLogo = false;
    private boolean noSearch = false;
    private String subtitle;
    private boolean publicLaunch = false;
	private OldChannelDirConverter oldChannelDirConverter;
    
    public final void setServletRequest(HttpServletRequest req) {
		this.request = req;
		config = ConfigUtils.getSystemConfig();
		publicLaunch = config.getBoolean("publicLaunch", false);
	}

    public final HttpServletRequest getServletRequest() {
    	return request;
    }
    
    public final HttpServletResponse getServletResponse() {
    	return response;
    }
    
	public void setSession(Map session) {
		this.session = session;
	}
	
	protected final Map getSession() {
		return session;
	}
	
	public String getFullRequestURL() {
		StringBuffer url = getServletRequest().getRequestURL();
		String queryString = getServletRequest().getQueryString();
		if (queryString != null && queryString.length() > 0) {
			if (queryString.charAt(0) != '?') {
    			url.append('?');
			}
			url.append(queryString);
		}
		return url.toString();
	}
	
	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getBaseUrl() {
		if (baseUrl == null) {
			config = ConfigUtils.getSystemConfig();
			baseUrl = config.getString("url");
		}
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public String getPagedir() {
		return pagedir;
	}

	public void setPagedir(String pagedir) {
		this.pagedir = pagedir;
	}
	
	public String getQuery() {
		String rtn = (String)getSession().get("query");
		if (rtn == null) {
			return "";
		}
		return rtn;
	}
	
	@SuppressWarnings("unchecked")
	public void setSearchType(String searchType) {
		SearchType type = SearchType.FUTURE;
		if (searchType != null) {
			try {
				type = SearchType.valueOf(searchType);
			} catch (IllegalArgumentException e) {
				log.debug("Error trying to parse the SearchType: " + searchType);
			}
		}
		getSession().put("searchType", type);
		User user = getUser();
		if (user != null) {
			user.setSearchType(type.name());
			user.save();
		}
	}
	
	public String getSearchType() {
		return getSearchTypeAsSearchType().toString();
	}
	
	public SearchType getSearchTypeAsSearchType() {
		SearchType type = (SearchType)getSession().get("searchType");
		if (type == null) {
			User user = getUser();
			if (user != null) {
				return SearchType.valueOf(user.getSearchType());
			}
			return SearchType.ALL;
		} else {
			return type;
		}		
	}	
	@SuppressWarnings("unchecked")
	public void setQuery(String query) {
		getSession().put("query", query.trim().replaceAll("\\s+", " "));
	}	
	public void setQ(String googleQuery) {
		setQuery(googleQuery);
	}
	
	public String getQ() {
		return getQuery();
	}
	
	/*
	public void setUsername(String username) {
		setUser(User.findByUsername(username));
	}
	*/
	
	public String getUsername() {
		return getServletRequest().getRemoteUser();
	}
	
	/**
	 * 
	 * @return
	 */
	public User getUser() {
		// If we can use the PK lookup, our cache hit ratio will be vastly higher in
		// the presence of update statements, and we get a better index.
		Long userId = (Long)getServletRequest().getSession().getAttribute(USER_ID);
		if (userId != null) {
			return User.findById(userId);
		}
		String username = getServletRequest().getRemoteUser();
		if (username == null) {
			return null;
		}
		User user = User.findByUsername(username);
		if (user != null) {
			getServletRequest().getSession().setAttribute(USER_ID, new Long(user.getUserId()));
		}
		return user;
	}
	
	public String getPageType() {
		if (pageType == null) {
			pageType = "default";
		}
		return pageType;
	}

	public void setPageType(String pageType) {
		this.pageType = pageType;
	}

	public boolean getNoLogo() {
		return noLogo;
	}

	public void setNoLogo(boolean noLogo) {
		this.noLogo = noLogo;
	}

	public boolean isNoSearch() {
		return noSearch;
	}

	public void setNoSearch(boolean noSearch) {
		this.noSearch = noSearch;
	}

	public InstantiableURLEncoder getUrlEncoder() {
		return urlEncoder;
	}
	
	
	
    /**
	 * @param response The response to set.
	 */
	public void setServletResponse(HttpServletResponse response) {
		this.response = response;
	}
		
	public String getLineup() {
		String lineup = null;
		//Get if from the user if there is one
		User user = getUser();
		if (user != null) {
			lineup = user.getLineupId();
			getServletRequest().getSession().setAttribute(LINEUP_ID, lineup);
		} else {
			lineup = (String)getServletRequest().getSession().getAttribute(LINEUP_ID);		
			if (lineup == null) {		
				// No user, and its not stored in the session, so check for a cookie. If there is no cookie, default them to pacific
				//Right now the lineup is not getting stored in the session when it is loaded by the cookie.
				//The reason is that the cookie gets set before they login and it would not get set with
				//The lineup from the user.
		    	Cookie[] cookies = getServletRequest().getCookies();
		    	if (cookies != null) {
			    	for (Cookie cookie: cookies) {
				    	if (cookie.getName().equals(LINEUP_ID)) {
				    		cookie.setMaxAge(Integer.MAX_VALUE);
				    		lineup = cookie.getValue();
				    		break;
				    	} 
			    	}
		    	}
		    	if (lineup == null) {
		    		lineup = DEFAULT_LINEUP;
		    		Cookie cookie = new Cookie(LINEUP_ID, lineup);
		    		cookie.setMaxAge(Integer.MAX_VALUE);
		    		response.addCookie(cookie);
					getServletRequest().getSession().setAttribute(LINEUP_ID, lineup);
		    	}		    		
			}
		}
		return lineup;		
	}
	
	protected void setLineup(String lineup) {
		getServletRequest().getSession().setAttribute(LINEUP_ID, lineup);		
		Cookie cookie = new Cookie(LINEUP_ID, lineup);
		cookie.setMaxAge(Integer.MAX_VALUE);
		response.addCookie(cookie);	
	}
	
	protected void setTimeZone(TimeZone timeZone) {
		getServletRequest().getSession().setAttribute(TIMEZONE_ID, timeZone);
		Cookie cookie = new Cookie(TIMEZONE_ID, timeZone.getID());
		cookie.setMaxAge(Integer.MAX_VALUE);
		response.addCookie(cookie);		
	}
	
	public TimeZone getTimeZone() {
		User user = getUser();
		if (user != null) {
			getServletRequest().getSession().setAttribute(TIMEZONE_ID, user.getTimeZone());
			return user.getTimeZone();
		} else {
			TimeZone zone = (TimeZone)getServletRequest().getSession().getAttribute(TIMEZONE_ID);	
			if (zone == null) {
				String timeZoneId = null;		
		    	Cookie[] cookies = getServletRequest().getCookies();
		    	if (cookies != null) {
			    	for (Cookie cookie: cookies) {
				    	if (cookie.getName().equals(TIMEZONE_ID)) {
				    		cookie.setMaxAge(Integer.MAX_VALUE);
				    		timeZoneId = cookie.getValue();
				    		break;
				    	} 
			    	}
		    	}
		    	if (timeZoneId == null) {
		    		timeZoneId = DEFAULT_TIMEZONE_ID;
		    		Cookie cookie = new Cookie(TIMEZONE_ID, timeZoneId);
		    		cookie.setMaxAge(Integer.MAX_VALUE);
		    		response.addCookie(cookie);
		    	}		
		    	zone = TimeZone.getTimeZone(timeZoneId);
				getServletRequest().getSession().setAttribute(TIMEZONE_ID, zone);
				return zone;
			} else {
				return zone;
			}
		}
	}
	
	public Locale getLocal() {
		return Locale.US;
	}


	protected String getCookieId() {
    	Cookie[] cookies = getServletRequest().getCookies();
    	if (cookies != null) {
	    	for (Cookie cookie: cookies) {
		    	if (cookie.getName().equals(BaseAction.COOKIE_ID)) {
		    		cookie.setMaxAge(Integer.MAX_VALUE);
		    		return cookie.getValue();
		    	} 
	    	}
    	}
    	//No cookie found;
    	String cookieValue = request.getRemoteAddr() + System.currentTimeMillis();
		Cookie cookie = new Cookie(COOKIE_ID, cookieValue);
		cookie.setMaxAge(Integer.MAX_VALUE);
		response.addCookie(cookie);
		return cookieValue;    	
    }

	protected StringBuilder getCookieValue() {
		StringBuilder sb = new StringBuilder();
		sb.append("cookie=" + getCookieId());
		sb.append("|IP=");
		sb.append(getServletRequest().getRemoteAddr());
		sb.append("|searchType=");
		sb.append(getSearchType());
		sb.append("|user=");
		User user = getUser();
		sb.append(user == null ? "NULL" : user.getUsername());
		return sb;
	}

	public boolean isPublicLaunch() {
		return publicLaunch;
	}

	public void setPublicLaunch(boolean publicLaunch) {
		this.publicLaunch = publicLaunch;
	}	
	
	public Date getNewEpgDate() {
		if (NEW_EPG_DATE == null) {
			Calendar cal = Calendar.getInstance();
			cal.set(2007, 5, 8);
			NEW_EPG_DATE = cal.getTime();
		}
		return NEW_EPG_DATE;
	}
	
	public TimeZone getGmtTimeZone() {
		return TimeZone.getTimeZone("GMT");
	}
	
	public OldChannelDirConverter getOldChannelDirConverter() {
		if (oldChannelDirConverter == null) {
			oldChannelDirConverter = new OldChannelDirConverter();
		}
		return oldChannelDirConverter;
	}
	
	public static class OldChannelDirConverter {
    	private static Map<String,String> channelMap;
		public String getChannel(String stationCallSign) {
    		if (channelMap == null) {
    			HashMap<String,String> channelMap = new HashMap<String,String>();
    			channelMap.put("CW", "5");
    			channelMap.put("FOX", "6");
    			channelMap.put("NBC", "7");
    			channelMap.put("CBS", "8");
    			channelMap.put("ABC", "10");
    			channelMap.put("VERSUS", "41");
    			channelMap.put("DSC", "44");
    			channelMap.put("AETV", "45");
    			channelMap.put("FOOD", "51");
    			channelMap.put("HGTV", "53");
    			channelMap.put("TLC", "55");
    			channelMap.put("HISTORY", "56");
    			channelMap.put("SCIFI", "57");
    			channelMap.put("TRAV", "70");
    			this.channelMap = channelMap;
    		}
    		return channelMap.get(stationCallSign);
		}
	}
}
