<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="" otherwise="/login.htm" redirect="/admin/concepts/proposeConcept.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.ConceptSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
	
	var cSearch;
	var popup;
	
	dojo.addOnLoad( function() {
		cSearch = dojo.widget.manager.getWidgetById("cSearch");
		popup = dojo.widget.manager.getWidgetById("cSelection");
		
		dojo.event.topic.subscribe("cSearch/select", 
			function(msg) {
				if (msg) {
					var concept = msg.objs[0];
					popup.displayNode.innerHTML = concept.name;
					dojo.byId("conceptDescription").innerHTML = concept.description;
					popup.hiddenInputNode.value = concept.conceptId;
				}
			}
		);		
	})

</script>

<h2><spring:message code="ConceptProposal.title"/></h2>

<style>
	th { text-align: left; }
</style>

<form method="post">
<c:if test="${conceptProposal.encounter != null}">
	<table>
		<tr>
			<th valign="top"><spring:message code="ConceptProposal.encounter"/></th>
			<td>
				<spring:bind path="conceptProposal.encounter">
					${status.value.encounterId}
					<a href="${pageContext.request.contextPath}/admin/encounters/encounter.form?encounterId=${status.value.encounterId}"><spring:message code="general.view"/>/<spring:message code="general.edit"/></a>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th valign="top"><spring:message code="ConceptProposal.obsConcept" /></th>
			<td>
				<spring:bind path="conceptProposal.obsConcept">
					<c:choose>
						<c:when test="${conceptProposal.obsConcept != null}">
							<div id="conceptName">${conceptName}</div>
						</c:when>
						<c:otherwise>
							<div dojoType="ConceptSearch" widgetId="cSearch" showVerboseListing="true"></div>
							<div dojoType="OpenmrsPopup" widgetId="cSelection" hiddenInputName="conceptId" searchWidget="cSearch" searchTitle='<spring:message code="general.search"/>'></div>
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</c:otherwise>
					</c:choose>
					<div class="description" style="clear: left;" id="conceptDescription">${conceptName.description}</div>
				</spring:bind>
			</td>
		</tr>
	</table>
</c:if>

<spring:message code="ConceptProposal.proposeWarning"/> <br/>
<spring:message code="ConceptProposal.proposeInfo"/>
<spring:bind path="conceptProposal.originalText">
	<input type="text" name="${status.expression}" id="originalText" value="" size="60" />
	<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
</spring:bind>

<br /><br />
<input type="submit" value="<spring:message code="ConceptProposal.propose"/>">

</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>