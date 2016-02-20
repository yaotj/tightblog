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
package org.apache.roller.weblogger.ui.rendering.processors;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.Renderer;
import org.apache.roller.weblogger.ui.rendering.RendererManager;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository;
import org.apache.roller.weblogger.ui.rendering.model.Model;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.weblogger.ui.rendering.util.WeblogSearchRequest;
import org.apache.roller.weblogger.util.cache.CachedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles search queries for weblogs.
 */
@RestController
@RequestMapping(path="/roller-ui/rendering/search/**")
public class SearchProcessor {

    private static Log log = LogFactory.getLog(SearchProcessor.class);

    public static final String PATH = "/roller-ui/rendering/search";

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    private RendererManager rendererManager = null;

    public void setRendererManager(RendererManager rendererManager) {
        this.rendererManager = rendererManager;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing SearchProcessor...");
    }

    @RequestMapping(method = RequestMethod.GET)
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        log.debug("Entering");

        Weblog weblog = null;
        WeblogSearchRequest searchRequest = null;

        // first off lets parse the incoming request and validate it
        try {
            searchRequest = new WeblogSearchRequest(request);

            // now make sure the specified weblog really exists
            weblog = weblogManager.getWeblogByHandle(searchRequest.getWeblogHandle(),
                            Boolean.TRUE);

        } catch (Exception e) {
            // invalid search request format or weblog doesn't exist
            log.debug("error creating weblog search request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Get the deviceType from user agent
        MobileDeviceRepository.DeviceType deviceType = MobileDeviceRepository
                .getRequestType(request);

        // for previews we explicitly set the deviceType attribute
        if (request.getParameter("type") != null) {
            deviceType = request.getParameter("type").equals("standard") ? MobileDeviceRepository.DeviceType.standard
                    : MobileDeviceRepository.DeviceType.mobile;
        }

        // lookup template to use for rendering
        ThemeTemplate page = null;
        try {

            // try looking for a specific search page
            page = weblog.getTheme().getTemplateByAction(ThemeTemplate.ComponentType.SEARCH);

            // if not found then fall back on default page
            if (page == null) {
                page = weblog.getTheme().getTemplateByAction(ThemeTemplate.ComponentType.WEBLOG);
            }

            // if still null then that's a problem
            if (page == null) {
                throw new WebloggerException("Could not lookup default page "
                        + "for weblog " + weblog.getHandle());
            }
        } catch (Exception e) {
            log.error(
                    "Error getting default page for weblog "
                            + weblog.getHandle(), e);
        }

        // set the content type
        response.setContentType("text/html; charset=utf-8");

        // looks like we need to render content
        Map<String, Object> model;
        try {
            // populate the rendering model
            Map<String, Object> initData = new HashMap<>();
            initData.put("request", request);

            // this is a little hacky, but nothing we can do about it
            // we need the 'weblogRequest' to be a pageRequest so other models
            // are properly loaded, which means that searchRequest needs its
            // own custom initData property aside from the standard
            // weblogRequest.
            // possible better approach is make searchRequest extend
            // pageRequest.
            WeblogPageRequest pageRequest = new WeblogPageRequest();
            pageRequest.setWeblogHandle(searchRequest.getWeblogHandle());
            pageRequest.setWeblogCategoryName(searchRequest
                    .getWeblogCategoryName());
            pageRequest.setDeviceType(searchRequest.getDeviceType());
            initData.put("parsedRequest", pageRequest);
            initData.put("searchRequest", searchRequest);

            // Load models for pages
            model = Model.getModelMap("searchModelSet", initData);

            // Load special models for site-wide blog
            if (WebloggerRuntimeConfig.isSiteWideWeblog(weblog.getHandle())) {
                model.putAll(Model.getModelMap("siteModelSet", initData));
            }

        } catch (WebloggerException ex) {
            log.error("Error loading model objects for page", ex);

            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // lookup Renderer we are going to use
        Renderer renderer;
        try {
            log.debug("Looking up renderer");
            renderer = rendererManager.getRenderer(page, deviceType);
        } catch (Exception e) {
            // nobody wants to render my content :(
            log.error("Couldn't find renderer for rsd template", e);

            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // render content
        CachedContent rendererOutput = new CachedContent(WebloggerCommon.FOUR_KB_IN_BYTES);
        try {
            log.debug("Doing rendering");
            renderer.render(model, rendererOutput.getCachedWriter());

            // flush rendered output and close
            rendererOutput.flush();
            rendererOutput.close();
        } catch (Exception e) {
            // bummer, error during rendering
            log.error("Error during rendering for rsd template", e);

            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // post rendering process

        // flush rendered content to response
        log.debug("Flushing response output");
        response.setContentLength(rendererOutput.getContent().length);
        response.getOutputStream().write(rendererOutput.getContent());

        log.debug("Exiting");
    }

}