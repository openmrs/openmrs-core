<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Synchronization Status" otherwise="/login.htm" redirect="/admin/synchronization/synchronizationConfig.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:htmlInclude file="/dwr/interface/DWRSynchronizationService.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<%@ include file="localHeader.jsp" %>

<script language="JavaScript">

	//Called to disable content when sync is disabled
	function disableDIVs() {
		hideDiv('advanced');
		hideDiv('serverList');
	}

	function confirmDelete(id) {
		var isConfirmed = confirm("<spring:message code="Synchronization.config.server.confirmDelete" />");
		if ( isConfirmed ) {
			document.getElementById("deleteServer" + id).submit();
		}
	}
</script>

<table>
	<tr>
		<td>
			<h2><spring:message code="Synchronization.config.title"/></h2>
		</td>
		<td>
			&nbsp;&nbsp;
			<a href="javascript://" onclick="hideDiv('advanced');showDiv('general');"><spring:message code="Synchronization.config.menu.general" /></a>

			<!-- display advanced only if sync is enabled -->
			<c:if test="${localServerSyncStatusValue == 'ENABLED_STRICT' || localServerSyncStatusValue == 'ENABLED_CONTINUE_ON_ERROR'}">			
				  | <a href="javascript://" onclick="hideDiv('general');showDiv('advanced');"><spring:message code="Synchronization.config.menu.advanced" /></a>
			</c:if>
		</td>
	</tr>
</table>

