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
package org.apache.roller.weblogger.ui.struts2.editor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;

/**
 * A bean for managing comments.
 */
public class CommentsBean {
    
    private String entryId = null;
    private String searchString = null;
    private String startDateString = null;
    private String endDateString = null;
    private String approvedString = "ALL";
    private int page = 0;
    
    private String[] approvedComments = new String[0];
    private String[] spamComments = new String[0];
    private String[] deleteComments = new String[0];
    
    // Limit updates to just this set of comma-separated IDs
    private String ids = null;

    public void loadCheckboxes(List<WeblogEntryComment> comments) {
        
        List<String> allComments = new ArrayList<>();
        List<String> approvedList = new ArrayList<>();
        List<String> spamList = new ArrayList<>();
        
        for (WeblogEntryComment comment : comments) {
            allComments.add(comment.getId());
            
            if (ApprovalStatus.APPROVED.equals(comment.getStatus())) {
                approvedList.add(comment.getId());
            } else if(ApprovalStatus.SPAM.equals(comment.getStatus())) {
                spamList.add(comment.getId());
            }
        }
        
        // list of ids we are working on
        String[] idArray = allComments.toArray(new String[allComments.size()]);
        setIds(StringUtils.join(idArray, ','));
        
        // approved ids list
        setApprovedComments(approvedList.toArray(new String[approvedList.size()]));
        
        // spam ids list
        setSpamComments(spamList.toArray(new String[spamList.size()]));
    }
    
    
    public ApprovalStatus getStatus() {
        switch (approvedString) {
            case "ONLY_APPROVED":
                return ApprovalStatus.APPROVED;
            case "ONLY_DISAPPROVED":
                return ApprovalStatus.DISAPPROVED;
            case "ONLY_PENDING":
                return ApprovalStatus.PENDING;
            case "ONLY_SPAM":
                return ApprovalStatus.SPAM;
            default:
                // show all comments regardless of status
                return null;
        }
    }

    public LocalDate getStartDate() {
        if(!StringUtils.isEmpty(getStartDateString())) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                return LocalDate.parse(getStartDateString(), formatter);
            } catch (Exception ignored) { }
        }
        return null;
    }

    public LocalDate getEndDate() {
        if (!StringUtils.isEmpty(getEndDateString())) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                LocalDate day = LocalDate.parse(getStartDateString(), formatter);
                if (day == null) {
                    day = LocalDate.now();
                }
                return day;
            } catch (Exception ignored) {}
        }
        return null;
    }

    public String getIds() {
        return ids;
    }
    
    public void setIds(String ids) {
        this.ids = ids;
    }
    
    public String getSearchString() {
        return searchString;
    }
    
    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String[] getSpamComments() {
        return spamComments;
    }

    public void setSpamComments(String[] spamComments) {
        this.spamComments = spamComments;
    }

    public String[] getDeleteComments() {
        return deleteComments;
    }

    public void setDeleteComments(String[] deleteComments) {
        this.deleteComments = deleteComments;
    }

    public String getApprovedString() {
        return approvedString;
    }

    public void setApprovedString(String approvedString) {
        this.approvedString = approvedString;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getStartDateString() {
        return startDateString;
    }

    public void setStartDateString(String startDateString) {
        this.startDateString = startDateString;
    }

    public String getEndDateString() {
        return endDateString;
    }

    public void setEndDateString(String endDateString) {
        this.endDateString = endDateString;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String[] getApprovedComments() {
        return approvedComments;
    }

    public void setApprovedComments(String[] approvedComments) {
        this.approvedComments = approvedComments;
    }

}
