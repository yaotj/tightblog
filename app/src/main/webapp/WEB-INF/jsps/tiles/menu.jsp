<%--
  Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  The ASF licenses this file to You
  under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.  For additional information regarding
  copyright in this work, please see the NOTICE file in the top level
  directory of this distribution.
--%>
<%@ include file="/WEB-INF/jsps/tightblog-taglibs.jsp" %>

<c:if test="${menu != null}">
<table class="menuTabTable" cellspacing="0">
<tr>
<c:forEach var="tab" items="${menu.tabs}">
    <c:choose>
        <c:when test="${tab.selected}">
            <c:set var="selectedTab" value="${tab}"/>
            <td class="menuTabSelected">
        </c:when>
        <c:otherwise>
            <td class="menuTabUnselected">
        </c:otherwise>
    </c:choose>
    <div class="menu-tr">
        <c:set var="actionUrl">
            <c:url value='${tab.items.get(0).action}'>
                <c:if test="${weblogId != null}">
                    <c:param name='weblogId' value='${weblogId}'/>
                </c:if>
            </c:url>
        </c:set>
        <div class="menu-tl">
            &nbsp;&nbsp;<a href="${actionUrl}"><fmt:message key="${tab.key}" /></a>&nbsp;&nbsp;
        </div>
    </div>
    </td>
    <td class="menuTabSeparator"></td>
</c:forEach>
</tr>
</table>

<table class="menuItemTable" cellspacing="0" >
    <tr>
        <td class="padleft">
            <c:forEach var="tabItem" items="${selectedTab.items}" varStatus="stat">
                <c:if test="${!stat.first}">|</c:if>
                <c:set var="actionUrl">
                    <c:url value='${tabItem.action}'>
                        <c:if test="${weblogId != null}">
                            <c:param name='weblogId' value='${weblogId}'/>
                        </c:if>
                    </c:url>
                </c:set>
                <c:choose>
                    <c:when test="${tabItem.selected}">
                        <a class="menuItemSelected" href="${actionUrl}"><fmt:message key="${tabItem.key}" /></a>
                    </c:when>
                    <c:otherwise>
                        <a class="menuItemUnselected" href="${actionUrl}"><fmt:message key="${tabItem.key}" /></a>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </td>
    </tr>
</table>

</c:if>
