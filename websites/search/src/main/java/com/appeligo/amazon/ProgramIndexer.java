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

import com.appeligo.config.ConfigurationService;
import com.appeligo.search.util.ConfigUtils;
import com.knowbout.epg.service.Program;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.rpc.ServiceException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;

public class ProgramIndexer {
    private static final Log log = LogFactory.getLog(ProgramIndexer.class);
    
    static {
        ConfigurationService.setRootDir(new File("src/webapp/WEB-INF/config"));
    }
    
    private IndexSearcher searcher;
    private IndexWriter writer;
    
    private Map<String, AmazonItem> savedSearches = new HashMap<String, AmazonItem>();
    
    private Analyzer analyzer = new StandardAnalyzer();
    
    private File indexLocation = new File("/var/lucene/dev/amazonProgramIndex");
    private String jdbcDriver;
    private String jdbcUrl;
    private String jdbcUsername;
    private String jdbcPassword;
    private int fetchSize;
    private int staleDays;

    private Connection conn;
    
    public ProgramIndexer() {
        ConfigurationService.init();
        Configuration config = ConfigUtils.getAmazonConfig();
        indexLocation = new File(config.getString("programIndex"));
        jdbcDriver = config.getString("jdbc.driver", "com.mysql.jdbc.Driver");
        jdbcUrl = config.getString("jdbc.url");
        jdbcUsername = config.getString("jdbc.username");
        jdbcPassword = config.getString("jdbc.password");
        fetchSize = config.getInt("fetchSize", 1000);
        staleDays = config.getInt("staleDays", 30);
    }
    
    public void run() {
        if (!indexLocation.exists()) {
            //create the index directory
            indexLocation.mkdirs();
        }
        
        try {
            savedSearches.clear();
            writer = new IndexWriter(indexLocation, analyzer);
            searcher = new IndexSearcher(indexLocation.getPath());
            
            deleteExpiredPrograms();
            writer.flush();
            
            //reopen searcher so the deleted documents are not present
            close(searcher);
            searcher = new IndexSearcher(indexLocation.getPath());
            
            addNewPrograms();
            
        } catch (Exception e) {
            log.error("Error occurred during indexing.", e);
            
        } finally {
            savedSearches.clear();
            close(searcher);
            searcher = null;
            
            close(writer);
            writer = null;
            
            close(conn);
            conn = null;
        }
    }

    private void close(IndexSearcher searcher) {
        if (searcher != null) {
            try {
                searcher.close();
            } catch (IOException e) {
                log.warn("Cannot close index searcher.", e);
            }
        }
    }

