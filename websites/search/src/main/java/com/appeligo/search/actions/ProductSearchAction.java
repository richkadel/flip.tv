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

import com.appeligo.amazon.AmazonItem;
import com.appeligo.amazon.AmazonSearcher;
import com.appeligo.amazon.AmazonService;
import com.appeligo.epg.DefaultEpg;
import com.appeligo.search.util.ConfigUtils;
import com.caucho.hessian.client.HessianProxyFactory;
import com.knowbout.epg.service.EPGProvider;
import com.knowbout.epg.service.Program;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

public class ProductSearchAction extends BaseAction {

    private static final long serialVersionUID = -4991681498693772727L;
    
    private static final Logger log = Logger.getLogger(ProductSearchAction.class);
    
    private AmazonSearcher searcher;
    private AmazonService service;
    private EPGProvider epgProvider;
    private String programIds;
    private String keywords;
    private HashMap<String, AmazonItem> productMap;
    private List<AmazonItem> featuredItems;
    
    public ProductSearchAction() {
        epgProvider = DefaultEpg.getInstance();
        service = AmazonService.getInstance();
        searcher = new AmazonSearcher();
    }

    public String execute() throws Exception {
        if (programIds != null && programIds.length() > 0) {
            String[] programIdList = programIds.split(",");

            productMap = new HashMap<String, AmazonItem>();
            for (String programId : programIdList) {
                if (!productMap.containsKey(programId)) {
                    AmazonItem item = searcher.getProgramPurchases(programId);
                    if (item != null && !productMap.containsValue(item)) {
                        productMap.put(programId, item);
                    }
                }
            }
            return "programList";
            
        } else if (keywords != null && keywords.length() > 0) {
            featuredItems = service.getKeywordPurchases(keywords, 6);
            return "adsense";
            
        } else {
            return ERROR;
        }
        
    }

    public String getProgramIds() {
        return programIds;
    }

    public void setProgramIds(String programIds) {
        this.programIds = programIds;
    }

    public HashMap<String, AmazonItem> getProductMap() {
        return productMap;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public List<AmazonItem> getFeaturedItems() {
        return featuredItems;
    }
}
