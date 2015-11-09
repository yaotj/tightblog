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

package org.apache.roller.weblogger.ui.struts2.util;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.core.util.menu.Menu;
import org.apache.roller.weblogger.ui.core.util.menu.MenuHelper;
import org.apache.struts2.interceptor.RequestAware;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Extends the Struts2 ActionSupport class to add in support for handling an
 * error and status success.  Other actions extending this one only need to
 * calle setError() and setSuccess() accordingly.
 * 
 * NOTE: as a small convenience, all errors and messages are assumed to be keys
 * which point to a success in a resource bundle, so we automatically call
 * getText(key) on the param passed into setError() and setSuccess().
 */
public abstract class UIAction extends ActionSupport
        implements UIActionPreparable, UISecurityEnforced, RequestAware {

    private static final List LOCALES;
    private static final List TIME_ZONES;

    private static Comparator<Locale> LocaleComparator = new Comparator<Locale>() {
        public int compare(Locale locale1, Locale locale2) {
            int compName = locale1.getDisplayName().compareTo(locale2.getDisplayName());
            if (compName == 0) {
                return locale1.toString().compareTo(locale2.toString());
            }
            return compName;
        }
    };

    // load up the locales and time zones lists
    static {
        // build locales list
        LOCALES = Arrays.asList(Locale.getAvailableLocales());
        Collections.sort(LOCALES, LocaleComparator);

        // build time zones list
        TIME_ZONES = Arrays.asList(TimeZone.getAvailableIDs());
        Collections.sort(TIME_ZONES);
    }

    // a result that sends the user to an access denied warning
    public static final String DENIED = "access-denied";
    
    // a common result name used to indicate the result should list some data
    public static final String LIST = "list";
    
    // a result for a cancel.
    public static final String CANCEL = "cancel";
    
    // the authenticated user accessing this action, or null if client is not logged in
    private User authenticatedUser = null;
    
    // the weblog this action is intended to work on, or null if no weblog specified
    private Weblog actionWeblog = null;
    
    // the weblog handle of the action weblog
    private String weblog = null;
    
    // action name (used by tabbed menu utility)
    protected String actionName = null;
    
    // the name of the menu this action wants to show, or null for no menu
    protected String desiredMenu = null;
    
    // page title, called by some Tiles JSPs (e.g., tiles-simplepage.jsp)
    protected String pageTitle = null;

    protected String salt = null;
    
    public void myPrepare() {
        // no-op
    }
	
	public void setRequest(Map<String, Object> map) {
		this.salt = (String) map.get("salt");
	}

	public String getSalt() {
		return salt;
	}
	
    /**
     * Necessary to avoid showing up "Error setting expression 'salt' with value ...".
     * See also https://issues.apache.org/jira/browse/ROL-2068
     * @param salt previous salt
     */
    public void setSalt(String salt) {
        // no-op
    }

    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.ADMIN;
    }

    @Override
    public WeblogRole requiredWeblogRole() {
        return WeblogRole.OWNER;
    }

    /**
     * Cancel.
     *
     * @return "CANCEL" string constant.
     */
    public String cancel() {
        return CANCEL;
    }

    public String getSiteURL() {
        return WebloggerRuntimeConfig.getRelativeContextURL();
    }
    
    public String getAbsoluteSiteURL() {
        return WebloggerRuntimeConfig.getAbsoluteContextURL();
    }
    
    public String getProp(String key) {
        // first try static config
        String value = WebloggerConfig.getProperty(key);
        if(value == null) {
            value = WebloggerRuntimeConfig.getProperty(key);
        }
        
        return (value == null) ? key : value;
    }
    
    public boolean getBooleanProp(String key) {
        // first try static config
        String value = WebloggerConfig.getProperty(key);
        if(value == null) {
            value = WebloggerRuntimeConfig.getProperty(key);
        }
        
        return (value == null) ? false : Boolean.valueOf(value);
    }
    
    public int getIntProp(String key) {
        // first try static config
        String value = WebloggerConfig.getProperty(key);
        if(value == null) {
            value = WebloggerRuntimeConfig.getProperty(key);
        }
        
        return (value == null) ? 0 : Integer.valueOf(value);
    }

    @Override
    public String getText(String aTextName) {
        return super.getText(cleanTextKey(aTextName));
    }

    @Override
    public String getText(String aTextName, String defaultValue) {
        return super.getText(cleanTextKey(aTextName), cleanTextKey(defaultValue));
    }

    @Override
    public String getText(String aTextName, String defaultValue, String obj) {
        return super.getText(cleanTextKey(aTextName), cleanTextKey(defaultValue), cleanTextArg(obj));
    }

    @Override
    public String getText(String aTextName, List<?> args) {
        List<Object> cleanedArgs = new ArrayList<Object>(args.size());
        for (Object el : args) {
            cleanedArgs.add(el instanceof String ? cleanTextArg((String) el) : el);
        }
        return super.getText(cleanTextKey(aTextName), cleanedArgs);
    }

    @Override
    public String getText(String key, String[] args) {
        String[] cleanedArgs = new String[args.length];
        for (int i = 0; i < args.length; ++i) {
            cleanedArgs[i] = cleanTextArg(args[i]);
        }
        return super.getText(cleanTextKey(key), cleanedArgs);
    }

    @Override
    public String getText(String aTextName, String defaultValue, List<?> args) {
        List<Object> cleanedArgs = new ArrayList<Object>(args.size());
        for (Object el : args) {
            cleanedArgs.add(el instanceof String ? cleanTextArg((String) el) : el);
        }
        return super.getText(cleanTextKey(aTextName), cleanTextKey(defaultValue), cleanedArgs);
    }

    @Override
    public String getText(String key, String defaultValue, String[] args) {
        String[] cleanedArgs = new String[args.length];
        for (int i = 0; i < args.length; ++i) {
            cleanedArgs[i] = cleanTextArg(args[i]);
        }
        return super.getText(cleanTextKey(key), cleanTextKey(defaultValue), cleanedArgs);
    }

    public void addError(String errorKey) {
        addActionError(getText(errorKey));
    }
    
    public void addError(String errorKey, String param) {
        addActionError(getText(errorKey, errorKey, param));
    }
    
    public void addError(String errorKey, List args) {
        addActionError(getText(errorKey, args));
    }
    
    /**
     * This simply returns the result of hasActionErrors() but we need it
     * because without it you can't easily check if there were errors since
     * you can't call a hasXXX() method via OGNL.
     */
    public boolean errorsExist() {
        return hasActionErrors();
    }
    
    
    public void addMessage(String msgKey) {
        addActionMessage(getText(msgKey));
    }
    
    public void addMessage(String msgKey, String param) {
        addActionMessage(getText(msgKey, msgKey, param));
    }
    
    public void addMessage(String msgKey, List args) {
        addActionMessage(getText(msgKey, args));
    }
    
    /**
     * This simply returns the result of hasActionMessages() but we need it
     * because without it you can't easily check if there were messages since
     * you can't call a hasXXX() method via OGNL.
     */
    public boolean messagesExist() {
        return hasActionMessages();
    }
    

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(User authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    public Weblog getActionWeblog() {
        return actionWeblog;
    }

    public void setActionWeblog(Weblog workingWeblog) {
        this.actionWeblog = workingWeblog;
    }

    public String getWeblog() {
        return weblog;
    }

    public void setWeblog(String weblog) {
        this.weblog = weblog;
    }
    
    public String getPageTitle() {
        return getText(pageTitle);
    }

    public void setPageTitle(String pageTitle) {
        // disabled by default as it causes page titles not
        // to update on chain actions defined in struts.xml
        // override in subclasses where you want this to occur.
        // this.pageTitle = pageTitle;
    }

    public String getActionName() {
        return this.actionName;
    }
    
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getDesiredMenu() {
        return desiredMenu;
    }

    public void setDesiredMenu(String desiredMenu) {
        this.desiredMenu = desiredMenu;
    }
    
    public Menu getMenu() {
        return MenuHelper.getMenu(getDesiredMenu(), getActionName(), getAuthenticatedUser(), getActionWeblog());
    }
    
    
    public String getShortDateFormat() {
        DateFormat sdf = DateFormat.getDateInstance(
                DateFormat.SHORT, getLocale());
        if (sdf instanceof SimpleDateFormat) {
            return ((SimpleDateFormat)sdf).toPattern();
        }
        return "yyyy/MM/dd";
    }
    
    public String getMediumDateFormat() {
        DateFormat sdf = DateFormat.getDateInstance(
                DateFormat.MEDIUM, getLocale());
        if (sdf instanceof SimpleDateFormat) {
            return ((SimpleDateFormat)sdf).toPattern();
        }
        return "MMM dd, yyyy";
    }
    
    public List getLocalesList() {
        return LOCALES;
    }

    public List getTimeZonesList() {
        return TIME_ZONES;
    }
    
    public List getHoursList() {
        List ret = new ArrayList();
        for (int i=0; i<24; i++) {
            ret.add(i);
        }
        return ret;
    }
    
    public List getMinutesList() {
        List ret = new ArrayList();
        for (int i=0; i<60; i++) {
            ret.add(i);
        }
        return ret;
    }
    
    public List getSecondsList() {
        return getMinutesList();
    }

    public List<Pair<Integer, String>> getCommentDaysList() {
        List<Pair<Integer, String>> opts = new ArrayList<>();
        opts.add(Pair.of(0, getText("weblogEdit.unlimitedCommentDays")));
        opts.add(Pair.of(3, getText("weblogEdit.days3")));
        opts.add(Pair.of(7, getText("weblogEdit.days7")));
        opts.add(Pair.of(14, getText("weblogEdit.days14")));
        opts.add(Pair.of(30, getText("weblogEdit.days30")));
        opts.add(Pair.of(60, getText("weblogEdit.days60")));
        opts.add(Pair.of(90, getText("weblogEdit.days90")));
        return opts;
    }

    private static String cleanExpressions(String s) {
        return (s == null || s.contains("${") || s.contains("%{")) ? "" : s;
    }

    public static String cleanTextKey(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        // escape HTML
        return StringEscapeUtils.escapeHtml4(cleanExpressions(s));
    }

    public static String cleanTextArg(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return StringEscapeUtils.escapeHtml4(s);
    }

}
