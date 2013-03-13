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

import com.appeligo.lucene.IndexerQueue;
import com.appeligo.search.util.ConfigUtils;
import com.knowbout.epg.service.Program;
import java.util.Date;

import javax.xml.rpc.ServiceException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.Term;
import javax.xml.rpc.ServiceException;

public class AmazonIndexer {
    private static final Log log = LogFactory.getLog(AmazonIndexer.class);
    
    private static final AmazonIndexer instance = new AmazonIndexer();
    
    private IndexerQueue queue;
    private AmazonService service;
    
    //http://ecs.amazonaws.com/onca/xml?Service=AWSECommerceService&AWSAccessKeyId=017FHFKV6MJ6E4Q6KHR2&Operation=ItemSearch&ResponseGroup=ItemAttributes&SearchIndex=Video&Keywords=The+Young+and+the+Restless
    
    private AmazonIndexer() {
        Configuration config = ConfigUtils.getAmazonConfig();
        String index = config.getString("productIndex");
        queue = null;//new IndexerQueue(index, new StandardAnalyzer());
        service = AmazonService.getInstance();
    }
    
    public static AmazonIndexer getInstance() {
        return instance;
    }
    
    public void addProgram(Program program) {
        try {
            queue.deleteDocuments(new Term("programId", program.getProgramId()));
            service.throttle();
            AmazonItem item = service.getProgramPurchases(program);
            if (item != null) {
                addAmazonItem(item, program.getProgramId());
            }
        } catch (ServiceException e) {
            if (log.isInfoEnabled()) {
                log.info("Cannot index products for program: " + program, e);
            }
		}
    }
    
    public void addAmazonItem(AmazonItem item, String programId) {
        Document doc = new Document();
        doc.add(new Field("asin", item.getId(), Store.YES, Index.UN_TOKENIZED));
        if (item.getTitle() != null) {
            doc.add(new Field("title", item.getTitle(), Store.YES, Index.TOKENIZED));
        }
        if (item.getDetailsUrl() != null) {
            doc.add(new Field("detailsUrl", item.getDetailsUrl(), Store.YES, Index.NO));
        }
        if (item.getSmallImageUrl() != null) {
            doc.add(new Field("smallImageUrl", item.getSmallImageUrl(), Store.YES, Index.NO));
            doc.add(new Field("smallImageWidth", Integer.toString(item.getSmallImageWidth()), 
                    Store.YES, Index.NO));
            doc.add(new Field("smallImageHeight", Integer.toString(item.getSmallImageHeight()), 
                    Store.YES, Index.NO));
        }
        doc.add(new Field("programId",  programId, Store.YES, Index.UN_TOKENIZED));
        doc.add(new Field("storeTime",  DateTools.dateToString(new Date(), Resolution.MINUTE), 
                Store.YES, Index.UN_TOKENIZED));
    
        queue.addDocument(doc);
    }
}
