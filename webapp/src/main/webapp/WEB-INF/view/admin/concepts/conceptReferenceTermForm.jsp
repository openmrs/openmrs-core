<%@page import="java.util.Locale" %>
<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Reference Terms" otherwise="/login.htm"
                 redirect="/admin/concepts/conceptReferenceTerm.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRConceptService.js" />
<openmrs:htmlInclude file="/scripts/jquery/autocomplete/OpenmrsAutoComplete.js" />

<script type="text/javascript">

var numberOfClonedElements = 0;
function addConceptReferenceTermMap(initialMapSize){
	var index = initialMapSize+numberOfClonedElements;
	var row = document.getElementById('newTermMapRow');
	var newrow = row.cloneNode(true);
	newrow.style.display = "";		
	newrow.id = 'map[' + index + ']';
	row.parentNode.insertBefore(newrow, row);
	var inputs = newrow.getElementsByTagName("input");
	var selects = newrow.getElementsByTagName("select");
	for (var i in selects) {
		var select = selects[i];
		if (select && select.name == "conceptMapType") {					
			select.name = 'termMaps[' + index + '].conceptMapType';
		}else if (select && select.name == "term.source") {
			select.id = 'term[' + index + '].source';
		}
	}
	
	//find the termB id hidden field and display name input
	for (var x = 0; x < inputs.length; x++) {
		var input = inputs[x];
		if (input && input.name == 'term.name' && input.type == 'text') {
			input.id = 'mappedTerm[' + index + '].name';
		}
		else if (input && input.name == 'termBId' && input.type == 'hidden') {
			input.name = 'termMaps[' + index + '].termB';
			input.id = 'mappedTerm[' + index + '].termB';
		}
		else if (input && input.name == '_termMaps[x].exists' && input.type == 'hidden') {
			input.name = input.name.replace('x', index);
		}
	}
	
	var selectOption = document.getElementById('term[' + index + '].source');
	// set up the autocomplete on the termB input box for the new mapping
	new AutoComplete('mappedTerm[' + index + '].name', new CreateCallback().conceptReferenceTermCallback(selectOption), {
		select: function(event, ui) {
			jquerySelectEscaped('mappedTerm[' + index + '].termB').val(ui.item.object.conceptReferenceTermId);
			//set the concept source in the source dropdown to the concept source for the selected term
			selectOption.value = ui.item.object.conceptSourceId;
		},
		placeholder:omsgs.referencTermSearchPlaceholder
	});
		
	numberOfClonedElements++;
}

<c:if test="${conceptReferenceTermModel.conceptReferenceTerm.conceptReferenceTermId != null}">
$j(document).ready( function() {
	$j("#retire-dialog").dialog({
		autoOpen: false,
		resizable: false,
		width:'auto',
		height:'auto',
		modal: true,
		beforeClose: function(event, ui){
			$j('#retireReason').val('');
		}
	});
	
	$j("#retire-dialog").addClass("content_align_center");
	
	$j("#purge-dialog").dialog({
		autoOpen: false,
		resizable: false,
		width:'auto',
		height:'auto',
		modal: true
	});
	
	$j("#purge-dialog").addClass("content_align_center");
});
</c:if>

</script>

<style type="text/css">
.list-item-no-style{
	list-style: none;
}
</style>

<h2><openmrs:message code="ConceptReferenceTerm.form.title"/></h2>
<br/>

<c:if test="${conceptReferenceTermModel.conceptReferenceTerm.conceptReferenceTermId != null && 
	conceptReferenceTermModel.conceptReferenceTerm.retired}">
<form:form action="unretireConceptReferenceTerm.htm" method="post" modelAttribute="conceptReferenceTermModel">
	<input type="hidden" name="conceptReferenceTermId" value="${param.conceptReferenceTermId}"/>
	<div class="voidedMessage">
		<div>
		<openmrs:message code="ConceptReferenceTerm.retiredMessage"/> &nbsp;<input type="submit" value='<openmrs:message code="general.restore"/>'/>
		</div>
	</div>
</form:form>
</c:if>

<spring:hasBindErrors name="conceptReferenceTermModel">
    <openmrs_tag:errorNotify errors="${errors}" />
</spring:hasBindErrors>

