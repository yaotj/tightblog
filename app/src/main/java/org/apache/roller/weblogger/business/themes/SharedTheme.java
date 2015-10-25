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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.*;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.Theme;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;

/**
 * The Theme object encapsulates all elements of a single weblog theme. It is
 * used mostly to contain all the templates for a theme, but does contain other
 * theme related attributes such as name, last modified date, etc.
 */
public class SharedTheme implements Theme, Serializable {

    private String id = null;
    private String name = null;
    private String description = null;
    private String author = null;
    private Date lastModified = null;
    private boolean enabled = false;

    private static Log log = LogFactory.getLog(SharedTheme.class);

    // the filesystem directory where we should read this theme from
    private String themeDir = null;

    // the theme preview image path from the shared theme's base folder
    private String previewImagePath = null;

    // the theme stylesheet
    private ThemeTemplate stylesheet = null;

    // we keep templates in a Map for faster lookups by name
    private Map<String, ThemeTemplate> templatesByName = new HashMap<String, ThemeTemplate>();

    // we keep templates in a Map for faster lookups by link
    private Map<String, ThemeTemplate> templatesByLink = new HashMap<String, ThemeTemplate>();

    // we keep templates in a Map for faster lookups by action
    private Map<ComponentType, ThemeTemplate> templatesByAction = new HashMap<ComponentType, ThemeTemplate>();

