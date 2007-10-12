<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Synchronization Status" otherwise="/login.htm" redirect="/admin/synchronization/synchronizationImport.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Synchronization.import.title"/></h2>

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
	
		function doSubmit() {
			document.getElementById("submitButton").disabled = true;
			showDiv("infoText");
			return true;
		}
		
	-->
</script>

<b class="boxHeader"><spring:message code="SynchronizationImport.import.from.file"/></b>
<div class="box">
	<table>
		<tr>
			<td>
				<form method="post" enctype="multipart/form-data" onSubmit="return doSubmit();">
				
					<spring:message code="SynchronizationImport.filePrompt" />
					
					<input type="file" name="syncDataFile" value="" />
					<input type="hidden" name="upload" value="true" />
					<input type="submit" value="<spring:message code="SynchronizationImport.importData" />" id="submitButton" />
				
					<span id="infoText" style="display:none;"><spring:message code="SynchronizationImport.generatingResponse" /></span>
				</form>
			</td>
		</tr>
	</table>
</div>

<br>
&nbsp;&nbsp;<a href="javascript://" onclick="showHideDiv('pasteImport');">Import via copy/paste</a>

<div id="pasteImport" style="display:none;">
	<br>
	<br>
	
	<b class="boxHeader"><spring:message code="SynchronizationImport.paste.data"/></b>
	<div class="box">
		<form method="post" action="synchronizationImport.list">
			<table>
				<tr>
					<td align="right" valign="top">
						<b><spring:message code="SynchronizationImport.paste.here" /></b>
					</td>
					<td align="left" valign="top">
						<textarea name="syncData" rows="16" cols="80"></textarea>
					</td>
				</tr>
				<tr>
					<td></td>
					<td>
						<input type="submit" value="<spring:message code="SynchronizationImport.importData" />" />
					</td>
				</tr>
			</table>
		</form>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
