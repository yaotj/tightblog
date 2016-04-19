/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.util.Utilities;

/**
 * Represents a request for a Roller weblog feed.
 * 
 * /tb-ui/rendering/feeds/*
 *
 * We use this class as a helper to parse an incoming url and sort out the
 * information embedded in the url for later use.
 */
public class WeblogFeedRequest extends WeblogRequest {
    
    private static Log log = LogFactory.getLog(WeblogFeedRequest.class);
    
    private String type = null;
    private String format = null;
    private String weblogCategoryName = null;
    private List<String> tags = null;
    private boolean siteWideFeed = false;
    private int page = 0;

    /**
     * Construct the WeblogFeedRequest by parsing the incoming url
     */
    public WeblogFeedRequest(HttpServletRequest request) {
        
        // let our parent take care of their business first
        // parent determines weblog handle and locale if specified
        super(request);
        
        // we only want the path info left over from after our parents parsing
        String pathInfo = getPathInfo();
        
        // parse the request object and figure out what we've got
        log.debug("parsing path " + pathInfo);
        
        /*
         * parse the path info.  Format:
         *
         * must look like this ...
         *
         * /<type>/<format>
         */
        if (pathInfo != null && pathInfo.trim().length() > 1) {

            String[] pathElements = pathInfo.split("/");
            if(pathElements.length == 2) {
                type = pathElements[0];
                format = pathElements[1];
            } else {
                throw new IllegalArgumentException("Invalid feed path info: "+ request.getRequestURL());
            }

        } else {
            throw new IllegalArgumentException("Invalid feed path info: "+ request.getRequestURL());
        }
        
        // parse request parameters
        if (request.getParameter("cat") != null) {
            // replacing any plus signs with their encoded equivalent (http://stackoverflow.com/a/6926987)
            weblogCategoryName =
                    Utilities.decode(request.getParameter("cat").replace("+", "%2B"));
        }
        
        if (request.getParameter("tags") != null) {
            tags = Utilities.splitStringAsTags(request.getParameter("tags"));
            int maxSize = WebloggerStaticConfig.getIntProperty("tags.queries.maxIntersectionSize", 3);
            if (tags.size() > maxSize) {
                throw new IllegalArgumentException("Max number of tags allowed is " + maxSize + ", "
                        + request.getRequestURL());
            }
        }

        if (request.getParameter("page") != null) {
            try {
                page = Integer.parseInt(request.getParameter("page"));
            } catch(NumberFormatException ignored) {
            }
        }     
        
        if ((tags != null && tags.size() > 0) && weblogCategoryName != null) {
            throw new IllegalArgumentException("Please specify either category or tags but not both: " + request.getRequestURL());
        }

        if (log.isDebugEnabled()) {
            log.debug("type = " + type);
            log.debug("page = " + type);
            log.debug("format = " + format);
            log.debug("weblogCategory = " + weblogCategoryName);
            log.debug("tags = " + tags);
        }
    }

    public String getType() {
        return type;
    }

    public String getFormat() {
        return format;
    }

    public String getWeblogCategoryName() {
        return weblogCategoryName;
    }

    public List<String> getTags() {
      return tags;
    }

    public int getPage() {
        return page;
    }

    public boolean isSiteWideFeed() {
        return siteWideFeed;
    }

    public void setSiteWideFeed(boolean siteWideFeed) {
        this.siteWideFeed = siteWideFeed;
    }
}
