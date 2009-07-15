<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Synchronization Status" otherwise="/login.htm" redirect="/admin/synchronization/synchronizationConfigServer.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:htmlInclude file="/dwr/interface/DWRSynchronizationService.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<%@ include file="localHeader.jsp" %>

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

		function showDiv(id) {
			var div = document.getElementById(id);
			if ( div ) {
				div.style.display = "";
			}
		}

		function hideDiv(id) {
			var div = document.getElementById(id);
			if ( div ) {
				div.style.display = "none";
			}
		}
		
		function toggleChecks(group, prefix) {
			var isOn = document.getElementById(prefix + "_" + group).checked;
			//if ( isOn ) alert("group " + group + " " + prefix + " turned on");
			//else alert("group " + group + " " + prefix + " turned off");
			<c:if test="${not empty syncClassGroups}">
				<c:forEach var="syncClasses" items="${syncClassGroups}" varStatus="status">
					if ( group == "${syncClasses.key}" ) {
						<c:if test="${not empty syncClasses.value}">
							<c:forEach var="syncClass" items="${syncClasses.value}" varStatus="statusClass">
								document.getElementById(prefix + "_" + ${syncClass.syncClass.syncClassId}).checked = isOn;
							</c:forEach>
						</c:if>
					}
				</c:forEach>
			</c:if>
		}

		function cancelChanges() {
			DWRUtil.setValue("nickname", "${server.nickname}");
			DWRUtil.setValue("address", "${server.address}");
			DWRUtil.setValue("username", "${server.username}");
			DWRUtil.setValue("password", "${server.password}");
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

<c:choose>
	<c:when test="${server.serverType == 'CHILD' && not empty server.serverId}">
		<h2><spring:message code="Synchronization.config.child.edit.title"/></h2>
	</c:when>
	<c:when test="${server.serverType == 'PARENT' && not empty server.serverId}">
		<h2><spring:message code="Synchronization.config.parent.edit.title"/></h2>
	</c:when>
	<c:when test="${server.serverType == 'PARENT' && empty server.serverId}">
		<h2><spring:message code="Synchronization.config.parent.add.title"/></h2>
	</c:when>
	<c:otherwise>
		<h2><spring:message code="Synchronization.config.child.add.title"/></h2>
	</c:otherwise>
</c:choose>

<div id="general">
	<form method="post" action="">
		<input type="hidden" name="action" value="save" />
		<input type="hidden" name="type" value="${server.serverType}" />
		<c:if test="${server.serverType == 'CHILD' || type == 'CHILD'}">
			<input type="hidden" name="address" value="" />
		</c:if>
		<c:if test="${not empty server.serverId}">
			<input type="hidden" name="serverId" value="${server.serverId}" />
		</c:if>
	
		<b class="boxHeader"><spring:message code="SynchronizationConfig.server.configure"/></b>
		<div class="box">
			<table>
				<tr>
					<td align="right" valign="top">
						<b><spring:message code="SynchronizationConfig.server.nickname" /></b>
					</td>
					<td align="left" valign="top">
						<input type="text" size="25" maxlength="250" id="nickname" name="nickname" value="${server.nickname}" />
						<br>
						<i><span style="color: #bbbbbb; font-size: 0.9em;"><spring:message code="SynchronizationConfig.server.nickname.hint" /></span></i>
					</td>
				</tr>
				<c:if test="${(server.serverType == 'CHILD' || type == 'CHILD') && not empty server.serverId}">
					<tr>
						<td align="right" valign="top">
							<b><spring:message code="SynchronizationConfig.server.guid" /></b>
						</td>
						<td align="left" valign="top">
							<input type="text" size="48" maxlength="250" id="guid" name="guid" value="${server.guid}" />
						</td>
					</tr>
				</c:if>
				<c:if test="${(server.serverType == 'CHILD' || type == 'CHILD') && empty server.serverId}">
					<tr>
						<td style="border-top: 1px solid #fff;">
						</td>
						<td style="border-top: 1px solid #fff;">
							<spring:message code="SynchronizationConfig.server.option.login" />
						</td>
					</tr>
					<tr>
						<td align="right" valign="top">
							<b><spring:message code="SynchronizationConfig.child.username" /></b>
						</td>
						<td align="left" valign="top">
							<input type="text" size="25" maxlength="250" id="username" name="username" value="" />
						</td>
					</tr>
					<tr>
						<td align="right" valign="top">
							<b><spring:message code="SynchronizationConfig.child.password" /></b>
						</td>
						<td align="left" valign="top">
							<input type="password" size="25" maxlength="250" id="password" name="password" value="" />
						</td>
					</tr>
					<tr>
						<td align="right" valign="top">
							<b><spring:message code="SynchronizationConfig.child.password.retype" /></b>
						</td>
						<td align="left" valign="top">
							<input type="password" size="25" maxlength="250" id="passwordRetype" name="passwordRetype" value="" />
						</td>
					</tr>
					<tr>
						<td align="right" valign="top">
							<b><spring:message code="SynchronizationConfig.server.adminEmail" /></b>
						</td>
						<td align="left" valign="top">
							<input type="checkbox" name="shouldEmail" value="true" checked style="margin-top: 0px; margin-bottom: 0px;" />
							&nbsp;<spring:message code="SynchronizationConfig.server.adminEmail.address" />
							<input type="text" size="25" maxlength="250" id="adminEmail" name="adminEmail" value="" />
							<br />
							<i><span style="color: #bbbbbb; font-size: 0.9em;"><spring:message code="SynchronizationConfig.server.adminEmail.instructions" /></span></i>
						</td>
					</tr>
				</c:if>
				<c:if test="${not (server.serverType == 'CHILD' || type == 'CHILD')}">
					<tr>
						<td align="right" valign="top">
							<b><spring:message code="SynchronizationConfig.server.address" /></b>
						</td>
						<td align="left" valign="top">
							<input type="text" size="70" maxlength="250" id="address" name="address" value="${server.address}" />
							<br>
							<i><span style="color: #bbbbbb;"><spring:message code="SynchronizationConfig.parent.address.hint" /></span></i>
						</td>
					</tr>
					<tr>
						<td align="right" valign="top">
							<b><spring:message code="SynchronizationConfig.parent.username" /></b>
						</td>
						<td align="left" valign="top">
							<input type="text" size="25" maxlength="250" id="username" name="username" value="${server.username}" />
						</td>
					</tr>
					<tr>
						<td align="right" valign="top">
							<b><spring:message code="SynchronizationConfig.parent.password" /></b>
						</td>
						<td align="left" valign="top">
							<input type="password" size="25" maxlength="250" id="password" name="password" value="${server.password}" />
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
											<c:if test="${serverSchedule.started}">checked</c:if> onClick="showHideDiv('scheduleInfo');" />
									</td>
									<td>
										<div id="scheduleInfo" style="margin-bottom: 0px; <c:if test="${empty serverSchedule || serverSchedule.started == false}">display:none;</c:if>">
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
				</c:if>
				<tr>
					<td></td>
					<td>
						<input type="submit" value="<spring:message code="general.save" />" />
						<input type="button" onClick="history.back();" value="<spring:message code="SynchronizationConfig.parent.cancel" />" />					
					</td>
				</tr>
			</table>
		</div>		
	
		<br>
		&nbsp;&nbsp;<a href="javascript://" onclick="showHideDiv('details');"><spring:message code="general.showHideMoreOptions" /></a>
		<br>
		<br>
	
		<div id="details" style="display:none;">
	
			<b class="boxHeader"><spring:message code="SynchronizationConfig.advanced.objects"/></b>
			<div class="box">
				<input type="hidden" name="action" value="saveClasses" />
				<table>
					<tr>
						<td style="padding-right: 80px;" valign="top">
							<table id="syncChangesTable" cellpadding="4" cellspacing="0">
								<thead>
									<tr>
										<th colspan="2" valign="bottom"><spring:message code="SynchronizationConfig.class.item" /></th>
										<th colspan="2" align="center"></th>
									</tr>
								</thead>
								<tbody id="globalPropsList">
									<c:if test="${not empty syncClassGroupsLeft}">
										<c:forEach var="syncClasses" items="${syncClassGroupsLeft}" varStatus="status">
											<tr>
												<td style="border-top: 1px solid #aaa; background-color: whitesmoke;" colspan="2" align="left">
													<b>${syncClasses.key}</b>
												</td>
												<td style="padding-right: 20px; border-top: 1px solid #aaa; background-color: whitesmoke;" align="center">
													<input onclick="toggleChecks('${syncClasses.key}', 'to');" id="to_${syncClasses.key}" style="margin-top: 0px; margin-bottom: 0px;" type="checkbox" name="groupToDefault" value="true" <c:if test="${syncClassGroupTo[syncClasses.key]}">checked</c:if>
														 <c:if test="${syncClasses.key == 'REQUIRED' && 1 == 0}">disabled</c:if>
													><span style="font-size: 0.9em;<c:if test="${syncClasses.key == 'REQUIRED' && 1 == 0}"> color: #aaa;</c:if>"><b><spring:message code="SynchronizationConfig.class.defaultTo" /></b></span>
												</td>
												<td style="border-top: 1px solid #aaa; background-color: whitesmoke;" align="center">
													<input onclick="toggleChecks('${syncClasses.key}', 'from');" id="from_${syncClasses.key}" style="margin-top: 0px; margin-bottom: 0px; margin-right: 1px;" type="checkbox" name="groupFromDefault" value="true" <c:if test="${syncClassGroupFrom[syncClasses.key]}">checked</c:if>
														 <c:if test="${syncClasses.key == 'REQUIRED' && 1 == 0}">disabled</c:if>
													><span style="font-size: 0.9em;<c:if test="${syncClasses.key == 'REQUIRED' && 1 == 0}"> color: #aaa;</c:if>"><b><spring:message code="SynchronizationConfig.class.defaultFrom" /></b></span>
												</td>
											</tr>
											<c:if test="${not empty syncClasses.value}">
												<c:forEach var="syncClass" items="${syncClasses.value}" varStatus="statusClass">
													<tr>
														<td>&nbsp;</td>
														<td align="left">
															${syncClass.syncClass.name}
														</td>
														<td align="center" style="padding-right: 20px;">
															<input id="to_${syncClass.syncClass.syncClassId}" style="margin-top: 0px; margin-bottom: 0px;" type="checkbox" name="toDefault" value="${syncClass.syncClass.syncClassId}" 
																<c:if test="${syncClass.sendTo}">checked</c:if> <c:if test="${syncClasses.key == 'REQUIRED' && 1 == 0}">disabled</c:if>
															><span style="font-size: 0.9em;<c:if test="${syncClasses.key == 'REQUIRED' && 1 == 0}"> color: #aaa;</c:if>"><spring:message code="SynchronizationConfig.class.defaultTo" /></span>
														</td>
														<td align="center">
															<input id="from_${syncClass.syncClass.syncClassId}" style="margin-top: 0px; margin-bottom: 0px;" type="checkbox" name="fromDefault" value="${syncClass.syncClass.syncClassId}" 
																<c:if test="${syncClass.receiveFrom}">checked</c:if> <c:if test="${syncClasses.key == 'REQUIRED' && 1 == 0}">disabled</c:if>
															><span style="font-size: 0.9em;<c:if test="${syncClasses.key == 'REQUIRED' && 1 == 0}"> color: #aaa;</c:if>"><spring:message code="SynchronizationConfig.class.defaultFrom" /></span>
														</td>
													</tr>
												</c:forEach>
											</c:if>
											<c:if test="${empty syncClasses.value}">
												<td colspan="5" align="left">
													<i><spring:message code="SynchronizationConfig.classes.none" /></i>
												</td>
											</c:if>
										</c:forEach>
									</c:if>
									<c:if test="${empty syncClassGroupsLeft}">
										<td colspan="4" align="left">
											<i><spring:message code="SynchronizationConfig.classes.none" /></i>
										</td>
									</c:if>
								</tbody>
							</table>
						</td>
						<td valign="top">
							<table id="syncChangesTable" cellpadding="4" cellspacing="0">
								<thead>
									<tr>
										<th colspan="2" valign="bottom"><spring:message code="SynchronizationConfig.class.item" /></th>
										<th colspan="2" align="center"></th>
									</tr>
								</thead>
								<tbody id="globalPropsList">
									<c:if test="${not empty syncClassGroupsRight}">
										<c:forEach var="syncClasses" items="${syncClassGroupsRight}" varStatus="status">
											<tr>
												<td style="border-top: 1px solid #aaa; background-color: whitesmoke;" colspan="2" align="left">
													<b>${syncClasses.key}</b>
												</td>
												<td style="padding-right: 20px; border-top: 1px solid #aaa; background-color: whitesmoke;" align="center">
													<input onclick="toggleChecks('${syncClasses.key}', 'to');" id="to_${syncClasses.key}" style="margin-top: 0px; margin-bottom: 0px;" type="checkbox" name="groupToDefault" value="true" <c:if test="${syncClassGroupTo[syncClasses.key]}">checked</c:if>
														 <c:if test="${syncClasses.key == 'REQUIRED'}">disabled</c:if>
													><span style="font-size: 0.9em;<c:if test="${syncClasses.key == 'REQUIRED'}"> color: #aaa;</c:if>"><b><spring:message code="SynchronizationConfig.class.defaultTo" /></b></span>
												</td>
												<td style="border-top: 1px solid #aaa; background-color: whitesmoke;" align="center">
													<input onclick="toggleChecks('${syncClasses.key}', 'from');" id="from_${syncClasses.key}" style="margin-top: 0px; margin-bottom: 0px; margin-right: 1px;" type="checkbox" name="groupFromDefault" value="true" <c:if test="${syncClassGroupFrom[syncClasses.key]}">checked</c:if>
														 <c:if test="${syncClasses.key == 'REQUIRED'}">disabled</c:if>
													><span style="font-size: 0.9em;<c:if test="${syncClasses.key == 'REQUIRED'}"> color: #aaa;</c:if>"><b><spring:message code="SynchronizationConfig.class.defaultFrom" /></b></span>
												</td>
											</tr>
											<c:if test="${not empty syncClasses.value}">
												<c:forEach var="syncClass" items="${syncClasses.value}" varStatus="statusClass">
													<tr>
														<td>&nbsp;</td>
														<td align="left">
															${syncClass.syncClass.name}
														</td>
														<td align="center" style="padding-right: 20px;">
															<input id="to_${syncClass.syncClass.syncClassId}" style="margin-top: 0px; margin-bottom: 0px;" type="checkbox" name="toDefault" value="${syncClass.syncClass.syncClassId}" 
																<c:if test="${syncClass.sendTo}">checked</c:if> <c:if test="${syncClasses.key == 'REQUIRED'}">disabled</c:if>
															><span style="font-size: 0.9em;<c:if test="${syncClasses.key == 'REQUIRED'}"> color: #aaa;</c:if>"><spring:message code="SynchronizationConfig.class.defaultTo" /></span>
														</td>
														<td align="center">
															<input id="from_${syncClass.syncClass.syncClassId}" style="margin-top: 0px; margin-bottom: 0px;" type="checkbox" name="fromDefault" value="${syncClass.syncClass.syncClassId}" 
																<c:if test="${syncClass.receiveFrom}">checked</c:if> <c:if test="${syncClasses.key == 'REQUIRED'}">disabled</c:if>
															><span style="font-size: 0.9em;<c:if test="${syncClasses.key == 'REQUIRED'}"> color: #aaa;</c:if>"><spring:message code="SynchronizationConfig.class.defaultFrom" /></span>
														</td>
													</tr>
												</c:forEach>
											</c:if>
											<c:if test="${empty syncClasses.value}">
												<td colspan="5" align="left">
													<i><spring:message code="SynchronizationConfig.classes.none" /></i>
												</td>
											</c:if>
										</c:forEach>
									</c:if>
									<c:if test="${empty syncClassGroupsRight}">
										<td colspan="4" align="left">
											<i><spring:message code="SynchronizationConfig.classes.none" /></i>
										</td>
									</c:if>
								</tbody>
							</table>
						</td>
					</tr>
					<tr>
						<td colspan="2" align="center">
							<input type="submit" value="<spring:message code="general.save" />" />
							<input type="button" onclick="history.back()" value="<spring:message code="general.cancel" />" />
						</td>
					</tr>
				</table>
			</div>
		</div>
	</form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
