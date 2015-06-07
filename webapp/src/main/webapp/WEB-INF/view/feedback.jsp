<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:message var="pageTitle" code="feedback.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp"%>

<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRMessageService.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>

<script type="text/javascript">

 function sendFeedback() {
    clearNote();
	var from = document.getElementById("feedbackFrom").value;
	var subj = document.getElementById("feedbackSubject").value;
	var content = document.getElementById("feedbackContent").value;
	DWRMessageService.sendFeedback(from, subj, content, complete);
 }
 
 var complete = function(success) {
	var note = document.getElementById("feedbackNote");
	note.style.display = "";
	if (success == true)
		note.innerHTML = '<openmrs:message code="feedback.success"/>';
	else
		note.innerHTML = '<openmrs:message code="feedback.error"/>';
 }
 
 function clearNote() {
   var note = document.getElementById("feedbackNote");
   note.style.display = "none";
   note.innerHTML = "";
 }
	
</script>

<style>
#feedbackNote {
	border: 1px dashed lightgrey;
	padding: 2px;
	margin: 4px;
	width: 95%;
	background-color: InfoBackground;
}
</style>

<h2><openmrs:message code="feedback.title" /></h2>

<div id="feedbackNote"></div>

<table>
	<tr>
		<td><openmrs:message code="feedback.email"/></td>
		<td><input type="text" id="feedbackFrom" size="35" value=""></td>
	</tr>
	<tr>
		<td><openmrs:message code="feedback.subject"/></td>
		<td><input type="text" id="feedbackSubject" size="35" value=""></td>
	</tr>
	<tr>
		<td colspan="2"><openmrs:message code="feedback.message"/></td>
	</tr>
	<tr>
		<td colspan="2"><textarea id="feedbackContent" rows="5" cols="53"></textarea></td>
	</tr>
</table>

<br />
<input type="submit" value='<openmrs:message code="feedback.send"/>' onclick="sendFeedback()">

<script type="text/javascript">

document.getElementById("feedbackNote").style.display = "none";

</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>
