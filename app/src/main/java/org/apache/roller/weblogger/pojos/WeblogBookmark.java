/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;


/**
 * <p>Represents a single URL for a weblog.</p>
 */
public class WeblogBookmark implements Serializable, Comparable<WeblogBookmark> {
    
    public static final long serialVersionUID = 2315131256728236003L;
    
    private Weblog weblog;
    
    private String id = UUIDGenerator.generateUUID();
    private String name;
    private String description;
    private String url;
    private Integer priority;
    private String image;

    //----------------------------------------------------------- Constructors
    
    /** Default constructor, for use in form beans only. */
    public WeblogBookmark() {
    }
    
    public WeblogBookmark(
            Weblog parent,
            String name,
            String desc,
            String url,
            String image) {
        this.weblog = parent;
        this.name = name;
        this.description = desc;
        this.url = url;
        this.image = image;
        weblog.addBookmark(this);
        calculatePriority();
    }

    //------------------------------------------------------------- Attributes
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public void calculatePriority() {
        int size = weblog.getBookmarks().size();
        if (size == 1) {
            this.priority = 0;
        } else {
            this.priority = weblog.getBookmarks().get(size - 2).getPriority() + 1;
        }
    }

    /**
     * Name of bookmark.
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Description of bookmark.
     */
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * URL of bookmark.
     */
    public String getUrl() {
        return this.url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    /**
     * Priority determines order of display
     */
    public java.lang.Integer getPriority() {
        return this.priority;
    }
    
    public void setPriority(java.lang.Integer priority) {
        this.priority = priority;
    }
    
    public String getImage() {
        return this.image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    //---------------------------------------------------------- Relationships

    public Weblog getWeblog() {
        return this.weblog;
    }
    
    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }
    
    //------------------------------------------------------- Good citizenship
    
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getId());
        buf.append(", ").append(getUrl());
        buf.append("}");
        return buf.toString();
    }
    
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof WeblogBookmark)) {
            return false;
        }
        WeblogBookmark o = (WeblogBookmark)other;
        return new EqualsBuilder()
        .append(getName(), o.getName())
        .append(getWeblog(), o.getWeblog())
        .append(getUrl(), o.getUrl())
        .isEquals();
    }
    
    public int hashCode() {
        return new HashCodeBuilder()
        .append(getName())
        .append(getWeblog())
        .append(getUrl())
        .toHashCode();
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(WeblogBookmark o) {
        return priority.compareTo(o.getPriority());
    }
    
}
