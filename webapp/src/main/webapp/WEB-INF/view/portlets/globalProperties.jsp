<%@ include file="/WEB-INF/template/include.jsp" %>

<c:if test="${model.authenticatedUser != null}">

	<openmrs:htmlInclude file="/dwr/interface/DWRAdministrationService.js" />
	<openmrs:htmlInclude file="/dwr/util.js" />

	<c:if test="${not empty model.title}">
		<h3>${model.title}</h3>
	</c:if>
	<table cellspacing="0" cellpadding="2">
		<c:if test="${model.showHeader}">
			<tr>
				<th><openmrs:message code="general.name" /></th>
				<th><openmrs:message code="general.value" /></th>
				<th></th>
			</tr>
		</c:if>
		<c:forEach var="prop" items="${model.properties}" varStatus="status">
			<c:set var="ind" value="${status.count}"/>
			<tr valign="top" <c:if test="${ind % 2 == 1}">style="background-color: #f0f0f0"</c:if>>
				<td style="width: 20em">
					<c:choose>
						<c:when test="${model.hidePrefix == 'true'}">
							<c:out value="${fn:substring(prop.property, fn:length(model.propertyPrefix), -1)}"/>
						</c:when>
						<c:otherwise>
							${prop.property}
						</c:otherwise>
					</c:choose>
				</td>
				<td>
					<c:set var="readOnly" value="${model.readOnly}"/>
					<c:if test="${not readOnly}">
						<openmrs:hasPrivilege privilege="Manage Global Properties" inverse="true">
							<c:set var="readOnly" value="true"/>
						</openmrs:hasPrivilege>
					</c:if>
					<c:choose>
						<c:when test="${readOnly}">
							<c:out value="${prop.propertyValue}"></c:out>
						</c:when>
						<c:otherwise>	
							<input type="text" size="80" id="gp_${ind}-${model.portletUUID}" onKeyUp="showDiv('gp_${ind}-${model.portletUUID}_actions')"/>		
						</c:otherwise>
					</c:choose>
                </td>
				<td style="width: 10em">
					<span id="gp_${ind}-${model.portletUUID}_actions" style="display: none">
						<input type="button" value="<openmrs:message code="general.save"/>"
							onClick='
								hideDiv("gp_${ind}-${model.portletUUID}_actions");
								showDiv("gp_${ind}-${model.portletUUID}_saving");
								DWRAdministrationService.setGlobalProperty("${prop.property}", dwr.util.getValue("gp_${ind}-${model.portletUUID}"),
									function() { hideDiv("gp_${ind}-${model.portletUUID}_saving") });
							'
						/>
						<input type="button" value="<openmrs:message code="general.cancel"/>"
							onClick='
								hideDiv("gp_${ind}-${model.portletUUID}_actions");
								dwr.util.setValue("gp_${ind}-${model.portletUUID}", "${prop.propertyValue}");
						'/>
					</span>
					<span id="gp_${ind}-${model.portletUUID}_saving" style="display: none; background-color: #e0e0e0">
						<openmrs:message code="general.saving" arguments="..."/>
					</span>
				</td>
			</tr>
			<c:if test="${model.hideDescription != 'true'}">
				<tr <c:if test="${ind % 2 == 1}">style="background-color: #f0f0f0"</c:if>><td colspan="3">
					<small>
						<c:if test="${not empty prop.description}"><i>${prop.description}</i><br/></c:if>
						&nbsp;
					</small>
				<td></tr>
			</c:if>
			<script type="text/javascript">
				var gpVal = "<openmrs:message text="${prop.propertyValue}" javaScriptEscape="true"/>";
				dwr.util.setValue('gp_${ind}-${model.portletUUID}', gpVal);
			</script>
		</c:forEach>
	</table>

</c:if>