    private void close(IndexWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                log.warn("Cannot close index writer.", e);
            }
        }
    }
    
    private void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("Cannot close connection.", e);
            }
        }
    }
    
    private void close(Statement s) {
        if (s != null) {
            try {
                s.close();
            } catch (SQLException e) {
                log.warn("Cannot close statement.", e);
            }
        }
    }
    
    private void close(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                log.warn("Cannot close statement.", e);
            }
        }
    }
    
    private void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.warn("Cannot close result set.", e);
            }
        }
    }
    
    protected void deleteExpiredPrograms() throws SQLException, IOException {        
        //do a range query to find all the documents we need to delete
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -staleDays);
        String start = DateTools.dateToString(new Date(0), Resolution.DAY);
        String end = DateTools.dateToString(calendar.getTime(), Resolution.DAY);
        
        RangeQuery query = new RangeQuery(new Term("storeTime", start), new Term("storeTime", end), true);
        Hits hits = searcher.search(query);
        
        //build a term list of terms that match the programs we want to delete
        int length = hits.length();
        Term[] terms = new Term[length];
        String programId;
        Document doc;
        for (int i=0; i < length; i++) {
            doc = hits.doc(i);
            programId = doc.get("programId");
            terms[i] = new Term("programId", programId);
        }
        writer.deleteDocuments(terms);
        if (log.isInfoEnabled()) {
            log.info("Deleted " + length + " stale documents from product index.");
        }
    }
    
    protected void addNewPrograms() throws SQLException, IOException {
        Connection conn = getConnection();
        
        Statement s = null;
        ResultSet rs = null;
        try {
            if (log.isInfoEnabled()) {
                log.info("Querying epg database for programs...");
            }
            s = conn.createStatement();
            s.setFetchSize(fetchSize);
//            rs = s.executeQuery("select program_id, title from programs");
            rs = s.executeQuery("select program_id, title from programs limit 4 offset 14");

            if (log.isInfoEnabled()) {
                log.info("Traversing program list and adding all missing programs.");
            }
            int count = 0;
            int addedCount = 0;
            while (rs.next()) {
                int i=1;
                String programId = rs.getString(i++);
                String title = rs.getString(i++);
                
                if (!containsProgram(programId)) {
                    //it's not already in the index
                    if (addProgram(programId, title)) {
                        addedCount++;
                    } else {
                        addMarkerProgram(programId);
                    }
                }
                count++;
            }
            if (log.isInfoEnabled()) {
                log.info(count + " programs were evaluated.");
            }
            if (log.isInfoEnabled()) {
                log.info(addedCount + " programs were added.");
            }
            
        } finally {
            close(rs);
            close(s);
        }
        
    }
    
    protected boolean containsProgram(String programId) throws IOException {
        Hits hits = searcher.search(new TermQuery(new Term("programId", programId)));
        return hits.length() > 0;
    }
    
    protected boolean addProgram(String programId, String title) {
        boolean added = false;
        AmazonService service = AmazonService.getInstance();
        
        Program program = new Program();
        program.setProgramId(programId);
        program.setProgramTitle(title);

        String showId = getShowId(programId);
        AmazonItem item = savedSearches.get(showId);
        if (item == null) {
            //search the amazon server
            try {
                item = service.getProgramPurchases(program);
            } catch (ServiceException e) {
                log.warn("Cannot get purchases for program: " + program, e);
            }
        }
        
        if (item != null) {
            //save the search by show Id
            savedSearches.put(showId, item);
            
            if (log.isInfoEnabled()) {
                log.info("Adding program " + programId);
            }
            
            //we found a program item on amazon
            Document doc = createProductDocument(item, programId);
            try {
                writer.addDocument(doc);
                added = true;
            } catch (IOException e) {
                log.error("Cannot add program document to index: " + doc, e);
            }
        }
        return added;
    }
    
    private String getShowId(String programId) {
        if (programId.startsWith("EP")) {
            return programId.substring(0, programId.length()-3) + "000";
        } else {
            return programId;
        }
    }
    
    /**
     * Adds a marker program so it won't query for this programId until the time expires.
     * @param programId the programId to add a marker for
     * @throws IOException
     */
    private void addMarkerProgram(String programId) throws IOException {
        Document doc = new Document();
        doc.add(new Field("type", "marker", Store.NO, Index.UN_TOKENIZED));
        doc.add(new Field("programId",  programId, Store.YES, Index.UN_TOKENIZED));
        doc.add(new Field("storeTime",  DateTools.dateToString(new Date(), Resolution.DAY), 
                Store.YES, Index.UN_TOKENIZED));
        writer.addDocument(doc);
    }
    
    protected Document createProductDocument(AmazonItem item, String programId) {
        Document doc = new Document();
        doc.add(new Field("type", "product", Store.NO, Index.UN_TOKENIZED));
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
        doc.add(new Field("storeTime",  DateTools.dateToString(new Date(), Resolution.DAY), 
                Store.YES, Index.UN_TOKENIZED));
    
        return doc;
    }
    
    public Connection getConnection() throws SQLException{
        if (conn == null) {
            try {
                Class.forName(jdbcDriver);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Cannot load jdbc driver class.", e);
            }
            conn = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
        }
        return conn;
    }
    
    public static void main(String[] args) {
        for (int i=0; i < args.length; i++) {
            if (args[i].equals("-configRoot")) {
                ConfigurationService.setRootDir(new File(args[++i]));
            }
        }
        
        ProgramIndexer indexer = new ProgramIndexer();
        
        indexer.run();
    }
}
