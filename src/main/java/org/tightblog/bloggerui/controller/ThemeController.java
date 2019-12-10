package org.tightblog.bloggerui.controller;

import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.tightblog.bloggerui.model.SuccessResponse;
import org.tightblog.bloggerui.model.Violation;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogManager;
import org.tightblog.domain.SharedTheme;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template;
import org.tightblog.domain.User;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogRole;
import org.tightblog.domain.WeblogTemplate;
import org.tightblog.domain.WeblogTheme;
import org.tightblog.dao.UserDao;
import org.tightblog.dao.WeblogDao;
import org.tightblog.dao.WeblogTemplateDao;
import org.tightblog.bloggerui.model.ValidationErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
public class ThemeController {

    private static Logger log = LoggerFactory.getLogger(ThemeController.class);

    private WeblogDao weblogDao;
    private WeblogTemplateDao weblogTemplateDao;
    private UserDao userDao;
    private ThemeManager themeManager;
    private WeblogManager weblogManager;
    private UserManager userManager;
    private MessageSource messages;

    @Autowired
    public ThemeController(WeblogDao weblogDao, WeblogTemplateDao weblogTemplateDao,
                           UserDao userDao, ThemeManager themeManager, WeblogManager weblogManager,
                           UserManager userManager, MessageSource messages) {
        this.weblogDao = weblogDao;
        this.weblogTemplateDao = weblogTemplateDao;
        this.userDao = userDao;
        this.themeManager = themeManager;
        this.weblogManager = weblogManager;
        this.userManager = userManager;
        this.messages = messages;
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/switchtheme/{newThemeId}")
    public ResponseEntity switchTheme(@PathVariable String weblogId, @PathVariable String newThemeId, Principal p,
                                      Locale locale) {

        Weblog weblog = weblogDao.findById(weblogId).orElse(null);
        SharedTheme newTheme = themeManager.getSharedTheme(newThemeId);
        User user = userDao.findEnabledByUserName(p.getName());

        if (weblog != null && newTheme != null) {
            if (userManager.checkWeblogRole(user, weblog, WeblogRole.OWNER)) {
                List<Violation> errors = validateTheme(weblog, newTheme, locale);
                if (errors.size() > 0) {
                    return ValidationErrorResponse.badRequest(errors);
                }

                // Remove old template overrides
                List<WeblogTemplate> oldTemplates = weblogTemplateDao.getWeblogTemplateMetadata(weblog);

                for (WeblogTemplate template : oldTemplates) {
                    if (template.getDerivation() == Template.Derivation.OVERRIDDEN) {
                        weblogTemplateDao.deleteById(template.getId());
                    }
                    weblogManager.evictWeblogTemplateCaches(weblog, template.getName(), template.getRole());
                }

                weblog.setTheme(newThemeId);

                log.debug("Saving theme {} for weblog {}", newThemeId, weblog.getHandle());

                // save updated weblog so its cached pages will expire
                weblogManager.saveWeblog(weblog, true);
                weblogTemplateDao.evictWeblogTemplates(weblog);

                return SuccessResponse.textMessage(messages.getMessage("themeEdit.setTheme.success",
                                new Object[] {newTheme.getName()}, locale));
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    private List<Violation> validateTheme(Weblog weblog, SharedTheme newTheme, Locale locale) {
        List<Violation> violations = new ArrayList<>();

        WeblogTheme oldTheme = new WeblogTheme(weblogTemplateDao, weblog,
                themeManager.getSharedTheme(weblog.getTheme()));

        oldTheme.getTemplates().stream().filter(
                old -> old.getDerivation() == Template.Derivation.SPECIFICBLOG).forEach(old -> {
                    if (old.getRole().isSingleton() && newTheme.getTemplateByRole(old.getRole()) != null) {
                        violations.add(new Violation(messages.getMessage("themeEdit.conflicting.singleton.role",
                                new Object[]{old.getRole().getReadableName()}, locale)));
                    } else if (newTheme.getTemplateByName(old.getName()) != null) {
                        violations.add(new Violation(messages.getMessage("themeEdit.conflicting.name",
                                new Object[]{old.getName()}, locale)));
                    }
        });

        return violations;
    }
}