<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Edit Patient Searches"
	otherwise="/login.htm" redirect="/admin/reports/patientSearch.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<script type="text/javascript">

function activateXMLEditor() {
	var textArea = document.getElementById('xmlStringTextArea');
	var name = document.getElementById('name');
	var description = document.getElementById('description');
	if (textArea.readOnly == true){
		textArea.readOnly = false;
		name.readOnly = true;
		description.readOnly = true;
	}
	else{
		textArea.readOnly = true;
		name.readOnly = false;
		description.readOnly = false;
	}	
  }
  
function hasXMLChanged(){
 	var xml = document.getElementById('XMLHasChanged');
 	xml.value = '1';
 }
  
</script>
<h2><spring:message code="PatientSearch.manage.title" /></h2>
<Br>
<form method="post" class="box">
<h3><spring:message code="PatientSearch.bookkeeping" /></h3>

<table border=0>
	<tr>
		<th valign="top"><spring:message code="general.id" /></th>
		<td valign="top">${patientSearchForm.reportObjectId} <input
			type="hidden" name="patientSearchForm.patientSearchID"
			value="${patientSearchForm.reportObjectId}"></td>
	</tr>
	<tr>
		<th valign="top"><spring:message code="general.name" /></th>
		<td valign="top"><spring:bind
			path="patientSearchForm.name">
			<textarea id="name" name="name" rows="1" cols="40">${status.value}</textarea>
			<input type="hidden" name="hiddenName" value="${status.value}">
		</spring:bind></td>
	</tr>
	<tr>
		<th valign="top"><spring:message code="general.description" /></th>
		<td valign="top"><spring:bind
			path="patientSearchForm.description">
			<textarea id="description" name="description" rows="2" cols="40">${status.value}</textarea>
			<input type="hidden" name="hiddenDesc" value="${status.value}">
		</spring:bind></td>
	</tr>
	<c:if test="${!empty patientSearchForm.patientSearch.filterClass}">
		<tr>
			<th valign="top"><spring:message code="PatientSearch.javaclass" /></th>
			<td valign="top">
				${fn:replace(patientSearchForm.patientSearch.filterClass,"class","")}
			</td>
		</tr>
	</c:if>
</table>
<br>
<c:if test="${!empty patientSearchForm.patientSearch.arguments}">
	<h3><spring:message code="PatientSearch.searchArguments" /></h3>
	<table border=0>
		<tr>
			<td rows="1"><b><spring:message code="general.name" /></b></td>
			<td rows="1"><b><spring:message code="PatientSearch.value" /></b></td>
			<td rows="1"><b><spring:message code="PatientSearch.javaclass" /></b></td>
		</tr>
		<!--  start arguments iteration here -->
		<c:forEach items="${patientSearchForm.patientSearch.arguments}" var="argument" varStatus="varStatus">
			<tr>
				<td valign="top">
					${argument.name}				
				</td>
				<td valign="top">
					<spring:bind path="patientSearchForm.patientSearch.arguments[${varStatus.index}].value">
						<textarea name="value${varStatus.index}" rows="1" cols="20">${status.value}</textarea>
						<input type="hidden" name="hiddenValue${varStatus.index}" value="${status.value}">
					</spring:bind>
				</td>
					<td valign="top">
					${fn:replace(argument.propertyClass,"class ","")}
				</td>
			</tr>
		</c:forEach>
		<input type="hidden" name="argumentsSize" value="${fn:length(patientSearchForm.patientSearch.arguments)}">
		<tr>
			<td colspan="3">Add new search argument:</td>
		</tr>
		<tr>
			<td><input type="text" name="newSearchArgName"></td>
			<td><input type="text" name="newSearchArgValue"></td>
			<td><input type="text" name="newSearchArgClass"></td>
		</tr>
	</table>
</c:if> 

<c:if test="${empty patientSearchForm.patientSearch.arguments}">
	<input type="hidden" name="argumentsSize" value="0">
</c:if> 

<c:if test="${!empty patientSearchForm.patientSearch.parsedComposition}">
	<h3><spring:message code="PatientSearch.composition" /></h3>
	<table>
		<tr>
			<td>&nbsp;</td>
			<td><a href="javascript:activateXMLEditor()"><spring:message code="PatientSearch.advanced" /></a></td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td>
				<textarea readonly="true" id="xmlStringTextArea" name="xmlStringTextArea" rows="${xmlStringSize}" cols="100" onChange="javascript:hasXMLChanged()">${xmlString}</textarea>
			</td>
		</tr>
	</table>
</c:if> <br/>
<input type="hidden" id="XMLHasChanged" name="patientSearchXMLHasChanged" value="0">
<input type="submit" name="action"
	value='<spring:message code="PatientSearch.save"/>'>
	<input type="submit" name="action"
	value='<spring:message code="general.cancel"/>'></form>
<%@ include file="/WEB-INF/template/footer.jsp"%>
