<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage HL7Messages" otherwise="/login.htm"
	redirect="/admin" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>
<openmrs:htmlInclude file="/scripts/hl7_in_archive.js"/>
<openmrs:htmlInclude file="/dwr/interface/DWRHL7Service.js"/>
<openmrs:htmlInclude file="/dwr/util.js"/>

<h2><spring:message code="Hl7InArchive.header" /></h2>

<div style="overflow: xscroll"><b class="boxHeader"><spring:message
	code="Hl7InArchive.migrate.title"/></b>		
	<div class="box" id="archive_migration_archive_dir" style="padding:15px">   				
		<table cellpadding="3" cellspacing="10" border="0" style="width:auto">
			<tr>
				<td>
					<img src="/openmrs/images/info.gif"/>
					<b><spring:message code="Hl7InArchive.migrate.archives.dir"/>: </b>		
				</td>
			</tr>
			<tr>
				<td>${hl7_archives_dir}</td>
			</tr>
		</table>
	</div>
	<br />	
	<c:if test="${isMigrationRequired == true}">
	<div class="box" id="archive_migration_form" style="padding: 15px">
		<input id="startButton" type="button" 
			value="<spring:message code="Hl7InArchive.migrate.start"/>">		
		<input id="stopButton" type="button" 
			style='display:<c:if test="${migration_status != null && migration_status == 'NONE'}">none</c:if>' 	
			value="<spring:message code="Hl7InArchive.migrate.stop"/>">
		
		<div style="padding-top: 15px">			
			<table cellpadding="3" cellspacing="5" border="0" style="width:auto; font-size:small">										
				<tr id="archive_migration_status">
					<td colspan="2"></td>					
				</tr>			
				<tr class="hl7.migrate.label" id="number_span" style="display:none">
					<td><b><spring:message code="Hl7InArchive.migrate.number.transferred.label"/>:</b></td>
					<td><span id="numberMigrated">0</span></td>
				</tr>
				<tr class="hl7.migrate.label" id="status_span" style="display: none">
					<td>
						<b><spring:message code="Hl7InArchive.migrate.status.label"/>:</b>
					</td>
					<td>
						<img id="archive_migration_progress_img" src="/openmrs/images/loading.gif" style="display:none"/> 						
						<span id="msg_running" style="display:none"><spring:message code="Hl7InArchive.migrate.running"/></span>
						<span id="msg_complete_not_all" style="display:none"><spring:message code="Hl7InArchive.migrate.complete.not.all"/></span>
						<span id="msg_complete_all" style="display:none"><spring:message code="Hl7InArchive.migrate.complete.all"/></span>
						<span id="msg_error" style="display:none"><spring:message code="Hl7InArchive.migrate.error"/></span>
						<span id="msg_stopped" style="display:none"><spring:message code="Hl7InArchive.migrate.stopped"/></span>
					</td>
				</tr>
				<tr>
					<td colspan="2" style="padding-top:5px" id="archive_migration_stop_status">						
					</td>					
				</tr>
				<tr id="archive_migration_warning">
					<td colspan="2">						
						<div style="width:600; text-align: justify"><spring:message code="Hl7InArchive.migrate.warning.message"/></div>																		
					</td>									
				</tr>														
			</table>			
		</div>
		<span id="time_out" style="display:none">${time_out}</span>
		<span id="migration_status" style="display:none">${migration_status}</span>
	</div>	
	</c:if>	
</div>
<%@ include file="/WEB-INF/template/footer.jsp"%>