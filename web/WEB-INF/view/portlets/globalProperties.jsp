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
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.value" /></th>
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
					<input type="text" size="80" id="gp_${ind}" onKeyUp="showDiv('gp_${ind}_actions')"/>
				</td>
				<td style="width: 10em">
					<span id="gp_${ind}_actions" style="display: none">
						<input type="button" value="<spring:message code="general.save"/>"
							onClick='
								hideDiv("gp_${ind}_actions");
								showDiv("gp_${ind}_saving");
								DWRAdministrationService.setGlobalProperty("${prop.property}", dwr.util.getValue("gp_${ind}"),
									function() { hideDiv("gp_${ind}_saving") });
							'
						/>
						<input type="button" value="<spring:message code="general.cancel"/>"
							onClick='
								hideDiv("gp_${ind}_actions");
								dwr.util.setValue("gp_${ind}", "${prop.propertyValue}");
						'/>
					</span>
					<span id="gp_${ind}_saving" style="display: none; background-color: #e0e0e0">
						<spring:message code="general.saving" arguments="..."/>
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
				var gpVal = "<spring:message text="${prop.propertyValue}" javaScriptEscape="true"/>";
				dwr.util.setValue('gp_${ind}', gpVal);
			</script>
		</c:forEach>
	</table>

</c:if>