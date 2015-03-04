<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage HL7 Messages" 
	otherwise="/login.htm" redirect="/admin/hl7/hl7InArchiveMigration.htm" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<openmrs:htmlInclude file="/scripts/hl7_in_archive.js"/>
<openmrs:htmlInclude file="/dwr/interface/DWRHL7Service.js"/>
<openmrs:htmlInclude file="/dwr/util.js"/>

<h2><openmrs:message code="Hl7InArchive.header" /></h2>

<b class="boxHeader"><openmrs:message code="Hl7InArchive.migrate.title"/></b>		
<form class="box" id="archive_migration_archive_dir" method="post" action="">
	<div style="padding: 1em;">
		<div id="message" class="ui-widget" style="margin-bottom: 1em;">
			<div class="ui-state-highlight ui-corner-all" style="padding: 0.5em;">
				<span class="ui-icon ui-icon-info"
					style="float: left; margin-right: 0.3em;"></span> 
				<span class="content" style="font-size: 0.9em;"><openmrs:message htmlEscape="false" code="Hl7InArchive.migrate.warning.message"/></span>
			</div>
		</div>
		<p>
			<b><openmrs:message code="Hl7InArchive.migrate.archives.dir"/>:</b> ${hl7_archives_dir}
		</p>
		<span id="daysKeptHighlight" style="margin:-0.5em 0.5em 0 -0.5em; padding:0.5em; whitespace:nowrap;">
			<label for="daysKept"><openmrs:message code="Hl7InArchive.migrate.days.kept"/></label>:
			<input id="daysKept" name="daysKept" type="text" value="365" size="5"/>
			<span id="daysKeptError"><openmrs:message code="Hl7InArchive.migrate.days.kept.error"/></span>
		</span>
		<input id="startButton" type="button" 
			value="<openmrs:message code="Hl7InArchive.migrate.start"/>"/>		
		<input id="stopButton" type="button" 
			style='display:<c:if test="${migration_status != null && migration_status == 'NONE'}">none</c:if>' 	
			value="<openmrs:message code="Hl7InArchive.migrate.stop"/>"/>
		<div></div>
	</div>
</form>

<br />

<div class="box" id="archive_migration_form" style="padding: 15px">
	
	<div style="padding-top: 15px">			
		<table cellpadding="3" cellspacing="5" border="0" style="width:auto; font-size:small">										
			<tr id="archive_migration_status">
				<td colspan="2"></td>					
			</tr>
			<tr class="hl7.migrate.label" id="number_span" style="display:none">
				<td><b><openmrs:message code="Hl7InArchive.migrate.number.transferred.label"/>:</b></td>
				<td><span id="numberMigrated">0</span></td>
			</tr>
			<tr class="hl7.migrate.label" id="status_span" style="display: none">
				<td>
					<b><openmrs:message code="Hl7InArchive.migrate.status.label"/>:</b>
				</td>
				<td>
					<img id="archive_migration_progress_img" src="<openmrs:contextPath/>/images/loading.gif" style="display:none"/> 						
					<span id="msg_running" style="display:none"><openmrs:message code="Hl7InArchive.migrate.running"/></span>
					<span id="msg_complete_not_all" style="display:none"><openmrs:message code="Hl7InArchive.migrate.complete.not.all"/></span>
					<span id="msg_complete_all" style="display:none"><openmrs:message code="Hl7InArchive.migrate.complete.all"/></span>
					<span id="msg_error" style="display:none"><openmrs:message code="Hl7InArchive.migrate.error"/></span>
					<span id="msg_stopped" style="display:none"><openmrs:message code="Hl7InArchive.migrate.stopped"/></span>
				</td>
			</tr>
			<tr>
				<td colspan="2" style="padding-top:5px" id="archive_migration_stop_status">						
				</td>					
			</tr>
		</table>
	</div>

	<span id="time_out" style="display:none">${time_out}</span>
	<span id="migration_status" style="display:none">${migration_status}</span>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>