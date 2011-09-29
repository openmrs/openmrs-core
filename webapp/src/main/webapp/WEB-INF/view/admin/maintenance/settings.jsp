<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:require privilege="Manage Global Properties"
	otherwise="/login.htm" redirect="/admin/maintenance/settings.list" />
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<script type="text/javascript">
	var $j = jQuery.noConflict();
	var saveButton;
	var cancelButton;
	$j(document).ready(function() {
		saveButton = $j("#saveButton");
		cancelButton = $j("#cancelButton");
	});

	function enableSaveButton() {
		$j(saveButton).removeAttr("disabled");
		$j(cancelButton).removeAttr("disabled");
	}
</script>

<h2>
	<spring:message code="Settings.title" />
</h2>
<table style="width: 98%;">
	<tr style="vertical-align: top;">
		<td style="width: 20%; padding: 0px;"><c:forEach
				items="${sections}" var="item">
				<c:choose>
					<c:when test="${settingsForm.section eq item}">
						<div class="boxHeader" style="padding: 0px">${item}</div>
					</c:when>
					<c:otherwise>
						<div>
							<a href="?section=${item}">${item}</a>
						</div>
					</c:otherwise>
				</c:choose>
			</c:forEach></td>
		<td class="box"><form:form method="post"
				commandName="settingsForm">
				<form:hidden path="section"/>
				<table>
					<c:forEach items="${settingsForm.settings}" var="item"
						varStatus="status">
						<tr>
							<td style="width: 70%;">${item.name } <br /> <span
								class="description">${item.globalProperty.description }</span>
							</td>
							<td><form:input
									path="settings[${status.index}].globalProperty.propertyValue"
									size="50" maxlength="4000" onkeyup="enableSaveButton();" /></td>
						</tr>
					</c:forEach>
					<tr>
						<td colspan="2"><p>
								<input id="saveButton" type="submit"
									value="<spring:message code="general.save"/>"
									disabled="disabled" /> <input id="cancelButton" type="button"
									value="<spring:message code="general.cancel"/>"
									onclick="window.location=''" disabled="disabled" />
							</p></td>
					</tr>
				</table>
			</form:form></td>
	</tr>
</table>

<%@ include file="/WEB-INF/template/footer.jsp"%>