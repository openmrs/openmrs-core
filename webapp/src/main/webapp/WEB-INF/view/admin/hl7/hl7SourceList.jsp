<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View HL7 Source" otherwise="/login.htm" redirect="/admin/hl7/hl7Source.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="Hl7Source.manage.title"/></h2>	
<a href="hl7Source.form"><openmrs:message code="Hl7Source.add"/></a> 
 <openmrs:extensionPoint pointId="org.openmrs.admin.hl7.hl7SourceList.afterAdd" type="html" />

<br />
<br />

<b class="boxHeader"><openmrs:message code="Hl7Source.list.title"/></b>
<div class="box">
	<table cellspacing="0" cellpadding="3">
		<tr>
			<th> <openmrs:message code="general.name" /> </th>
			<th> <openmrs:message code="general.description" /> </th>
		</tr>
		<c:forEach var="hl7Source" items="${hl7SourceList}" varStatus="status">
			<tr class='${status.index % 2 == 0 ? "evenRow" : "oddRow"} ${hl7Source.retired ? "voided" : ""}'>
				<td valign="top">
					<a href="hl7Source.form?hl7SourceId=${hl7Source.id}">
						${hl7Source.name}
					</a>
				</td>
				<td valign="top">${hl7Source.description}</td>
			</tr>
		</c:forEach>
	</table>
	<openmrs:extensionPoint pointId="org.openmrs.admin.hl7Source.hl7SourceList.inbox" type="html" />
</div>

<openmrs:extensionPoint pointId="org.openmrs.admin.hl7Source.hl7SourceList.footer" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>
