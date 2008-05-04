<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Synchronization Status" otherwise="/login.htm" redirect="/admin/synchronization/synchronizationStatus.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/dwr/util.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRSynchronizationService.js" />

<h2><spring:message code="Synchronization.status.title"/></h2>

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
	
		function doSubmitFileExport() {
			document.getElementById("fileExportSubmit").disabled = true;
			setTimeout("location.reload();", 5000);
			return true;
		}

		function doSubmitUploadResponse() {
			document.getElementById("uploadResponseSubmit").disabled = true;
			return true;
		}

		/* obsolete
		function doSubmitWebExport() {
			document.getElementById("webExportSubmit").disabled = true;
			return true;
		}
		*/
		
		function getMessage(code) {
			<c:forEach items="${transmissionState}" var="state" >
				if ( code == "${state.key}" ) return '${state.value}';
			</c:forEach>
		
			return code;
		}
		
		function processRecord(record) {
			var state = "<span class='syncFAILED'><b><spring:message code="Synchronization.record.state_FAILED" /></b></span>";
			if ( record.state == "COMMITTED" ) state = "<span class='syncCOMMITTED'><b><spring:message code="Synchronization.record.state_COMMITTED" /></b></span>";
			else if ( record.state !=  "FAILED" ) state = "<span class='syncNEUTRAL'><b>" + getMessage(record.state) + "</b></span>";
			DWRUtil.setValue("state_" + record.guid, state);
			if ( record.state != "COMMITTED" ) {
				DWRUtil.setValue("message_" + record.guid, record.errorMessage);
			}
			
			/*
			var items = record.syncImportItems;
			if ( items && items.length > 0 ) {
				for ( var i = 0; i < items.length; i++ ) {
					var item = items[i];
					DWRUtil.setValue("state_" + item.key, state);
					if ( record.state != "COMMITTED" ) {
						DWRUtil.setValue("message_" + item.key, item.errorMessage);
					}
				}	
			}
			*/
		}
		
		function displaySyncResults(result) {
			//alert("guid is " + result.guid + ", state is " + result.transmissionState + ", em is " + result.errorMessage);
			var success = "<spring:message code="SynchronizationStatus.transmission.ok.allItems" />";
			//success += " &nbsp;<a href=\"javascript://\" onclick=\"showHideDiv('syncDetails');\">details</a>";
			//var details = "<br>";
			//details += "<spring:message code="SynchronizationStatus.transmission.details" />:";
			//details += "<br><br>";
			var records = result.syncImportRecords;
			if ( records && records.length > 0 ) {
				for ( var i = 0; i < records.length; i++ ) {
					var record = records[i];
					processRecord(record);
					//details += record.guid + " - " + record.state + "<br>";
				}
			} else {
				//details += "<spring:message code="SynchronizationStatus.transmission.details.noItems" />:";
			}

			if ( result.transmissionState == "OK" ) {
				DWRUtil.setValue("syncInfo", success);			
			} else {
				// just show error message
				DWRUtil.setValue("syncInfo", getMessage(result.transmissionState));
			}
			document.getElementById("webExportButton").disabled = false;
		}

		function syncToParent() {
			document.getElementById("webExportButton").disabled = true;
			DWRUtil.setValue("syncInfo", "<spring:message code="SynchronizationStatus.export.viaWeb.sending" arguments="${fn:length(synchronizationStatusList)}" />");
			DWRSynchronizationService.syncToParent(displaySyncResults);
		}
		
	-->
</script>

