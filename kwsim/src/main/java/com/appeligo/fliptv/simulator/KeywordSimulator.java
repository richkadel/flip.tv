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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class KeywordSimulator {
    private Date startTime;
    private Date endTime;
    private String listenerEndpoint = "http://localhost:8080/fliptv/internal/keywordListener";
    private String demoOffsetEndpoint = "http://localhost:8080/fliptv/demo/offset.action?startDemo=true&deleteSearch=41";
    public String getListenerEndpoint() {
        return listenerEndpoint;
    }
    
    public void setListenerEndpoint(String listenerEndpoint) {
        this.listenerEndpoint = listenerEndpoint;
    }

    /**
	 * @return Returns the demoOffsetEndpoint.
	 */
	public String getDemoOffsetEndpoint() {
		return demoOffsetEndpoint;
	}

	/**
	 * @param demoOffsetEndpoint The demoOffsetEndpoint to set.
	 */
	public void setDemoOffsetEndpoint(String demoOffsetEndpoint) {
		this.demoOffsetEndpoint = demoOffsetEndpoint;
	}

	public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    public void export() throws IOException {
        System.out.println("Start: " + startTime);
        System.out.println("End: " + endTime);
        System.err.println("Feature not implemented yet.");
    }
    
    public void play(File file) throws MalformedURLException {
        if (file == null) {
            System.err.println("You must specify a file.");
            printHelp();
        }
        if (!file.isFile()) {
            System.err.println("The file does not exist: " + file);
            printHelp();
        }
        
        ScriptPlayer player = new ScriptPlayer(this, file);
        player.start();
    }

    public static void printHelp() {
        System.out.println("Usage: java com.appeligo.fliptv.simulator.KeywordSimulator -[e] [options] <file>");
        System.out.println("    -e <start> <end>: exports a script for a certain time period.  Dates ");
        System.out.println("           must be in the format: yyyy-MM-dd'T'HH:mm:ss (ex. 2007-06-25T12:08:56)");
        System.out.println("    -h <server>: Specify the hostname to connect to (default: localhost)");
        System.out.println("    -p <port>: Specify the port to connect to (default: 8080)");
        System.out.println("    --help : prints out the help screen");
        System.exit(0);
    }
    
    public static void main(String[] args) throws Exception {
//      if (sendOffset) {
//    	try {
//    	URL url = new URL("http://localhost:80/fliptv/demo/offset.action?startDemo=true&deleteSearch=41");
//    	URLConnection connect = url.openConnection();
//    	connect.connect();
//    	InputStream stream = connect.getInputStream();
//    	stream.close();
//    	} catch (MalformedURLException e) {
//    	e.printStackTrace();
//    	System.exit(0);
//    	} catch (IOException e) {
//    		e.printStackTrace();
//        	System.exit(0);
//    	}
//    }
    	
        KeywordSimulator sim = new KeywordSimulator();
        
        boolean export = false;
        File file = null;
        String arg;
        String host = "localhost";
        String port = "8080";
        for (int i=0; i < args.length; i++) {
            arg = args[i];
            if ("--help".equals(arg)) {
                printHelp();
                
            } else if ("-e".equals(arg)) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                if (i+2 >= args.length) {
                    System.err.println("You must specify a start and end date.");
                    printHelp();
                }
                sim.setStartTime(df.parse(args[++i]));
                sim.setEndTime(df.parse(args[++i]));
                export = true;
                
            } else if ("-h".equals(arg)) {
                host = args[++i];
                
            } else if ("-p".equals(arg)) {
                port = args[++i];
                
            } else {
                if (file != null) {
                    printHelp();
                }
                file = new File(arg);
            }
        }
        
        sim.setListenerEndpoint("http://" + host + ":" + port + "/fliptv/internal/keywordListener");
        sim.setDemoOffsetEndpoint("http://" + host + ":" + port + "/fliptv/demo/offset.action?startDemo=true&deleteSearch=41");
        
        if (export) {
            sim.export();
        } else {
            sim.play(file);
        }
    }
}
