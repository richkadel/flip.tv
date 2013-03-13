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

package com.knowbout.epg.service;

import java.io.Serializable;

public class ServiceLineup implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6234120940756203112L;

	/**
	 * The name of the Headend/ServiceLineup (Time Warner or Cox)
	 */
	private String name;

	/**
	 * The name of the lineup (Digital, Cable, Satellite)
	 */
	private String lineup;
	
	/**
	 * The zipcode for this ServiceLineup.  There can be multiple zipcodes
	 * for a single provider.  This will be the zip code that was used to locate 
	 * the provider
	 */
	private String zipCode;
	
	/**
	 * The id for this ServiceLineup
	 */
	private String id;
	
	/**
	 * The DMA (Demographic Market Area) served by this ServiceLineup
	 */
	private String demographicMarketArea;


	
	/**
	 * Construcsts a new ServiceLineup instance
	 */
	public ServiceLineup() {
	}



	/**
	 * Construcsts a new ServiceLineup instance
	 * @param id The id for this ServiceLineup
	 * @param name The name of the Headend/ServiceLineup (Time Warner or Cox)
	 * @param lineup The name of the lineup (Digital, Cable, Satellite)
	 * @param zipCode The zipcode for this ServiceLineup
	 * @param demographicMarketArea The DMA (Demographic Market Area) served by this ServiceLineup
	 */
	public ServiceLineup(String id, String name, String lineup, String zipCode, String demographicMarketArea) {
		this.name = name;
		this.lineup = lineup;
		this.zipCode = zipCode;
		this.id = id; 
		this.demographicMarketArea = demographicMarketArea;
	}



	/**
	 * The DMA (Demographic Market Area) served by this ServiceLineup
	 * @return Returns the demographicMarketArea.
	 */
	public String getDemographicMarketArea() {
		return demographicMarketArea;
	}



	/**
	 * The DMA (Demographic Market Area) served by this ServiceLineup
	 * @param demographicMarketArea The demographicMarketArea to set.
	 */
	public void setDemographicMarketArea(String demographicMarketArea) {
		this.demographicMarketArea = demographicMarketArea;
	}



	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}



	/**
	 * @param id The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}



	/**
	 * The name of the lineup (Digital, Cable, Satellite)
	 * @return Returns the lineup.
	 */
	public String getLineup() {
		return lineup;
	}



	/**
	 * The name of the lineup (Digital, Cable, Satellite)
	 * @param lineup The lineup to set.
	 */
	public void setLineup(String lineup) {
		this.lineup = lineup;
	}



	/**
	 * The name of the Headend/ServiceLineup (Time Warner or Cox)
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}



	/**
	 * The name of the Headend/ServiceLineup (Time Warner or Cox)
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}



	/**
	 * The zipcode for this ServiceLineup.  There can be multiple zipcodes
	 * for a single provider.  This will be the zip code that was used to locate 
	 * the provider
	 * @return Returns the zipCode.
	 */
	public String getZipCode() {
		return zipCode;
	}



	/**
	 * The zipcode for this ServiceLineup.  There can be multiple zipcodes
	 * for a single provider.  This will be the zip code that was used to locate 
	 * the provider
	 * @param zipCode The zipCode to set.
	 */
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	
	public String toString() {
		return "ServiceLineup[id: "+id+", zipCode: "
		+ zipCode + ", name: "+ name +", DMA: " + demographicMarketArea 
		+", lineup: " + lineup+"]";
	}

}
