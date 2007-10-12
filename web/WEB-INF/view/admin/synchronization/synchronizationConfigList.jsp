<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Synchronization Status" otherwise="/login.htm" redirect="/admin/synchronization/synchronizationStatus.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:htmlInclude file="/dwr/interface/DWRSynchronizationService.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Synchronization.config.title"/></h2>

<script language="JavaScript">
	<!--
	
		function showHideDiv(id) {
			var div = document.getElementById(id);
			if ( div ) {
				if ( div.style.display != "none" ) {
					div.style.display = "none";
				} else { 
					div.style.display = "";
				}
			}
		}

		function cancelChanges() {
			DWRUtil.setValue("address", "${parent.address}");
			DWRUtil.setValue("username", "${parent.username}");
			DWRUtil.setValue("password", "${parent.password}");
		}
	
		function getMessage(code) {
			<c:forEach items="${connectionState}" var="state" >
				if ( code == "${state.key}" ) return "${state.value}";
			</c:forEach>
		
			return code;
		}
	
		function showTestResult(result) {

			//alert("state is " + result.connectionState + ", errorMessage is " + result.errorMessage + ", payload is " + result.responsePayload);
		
			var img = '<img src="${pageContext.request.contextPath}/images/error.gif" border="0" style="margin-bottom: -3px;">';
			if ( result.connectionState == "OK" ) img = '<img src="${pageContext.request.contextPath}/images/accept.gif" border="0" style="margin-bottom: -3px;">';
			
			var display = getMessage(result.connectionState) + "&nbsp;" + img;
			DWRUtil.setValue("testInfo", display);
			document.getElementById("testConnectionButton").disabled = false;
		}
	
		function testConnection() {
			document.getElementById("testConnectionButton").disabled = true;
			DWRUtil.setValue("testInfo", "<spring:message code="SynchronizationConfig.server.connection.testing" />");
			var address = DWRUtil.getValue("address");
			var username = DWRUtil.getValue("username");
			var password = DWRUtil.getValue("password");
			DWRSynchronizationService.testConnection(address, username, password, showTestResult);
		}
	
	-->
</script>

<form method="post" action="synchronizationConfig.list">
	<input type="hidden" name="action" value="saveParent" />
	<input type="hidden" name="nickname" value="" />
	<%--
	<b class="boxHeader"><spring:message code="SynchronizationConfig.server.local"/></b>
	<div class="box">
		<table>
			<tr>
				<td align="right" valign="top">
					<b><spring:message code="SynchronizationConfig.server.local.status" /></b>
				</td>
				<td align="left" valign="top">
					<c:choose>
						<c:when test="${localStatus == 'true'}">
							<spring:message code="general.enabled"/>
						</c:when>
						<c:otherwise>
							<spring:message code="general.disabled"/>
						</c:otherwise>
					</c:choose>
				</td>				
			</tr>
			<tr>
				<td align="right" valign="top">
					<b><spring:message code="SynchronizationConfig.server.local.guid" /></b>
				</td>
				<td align="left" valign="top">
					${localServerGuid}
				</td>
			</tr>
		</table>
	</div>		
	--%>
	<b class="boxHeader"><spring:message code="SynchronizationConfig.server.parent"/></b>
	<div class="box">
		<table>
			<tr>
				<td align="right" valign="top">
					<b><spring:message code="SynchronizationConfig.parent.address" /></b>
				</td>
				<td align="left" valign="top">
					<input type="text" size="70" maxlength="250" id="address" name="address" value="${parent.address}" />
					<br>
					<i><span style="color: #bbbbbb;"><spring:message code="SynchronizationConfig.parent.address.hint" /></span></i>
				</td>
			</tr>
			<tr>
				<td align="right" valign="top">
					<b><spring:message code="SynchronizationConfig.parent.username" /></b>
				</td>
				<td align="left" valign="top">
					<input type="text" size="25" maxlength="250" id="username" name="username" value="${parent.username}" />
				</td>
			</tr>
			<tr>
				<td align="right" valign="top">
					<b><spring:message code="SynchronizationConfig.parent.password" /></b>
				</td>
				<td align="left" valign="top">
					<input type="password" size="25" maxlength="250" id="password" name="password" value="${parent.password}" />
					&nbsp;&nbsp;
					<input type="button" id="testConnectionButton" onClick="testConnection();" value="<spring:message code="SynchronizationConfig.parent.test" />" />
					<span id="testInfo"></span>
				</td>
			</tr>
			<tr>
				<td align="right" valign="middle">
					<b><spring:message code="SynchronizationConfig.parent.scheduled" /></b>
				</td>
				<td align="left" valign="top">
					<table cellpadding="0" cellspacing="0" border="0">
						<tr>
							<td>
								<input style="margin-left: 0px;" type="checkbox" id="started" name="started" value="true" 
									<c:if test="${parentSchedule.started}">checked</c:if> onClick="showHideDiv('scheduleInfo');" />
							</td>
							<td>
								<div id="scheduleInfo" style="margin-bottom: 0px; <c:if test="${parentSchedule.started == false}">display:none;</c:if>">
									&nbsp;
									<spring:message code="SynchronizationConfig.parent.scheduled.every" />
									<input type="text" size="3" maxlength="3" id="repeatInterval" name="repeatInterval" value="${repeatInterval}" />
									<spring:message code="SynchronizationConfig.parent.scheduled.minutes" />
								</div>
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td></td>
				<td>
					<input type="submit" value="<spring:message code="SynchronizationConfig.parent.submit" />" />
					<input type="button" onClick="cancelChanges();" value="<spring:message code="SynchronizationConfig.parent.cancel" />" />					
				</td>
			</tr>
		</table>
	</div>		
</form>

<br/>

<c:if test="${1 == 0}">
<b class="boxHeader"><spring:message code="SynchronizationConfig.servers.remote"/></b>
<div class="box">
	<table id="syncChangesTable" cellpadding="4">
		<thead>
			<tr>
				<th><spring:message code="SynchronizationConfig.server.name" /></th>
				<th><spring:message code="SynchronizationConfig.server.type" /></th>
				<th><spring:message code="SynchronizationConfig.server.lastSync" /></th>
			</tr>
		</thead>
		<tbody id="globalPropsList">
			<c:if test="${not empty synchronizationConfigList}">
				<c:forEach var="server" items="${synchronizationConfigList}" varStatus="status">
					<tr>
						<td>
							<c:choose>
								<c:when test="${not empty server.nickname}">
									<b>${server.nickname}</b><br>
									(${server.address})								
								</c:when>
								<c:otherwise>
									<b>${server.address}</b>
								</c:otherwise>
							</c:choose>
						</td>
						<td style="text-align:center;">${server.serverType}</td>
						<td style="text-align:center;"></td>
					</tr>
				</c:forEach>
			</c:if>
			<c:if test="${empty synchronizationStatusList}">
				<td colspan="5" align="left">
					<i><spring:message code="SynchronizationConfig.servers.noItems" /></i>
				</td>
			</c:if>
		</tbody>
	</table>
</div>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>
