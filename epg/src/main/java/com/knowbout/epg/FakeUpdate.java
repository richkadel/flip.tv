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

package com.knowbout.epg;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Environment;

import com.appeligo.alerts.api.AlertQueue;
import com.caucho.hessian.client.HessianProxyFactory;
import com.knowbout.epg.processor.Downloader;
import com.knowbout.epg.processor.Parser;
import com.knowbout.hibernate.HibernateUtil;
import com.knowbout.hibernate.TransactionManager;

public class FakeUpdate {
	
	private static final Log log = LogFactory.getLog(FakeUpdate.class);
	
	public static void main(String[] args) {
		String configFile = "/etc/knowbout.tv/epg.xml";
		int nextarg = 0;
		if (args.length > 1) {
			if (args.length == 3 && args[0].equals("-config")) {
				configFile = args[1];
				nextarg = 2;
			} else {
				System.err.println("Usage: java "+FakeUpdate.class.getName()+" [-config <xmlfile>] <program title>");
				System.exit(1);
			}
		}
		if (args.length <= nextarg) {
			System.err.println("Usage: java "+FakeUpdate.class.getName()+" [-config <xmlfile>] <program title>");
			System.exit(1);
		}
		String title = args[nextarg];
		
		try {
			XMLConfiguration config = new XMLConfiguration(configFile);
			
			HashMap<String, String> hibernateProperties = new HashMap<String, String>();
			Configuration database = config.subset("database");
			hibernateProperties.put(Environment.DRIVER, database.getString("driver"));
			hibernateProperties.put(Environment.URL, database.getString("url"));
			hibernateProperties.put(Environment.USER, database.getString("user"));
			hibernateProperties.put(Environment.PASS, database.getString("password"));
			hibernateProperties.put(Environment.DATASOURCE, null);
			
			HibernateUtil.setProperties(hibernateProperties);
			
			Session session = HibernateUtil.openSession();
			Transaction tx = session.beginTransaction();

			String hqlUpdate = "update Program set lastModified = :now where title = :title";
			int updatedEntities = session.createQuery( hqlUpdate )
			        .setTimestamp("now", new Date())
			        .setString("title", title)
			        .executeUpdate();
			tx.commit();
			session.close();
			
			String alertUrl = config.getString("alertUrl");
			HessianProxyFactory factory = new HessianProxyFactory();
			AlertQueue alerts = (AlertQueue)factory.create(AlertQueue.class,alertUrl);
			alerts.checkAlerts();
		} catch (ConfigurationException e) {
			log.fatal("Configuration error in file "+configFile, e);
		} catch (IOException e) {
			log.fatal("Error downloading or processing EPG information", e);
		}

	}

}
