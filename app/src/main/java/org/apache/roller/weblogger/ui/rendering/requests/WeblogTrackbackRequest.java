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
package org.apache.roller.weblogger.ui.rendering.requests;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.ui.rendering.processors.IncomingTrackbackProcessor;

/**
 * Represents a request to post a weblog entry trackback.
 */
public class WeblogTrackbackRequest extends WeblogRequest {
    
    private static Log log = LogFactory.getLog(WeblogTrackbackRequest.class);
    
    // lightweight attributes
    private String blogName = null;
    private String url = null;
    private String excerpt = null;
    private String title = null;
    private String weblogAnchor = null;
    
    // heavyweight attributes
    private WeblogEntry weblogEntry = null;
    
    public WeblogTrackbackRequest(HttpServletRequest request) {
        
        // let our parent take care of their business first
        // parent determines weblog handle and locale if specified
        super(request);
        
        String servlet = request.getServletPath();
        
        // we only want the path info left over from after our parents parsing
        String pathInfo = this.getPathInfo();
        
        // was this request bound for the comment servlet?
        if(servlet == null || !IncomingTrackbackProcessor.PATH.equals(servlet)) {
            throw new IllegalArgumentException("Not a weblog trackback request: "+
                    request.getRequestURL());
        }
        
        
        /*
         * parse path info.  we expect ...
         *
         * /entry/<anchor> - permalink
         */
        if(pathInfo != null && pathInfo.trim().length() > 0) {
            
            // we should only ever get 2 path elements
            String[] pathElements = pathInfo.split("/");
            if(pathElements.length == 2) {
                
                String context = pathElements[0];
                if("entry".equals(context)) {
                    try {
                        this.weblogAnchor = URLDecoder.decode(pathElements[1], "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        // should never happen
                        log.error(ex);
                    }
                } else {
                    throw new IllegalArgumentException("bad path info: "+ request.getRequestURL());
                }
            } else {
                throw new IllegalArgumentException("bad path info: "+ request.getRequestURL());
            }
        } else {
            throw new IllegalArgumentException("bad path info: "+ request.getRequestURL());
        }
        
        
        /*
         * parse request parameters
         *
         * the only params we currently care about are:
         *   blog_name - comment author
         *   url - comment referring url
         *   excerpt - comment contents
         *   title - comment title
         */
        if(request.getParameter("blog_name") != null) {
            this.blogName = request.getParameter("blog_name");
        }
        
        if(request.getParameter("url") != null) {
            this.url = request.getParameter("url");
        }
        
        if(request.getParameter("excerpt") != null) {
            this.excerpt = request.getParameter("excerpt");
        }
        
        if(request.getParameter("title") != null) {
            this.title = request.getParameter("title");
        }
        
        // a little bit of validation, trackbacks enforce that all params
        // must have a value, so any nulls equals a bad request
        if(this.blogName == null || this.url == null || 
                this.excerpt == null || this.title == null) {
            throw new IllegalArgumentException("Bad request data.  Did not "+
                    "receive values for all trackback params (blog_name, url, excerpt, title)");
        }
        
        if(log.isDebugEnabled()) {
            log.debug("name = "+this.blogName);
            log.debug("url = "+this.url);
            log.debug("excerpt = "+this.excerpt);
            log.debug("title = "+this.title);
            log.debug("weblogAnchor = "+this.weblogAnchor);
        }
    }

    public String getBlogName() {
        return blogName;
    }

    public String getUrl() {
        return url;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWeblogAnchor() {
        return weblogAnchor;
    }

    public WeblogEntry getWeblogEntry() {
        
        if(weblogEntry == null && weblogAnchor != null) {
            try {
                WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
                weblogEntry = wmgr.getWeblogEntryByAnchor(getWeblog(), weblogAnchor);
            } catch (WebloggerException ex) {
                log.error("Error getting weblog entry "+weblogAnchor, ex);
            }
        }
        
        return weblogEntry;
    }

}