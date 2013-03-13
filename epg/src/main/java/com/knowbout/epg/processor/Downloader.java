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

package com.knowbout.epg.processor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class Downloader {
	
	private static final Log log = LogFactory.getLog(Downloader.class);

	private InetAddress server;
	private File destinationFolder;
	private String username;
	private String password;
	private String remoteWorkingDirectory;
	private boolean forceDownload;
	private FTPClient client;
	
	
	public Downloader(InetAddress server, String username, String password, File destinationFolder, String remoteWorkingDirectory, boolean forceDownload) {
		this.server = server;
		this.destinationFolder = destinationFolder;
		if (!destinationFolder.exists() || !destinationFolder.isDirectory()) {
			throw new IllegalArgumentException("The destinationFolder must exist and be a directory: " + destinationFolder);
		}
		this.username = username;
		this.password = password;
		this.remoteWorkingDirectory = remoteWorkingDirectory;
		this.forceDownload = forceDownload;
		client = new FTPClient();
	}
	
	public boolean downloadFile(String filename) throws IOException{
		connect();
		try {
			FTPFile[] files = client.listFiles(filename);
			if (files != null && files.length == 1) {
				return downloadFile(files[0]); 
			}
			return false;
		} finally {
			disconnect();
		}
	}
	
	public int downloadFiles(List<String> fileNames) throws IOException {
		connect();
		int downloads = 0;
		try {
			for (String filename: fileNames) {
				FTPFile[] files = client.listFiles(filename);
				if (files != null && files.length == 1) {
					if (downloadFile(files[0])) {
						downloads++;
					}
				}					
			}
		} finally {
			disconnect();
		}
		return downloads;
	}
	
	public int downloadFiles() throws IOException {
		connect();
		int downloads = 0;
		try {
			log.debug("DownloadFiles, about to list files");
			
			FTPFile[] files = client.listFiles();
			log.debug("Filelisting: " +files.length);
			for (FTPFile file: files) {
				log.debug("About to download the file: " + file.getName());
				if (downloadFile(file)) {
					downloads++;
				}
			}								
		} finally {
			disconnect();
		}
		return downloads;
	}
	
	
	private void disconnect() throws IOException {
		client.logout();
		client.disconnect();
	}
	
	private void connect() throws IOException {
		client.connect(server);
		boolean loggedIn = client.login(username, password);
		client.enterLocalPassiveMode();
		log.debug("Able to log into " + server + "? " + loggedIn);
		client.setFileType(FTP.BINARY_FILE_TYPE);
		client.changeWorkingDirectory(remoteWorkingDirectory);
		client.setReaderThread(false);
		log.debug("Working directory: " + client.printWorkingDirectory());
	}
	
	private boolean downloadFile(FTPFile remoteFile) throws IOException {
		boolean success = false;
		File file = new File(destinationFolder + File.separator + remoteFile.getName());
		long lastModified = remoteFile.getTimestamp().getTimeInMillis();
		log.debug("Remote file is " +remoteFile.getName() + " local file is " + file.getAbsoluteFile() + " does it exist:" + file.exists());
		if (forceDownload || !file.exists() || (file.lastModified() < lastModified)) {
			log.debug("Downloading " +remoteFile.getName() + " " + remoteFile.getSize() + " to " + file.getAbsolutePath());
			
			FileOutputStream fos = new FileOutputStream(file); 
			client.retrieveFile(remoteFile.getName(), fos);
			fos.close();
			fos.flush();
			file.setLastModified(lastModified);
			success = true;
		}
		return success;
	}
	
	public static void main(String[] args) throws Exception{
		InetAddress server = InetAddress.getByName("ftp.tmstv.com");
		File destinationFolder = new File("data/samples");
		Downloader downloader = new Downloader(server, "kno868cb", "FYS141ca", destinationFolder, "pub", false);
		int count = downloader.downloadFiles();
		System.err.println("Downloaded " + count + " files");
	}
}
