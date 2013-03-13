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

package com.appeligo.velocity.view;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ViewTool;

import com.knowbout.epg.service.Network;

public class LogoTool implements ViewTool {


    private File appDir;

    /**
     * Initializes this instance for the current request.
     *
     * @param obj the ViewContext of the current request
     */
    public void init(Object obj)
    {
        ViewContext context = (ViewContext)obj;
        ServletContext servletContext = context.getServletContext();
        appDir = new File(servletContext.getRealPath("/"));
    }
    
	public String network(String dir, String extension, Network network) {
		if (network == null || extension == null) {
			return "";
		} else {
			//Check to see if the file exists
			String logo = dir + File.separator + network.getStationCallSign().toLowerCase() + "." +extension;
			return image(logo, network.getStationCallSign());
		}
	}
	
	public String callSign(String dir, String extension, String callSign) {
		if (extension == null || callSign == null) {
			return "";
		} else {
			//Check to see if the file exists
			String logo = dir + File.separator + callSign.toLowerCase() + "." +extension;
			return image(logo, callSign);
		}
	}
	
	public String networkImageAttributes(String dir, String extension, Network network) {
		if (network == null || extension == null) {
			return "";
		} else {
			//Check to see if the file exists
			String logo = dir + File.separator + network.getStationCallSign().toLowerCase() + "." +extension;
			return imageInnards(logo, network.getStationCallSign());
		}
	}
	
	public String callSignImageAttributes(String dir, String extension, String callSign) {
		if (extension == null || callSign == null) {
			return "";
		} else {
			//Check to see if the file exists
			String logo = dir + File.separator + callSign.toLowerCase() + "." +extension;
			return imageInnards(logo, callSign);
		}
	}
	
	public boolean networkExists(String dir, String extension, Network network) {
		if (network == null || extension == null) {
			return false;
		} else {
			//Check to see if the file exists
			String logo = dir + File.separator + network.getStationCallSign().toLowerCase() + "." +extension;
			return imageExists(logo);
		}
	}
	
	public boolean callSignExists(String dir, String extension, String callSign) {
		if (extension == null || callSign == null) {
			return false;
		} else {
			//Check to see if the file exists
			String logo = dir + File.separator + callSign.toLowerCase() + "." +extension;
			return imageExists(logo);
		}
	}
	
	private String image(String logoPath, String alt) {
		return "<img "+imageInnards(logoPath, alt)+"\"/>";			
	}
	
	private String imageInnards(String logoPath, String alt) {
		File logoFile = new File(appDir, logoPath);
		if (logoFile.exists()) {
			logoPath = logoPath.replace("\\", "/");
			return "src=\""+logoPath+"\" alt=\""+alt+"\"";			
		} else {
			return "";
		}
	}
	
	private boolean imageExists(String logoPath) {
		File logoFile = new File(appDir, logoPath);
		if (logoFile.exists()) {
			return true;
		} else {
			return false;
		}
	}
}
