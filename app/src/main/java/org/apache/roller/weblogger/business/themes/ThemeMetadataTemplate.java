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
package org.apache.roller.weblogger.business.themes;

import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A parsed 'template' element of a theme metadata descriptor.
 */
public class ThemeMetadataTemplate {
    
    private ComponentType action = null;
    private String name = null;
    private String description = null;
    private String link = null;
    private boolean navbar = false;
    private boolean hidden = false;
    private String contentType = null;

    // Hash table to keep metadata about parsed template code files
    private Map<RenditionType, ThemeMetadataTemplateRendition> templateRenditionTable
            = new HashMap<>();

    public ComponentType getAction() {
        return action;
    }

    @XmlAttribute
    public void setAction(ComponentType action) {
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isNavbar() {
        return navbar;
    }

    public void setNavbar(boolean navbar) {
        this.navbar = navbar;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void addTemplateRendition(ThemeMetadataTemplateRendition templateCode) {
        this.getTemplateRenditionTable().put(templateCode.getType(), templateCode);
    }

    public Map<RenditionType, ThemeMetadataTemplateRendition> getTemplateRenditionTable() {
        return templateRenditionTable;
    }

    @XmlJavaTypeAdapter(value=MapAdapter.class)
    @XmlElements(@XmlElement(name="renditions"))
    public void setTemplateRenditionTable(Map<RenditionType, ThemeMetadataTemplateRendition> templateRenditionTable) {
        this.templateRenditionTable = templateRenditionTable;
    }

    static class MapAdapter extends XmlAdapter<MapAdapter.AdaptedMap, Map<RenditionType, ThemeMetadataTemplateRendition>> {

        static class AdaptedMap {
            @XmlElements(@XmlElement(name="rendition"))
            public List<ThemeMetadataTemplateRendition> renditions = new ArrayList<>();
        }

        @Override
        public Map<RenditionType, ThemeMetadataTemplateRendition> unmarshal(AdaptedMap list) throws Exception {
            Map<RenditionType, ThemeMetadataTemplateRendition> map = new HashMap<>();
            for(ThemeMetadataTemplateRendition item : list.renditions) {
                map.put(item.getType(), item);
            }
            return map;
        }

        @Override
        public AdaptedMap marshal(Map<RenditionType, ThemeMetadataTemplateRendition> map) throws Exception {
            // unused
            return null;
        }
    }
}
