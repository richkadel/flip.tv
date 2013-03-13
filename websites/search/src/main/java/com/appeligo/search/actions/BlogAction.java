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

import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.parsers.FeedParser;
import de.nava.informa.utils.NoOpEntityResolver;
import de.nava.informa.utils.ParserUtils;

public class BlogAction extends BaseAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3333260100035500673L;
	private static final Logger log = Logger.getLogger(BlogAction.class);
	
	private Collection<ItemIF> feedItems;
    
    @SuppressWarnings("unchecked")
	public String execute() throws Exception {
		URL feed = new URL("http://fliptv.blogspot.com/feeds/posts/default");
		
		ChannelIF channel = FeedParser.parse(new ChannelBuilder(), feed);
		//ChannelIF channel = FeedParser.parse(new ChannelBuilder(), inpSource, feed);
		
		setFeedItems(getTimeZone(), channel.getItems());
		    		  
        return SUCCESS;
    }

	public Collection<ItemIF> getFeedItems() {
		return feedItems;
	}

	public void setFeedItems(TimeZone timeZone, Collection<ItemIF> feedItems) {
		this.feedItems = feedItems;
		for (ItemIF feedItem : feedItems) {
			feedItem.setDescription(ParserUtils.unEscape(feedItem.getDescription()));
			Date published = ParserUtils.getDate(feedItem.getElementValue("published"));
			published.setTime(published.getTime()-TimeZone.getDefault().getOffset(published.getTime()));
			feedItem.setDate(published);
		}
	}
}


