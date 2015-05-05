<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Concepts" otherwise="/login.htm" redirect="/admin/concepts/conceptDrug.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.ConceptSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");

	<request:existsParameter name="autoJump">
		var autoJump = <request:parameter name="autoJump"/>;
	</request:existsParameter>

	dojo.addOnLoad( function() {
		
		var cSelection = dojo.widget.manager.getWidgetById("conceptSelection");
		var dfSelection = dojo.widget.manager.getWidgetById("dosageFormSelection");
		var rSelection = dojo.widget.manager.getWidgetById("routeSelection");
		
		dojo.event.topic.subscribe("conceptSearch/select", 
			function(msg) {
				cSelection.displayNode.innerHTML = "<a href='#View Concept' onclick='return gotoConcept(\"concept\")'>" + msg.objs[0].name + "</a>";
				cSelection.hiddenInputNode.value = msg.objs[0].conceptId;
			}
		);
		dojo.event.topic.subscribe("dosageFormSearch/select", 
			function(msg) {
				dfSelection.displayNode.innerHTML = "<a href='#View Concept' onclick='return gotoConcept(\"dosageForm\")'>" + msg.objs[0].name + "</a>";
				dfSelection.hiddenInputNode.value = msg.objs[0].conceptId;
			}
		);
		dojo.event.topic.subscribe("routeSearch/select", 
			function(msg) {
				rSelection.displayNode.innerHTML = "<a href='#View Concept' onclick='return gotoConcept(\"route\")'>" + msg.objs[0].name + "</a>";
				rSelection.hiddenInputNode.value = msg.objs[0].conceptId;
			}
		);
	});

	function gotoConcept(tagName, conceptId) {
		if (conceptId == null)
			conceptId = $(tagName).value;
		window.location = "${pageContext.request.contextPath}/dictionary/concept.form?conceptId=" + conceptId;
		return false;
	}

</script>

<style>
	#table { width: 100%; }
	#table th { text-align: left; }
	#table input[name=name], input#concept_selection { width: 99%; }
</style>

<h2><openmrs:message code="ConceptDrug.manage.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptDrugForm.afterTitle" type="html" parameters="drugId=${drug.drugId}" />

<openmrs:globalProperty var="dosageFormConceptClasses" key="conceptDrug.dosageForm.conceptClasses" defaultValue=""/>
<openmrs:globalProperty var="routeConceptClasses" key="conceptDrug.route.conceptClasses" defaultValue=""/>

<c:if test="${drug.retired}">
<form action="" method="post">
	<div class="retiredMessage">
	<div>
	<openmrs:message code="ConceptDrug.retiredMessage"/>
	<c:out value="${drug.retiredBy.personName}" />
				<openmrs:formatDate date="${drug.dateRetired}" type="medium" />
				-
				${drug.retireReason}
				<input type="submit" value='<openmrs:message code="ConceptDrug.unretireDrug"/>' name="unretireDrug"/>
			
	</div>
	</div>
	</form>
</c:if>

<spring:hasBindErrors name="drug">
	<openmrs:message htmlEscape="false" code="fix.error"/>
	<br />
</spring:hasBindErrors>

<openmrs:globalProperty var="dosageFormConceptClasses" key="conceptDrug.dosageForm.conceptClasses" defaultValue=""/>
<openmrs:globalProperty var="routeConceptClasses" key="conceptDrug.route.conceptClasses" defaultValue=""/>

