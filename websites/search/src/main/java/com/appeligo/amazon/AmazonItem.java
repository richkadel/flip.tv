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

public class AmazonItem {
    private String id;
    private String title;
    private String detailsUrl;
    private String smallImageUrl;
    private int smallImageWidth;
    private int smallImageHeight;
    
    public AmazonItem(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String toString() {
        return id + ":" + title;
    }
    public String getDetailsUrl() {
        return detailsUrl;
    }
    public void setDetailsUrl(String detailsUrl) {
        this.detailsUrl = detailsUrl;
    }
    public int getSmallImageHeight() {
        return smallImageHeight;
    }
    public void setSmallImageHeight(int smallImageHeight) {
        this.smallImageHeight = smallImageHeight;
    }
    public String getSmallImageUrl() {
        return smallImageUrl;
    }
    public void setSmallImageUrl(String smallImageUrl) {
        this.smallImageUrl = smallImageUrl;
    }
    public int getSmallImageWidth() {
        return smallImageWidth;
    }
    public void setSmallImageWidth(int smallImageWidth) {
        this.smallImageWidth = smallImageWidth;
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AmazonItem)) {
            return false;
        }
        AmazonItem item = (AmazonItem)obj;
        return id.equals(item.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
}