<div id="general">

	<b class="boxHeader"><spring:message code="SynchronizationConfig.syncStatus"/></b>
	<div class="box">
		<table id="syncStatus" cellpadding="10" cellspacing="0">
			<%--
			<thead>
				<tr>
					<th style="background-color: #eef3ff; font-weight: bold;"><spring:message code="SynchronizationConfig.property.name"/></th>
					<th style="background-color: #eef3ff; font-weight: bold;"><spring:message code="SynchronizationConfig.property.value"/></th>
					<th style="background-color: #eef3ff; font-weight: bold;"><spring:message code="SynchronizationConfig.property.comments"/></th>
				</tr>
			</thead>
			--%>
			<tbody>
				<tr>
					<%--<td><b><spring:message code="SynchronizationConfig.syncStatus.status" /></b></td>--%>
					<td nowrap><spring:message code="SynchronizationConfig.syncStatus.is" />: ${localServerSyncStatusText}</td>
					<td>${localServerSyncStatusMsg}</td>
				</tr>
				<%--
				<tr>
					<td><b><spring:message code="SynchronizationConfig.syncStatus.guid" /></b></td>
					<td>${localServerGuid}</td>
					<td>${localServerGuidMsg}</td>
				</tr>
				--%>
			</tbody>
		</table>
	</div>
	
	&nbsp;
	<div id="serverList">
		<b class="boxHeader"><spring:message code="SynchronizationConfig.servers.remote"/></b>
		<div class="box">
			<table id="syncChangesTable" cellpadding="10" cellspacing="0">
				<c:if test="${not empty synchronizationConfigList.serverList}">
					<thead>
						<tr>
							<th></th>
							<th></th>
							<th></th>
							<th align="center" colspan="2" style="background-color: #eef3ff; text-align: center; font-weight: normal;"><spring:message code="SynchronizationConfig.server.synchronize.manually" /></th>
							<th align="center" style="text-align: center; background-color: #fee; font-weight: normal;"><spring:message code="SynchronizationConfig.server.synchronize.automatic" /></th>
							<th></th>
						</tr>
						<tr>
							<th><spring:message code="SynchronizationConfig.server.name" /></th>
							<th style="text-align: center;"><spring:message code="SynchronizationConfig.server.type" /></th>
							<th><spring:message code="SynchronizationConfig.server.lastSync" /></th>
							<th style="background-color: #eef; text-align: center;"><img src="${pageContext.request.contextPath}/images/save.gif" border="0" style="margin-bottom: -3px;">
								<spring:message code="SynchronizationConfig.server.syncViaFile" />
							<th style="background-color: #efe; text-align: center;"><img src="${pageContext.request.contextPath}/images/lookup.gif" border="0" style="margin-bottom: -3px;">
								<spring:message code="SynchronizationConfig.server.syncViaWeb" />
							<th style="background-color: #fee; text-align: center;"><img src="${pageContext.request.contextPath}/images/scheduled_send.gif" border="0" style="margin-bottom: -3px;">
								<spring:message code="SynchronizationConfig.server.syncAutomatic" />
								(<spring:message code="general.scheduled" />)
							<th style="text-align: center;"><spring:message code="SynchronizationConfig.server.delete" /></th>
						</tr>
					</thead>
					<tbody id="globalPropsList">
						<c:set var="bgStyle" value="eee" />				
						<c:set var="bgStyleFile" value="dde" />				
						<c:set var="bgStyleWebMan" value="ded" />				
						<c:set var="bgStyleWebAuto" value="edd" />				
						<c:forEach var="server" items="${synchronizationConfigList.serverList}" varStatus="status">
							<tr>
								<td nowrap style="background-color: #${bgStyle};">
									<c:choose>
										<c:when test="${server.serverType == 'CHILD'}">
											<a href="synchronizationConfigServer.form?serverId=${server.serverId}" disabled><b>${server.nickname}</b></a>
										</c:when>
										<c:otherwise>
											<a href="synchronizationConfigServer.form?serverId=${server.serverId}"><b>${server.nickname}</b></a>
											<%--(${server.address})--%>
										</c:otherwise>
									</c:choose>
								</td>
								<td style="background-color: #${bgStyle}; text-align:center;">
									<c:choose>
										<c:when test="${server.serverType == 'PARENT'}">
											<b>${server.serverType}</b>
										</c:when>
										<c:otherwise>
											${server.serverType}
										</c:otherwise>
									</c:choose>
								</td>
								<td style="background-color: #${bgStyle}; text-align:center;">
									<openmrs:formatDate date="${server.lastSync}" format="${syncDateDisplayFormat}" />
								</td>
								<td style="background-color: #${bgStyleFile}; text-align:center;">
									<c:choose>
										<c:when test="${server.serverType == 'CHILD'}">
											<a href="synchronizationImport.list?serverId=${server.serverId}">
												<spring:message code="SynchronizationConfig.server.uploadAndReply" />
											</a>
										</c:when>
										<c:otherwise>
											<a href="synchronizationStatus.list?mode=SEND_FILE">
												<spring:message code="SynchronizationConfig.server.sendFile" />
											</a>
											&nbsp;
											<a href="synchronizationStatus.list?mode=UPLOAD_REPLY">
												<spring:message code="SynchronizationConfig.server.uploadResponse" />
											</a>
										</c:otherwise>
									</c:choose>
								</td>
								<td style="background-color: #${bgStyleWebMan}; text-align:center;">
									<c:choose>
										<c:when test="${server.serverType == 'CHILD'}">
											-
										</c:when>
										<c:otherwise>
											<a href="synchronizationStatus.list?mode=SEND_WEB">
												<spring:message code="SynchronizationConfig.server.synchronizeNow" />
											</a>
										</c:otherwise>
									</c:choose>
								</td>
								<td style="background-color: #${bgStyleWebAuto}; text-align:center;">
									<c:choose>
										<c:when test="${server.serverType == 'CHILD'}">
											-
										</c:when>
										<c:otherwise>
											<c:if test="${parentSchedule.started == false}">
												(<spring:message code="SynchronizationConfig.parent.not.scheduled" />)
												<a href="synchronizationConfigServer.form?serverId=${server.serverId}" style="font-size: 0.9em;">
													<spring:message code="general.configure" />
												</a>
											</c:if>
											<c:if test="${parentSchedule.started == true}">
												<spring:message code="SynchronizationConfig.parent.scheduled.every" />
												<b>${repeatInterval}</b>
												<spring:message code="SynchronizationConfig.parent.scheduled.minutes" />
												<a href="synchronizationConfigServer.form?serverId=${server.serverId}" style="font-size: 0.9em;">
													<spring:message code="general.configure" />
												</a>
											</c:if>
										</c:otherwise>
									</c:choose>
								</td>
								<td style="background-color: #${bgStyle}; text-align:center;">
									<c:choose>
										<c:when test="${server.serverType != 'PARENT'}">
											<form id="deleteServer${server.serverId}" action="synchronizationConfig.list" method="post">
												<input type="hidden" name="action" value="deleteServer" />
												<input type="hidden" id="serverId" name="serverId" value="${server.serverId}" />
												<a href="javascript:confirmDelete('${server.serverId}');"><img src="<%= request.getContextPath() %>/images/trash.gif" alt="delete" border="0" /></a>
											</form>
										</c:when>
										<c:otherwise>
											&nbsp;
										</c:otherwise>
									</c:choose>
								</td>								
							</tr>
							<c:choose>
								<c:when test="${bgStyle == 'eee'}">
									<c:set var="bgStyle" value="fff" />
									<c:set var="bgStyleFile" value="eef" />				
									<c:set var="bgStyleWebMan" value="efe" />				
									<c:set var="bgStyleWebAuto" value="fee" />				
								</c:when>
								<c:otherwise>
									<c:set var="bgStyle" value="eee" />
									<c:set var="bgStyleFile" value="dde" />				
									<c:set var="bgStyleWebMan" value="ded" />				
									<c:set var="bgStyleWebAuto" value="edd" />				
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</c:if>
					<c:if test="${empty synchronizationConfigList.serverList}">
						<td colspan="3" align="left">
							<i><spring:message code="SynchronizationConfig.servers.noItems" /></i>
						</td>
					</c:if>
					<tr>
						<td colspan="3">
							<br>
							<c:if test="${localServerSyncStatusValue == 'ENABLED_STRICT' || localServerSyncStatusValue == 'ENABLED_CONTINUE_ON_ERROR'}">			
								<a href="synchronizationConfigServer.form?type=CHILD"><img src="${pageContext.request.contextPath}/images/add.gif" style="margin-bottom: -3px;" border="0" /></a>
								<a href="synchronizationConfigServer.form?type=CHILD"><spring:message code="SynchronizationConfig.server.config.child" /></a>
								<c:if test="${empty parent}">
									 |
									<a href="synchronizationConfigServer.form?type=PARENT"><img src="${pageContext.request.contextPath}/images/add.gif" style="margin-bottom: -3px;" border="0" /></a>
									<a href="synchronizationConfigServer.form?type=PARENT"><spring:message code="SynchronizationConfig.server.config.parent" /></a>
								</c:if>
							</c:if>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</div>

