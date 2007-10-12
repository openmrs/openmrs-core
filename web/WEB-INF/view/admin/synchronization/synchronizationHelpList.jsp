<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Synchronization Status" otherwise="/login.htm" redirect="/admin/synchronization/synchronizationStatus.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/dwr/util.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRSynchronizationService.js" />

<h2><spring:message code="Synchronization.help.title"/></h2>

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
			
	-->
</script>

<b class="boxHeader"><spring:message code="Synchronization.help.heading"/></b>
<div class="box">

<b><spring:message code="Synchronization.help.whatIsSynchronization" /></b>
<p><spring:message code="Synchronization.help.whatIsSynchronizationAnswer" />
<p>
<b><spring:message code="Synchronization.help.whatIsDifferenceSyncAndImportExport" /></b>
<p><spring:message code="Synchronization.help.whatIsDifferenceSyncAndImportExportAnswer" />
<p>
<b><spring:message code="Synchronization.help.howDoIUseSynchronization" /></b>
<p><spring:message code="Synchronization.help.howDoIUseSynchronizationAnswer" />
<p>
<b><spring:message code="Synchronization.help.howDoIConfigureParent" /></b>
<p><spring:message code="Synchronization.help.howDoIConfigureParentAnswer" />
<p>
<b><spring:message code="Synchronization.help.howDoISendToParentViaWeb" /></b>
<p><spring:message code="Synchronization.help.howDoISendToParentViaWebAnswer" />
<p>
<b><spring:message code="Synchronization.help.howDoISendToParentViaDisk" /></b>
<p><spring:message code="Synchronization.help.howDoISendToParentViaDiskAnswer" />
<p>
<b><spring:message code="Synchronization.help.whatDoTheErrorsMean" /></b>
<p><spring:message code="Synchronization.help.whatDoTheErrorsMeanAnswer" />
<p>


</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
