<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/taskpane/concept.htm" />

<%@ include file="/WEB-INF/template/taskpane/header.jsp" %>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRConceptService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script src='<%= request.getContextPath() %>/scripts/conceptSearch.js'></script>

<script>

	var conceptClasses = new Array();
	<request:parameters id="className" name=">
		conceptClasses.push("${class}");
		<jsp:getProperty/>
	</request:parameters>

	var onSelect = function(conceptList) {
		for (i=0; i<conceptList.length; i++) {
			pickProblem('<%= request.getParameter("mode") %>', '//problem_list', conceptList[i]);
		}
	}
		
</script>



<h1><spring:message code="diagnosis.title"/></h1>

<form method="POST" onSubmit="return searchBoxChange('conceptTableBody', null, phrase);">
	<input name="mode" type="hidden" value='${request.mode}'>
	<input name="phrase" id="phrase" type="text" class="prompt" size="10" onkeyup="searchBoxChange('conceptTableBody', event, this, 400)"/>
	<br />
	<small><em><spring:message code="diagnosis.hint"/></em></small>
</form>

<table border="0">
	<tbody id="conceptTableBody">
	</tbody>
</table>

<br />
<br />
<p class="no_hit">
	If you believe that you have discovered a diagnosis that should be in the
	list, please
	<a href="mailto:amrsprod@iukenya.org&subject=Missing Diagnosis - ${request.phrase}">
		click here
	</a>.
<p>


<script type="text/javascript">
  document.getElementById('phrase').focus();
</script>


<%@ include file="/WEB-INF/template/taskpane/footer.jsp" %>