<form method="post">
<fieldset>
<table cellpadding="3" cellspacing="0" id="table">
	<tr>
		<th><openmrs:message code="general.name"/></th>
		<td>
			<spring:bind path="drug.name">			
				<input type="text" name="${status.expression}" size="40"
					   value="<c:out value="${status.value}" />" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><openmrs:message code="ConceptDrug.concept"/><span class="required">*</span></th>
		<td>
			<spring:bind path="drug.concept">
				<openmrs_tag:conceptField formFieldName="${status.expression}" formFieldId="concept" initialValue="${status.value}" includeClasses="Drug" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>				
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><openmrs:message code="ConceptDrug.combination"/></th>
		<td>
			<spring:bind path="drug.combination">	
				<input type="hidden" name="_${status.expression}" value=""/>		
				<input type="checkbox" name="${status.expression}" 
					   <c:if test="${status.value == true}">checked</c:if> />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><openmrs:message code="ConceptDrug.dosageForm"/></th>
		<td>
			<spring:bind path="drug.dosageForm">
				<openmrs_tag:conceptField formFieldName="${status.expression}" formFieldId="dosageForm" initialValue="${status.value}" includeClasses="${dosageFormConceptClasses}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>				
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><openmrs:message code="ConceptDrug.strength"/></th>
		<td>
			<spring:bind path="drug.strength">
				<input type="text" name="${status.expression}" size="10" 
					   value="<c:out value="${status.value}" />" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><openmrs:message code="ConceptDrug.minimumDailyDose"/></th>
		<td>
			<spring:bind path="drug.minimumDailyDose">			
				<input type="text" name="${status.expression}" size="10" 
					   value="<c:out value="${status.value}" />" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><openmrs:message code="ConceptDrug.maximumDailyDose"/></th>
		<td>
			<spring:bind path="drug.maximumDailyDose">
				<input type="text" name="${status.expression}" size="10" 
					   value="<c:out value="${status.value}" />" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><openmrs:message code="ConceptDrug.route"/></th>
		<td>
			<spring:bind path="drug.route">
				<openmrs_tag:conceptField formFieldName="${status.expression}" formFieldId="route" initialValue="${status.value}" includeClasses="${routeConceptClasses}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>				
			</spring:bind>
		</td>
	</tr>

	<tr>
		
		<th><openmrs:message code="ConceptDrug.ingredients"/></th>
		<td>
			<table cellpadding="3" cellspacing="0">
					<c:forEach var="ingredient" items="${drug.ingredients}">
						<tr><td><openmrs:format concept="${ingredient.ingredient}"/> - ${ingredient.quantity} <openmrs:format concept="${ingredient.units}"/></td></tr>        
					</c:forEach>
			</table>
		</td>
	</tr>

	<c:if test="${drug.creator != null}">
		<tr>
			<th><openmrs:message code="general.createdBy" /></th>
			<td>
				<a href="#View User" onclick="return gotoUser(null, '${drug.creator.userId}')"><c:out value="${drug.creator.personName}" /></a> -
				<openmrs:formatDate date="${drug.dateCreated}" type="medium" />
			</td>
		</tr>
	</c:if>
	<c:if test="${drug.changedBy != null}">
		<tr>
			<th><openmrs:message code="general.changedBy" /></th>
			<td>
				<a href="#View User" onclick="return gotoUser(null, '${drug.changedBy.userId}')"><c:out value="${drug.changedBy.personName}" /></a>
				<openmrs:formatDate date="${drug.dateChanged}" type="medium" />
			</td>
		</tr>
	</c:if>
	 <tr>
        <c:if test="${drug.drugId != null}">
          <th><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/></sub></font></th>
          <td colspan="${fn:length(locales)}"><font color="#D0D0D0"><sub>${drug.uuid}</sub></font></td>
        </c:if>
   </tr>
</table>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptDrugForm.inForm" type="html" parameters="drugId=${drug.drugId}" />

<br />
<input type="hidden" name="phrase" value='<request:parameter name="phrase" />'/>
<input type="submit" value='<openmrs:message code="ConceptDrug.save"/>'>
&nbsp;
<input type="button" value='<openmrs:message code="general.cancel"/>' onclick="history.go(-1); return; document.location='index.htm?autoJump=false&phrase=<request:parameter name="phrase"/>'">
</fieldset>
</form>
<br/>
<br/>
<c:if test="${not drug.retired && not empty drug.drugId}">
	<form action="" method="post">
		<fieldset>
			<h4><openmrs:message code="ConceptDrug.retireDrug"/></h4>
			
			<b><openmrs:message code="general.reason"/></b>
			<input type="text" value="" size="40" name="retireReason" />
			<spring:hasBindErrors name="drug">
				<c:forEach items="${errors.allErrors}" var="error">
					<c:if test="${error.code == 'retireReason'}"><span class="error"><openmrs:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span></c:if>
				</c:forEach>
			</spring:hasBindErrors>
			<br/>
			<input type="submit" value='<openmrs:message code="ConceptDrug.retireDrug"/>' name="retireDrug"/>
		</fieldset>
	</form>
</c:if>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptDrugForm.footer" type="html" parameters="drugId=${drug.drugId}" />

<%@ include file="/WEB-INF/template/footer.jsp" %>