<b class="boxHeader"><spring:message code="SynchronizationStatus.export.changes"/></b>
<div class="box">
	<table cellpadding="4">
		<c:choose>
			<c:when test="${mode == 'SEND_WEB'}">
				<tr>
					<td colspan="4">
						<img src="${pageContext.request.contextPath}/images/lookup.gif" border="0" style="margin-bottom: -3px;">
						<spring:message code="SynchronizationStatus.export.viaWeb" />
					</td>
				</tr>
				<tr>
					<td>
						&nbsp;&nbsp;
					</td>
					<td valign="top">
						<form method="post">
							<input type="button" onClick="syncToParent();" id="webExportButton" value='<spring:message code="SynchronizationStatus.createWebTx"/>'
							<c:if test="${empty parent || syncStatus == 'DISABLED_SYNC_DUE_TO_ERROR' || syncStatus == 'DISABLED_SYNC_AND_HISTORY' || syncStatus == 'DISABLED_SYNC'}">disabled</c:if> />
							<input type="hidden" name="action" value="createWebTx"/>
						</form>
					</td>
					<td></td>
					<td valign="top">
						<c:if test="${empty parent}">
							<span class="error"><i><spring:message code="SynchronizationStatus.export.viaWeb.enable" /></i></span>
						</c:if>
						<c:if test="${syncStatus == 'DISABLED_SYNC_DUE_TO_ERROR' || syncStatus == 'DISABLED_SYNC_AND_HISTORY' || syncStatus == 'DISABLED_SYNC'}">
							<span class="error"><i><spring:message code="SynchronizationStatus.export.viaWeb.enableSync" /></i></span>
						</c:if>
						<span id="syncInfo"></span><br><span id="syncDetails" style="display:none;"></span>
					</td>
				</tr>
			</c:when>
			<c:otherwise>
				<tr>
					<td colspan="4">
						<img src="${pageContext.request.contextPath}/images/save.gif" border="0" style="margin-bottom: -3px;">
						<spring:message code="SynchronizationStatus.export.viaFile" />
					</td>
				</tr>
				<tr>
					<td>
						&nbsp;&nbsp;
					</td>
					<td valign="top">
						<form method="post" onSubmit="return doSubmitFileExport();">
							<input type="submit" id="fileExportSubmit" value='<spring:message code="SynchronizationStatus.createTx"/>'
							<c:if test="${syncStatus == 'DISABLED_SYNC_DUE_TO_ERROR' || syncStatus == 'DISABLED_SYNC_AND_HISTORY' || syncStatus == 'DISABLED_SYNC'}">disabled</c:if> />
							<input type="hidden" name="action" value="createTx"/>
						</form>
					</td>
					<td valign="top">
						|
					</td>
					<td valign="top">
						<form method="post" enctype="multipart/form-data" onSubmit="return doSubmitUploadResponse();">
							<spring:message code="SynchronizationStatus.responsePrompt" />
							<input type="file" name="syncResponseFile" value="" />
							<input type="hidden" name="action" value="uploadResponse" />
							<input type="submit" id="uploadResponseSubmit" value="<spring:message code="SynchronizationStatus.uploadResponse" />" id="submitButton"
							<c:if test="${syncStatus == 'DISABLED_SYNC_DUE_TO_ERROR' || syncStatus == 'DISABLED_SYNC_AND_HISTORY' || syncStatus == 'DISABLED_SYNC'}">disabled</c:if> />
							<br>
							<span style="color: #bbbbbb; position: relative; top: 3px;"><i><spring:message code="SynchronizationStatus.export.viaDisk.instructions" /></i></span>
						</form>
					</td>
				</tr>
			</c:otherwise>
		</c:choose>
	</table>
</div>

<br/>

<b class="boxHeader"><spring:message code="Synchronization.changes.recent"/></b>
<div class="box">
	<table id="syncChangesTable" cellpadding="4" cellspacing="0">
		<thead>
			<tr>
				<th><spring:message code="SynchronizationStatus.itemTypeAndGuid" /></th>
				<th nowrap style="text-align: center;"><spring:message code="SynchronizationStatus.recordState" /></th>
				<th nowrap style="text-align: center;"><spring:message code="SynchronizationStatus.retryCount" /></th>
				<th></th>
			</tr>
		</thead>
		<tbody id="globalPropsList">
			<c:if test="${not empty synchronizationStatusList}">
				<c:set var="bgStyle" value="eee" />
				<c:forEach var="syncRecord" items="${synchronizationStatusList}" varStatus="status">
					<%--<c:forEach var="syncItem" items="${syncRecord.items}" varStatus="itemStatus">--%>
						<tr>
							<td valign="middle" nowrap style="background-color: #${bgStyle};">
								<b>${recordTypes[syncRecord.guid]}</b>
								<c:if test="${not empty recordText[syncRecord.guid]}">
									(${recordText[syncRecord.guid]})
								</c:if>
								<br>
								<span style="color: #bbb">
									<spring:message code="Synchronization.item.state_${recordChangeType[syncRecord.guid]}" /> -
									<openmrs:formatDate date="${syncRecord.timestamp}" format="${syncDateDisplayFormat}" />	
									<%--<c:if test="${not empty itemInfo[syncItem.key.keyValue]}">(${itemInfo[syncItem.key.keyValue]})</c:if></b>--%>
								</span>
							</td>
							<td valign="middle" nowrap style="background-color: #${bgStyle};" align="center" id="state_${syncRecord.guid}">
								<span class="sync${syncRecord.state}"><spring:message code="Synchronization.record.state_${syncRecord.state}" /></span>
							</td>
							<td valign="middle" nowrap style="background-color: #${bgStyle};" align="center">${syncRecord.retryCount}</td>
							<td valign="middle" style="background-color: #${bgStyle};"><span id="message_${syncRecord.guid}"></span></td>

							<%--
							<td valign="middle" nowrap style="background-color: #${bgStyle};">
								<b>${itemTypes[syncItem.key.keyValue]}</b>
								<br>
								(${itemGuids[syncItem.key.keyValue]})
							</td>
							--%>

						</tr>
						<c:choose>
							<c:when test="${bgStyle == 'eee'}"><c:set var="bgStyle" value="fff" /></c:when>
							<c:otherwise><c:set var="bgStyle" value="eee" /></c:otherwise>
						</c:choose>
					<%--</c:forEach>--%>
				</c:forEach>
			</c:if>
			<c:if test="${empty synchronizationStatusList}">
				<td colspan="5" align="left">
					<i><spring:message code="SynchronizationStatus.noItems" /></i>
				</td>
			</c:if>
		</tbody>
	</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
