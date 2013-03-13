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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MakeDonationAction extends BaseAction {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 7019890080969775786L;
	
	private static final Log log = LogFactory.getLog(MakeDonationAction.class);
	
	private String tx;
	private String st;
	private String amt;
	private String cc;
	private String cm;
	private String sig;

    public String execute() throws Exception {
    	
        return SUCCESS;
    }
    
    public String thankyou() throws Exception {
    	
        return SUCCESS;
    }

	public String getAmt() {
		return amt;
	}

	public void setAmt(String amt) {
		this.amt = amt;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getCm() {
		return cm;
	}

	public void setCm(String cm) {
		this.cm = cm;
	}

	public String getSig() {
		return sig;
	}

	public void setSig(String sig) {
		this.sig = sig;
	}

	public String getSt() {
		return st;
	}

	public void setSt(String st) {
		this.st = st;
	}

	public String getTx() {
		return tx;
	}

	public void setTx(String tx) {
		this.tx = tx;
	}
}
