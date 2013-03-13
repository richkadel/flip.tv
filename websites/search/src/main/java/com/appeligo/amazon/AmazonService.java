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

package com.appeligo.amazon;

import com.amazon.ecs.AWSECommerceServiceLocator;
import com.amazon.ecs.AWSECommerceServicePortType;
import com.amazon.ecs.Image;
import com.amazon.ecs.Item;
import com.amazon.ecs.ItemAttributes;
import com.amazon.ecs.ItemSearch;
import com.amazon.ecs.ItemSearchRequest;
import com.amazon.ecs.ItemSearchResponse;
import com.amazon.ecs.Items;
import com.amazon.ecs.OperationRequest;
import com.appeligo.search.util.ConfigUtils;
import com.knowbout.epg.service.Program;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.rpc.ServiceException;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

public class AmazonService {
    private static final Logger log = Logger.getLogger(AmazonService.class);
    
    private static AmazonService singleton;
    
    private AWSECommerceServicePortType service;
    private long lastServiceCall;
    private int throttle;
    
    private AmazonService() {
        Configuration config = ConfigUtils.getAmazonConfig();
        throttle = config.getInt("throttle", 1000);
    }
    
    public static synchronized AmazonService getInstance() {
    	if (singleton == null) {
    		singleton = new AmazonService();
    	}
    	return singleton;
    }
    
