<%@ page language="java" %>
<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ taglib uri="/struts-bean" prefix="bean" %>
<%@ taglib uri="/struts-html" prefix="html" %>
<%@ taglib uri="/struts-logic" prefix="logic" %>

<openmrs:require privilege="Manage Observations" otherwise="/login.jsp" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br />
<h2>Editing Mime Type</h2>

<html:form action="mimeTypeSubmit.do" method="post">
	<table>
		<tr>
			<td>Mime Type</td>
			<td>
				<html:text property="mimeType" size="35" errorStyleClass="error" />
				<html:errors property="mimeType" />
			</td>
		</tr>
		<tr>
			<td valign="top">Description</td>
			<td valign="top">
				<html:textarea property="description" rows="3" cols="40" styleClass="dummyClass" errorStyleClass="error" />
				<html:errors property="description"/>
			</td>
		</tr>
	</table>
	<html:hidden property="mimeTypeId" /><br />
	<html:submit value="Save Mime Type" />
</html:form>

<%@ include file="/WEB-INF/template/footer.jsp" %>