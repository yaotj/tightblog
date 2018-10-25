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
package org.tightblog.ui.restapi;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.tightblog.business.UserManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogBookmark;
import org.tightblog.pojos.WeblogRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tightblog.repository.BlogrollLinkRepository;
import org.tightblog.repository.WeblogRepository;

import javax.persistence.RollbackException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * List bookmarks and allow for moving them around and deleting them.
 */
@RestController
public class BlogrollController {

    private WeblogRepository weblogRepository;
    private BlogrollLinkRepository blogrollLinkRepository;
    private WeblogManager weblogManager;
    private UserManager userManager;

    @Autowired
    public BlogrollController(WeblogRepository weblogRepository, BlogrollLinkRepository blogrollLinkRepository,
                              WeblogManager weblogManager, UserManager userManager) {
        this.weblogRepository = weblogRepository;
        this.blogrollLinkRepository = blogrollLinkRepository;
        this.weblogManager = weblogManager;
        this.userManager = userManager;
    }

    @GetMapping(value = "/tb-ui/authoring/rest/weblog/{id}/bookmarks")
    public List<WeblogBookmark> getWeblogBookmarks(@PathVariable String id, HttpServletResponse response) {
        Weblog weblog = weblogRepository.findById(id).orElse(null);
        if (weblog != null) {
            return weblog.getBookmarks()
                    .stream()
                    .peek(bkmk -> bkmk.setWeblog(null))
                    .collect(Collectors.toList());
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @DeleteMapping(value = "/tb-ui/authoring/rest/bookmark/{id}")
    public void deleteBookmark(@PathVariable String id, Principal p, HttpServletResponse response)
            throws ServletException {

        try {
            WeblogBookmark itemToRemove = blogrollLinkRepository.findById(id).orElse(null);
            if (itemToRemove != null) {
                Weblog weblog = itemToRemove.getWeblog();
                if (userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.OWNER)) {
                    weblog.getBookmarks().remove(itemToRemove);
                    weblogManager.saveWeblog(weblog);
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    @PutMapping(value = "/tb-ui/authoring/rest/bookmark/{id}")
    public void updateBookmark(@PathVariable String id, @RequestBody WeblogBookmark newData, Principal p,
                               HttpServletResponse response) throws ServletException {
        try {
            WeblogBookmark bookmark = blogrollLinkRepository.getOne(id);
            if (bookmark != null) {
                Weblog weblog = bookmark.getWeblog();
                if (userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.OWNER)) {
                    WeblogBookmark bookmarkFromWeblog = weblog.getBookmarks().stream()
                            .filter(wb -> wb.getId().equals(bookmark.getId())).findFirst().orElse(null);
                    if (bookmarkFromWeblog != null) {
                        bookmarkFromWeblog.setName(newData.getName());
                        bookmarkFromWeblog.setUrl(newData.getUrl());
                        bookmarkFromWeblog.setDescription(newData.getDescription());
                        try {
                            weblogManager.saveWeblog(weblog);
                        } catch (RollbackException e) {
                            response.setStatus(HttpServletResponse.SC_CONFLICT);
                            return;
                        }
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        // should never happen
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    @PutMapping(value = "/tb-ui/authoring/rest/bookmarks")
    public void addBookmark(@RequestParam(name = "weblogId") String weblogId, @RequestBody WeblogBookmark newData, Principal p,
                            HttpServletResponse response) throws ServletException {
        try {
            Weblog weblog = weblogRepository.findById(weblogId).orElse(null);
            if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.OWNER)) {
                WeblogBookmark bookmark = new WeblogBookmark(weblog, newData.getName(),
                        newData.getUrl(), newData.getDescription());
                weblog.addBookmark(bookmark);
                try {
                    weblogManager.saveWeblog(weblog);
                } catch (RollbackException e) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    return;
                }
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }
}