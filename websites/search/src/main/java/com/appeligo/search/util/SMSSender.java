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

package com.appeligo.search.util;

import java.net.MalformedURLException;

import javax.mail.PasswordAuthentication;

import org.apache.commons.configuration.Configuration;

import com.appeligo.epg.DefaultEpg;
import com.caucho.hessian.client.HessianProxyFactory;
import com.knowbout.epg.service.EPGProvider;

public class SMSSender {

	private String mailHost;
	private int port;
	private String smtpUser;
	private String password;
	private EPGProvider epg;

	public SMSSender() throws MalformedURLException {
        Configuration config = ConfigUtils.getSystemConfig();
        mailHost = config.getString("smtpServer", "localhost");
        password = config.getString("smtpSenderPassword");
        smtpUser = config.getString("smtpUser", "alerts@flip.tv");
        port = config.getInt("smtpPort", 25);
    	epg = DefaultEpg.getInstance();
	}
	
	/*public void send(Collection<Alert> alerts, KeywordEvent event){
		Program program = epg.getProgram(event.getProgram().getProgramId());
		for (Alert alert: alerts) {
			alert.setEnabled(false);
			User user = alert.getUser();
			if (user.isAlertsEnabled() && user.getSmsEmail() != null) {
				String to = user.getSmsEmail();			
				String subject = "Flip.TV Alert " + alert.getKeywords();
				StringBuilder body = new StringBuilder();
				body.append("Alert Triggered on ");
				body.append(program.getProgramTitle());
				if (program.getEpisodeTitle().trim().length() > 0) {
					body.append('(');
					body.append(program.getEpisodeTitle());
					body.append(')');
				}	
				body.append(" on channel ");
				body.append(event.getChannel());
				try {
								
					Properties props = new Properties();
					
					//Specify the desired SMTP server
					props.put("mail.smtp.host", mailHost);
					props.put("mail.smtp.port", Integer.toString(port));
					// create a new Session object
					Session session = null;
					if (password != null) {
						props.put("mail.smtp.auth", "true");
						session = Session.getInstance(props,new SMTPAuthenticator(smtpUser, password));
					} else {
						session = Session.getInstance(props, null);
					}
					session.setDebug(true);
					// create a new MimeMessage object (using the Session created above)
					Message message = new MimeMessage(session);
					message.setFrom(new InternetAddress(smtpUser));
					message.setRecipients(Message.RecipientType.TO, new InternetAddress[] { new InternetAddress(to) });
					message.setSubject(subject);
					message.setContent(body.toString(), "text/plain");
					
					Transport.send(message);
//					System.err.println(message);
					
					
				} catch (Throwable t) {
					
					t.printStackTrace();
				}
			}
		}
		
	}
	*/
	/**
	* SimpleAuthenticator is used to do simple authentication
	* when the SMTP server requires it.
	*/
	private static class SMTPAuthenticator extends javax.mail.Authenticator {
		private String username;
		private String password;
		
		public SMTPAuthenticator(String username, String password) {
			this.username = username;
			this.password = password;
		}
	    public PasswordAuthentication getPasswordAuthentication() {
	        return new PasswordAuthentication(username, password);
	    }
	}
}
