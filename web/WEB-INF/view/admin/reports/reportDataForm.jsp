<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<openmrs:require privilege="View Patient Cohorts" otherwise="/login.htm" redirect="/reportData.form" />

<%@ include file="localHeader.jsp" %>

<h2>${reportData.reportSchema.name} </h2>
<i>${reportData.reportSchema.description }</i>

<br/>
<br/>

<b><spring:message code="Report.parameters"/></b>
<small>
	<table>
	    <c:forEach var="parameter" items="${reportData.reportSchema.reportParameters}" varStatus="varStatus">
	    <tr>
	        <td>
	            ${parameter.label}
	        </td>
	        <td>
	            <c:forEach var="ecParam" items="${reportData.evaluationContext.parameterValues}">
	                <c:forEach var="v" items="${ecParam.value}" varStatus="status">
	                    <c:if test="${v.key.name == parameter.name }">
	                        ${v.value}
	                    </c:if>
	                </c:forEach>
	            </c:forEach>
	        </td>
	    </tr>
	    </c:forEach>
	</table>
</small>

<c:if test="${fn:length(otherRenderingModes) > 0}">
	<br/>
	<form method="post">
		<input type="hidden" name="action" value="rerender"/>
		<spring:message code="Report.run.renderAgain"/>
		<select name="renderingMode">
			<c:forEach var="r" items="${otherRenderingModes}">
				<option value="${r.renderer.class.name}!${r.argument}">${r.label}</option>
			</c:forEach>
		</select>
		<input type="submit" value="<spring:message code="Report.renderAgain"/>"/>
	</form>
</c:if>

<br/>
<b class="boxHeader"><spring:message code="Report.cohortReport.data"/></b>
<div class="box">
    <table>
        <tr>
            <th>
                <spring:message code="Report.cohortReport.indicatorName"/>
            </th>
            <th name="optional"></th>
            <th name="optional">
                <spring:message code="Report.cohortReport.indicatorDescription"/>
            </th>
            <th></th>
            <th>
                <spring:message code="Report.cohortReport.indicatorValue"/>
            </th>
        </tr>
        <c:forEach var="dataSet" items="${reportData.dataSets}">
            <c:forEach var="cohortData" items="${dataSet.value.cohortData }" varStatus="varStatus">
                <tr>
                    <th>
                        ${cohortData.key } 
                    </th>
                    <td name="optional" >&nbsp;</td>
                    <td name="optional" >
                        <c:set var="hasDescription" value="false" />
                        <c:forEach var="def" items="${reportData.reportSchema.dataSetDefinitions}">
                            <c:forEach var="description" items="${def.descriptions}" varStatus="dVarStat">
                                <c:if test="${cohortData.key == description.key }">
                                    &nbsp;${description.value } <c:set var="hasDescription" value="true" />
                                </c:if>
                            </c:forEach>
                        </c:forEach>
                        <input type="hidden" id="hasDescription" value="${hasDescription }"/>
                    </td>
                    <td>&nbsp;</td>
                    <td>
                        &nbsp;<a href="reportData.list?indicator=${cohortData.key }">${cohortData.value }</a>
                    </td>
                </tr>
            </c:forEach>
        </c:forEach>
    </table>
</div>
<script type="text/javascript">

    checkForDescriptions( );

    function checkForDescriptions( ) {
        var hasDescription = document.getElementById("hasDescription");
        if (hasDescription.value != "true") {
            hideDescription( );
        }    
    }
    
    function hideDescription( ) {
        var descriptionCol = document.getElementsByName("optional");
        for (var i=0; i<descriptionCol.length; i++) {
            descriptionCol[i].style.display = "none";
        }
    }

</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>