    public void throttle() {
        long waitTime = 0;
        if (lastServiceCall != 0) {
            waitTime = throttle - (System.currentTimeMillis() - lastServiceCall);
        }
        if (waitTime > 0) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                log.info("Interrupted throttle.", e);
            }
        }
    }

    public List<AmazonItem> getKeywordPurchases(String keywords, int maxResults) throws ServiceException {
        //throttle();
        
        try {
            AWSECommerceServicePortType service = getService();
            Configuration config = ConfigUtils.getAmazonConfig();
            
            ItemSearch search = new ItemSearch();
            search.setAWSAccessKeyId(config.getString("awsId"));
            search.setAssociateTag(config.getString("associateTag"));
            
            ItemSearchRequest[] requests = new ItemSearchRequest[1];
            ItemSearchRequest request = new ItemSearchRequest();
            request.setSearchIndex("All");
            if (log.isInfoEnabled()) {
                log.info("Searching for keywords: " + keywords);
            }
            request.setKeywords(keywords + " -xxx");
            request.setResponseGroup(new String[] {
                    "Small", "Images"
            });
            requests[0] = request;
            
            search.setRequest(requests);
            ItemSearchResponse response = service.itemSearch(search);
            lastServiceCall = System.currentTimeMillis();
            
            checkError(response);

            Items[] responseList = response.getItems();
            Items items = responseList[0];
            
            ///////////////////////////////////////////////////////////////////////////////////
            // PENDING JMF: I was getting an ocasional NullPointerException here, particularly
            // when I got no search results from my action, seems reasonable.  I added this
            // defensive check and return an empty list.  We may very well want to can some
            // items to pack into a default list in this case.
            ///////////////////////////////////////////////////////////////////////////////////
            if (items != null && items.getItem() != null) {
                List<AmazonItem> productList = new ArrayList<AmazonItem>(items.getItem().length);
                for (int i=0; i < maxResults && i < items.getItem().length; i++) {
                    Item item = items.getItem()[i];
                    //if (isAllowed(item)) {
                        productList.add(createItem(item));
                    //}
                }
                return productList;
            } else {
            	return new ArrayList<AmazonItem>();
            }
            
        } catch (RemoteException e) {
            throw new ServiceException("Cannot lookup videos.", e.getCause());
        }
    }

    /*
    @SuppressWarnings("unchecked")
    private boolean isAllowed(Item item) {
        BrowseNodes nodes = item.getBrowseNodes();
        if (nodes != null) {
            Configuration config = Utils.getAmazonConfig();
            HashSet<String> excludeSet = new HashSet<String>(config.getList("browseNodeFilter.exclude"));
            return isAllowed(excludeSet, nodes.getBrowseNode());
        } else {
            return true;
        }
    }
    
    private boolean isAllowed(HashSet<String> excludeSet, BrowseNode[] nodes) {
        if (nodes != null) {
            for (BrowseNode node : nodes) {
                String nodeId = node.getBrowseNodeId();
                if (excludeSet.contains(nodeId)) {
                    return false;
                }
                
                //check ancestors of this node
                if (!isAllowed(excludeSet, node.getAncestors())) {
                    return false;
                }
            }
        }
        return true;
    }
    */
    
    public AmazonItem getProgramPurchases(Program program) throws ServiceException {
        throttle();
        
        try {
            AWSECommerceServicePortType service = getService();
            Configuration config = ConfigUtils.getSystemConfig();
            
            ItemSearch search = new ItemSearch();
            search.setAWSAccessKeyId(config.getString("amazon.awsId"));
            search.setAssociateTag(config.getString("amazon.associateTag"));
            
            ItemSearchRequest[] requests = new ItemSearchRequest[2];
            ItemSearchRequest request = new ItemSearchRequest();
            request.setSearchIndex("Merchants");
            String keywords = "unbox " + program.getProgramTitle() + " -xxx";
            if (log.isInfoEnabled()) {
                log.info("Searching for programs: " + keywords);
            }
            request.setKeywords(keywords);
            request.setResponseGroup(new String[] {
                    "Small", "Images"
            });
            requests[0] = request;

            request = new ItemSearchRequest();
            request.setSearchIndex("Video");
            keywords = program.getProgramTitle() + " -xxx";
            if (log.isInfoEnabled()) {
                log.info("Searching for programs: " + keywords);
            }
            request.setKeywords(keywords);
            request.setResponseGroup(new String[] {
                    "Small", "Images"
            });
            requests[1] = request;
            
            search.setRequest(requests);
            ItemSearchResponse response = service.itemSearch(search);
            lastServiceCall = System.currentTimeMillis();
            
            checkError(response);

            Items[] responseList = response.getItems();
            
            return chooseItem(program, responseList);
            
        } catch (RemoteException e) {
            throw new ServiceException("Cannot lookup videos.", e.getCause());
        }
    }
    
    private AmazonItem chooseItem(Program program, Items[] responseList) {
        String titleCheck = normalize(program.getProgramTitle());
            
        for (int i=0; i < responseList.length; i++) {
            Items items = responseList[i];
            Item[] itemList = items.getItem();
            if (itemList != null && itemList.length > 0) {
                if (log.isInfoEnabled()) {
                    log.info("Query [" + i + "] found " + itemList.length + " results.");
                }
                for (Item item : itemList) {
                    ItemAttributes atts = item.getItemAttributes();
                    String title = normalize(atts.getTitle());
                    
                    if (title.indexOf(titleCheck) != -1) {
                        //if (isAllowed(item)) {
                            return createItem(item);
                        //}
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Query [" + i + "] found no results.");
                }
            }
        }
        return null;
    }
    
    private String normalize(String str) {
        str = str.toLowerCase().replaceAll("[^\\s\\w]", "");
        return str;
    }
    
    private AmazonItem createItem(Item item) {
        ItemAttributes atts = item.getItemAttributes();
        AmazonItem amazonItem = new AmazonItem(item.getASIN());
        amazonItem.setTitle(atts.getTitle());
        amazonItem.setDetailsUrl(item.getDetailPageURL());
        
        Image image = item.getSmallImage();
        if (image != null) {
            amazonItem.setSmallImageUrl(image.getURL());
            amazonItem.setSmallImageWidth(image.getWidth().get_value().intValue());
            amazonItem.setSmallImageHeight(image.getHeight().get_value().intValue());
        }
        return amazonItem;
    }
    
    private void checkError(ItemSearchResponse response) throws ServiceException {
    	if (response == null) {
            throw new ServiceException("Null response in web service call");
    	}
        OperationRequest request = response.getOperationRequest();
        if (request.getErrors() != null && request.getErrors().length > 0) {
            //do something more user friendly
            throw new ServiceException("Error in web service call. First of "+request.getErrors().length+" errors: "+request.getErrors()[0]);
        }
    }
    
    private synchronized AWSECommerceServicePortType getService() throws ServiceException {
        if (service == null) {
            try {
                AWSECommerceServiceLocator locator = new AWSECommerceServiceLocator();
                service = locator.getAWSECommerceServicePort();
            } catch (Exception e) {
                log.fatal("Cannot instantiate Amazon ECS service.", e);
            }
        }
        return service;
    }
}