<form:form method="post" action="conceptReferenceTerm.form" modelAttribute="conceptReferenceTermModel">
	<c:if test="${param.conceptReferenceTermId != null}">
		<input type="hidden" name="conceptReferenceTermId" value="${param.conceptReferenceTermId}"/>
	</c:if>
	<fieldset>
	<legend><openmrs:message code="ConceptReferenceTerm.details" /></legend>
	<br/>
	<table cellpadding="3" cellspacing="3" class="form_align_left">
		<spring:nestedPath path="conceptReferenceTerm">
        <tr>
            <th class="alignRight" valign="top"><openmrs:message code="ConceptReferenceTerm.code"/><span class="required">*</span></th>
            <td valign="top">
                <spring:bind path="code">
                    <input type="text" name="${status.expression}" value="<c:out value="${status.value}" />"/>
                    <c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
                </spring:bind>
            </td>
        </tr>
        <tr>
            <th class="alignRight" valign="top"><openmrs:message code="general.name"/></th>
            <td valign="top">
                <spring:bind path="name">
                    <input type="text" name="${status.expression}" value="<c:out value="${status.value}" />"/>
                    <c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
                </spring:bind>
            </td>
        </tr>
        <tr>
            <th class="alignRight" valign="top"><openmrs:message code="ConceptReferenceTerm.source"/><span class="required">*</span></th>
            <td valign="top">
			<spring:bind path="conceptSource">
				<select name="${status.expression}">
					<option value=""></option>
					<openmrs:forEachRecord name="conceptSource">
					<option value="${record.conceptSourceId}" <c:if test="${record.conceptSourceId == status.value}">selected="selected"</c:if>>
						<c:out value="${record.name}" />  <c:if test="${not empty record.hl7Code}">[${record.hl7Code}]</c:if>
					</option>
					</openmrs:forEachRecord>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			</td>
        </tr>
        <tr>
       		<th class="alignRight" valign="top"><openmrs:message code="general.description"/></th>
            <td valign="top">
                <spring:bind path="description">
                	<textarea name="${status.expression}" rows="3" cols="50"><c:out value="${status.value}" /></textarea>
                    <c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
                </spring:bind>
            </td>
        </tr>
        <tr>
            <th class="alignRight"><openmrs:message code="general.version"/></th>
            <td>
                <spring:bind path="version">
                    <input type="text" name="${status.expression}" value="<c:out value="${status.value}" />"/>
                    <c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
                </spring:bind>
            </td>
        </tr>
        </spring:nestedPath>
        <tr>
        	<th class="alignRight" valign="top" style="padding-top: 8px"><openmrs:message code="ConceptReferenceTerm.relatedTerms"/></th>
        	<td valign="top">
        	<table cellpadding="3" cellspacing="1">
        		<tr class="headerRow" align="center">
        			<th><openmrs:message code="Concept.mappings.type"/></th>
        			<th><openmrs:message code="ConceptReferenceTerm.source"/></th>
        			<th><openmrs:message code="ConceptReferenceTerm.code"/></th>
        			<th><openmrs:message code="ConceptReferenceTerm.name"/></th>
        			<th>&nbsp;</th>
        		</tr>
        		
        		<c:forEach var="map" items="${conceptReferenceTermModel.termMaps}" varStatus="mapStatus">
				<spring:nestedPath path="termMaps[${mapStatus.index}]">
				<tr <c:if test="${mapStatus.index % 2 == 0}">class='evenRow'</c:if>>
					<td>
						<spring:bind path="conceptMapType">
							<c:set var="groupOpen" value="false" />
							<select name="${status.expression}">
							<openmrs:forEachRecord name="conceptMapType">
								<c:if test="${record.retired && !groupOpen}">
									<optgroup label="<openmrs:message code="Encounter.type.retired"/>">
									<c:set var="groupOpen" value="true" />
								</c:if>
								<option value="${record.conceptMapTypeId}" <c:if test="${record.conceptMapTypeId == status.value}">selected="selected"</c:if> >
									<c:out value="${record.name}" />
								</option>
							</openmrs:forEachRecord>
							<c:if test="${groupOpen}">
								</optgroup>
							</c:if>
							</select>
						</spring:bind>
					</td>
					<c:choose>
					<c:when test="${map.conceptReferenceTermMapId != null}">
					<td>${map.termB.conceptSource.name}</td>
					<td>${map.termB.code}</td>
					<td>${map.termB.name}</td>
					</c:when>
					<c:otherwise>
					<td>
						<select id="term[${mapStatus.index}].source">
							<option value=""><openmrs:message code="ConceptReferenceTerm.searchAllSources" /></option>
							<openmrs:forEachRecord name="conceptSource">
							<option value="${record.conceptSourceId}" <c:if test="${record.conceptSourceId == map.termB.conceptSource.conceptSourceId}">selected="selected"</c:if>>
								<c:out value="${record.name}" />
							</option>
							</openmrs:forEachRecord>
						</select>
					</td>
					<td>
						<spring:bind path="termB">
						<openmrs_tag:conceptReferenceTermField formFieldName="${status.expression}" formFieldId="${status.expression}" initialValue="${status.value}" />
						</spring:bind>
					</td>
					<td>&nbsp;</td>
					</c:otherwise>
					</c:choose>
					<td>
						<input type="hidden" name="_termMaps[${mapStatus.index}].exists" />
						<input type="button" value='<openmrs:message code="general.remove"/>' class="smallButton" 
							onClick="javascript:this.parentNode.parentNode.parentNode.removeChild(this.parentNode.parentNode)" />
						<spring:bind path="conceptReferenceTermModel.termMaps[${mapStatus.index}]" ignoreNestedPath="true">
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</spring:bind>
					</td>
				</tr>
				</spring:nestedPath>
				</c:forEach>
				
				<%-- The row from which to clone new reference term maps --%>
				<tr id="newTermMapRow" style="display: none">
					<td>
						<c:set var="groupOpen" value="false" />
						<select name="conceptMapType">
							<openmrs:forEachRecord  name="conceptMapType">
							<c:if test="${record.retired && !groupOpen}">
								<optgroup label="<openmrs:message code="Encounter.type.retired"/>">
								<c:set var="groupOpen" value="true" />
							</c:if>
								<option value="${record.conceptMapTypeId}">
                                    <c:out value="${record.name}" />
								</option>
							</openmrs:forEachRecord>
							<c:if test="${groupOpen}">
								</optgroup>
							</c:if>
						</select>
					</td>
					<td>
						<select id="map.source" name="term.source" >
							<option value=""><openmrs:message code="ConceptReferenceTerm.searchAllSources" /></option>
							<openmrs:forEachRecord  name="conceptSource">
							<option value="${record.conceptSourceId}">
                                <c:out value="${record.name}" />
							</option>
							</openmrs:forEachRecord>
						</select>
					</td>
					<td>
						<input type="text" id="term.name" name="term.name" />
						<input type="hidden" id="termBId" name="termBId" />
					</td>
					<td>&nbsp;</td>
					<td>
						<input type="hidden" name="_termMaps[x].exists" />
						<input type="button" value='<openmrs:message code="general.remove"/>' class="smallButton" 
							onClick="javascript:this.parentNode.parentNode.parentNode.removeChild(this.parentNode.parentNode)" />
					</td>
					
				</tr>
			</table>
			</td>
		</tr>
		<tr>
			<td></td>
			<td colspan="3">
				<input type="button" value='<openmrs:message code="Concept.mapping.add"/>' class="smallButton" 
					onClick="addConceptReferenceTermMap(${fn:length(conceptReferenceTermModel.termMaps)})" />
			</td>
		</tr>
		<c:if test="${fn:length(referenceTermMappingsToThisTerm) > 0}">
		<tr>
        	<th class="alignRight" valign="top"><openmrs:message code="ConceptReferenceTerm.termsWithMappingsToThis"/></th>
        	<td valign="top">
        		<c:forEach var="map" items="${referenceTermMappingsToThisTerm}">
        			<li class="list-item-no-style">
        				<a href="<openmrs:contextPath />/admin/concepts/conceptReferenceTerm.form?conceptReferenceTermId=${map.termA.conceptReferenceTermId}">
        					${map.termA.code}
        				</a>
        			</li>
        		</c:forEach>
        	</td>
        </tr>
        </c:if>
		<c:if test="${conceptReferenceTermModel.conceptReferenceTerm.creator != null}">
		<tr>
			<th class="alignRight"><openmrs:message code="general.createdBy" /></th>
			<td>
				<c:out value="${conceptReferenceTermModel.conceptReferenceTerm.creator.personName}" /> -
				<openmrs:formatDate date="${conceptReferenceTermModel.conceptReferenceTerm.dateCreated}" type="long" />
			</td>
		</tr>
		</c:if>
		<c:if test="${conceptReferenceTermModel.conceptReferenceTerm.changedBy != null}">
		<tr>
			<th class="alignRight"><openmrs:message code="general.changedBy" /></th>
			<td>
				<c:out value="${conceptReferenceTermModel.conceptReferenceTerm.changedBy.personName}" /> -
				<openmrs:formatDate date="${conceptReferenceTermModel.conceptReferenceTerm.dateChanged}" type="long" />
			</td>
		</tr>
		</c:if>
		<c:if test="${conceptReferenceTermModel.conceptReferenceTerm.retiredBy != null}">
		<tr>
			<th class="alignRight"><openmrs:message code="general.retiredBy" /></th>
			<td>
				<c:out value="${conceptReferenceTermModel.conceptReferenceTerm.retiredBy.personName}" /> -
				<openmrs:formatDate date="${conceptReferenceTermModel.conceptReferenceTerm.dateRetired}" type="long" />
			</td>
		</tr>
		</c:if>
		<c:if test="${conceptReferenceTermModel.conceptReferenceTerm.retireReason != null}">
		<tr>
			<th class="alignRight"><openmrs:message code="general.retireReason" /></th>
			<td>
				${conceptReferenceTermModel.conceptReferenceTerm.retireReason}
			</td>
		</tr>
		</c:if>
        <c:if test="${conceptReferenceTerm.conceptReferenceTermId != null}">
            <tr>
                <th class="alignRight"><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/></sub></font></th>
                <td colspan="${fn:length(locales)}"><font color="#D0D0D0"><sub>${conceptReferenceTerm.uuid}</sub></font></td>
            </tr>
        </c:if>
       	<tr>
			<th></th>
			<td>
				<table cellpadding="0" cellspacing="20" align="left">
        			<tr>
        				<td><input type="submit" name="" value="<openmrs:message code="general.save"/>"></td>
        				<td><input type="button" value="<openmrs:message code="general.cancel"/>" onclick="javascript:window.location='conceptReferenceTerms.htm'"></td>
        			</tr>
     			</table>
			</td>
		</tr>
    </table>
    </fieldset>
