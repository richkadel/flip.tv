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

import com.appeligo.search.util.ConfigUtils;
import java.io.IOException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

public class AmazonSearcher {
    private static final Log log = LogFactory.getLog(AmazonSearcher.class);
    private IndexSearcher productSearcher;
    
    public AmazonSearcher() {
    }
    
    public AmazonItem getProgramPurchases(String programId) {
        Searcher searcher = getProductSearcher();
        BooleanQuery query = new BooleanQuery();
        query.add(new BooleanClause(new TermQuery(new Term("programId", programId)), Occur.MUST));
        query.add(new BooleanClause(new TermQuery(new Term("type", "product")), Occur.MUST));
        AmazonItem item = null;
        try {
            Hits hits = searcher.search(query);
            if (hits.length() > 0) {
                item = createItem(hits.doc(0));
            }
        } catch (IOException e) {
            if (log.isWarnEnabled()) {
                log.warn("Error searching for program purchase: " + programId, e);
            }
        }
        return item;
    }
    
    private AmazonItem createItem(Document doc) {
        AmazonItem item = new AmazonItem(doc.get("asin"));
        item.setTitle(doc.get("title"));
        item.setDetailsUrl(doc.get("detailsUrl"));
        item.setSmallImageUrl(doc.get("smallImageUrl"));
        String smallImageWidth = doc.get("smallImageWidth");
        if (smallImageWidth != null) {
            item.setSmallImageWidth(Integer.parseInt(smallImageWidth));
        }
        String smallImageHeight = doc.get("smallImageHeight");
        if (smallImageHeight != null) {
            item.setSmallImageHeight(Integer.parseInt(smallImageHeight));
        }
        return item;
    }
    
    protected Searcher getProductSearcher() {
        if (productSearcher == null) {
            loadProductSearcher();
        }
        return productSearcher;
    }
    
    protected void loadProductSearcher() {
        Configuration config = ConfigUtils.getAmazonConfig();
        String indexLocation = config.getString("programIndex");
        try {
            productSearcher = new IndexSearcher(indexLocation);
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("Cannot load product index: " + indexLocation, e);
            }
        }
    }
}