<div id="advanced" style="display:none;">

	<form action="synchronizationConfig.list" method="post">
	<input type="hidden" name="action" value="saveClasses" />

		<b class="boxHeader"><spring:message code="SynchronizationConfig.advanced.configOptions"/></b>
		<div class="box">
			<table id="syncStatus" cellpadding="8" cellspacing="0">
				<tr>
					<td align="right" nowrap><b><spring:message code="SynchronizationConfig.advanced.serverGuid" /></b></td>
					<td><input type="text" size="50" name="serverGuid" id="serverGuid" value="${localServerGuid}" /></td>
					<td><spring:message code="SynchronizationConfig.advanced.serverGuid.info" /></td>
				</tr>
				<tr>
					<td align="right" nowrap><b><spring:message code="SynchronizationConfig.advanced.serverName" /></b></td>
					<td><input type="text" size="50" name="serverName" id="serverName" value="${localServerName}" /></td>
					<td><spring:message code="SynchronizationConfig.advanced.serverName.info" /></td>
				</tr>
				<tr>
					<td align="right" nowrap><b><spring:message code="SynchronizationConfig.advanced.serverAdminEmail" /></b></td>
					<td><input type="text" size="50" name="serverAdminEmail" id="serverAdminEmail" value="${localServerAdminEmail}" /></td>
					<td><spring:message code="SynchronizationConfig.advanced.serverAdminEmail.info" /></td>
				</tr>
			</table>
		</div>
	
		<br />
	
		<b class="boxHeader"><spring:message code="SynchronizationConfig.advanced.objects"/></b>
		<div class="box">
			<table>
				<tr>
					<td style="padding-right: 80px;" valign="top">
						<table id="syncChangesTable" cellpadding="4" cellspacing="0">
							<thead>
								<tr>
									<th colspan="2" valign="bottom"><spring:message code="SynchronizationConfig.class.item" /></th>
									<th colspan="2" align="center">&nbsp;&nbsp;<spring:message code="general.default.behavior" /></th>
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
														${syncClass.name}
													</td>
													<td align="center" style="padding-right: 20px;">
														<input id="to_${syncClass.syncClassId}" style="margin-top: 0px; margin-bottom: 0px;" type="checkbox" name="toDefault" value="${syncClass.syncClassId}" 
															<c:if test="${syncClass.defaultTo}">checked</c:if> <c:if test="${syncClasses.key == 'REQUIRED'}">disabled</c:if>
														><span style="font-size: 0.9em;<c:if test="${syncClasses.key == 'REQUIRED'}"> color: #aaa;</c:if>"><spring:message code="SynchronizationConfig.class.defaultTo" /></span>
													</td>
													<td align="center">
														<input id="from_${syncClass.syncClassId}" style="margin-top: 0px; margin-bottom: 0px;" type="checkbox" name="fromDefault" value="${syncClass.syncClassId}" 
															<c:if test="${syncClass.defaultFrom}">checked</c:if> <c:if test="${syncClasses.key == 'REQUIRED'}">disabled</c:if>
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
									<th colspan="2" align="center">&nbsp;&nbsp;<spring:message code="general.default.behavior" /></th>
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
														${syncClass.name}
													</td>
													<td align="center" style="padding-right: 20px;">
														<input id="to_${syncClass.syncClassId}" style="margin-top: 0px; margin-bottom: 0px;" type="checkbox" name="toDefault" value="${syncClass.syncClassId}" 
															<c:if test="${syncClass.defaultTo}">checked</c:if> <c:if test="${syncClasses.key == 'REQUIRED'}">disabled</c:if>
														><span style="font-size: 0.9em;<c:if test="${syncClasses.key == 'REQUIRED'}"> color: #aaa;</c:if>"><spring:message code="SynchronizationConfig.class.defaultTo" /></span>
													</td>
													<td align="center">
														<input id="from_${syncClass.syncClassId}" style="margin-top: 0px; margin-bottom: 0px;" type="checkbox" name="fromDefault" value="${syncClass.syncClassId}" 
															<c:if test="${syncClass.defaultFrom}">checked</c:if> <c:if test="${syncClasses.key == 'REQUIRED'}">disabled</c:if>
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
						<input type="button" onclick="location.href='synchronizationConfig.list';" value="<spring:message code="general.cancel" />" />
					</td>
				</tr>
			</table>
		</div>

	</form>
</div>

<!-- turn off content based on value of  localServerSyncStatusValue-->
<c:if test="${localServerSyncStatusValue == 'DISABLED_SYNC_AND_HISTORY'}">			
	<script language="JavaScript">
		disableDIVs();
	</script>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>