</form:form>

<c:if test="${conceptReferenceTermModel.conceptReferenceTerm.conceptReferenceTermId != null}">
<br/>
<table cellpadding="3" cellspacing="3">
	<tr>
		<td>
			<c:if test="${conceptReferenceTermModel.conceptReferenceTerm.retired == false }">
			<input type="submit" value='<openmrs:message code="general.retire"/>' onclick="javascript:$j('#retire-dialog').dialog('open')"/>
			<div id="retire-dialog" title="<openmrs:message code="general.retire.confirmation"/>">
			<form:form action="retireConceptReferenceTerm.htm" method="post" modelAttribute="conceptReferenceTermModel">
			<input type="hidden" name="conceptReferenceTermId" value="${param.conceptReferenceTermId}"/>
			<br/><br/>
			<table cellpadding="3" cellspacing="3" align="center">
				<tr>
					<th><openmrs:message code="general.reason"/></th>
					<td>
						<input id="retireReason" type="text" name="retireReason" size="40" />
					</td>
				</tr>
				<tr height="20"></tr>
				<tr>
					<td colspan="2" style="text-align: center">
						<input type="submit" value="<openmrs:message code="general.retire"/>" /> &nbsp; <input type="button" value="<openmrs:message code="general.cancel"/>" 
						onclick="javascript:$j('#retire-dialog').dialog('close')" /></td>
				</tr>
			</table>
			</form:form>
			</div>
			</c:if>
		</td>
		<td>
			<openmrs:hasPrivilege privilege="Purge Concept Reference Terms">
			<input type="button" value='<openmrs:message code="general.purge"/>' onclick="javascript:$j('#purge-dialog').dialog('open')" />
			<div id="purge-dialog" title="<openmrs:message code="general.purge.confirmation"/>">
				<form:form action="purgeConceptReferenceTerm.htm" method="post" modelAttribute="conceptReferenceTermModel">
				<input type="hidden" name="conceptReferenceTermId" value="${param.conceptReferenceTermId}"/>
				<br/><br/>
				<openmrs:message code="general.confirm.purge"/>
				<br/>
				<table cellpadding="3" cellspacing="30" align="center">
					<tr>
						<td>
							<input type="submit" value='<openmrs:message code="general.yes"/>' /> &nbsp; <input type="button" value="<openmrs:message code="general.no"/>" 
							onclick="javascript:$j('#purge-dialog').dialog('close')" />
						</td>
					</tr>
				</table>
				</form:form>
			</div>
			</openmrs:hasPrivilege>
		</td>
	</tr>
</table>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>