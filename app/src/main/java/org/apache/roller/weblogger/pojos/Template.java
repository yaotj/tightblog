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

import com.fasterxml.jackson.annotation.JsonValue;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.time.Instant;

/**
 * The Template interface represents the abstract concept of a single unit
 * of templated or non-rendered content.  For TightBlog we mainly think of
 * templates as Velocity templates which are meant to be fed into the
 * Velocity rendering engine.
 */
public interface Template {

    @XmlType
    @XmlEnum
    enum ComponentType {
        @XmlEnumValue("weblog")WEBLOG("Weblog", "text/html", true, "template.weblog.description"),
        @XmlEnumValue("permalink")PERMALINK("Permalink", "text/html", true, "template.permalink.description"),
        @XmlEnumValue("search")SEARCH("Search", "text/html", true, "template.search.description"),
        @XmlEnumValue("tagsIndex")TAGSINDEX("Tag Index", "text/html", true, "template.tagsIndex.description"),
        @XmlEnumValue("stylesheet")STYLESHEET("Stylesheet", "text/css", false, "template.stylesheet.description"),
        @XmlEnumValue("javascript")JAVASCRIPT("JavaScript file", "application/javascript", false,
                "template.javascript.description"),
        @XmlEnumValue("customInternal")CUSTOM_INTERNAL("Custom internal", "text/html", false,
                "template.customInternal.description"),
        @XmlEnumValue("customExternal")CUSTOM_EXTERNAL("Custom external", "text/html", false,
                "template.customExternal.description");

        private final String readableName;

        private final String contentType;

        private final boolean singleton;

        private final String descriptionProperty;

        ComponentType(String readableName, String contentType, boolean singleton, String descriptionProperty) {
            this.readableName = readableName;
            this.contentType = contentType;
            this.singleton = singleton;
            this.descriptionProperty = descriptionProperty;
        }

        public String getReadableName() {
            return readableName;
        }

        public String getContentType() {
            return contentType;
        }

        public boolean isSingleton() {
            return singleton;
        }

        public boolean isAccessibleViaUrl() {
            return !singleton && !CUSTOM_INTERNAL.equals(this);
        }

        public String getDescriptionProperty() {
            return descriptionProperty;
        }

        // so JSON will serialize name
        public String getName() {
            return name();
        }

    }

    /**
     * The template derivation provides the background for this template, useful for doing validation
     * during template customization.  Enum values:
     * SHARED - file-based only, the template came from a shared theme and was not overridden by the user
     * during template customization.
     * OVERRIDDEN - A database-stored template that overrides one provided by a shared theme.
     * SPECIFICBLOG - A database-stored template that does not override a shared template.  It is defined
     * for a single blog only.
     */
    enum TemplateDerivation {
        SHARED("Default"),
        OVERRIDDEN("Override"),
        SPECIFICBLOG("Blog-Only");

        private final String readableName;

        TemplateDerivation(String readableName) {
            this.readableName = readableName;
        }

        @JsonValue
        public String getReadableName() {
            return readableName;
        }
    }

    /**
     * The unique identifier for this Template.
     */
    String getId();

    /**
     * A simple name for this Template.
     */
    String getName();

    /**
     * A description of the contents of this Template.
     */
    String getDescription();

    /**
     * The last time the template was modified.
     */
    Instant getLastModified();

    /**
     * get the Template rendition object for the given type.
     */
    TemplateRendition getTemplateRendition(WeblogTemplateRendition.RenditionType type);

    /**
     * The role this template performs.
     */
    ComponentType getRole();

    /**
     * The derivation of this template.
     */
    TemplateDerivation getDerivation();

    /**
     * The relative path for this Template to add to the default page URL
     * to view the template from the browser providing it is not hidden.
     * Can be null or empty if hidden.
     */
    String getRelativePath();

}
