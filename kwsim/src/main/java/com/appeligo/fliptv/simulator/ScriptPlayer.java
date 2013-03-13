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

package com.appeligo.fliptv.simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.caucho.hessian.client.HessianProxyFactory;
import com.knowbout.keywords.listener.Keyword;
import com.knowbout.keywords.listener.KeywordEvent;
import com.knowbout.keywords.listener.KeywordListener;
import com.knowbout.keywords.listener.Program;

class ScriptPlayer implements Runnable {
    private File file;
    private KeywordSimulator sim;
    private Date simStart;
    private Thread thread;
    private KeywordListener listener;
    private String savedLine;
    private boolean sendOffset;
    
    public ScriptPlayer(KeywordSimulator sim, File file) throws MalformedURLException {
        this.sim = sim;
        this.file = file;
        sendOffset = true;
        HessianProxyFactory factory = new HessianProxyFactory();
        listener = (KeywordListener) factory.create(KeywordListener.class, 
                sim.getListenerEndpoint());
    }
    
    public void start() {
        if (thread == null) {
            thread = new Thread(this, "Simulator-Thread");
            thread.start();
        }
    }
    
    public void run() {
        
        simStart = new Date();
        
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            int eventCount = 0;
            while (true) {
                try {
                    if (!simulateEvent(reader, eventCount)) {
                        break;
                    }
                } catch (Throwable t) {
                    System.err.println("Error simulating event.");
                    t.printStackTrace();
                }
                eventCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                //close the stream
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private boolean simulateEvent(BufferedReader reader, int eventId) throws IOException {
        String line = readLine(reader);
        if (line == null) {
            return false;
        }
        System.out.println("Event " + eventId);
        String[] params = line.split("\t");

//        System.out.println("Event");
//        for (String param : params) {
//            System.out.println("   -> " + param);
//        }
        
        int i=0;
        String id = params[i++];
        int seconds = Integer.parseInt(params[i++]);
        String channel = params[i++];
        String headendId = params[i++];
        String programId = params[i++];
        String lineupDevice = params[i++];
        String keyword = params[i++];
        double weight = Double.parseDouble(params[i++]);
        
        Date startTime = new Date(simStart.getTime() + seconds*1000);
        Date endTime = new Date(simStart.getTime() + seconds*1000);

        Program program = new Program(headendId, lineupDevice, channel, programId);
        List<Keyword> keywords = new ArrayList<Keyword>();
        keywords.add(new Keyword(Keyword.Type.SEARCH, keyword, weight));
        
        while (true) {
            line = readLine(reader);
            if (line == null) {
                break;
            } else if (line.startsWith(id + '\t')) {
                //add the keyword
                params = line.split("\t");
                i=6;
                keyword = params[i++];
                weight = Double.parseDouble(params[i++]);
                keywords.add(new Keyword(Keyword.Type.SEARCH, keyword, weight));
                
            } else {
                unreadLine(line);
                break;
            }
        }
        
        KeywordEvent event = new KeywordEvent(program, keywords, "CA04542:R", "41",
                simStart.getTime(), startTime.getTime(), "simulation", "supplemental");
        
        //fire the event
        fireKeywordsFound(event);
        
        return true;
    }
    
    protected void fireKeywordsFound(KeywordEvent event) {
        //wait until the end time
        while (event.getEventTimestamp() > System.currentTimeMillis()) {
            try {
                synchronized(this) {
                    long millis = event.getEventTimestamp() - System.currentTimeMillis();
                    if (millis > 0) {
                        System.out.println("Next event fires at " + event.getEventTimestamp() + " in " + (millis/1000f) + " seconds");
                        wait(millis);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("Firing keywords found: " + event.getKeywords());
        listener.keywordsFound(event);
//        if (sendOffset) {
//        	try {
//        	URL url = new URL(sim.getDemoOffsetEndpoint());
//        	URLConnection connect = url.openConnection();
//        	connect.connect();
//        	InputStream stream = connect.getInputStream();
//        	stream.close();
//        	sendOffset = false;
//        	} catch (MalformedURLException e) {
//        	e.printStackTrace();	
//        	} catch (IOException e) {
//        		e.printStackTrace();
//        	}
//        }
    }
    
    private void unreadLine(String line) throws IOException {
        if (savedLine != null) {
            throw new IOException("Cannot unread more the one line.");
        }
        savedLine = line;
    }
    
    private String readLine(BufferedReader reader) throws IOException {
        if (savedLine != null) {
            String line = savedLine;
            savedLine = null;
            return line;
        } else {
            //read the next non-comment line
            String line = reader.readLine();
            while (line != null && line.trim().charAt(0) == '#') {
                line = reader.readLine();
            }
            return line;
        }
    }
}
