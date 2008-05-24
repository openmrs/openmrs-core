<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="Run Reports" otherwise="/login.htm" redirect="/admin/reports/runReport.form" />

<%@ include file="localHeader.jsp" %>

<div style="width: 60%">
	<h2>
		<spring:message code="Report.run.title"/>:
		${report.schema.name}
	</h2>
	<i>${report.schema.description}</i>
	
	<br/><br/>
	
	<spring:hasBindErrors name="reportFromXml">
		<spring:message code="fix.error"/>
		<div class="error">
			<c:forEach items="${errors.allErrors}" var="error">
				<spring:message code="${error.code}" text="${error.code}"/><br/>
			</c:forEach>
		</div>
		<br />
	</spring:hasBindErrors>
	
	<br/><br/>
	
	<form method="post">
		<b><spring:message code="Report.parameters"/></b>
		
		<spring:nestedPath path="report">
			<table>
				<c:forEach var="p" items="${report.schema.reportParameters}">
	                <tr>
	                    <spring:bind path="userEnteredParams[${p.name}]">
				            <td>
					           <spring:message code="${p.label}"/>:
		                    </td>
		                    <td>
		                    	<openmrs:fieldGen type="${p.clazz.name}" formFieldName="${status.expression}" val="${status.value}"/>
		                        <c:if test="${status.errorMessage != ''}">
		                            <span class="error">${status.errorMessage}</span>
		                        </c:if>
		                    </td>
			            </spring:bind>
	                </tr>
	            </c:forEach>
	            <spring:bind path="userEnteredParams">
			        <c:if test="${status.errorMessage != ''}">
			            <span class="error">${status.errorMessage}</span>
			        </c:if>
	            </spring:bind>
	        </table>
	        
	        <br/><br/>
			
			<b><spring:message code="Report.run.outputFormat"/></b>
			<spring:bind path="selectedRenderer">
	            <select name="${status.expression}">
	                <c:forEach var="r" items="${report.renderingModes}">
	                	<c:set var="thisVal" value="${r.renderer.class.name}!${r.argument}"/>
	                    <option
	                        <c:if test="${status.value == thisVal}"> selected</c:if>
	                        value="${thisVal}">
	                            ${r.label}
	                    </option>
	                </c:forEach>
	            </select>
	        </spring:bind>
		</spring:nestedPath>
		
		<br/>
		<br/>
		<input type="submit" value="<spring:message code="Report.run.button"/>" style="margin-left: 9em"/>
	</form>
</div>