    public SharedTheme(String themeDirPath)
            throws WebloggerException {

        this.themeDir = themeDirPath;

        // load the theme elements and cache 'em
        loadThemeFromDisk();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get the collection of all templates associated with this Theme.
     */
    public List<ThemeTemplate> getTemplates() {
        return new ArrayList<ThemeTemplate>(this.templatesByName.values());
    }

    /**
     * Lookup the stylesheet. Returns null if no stylesheet defined.
     */
    public ThemeTemplate getStylesheet() {
        return this.stylesheet;
    }

    /**
     * Looup the default template, action = weblog. Returns null if the template
     * cannot be found.
     */
    public ThemeTemplate getDefaultTemplate() {
        return this.templatesByAction.get(ComponentType.WEBLOG);
    }

    /**
     * Lookup the specified template by name. Returns null if the template
     * cannot be found.
     */
    public ThemeTemplate getTemplateByName(String name) {
        return this.templatesByName.get(name);
    }

    /**
     * Lookup the specified template by link. Returns null if the template
     * cannot be found.
     */
    public ThemeTemplate getTemplateByLink(String link) {
        return this.templatesByLink.get(link);
    }

    /**
     * Lookup the specified template by action. Returns null if the template
     * cannot be found.
     */
    public ThemeTemplate getTemplateByAction(ComponentType action) {
        return this.templatesByAction.get(action);
    }

    public String getPreviewImagePath() {
        return previewImagePath;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("\n");

        for (ThemeTemplate template : templatesByName.values()) {
            sb.append(template);
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Load all the elements of this theme from disk and cache them.
     */
    private void loadThemeFromDisk() throws WebloggerException {

        log.debug("Parsing theme descriptor for " + this.themeDir);

        ThemeMetadata themeMetadata;
        try {
            // lookup theme descriptor and parse it
            ThemeMetadataParser parser = new ThemeMetadataParser();
            InputStream is = new FileInputStream(this.themeDir + File.separator
                    + "theme.xml");
            themeMetadata = parser.unmarshall(is);
        } catch (Exception ex) {
            throw new WebloggerException(
                    "Unable to parse theme.xml for theme " + this.themeDir, ex);
        }

        log.debug("Loading Theme " + themeMetadata.getName());

        // use parsed theme descriptor to load Theme data
        setId(themeMetadata.getId());
        setName(themeMetadata.getName());
        if (StringUtils.isNotEmpty(themeMetadata.getDescription())) {
            setDescription(themeMetadata.getDescription());
        } else {
            setDescription(" ");
        }
        setAuthor(themeMetadata.getAuthor());
        setLastModified(null);
        setEnabled(true);

        // load resource representing preview image
        File previewFile = new File(this.themeDir + File.separator
                + themeMetadata.getPreviewImage());
        if (!previewFile.exists() || !previewFile.canRead()) {
            log.warn("Couldn't read theme [" + this.getName()
                    + "] preview image file ["
                    + themeMetadata.getPreviewImage() + "]");
        } else {
            this.previewImagePath = themeMetadata.getPreviewImage();
        }

        // available types with Roller
        List<RenditionType> availableTypesList = new ArrayList<RenditionType>();
        availableTypesList.add(RenditionType.STANDARD);
        if (themeMetadata.getDualTheme()) {
            availableTypesList.add(RenditionType.MOBILE);
        }

        // load stylesheet if possible
        if (themeMetadata.getStylesheet() != null) {

            ThemeMetadataTemplate stylesheetTmpl = themeMetadata
                    .getStylesheet();
            // getting the template codes for available types
            ThemeMetadataTemplateRendition standardTemplateCode = stylesheetTmpl
                    .getTemplateRenditionTable().get(RenditionType.STANDARD);
            ThemeMetadataTemplateRendition mobileTemplateCode = stylesheetTmpl
                    .getTemplateRenditionTable().get(RenditionType.MOBILE);

            // standardTemplateCode required
            if (standardTemplateCode == null) {
                throw new WebloggerException(
                        "Cannot retrieve required standard rendition for template's stylesheet");
            } else if (mobileTemplateCode == null && themeMetadata.getDualTheme()) {
                // clone the standard template code if no mobile is present
                mobileTemplateCode = new ThemeMetadataTemplateRendition();
                mobileTemplateCode.setContentsFile(standardTemplateCode
                        .getContentsFile());
                mobileTemplateCode.setTemplateLang(standardTemplateCode
                        .getTemplateLang());
                mobileTemplateCode.setType(RenditionType.MOBILE);

                stylesheetTmpl.addTemplateRendition(mobileTemplateCode);
            }

            // construct File object from path
            // we are getting the file path from standard as the default and
            // load it to initially.
            File templateFile = new File(this.themeDir + File.separator
                    + standardTemplateCode.getContentsFile());

            // read stylesheet contents
            String contents = loadTemplateFile(templateFile);
            if (contents == null) {
                // if we don't have any contents then skip this one
                log.error("Couldn't load stylesheet theme [" + this.getName()
                        + "] template file [" + templateFile + "]");
            } else {

                // construct ThemeTemplate representing this file
                // here we set content and template language from standard
                // template code assuming it is the default
                SharedThemeTemplate themeTemplate = new SharedThemeTemplate(
                        themeMetadata.getId() + ":"
                                + stylesheetTmpl.getName(),
                        stylesheetTmpl.getAction(), stylesheetTmpl.getName(),
                        stylesheetTmpl.getDescription(), contents,
                        stylesheetTmpl.getLink(), new Date(
                                templateFile.lastModified()), false, false);

                for (RenditionType type : availableTypesList) {
                    SharedThemeTemplateRendition rendition = createRendition(
                            themeTemplate.getId(),
                            stylesheetTmpl.getTemplateRendition(type));

                    themeTemplate.addTemplateRendition(rendition);

                    // Set Last Modified
                    Date lstModified = rendition.getLastModified();
                    if (getLastModified() == null
                            || lstModified.after(getLastModified())) {
                        setLastModified(lstModified);
                    }
                }
                // store it
                this.stylesheet = themeTemplate;

                // Update last modified
                themeTemplate.setLastModified(getLastModified());

                addTemplate(themeTemplate);
            }

        }

        // go through templates and read in contents to a ThemeTemplate
        SharedThemeTemplate themeTemplate;
        for (ThemeMetadataTemplate templateMetadata : themeMetadata.getTemplates()) {

            // getting the template codes for available types
            ThemeMetadataTemplateRendition standardTemplateCode = templateMetadata
                    .getTemplateRenditionTable().get(RenditionType.STANDARD);
            ThemeMetadataTemplateRendition mobileTemplateCode = templateMetadata
                    .getTemplateRenditionTable().get(RenditionType.MOBILE);

            // If no template code present for any type
            if (standardTemplateCode == null) {
                throw new WebloggerException(
                        "Cannot retrieve required standard rendition for template");
            } else if (mobileTemplateCode == null && themeMetadata.getDualTheme()) {
                // cloning the standard template code if no mobile is present
                mobileTemplateCode = new ThemeMetadataTemplateRendition();
                mobileTemplateCode.setContentsFile(standardTemplateCode
                        .getContentsFile());
                mobileTemplateCode.setTemplateLang(standardTemplateCode
                        .getTemplateLang());
                mobileTemplateCode.setType(RenditionType.MOBILE);

                templateMetadata.addTemplateRendition(mobileTemplateCode);
            }

            // construct File object from path
            File templateFile = new File(this.themeDir + File.separator
                    + standardTemplateCode.getContentsFile());

            String contents = loadTemplateFile(templateFile);
            if (contents == null) {
                // if we don't have any contents then skip this one
                throw new WebloggerException("Couldn't load theme ["
                        + this.getName() + "] template file [" + templateFile
                        + "]");
            }

            // construct ThemeTemplate representing this file
            themeTemplate = new SharedThemeTemplate(
                    themeMetadata.getId() + ":" + templateMetadata.getName(),
                    templateMetadata.getAction(), templateMetadata.getName(),
                    templateMetadata.getDescription(), contents,
                    templateMetadata.getLink(), new Date(
                            templateFile.lastModified()),
                    templateMetadata.isHidden(), templateMetadata.isNavbar());

            for (RenditionType type : availableTypesList) {
                SharedThemeTemplateRendition templateCode = createRendition(
                        themeTemplate.getId(),
                        templateMetadata.getTemplateRendition(type));

                themeTemplate.addTemplateRendition(templateCode);

                // Set Last Modified
                Date lstModified = templateCode.getLastModified();
                if (getLastModified() == null
                        || lstModified.after(getLastModified())) {
                    setLastModified(lstModified);
                }
            }

            themeTemplate.setLastModified(getLastModified());

            // add it to the theme
            addTemplate(themeTemplate);

        }
    }

    /**
     * Load a single template file as a string, returns null if can't read file.
     */
    private String loadTemplateFile(File templateFile) {
        // Continue reading theme even if problem encountered with one file
        if (!templateFile.exists() && !templateFile.canRead()) {
            return null;
        }

        char[] chars;
        int length;
        try {
            chars = new char[(int) templateFile.length()];
            FileInputStream stream = new FileInputStream(templateFile);
            InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
            length = reader.read(chars);
        } catch (Exception noprob) {
            log.error("Exception reading theme [" + this.getName()
                    + "] template file [" + templateFile + "]");
            if (log.isDebugEnabled()) {
                log.debug(noprob);
            }
            return null;
        }

        return new String(chars, 0, length);
    }

    /**
     * Set the value for a given template name.
     */
    private void addTemplate(ThemeTemplate template) {
        this.templatesByName.put(template.getName(), template);
        this.templatesByLink.put(template.getLink(), template);
        if (!ComponentType.CUSTOM.equals(template.getAction())) {
            this.templatesByAction.put(template.getAction(), template);
        }
    }

    private SharedThemeTemplateRendition createRendition(String templateId,
            ThemeMetadataTemplateRendition templateCodeMetadata) {
        SharedThemeTemplateRendition templateRendition = new SharedThemeTemplateRendition();

        // construct File object from path
        File templateFile = new File(this.themeDir + File.separator
                + templateCodeMetadata.getContentsFile());

        // read stylesheet contents
        String contents = loadTemplateFile(templateFile);
        if (contents == null) {
            // if we don't have any contents then load no string
            contents = "";
            log.error("Couldn't load stylesheet theme [" + this.getName()
                    + "] template file [" + templateFile + "]");
        }
        //TODO: remove templateId above
        templateRendition.setTemplate(contents);
        templateRendition.setTemplateLanguage(templateCodeMetadata.getTemplateLang());
        templateRendition.setType(templateCodeMetadata.getType());
        templateRendition.setLastModified(new Date(templateFile.lastModified()));

        return templateRendition;
    }

    public int compareTo(Theme other) {
        return getName().compareTo(other.getName());
    }
}
