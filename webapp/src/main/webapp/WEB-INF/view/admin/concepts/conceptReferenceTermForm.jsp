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
	$j("#delete-dialog").dialog({
		autoOpen: false,
		resizable: false,
		width:'auto',
		height:'auto',
		modal: true
	});
	
	$j("#delete-dialog").addClass("content_align_center");
	
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

<h2><spring:message code="ConceptReferenceTerm.form.title"/></h2>
<br/>

<c:if test="${conceptReferenceTermModel.conceptReferenceTerm.conceptReferenceTermId != null && 
	conceptReferenceTermModel.conceptReferenceTerm.retired}">
<form:form action="unretireConceptReferenceTerm.htm" method="post" modelAttribute="conceptReferenceTermModel">
	<input type="hidden" name="conceptReferenceTermId" value="${param.conceptReferenceTermId}"/>
	<div class="voidedMessage">
		<div>
		<spring:message code="ConceptReferenceTerm.retiredMessage"/> &nbsp;<input type="submit" value='<spring:message code="general.restore"/>'/>
		</div>
	</div>
</form:form>
</c:if>

<spring:hasBindErrors name="conceptReferenceTermModel">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/>- ${error.objectName}<br/>
		</c:forEach>
	</div>
	<br />
</spring:hasBindErrors>

<form:form method="post" action="conceptReferenceTerm.form" modelAttribute="conceptReferenceTermModel">
	<c:if test="${param.conceptReferenceTermId != null}">
		<input type="hidden" name="conceptReferenceTermId" value="${param.conceptReferenceTermId}"/>
	</c:if>
	<fieldset>
	<legend><spring:message code="ConceptReferenceTerm.details" /></legend>
	<br/>
	<table cellpadding="3" cellspacing="3" class="form_align_left">
		<spring:nestedPath path="conceptReferenceTerm">
        <tr>
            <th class="alignRight" valign="top"><spring:message code="ConceptReferenceTerm.code"/><span class="required">*</span></th>
            <td valign="top">
                <spring:bind path="code">
                    <input type="text" name="${status.expression}" value="${status.value}"/>
                    <c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
                </spring:bind>
            </td>
        </tr>
        <tr>
            <th class="alignRight" valign="top"><spring:message code="general.name"/></th>
            <td valign="top">
                <spring:bind path="name">
                    <input type="text" name="${status.expression}" value="${status.value}"/>
                    <c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
                </spring:bind>
            </td>
        </tr>
        <tr>
            <th class="alignRight" valign="top"><spring:message code="ConceptReferenceTerm.source"/><span class="required">*</span></th>
            <td valign="top">
			<spring:bind path="conceptSource">
				<select name="${status.expression}">
					<option value=""></option>
					<openmrs:forEachRecord name="conceptSource">
					<option value="${record.conceptSourceId}" <c:if test="${record.conceptSourceId == status.value}">selected="selected"</c:if>>
						${record.name} (${record.hl7Code})
					</option>
					</openmrs:forEachRecord>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			</td>
        </tr>
        <tr>
       		<th class="alignRight" valign="top"><spring:message code="general.description"/></th>
            <td valign="top">
                <spring:bind path="description">
                	<textarea name="${status.expression}" rows="3" cols="50">${status.value}</textarea>
                    <c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
                </spring:bind>
            </td>
        </tr>
        <tr>
            <th class="alignRight"><spring:message code="general.version"/></th>
            <td>
                <spring:bind path="version">
                    <input type="text" name="${status.expression}" value="${status.value}"/>
                    <c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
                </spring:bind>
            </td>
        </tr>
        </spring:nestedPath>
        <tr>
        	<th class="alignRight" valign="top" style="padding-top: 8px"><spring:message code="ConceptReferenceTerm.relatedTerms"/></th>
        	<td valign="top">
        	<table cellpadding="0" cellspacing="5" align="left">
        		<tr>
        			<th><spring:message code="Concept.mappings.type"/></th>
        			<th><spring:message code="ConceptReferenceTerm.source"/></th>
        			<th><spring:message code="ConceptReferenceTerm.code"/></th>
        			<th><spring:message code="ConceptReferenceTerm.name"/></th>
        			<th>&nbsp;</th>
        		</tr>
        		
        		<c:forEach var="map" items="${conceptReferenceTermModel.termMaps}" varStatus="mapStatus">
				<spring:nestedPath path="termMaps[${mapStatus.index}]">
				<tr>
					<td>
						<spring:bind path="conceptMapType">
							<select name="${status.expression}">
							<openmrs:forEachRecord name="conceptMapType">
								<option value="${record.conceptMapTypeId}" <c:if test="${record.conceptMapTypeId == status.value}">selected="selected"</c:if> >
									${record.name}
								</option>
							</openmrs:forEachRecord>
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
							<option value=""><spring:message code="ConceptReferenceTerm.searchAllSources" /></option>
							<openmrs:forEachRecord name="conceptSource">
							<option value="${record.conceptSourceId}" <c:if test="${record.conceptSourceId == map.termB.conceptSource.conceptSourceId}">selected="selected"</c:if>>
								${record.name}
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
						<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" 
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
						<select name="conceptMapType">
							<openmrs:forEachRecord  name="conceptMapType">
								<option value="${record.conceptMapTypeId}">
									${record.name}
								</option>
							</openmrs:forEachRecord>
						</select>						
					</td>
					<td>
						<select id="map.source" name="term.source" >
							<option value=""><spring:message code="ConceptReferenceTerm.searchAllSources" /></option>
							<openmrs:forEachRecord  name="conceptSource">
							<option value="${record.conceptSourceId}">
								${record.name}
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
						<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" 
							onClick="javascript:this.parentNode.parentNode.parentNode.removeChild(this.parentNode.parentNode)" />
					</td>
					
				</tr>
			</table>
			</td>
		</tr>
		<tr>
			<td></td>
			<td colspan="3">
				<input type="button" value='<spring:message code="Concept.mapping.add"/>' class="smallButton" 
					onClick="addConceptReferenceTermMap(${fn:length(conceptReferenceTermModel.termMaps)})" />
			</td>
		</tr>
		<c:if test="${fn:length(conceptMappingsToThisTerm) > 0}">
		<tr>
        	<th class="alignRight" valign="top"><spring:message code="ConceptReferenceTerm.termsWithMappingsToThis"/></th>
        	<td valign="top">
        		<c:forEach var="map" items="${conceptMappingsToThisTerm}">
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
			<th class="alignRight"><spring:message code="general.createdBy" /></th>
			<td>
				${conceptReferenceTermModel.conceptReferenceTerm.creator.personName} -
				<openmrs:formatDate date="${conceptReferenceTermModel.conceptReferenceTerm.dateCreated}" type="long" />
			</td>
		</tr>
		</c:if>
		<c:if test="${conceptReferenceTermModel.conceptReferenceTerm.changedBy != null}">
		<tr>
			<th class="alignRight"><spring:message code="general.changedBy" /></th>
			<td>
				${conceptReferenceTermModel.conceptReferenceTerm.changedBy.personName} -
				<openmrs:formatDate date="${conceptReferenceTermModel.conceptReferenceTerm.dateChanged}" type="long" />
			</td>
		</tr>
		</c:if>
		<c:if test="${conceptReferenceTermModel.conceptReferenceTerm.retiredBy != null}">
		<tr>
			<th class="alignRight"><spring:message code="general.retiredBy" /></th>
			<td>
				${conceptReferenceTermModel.conceptReferenceTerm.retiredBy.personName} -
				<openmrs:formatDate date="${conceptReferenceTermModel.conceptReferenceTerm.dateRetired}" type="long" />
			</td>
		</tr>
		</c:if>
		<c:if test="${conceptReferenceTermModel.conceptReferenceTerm.retireReason != null}">
		<tr>
			<th class="alignRight"><spring:message code="general.retireReason" /></th>
			<td>
				${conceptReferenceTermModel.conceptReferenceTerm.retireReason}
			</td>
		</tr>
		</c:if>
       	<tr>
			<th></th>
			<td>
				<table cellpadding="0" cellspacing="20" align="left">
        			<tr>
        				<td><input type="submit" name="" value="<spring:message code="general.save"/>"></td>
        				<td><input type="button" value="<spring:message code="general.cancel"/>" onclick="javascript:window.location='conceptReferenceTerms.htm'"></td>
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
			<input type="submit" value='<spring:message code="general.delete"/>' onclick="javascript:$j('#delete-dialog').dialog('open')"/>
			<div id="delete-dialog" title="<spring:message code="general.delete.confirmation"/>">
			<form:form action="retireConceptReferenceTerm.htm" method="post" modelAttribute="conceptReferenceTermModel">
			<input type="hidden" name="conceptReferenceTermId" value="${param.conceptReferenceTermId}"/>
			<br/><br/>
			<table cellpadding="3" cellspacing="3" align="center">
				<tr>
					<th><spring:message code="general.reason"/></th>
					<td>
						<input type="text" name="voidReason" size="40" />
					</td>
				</tr>
				<tr height="20"></tr>
				<tr>
					<td colspan="2" style="text-align: center">
						<input type="submit" value="<spring:message code="general.delete"/>" /> &nbsp; <input type="button" value="<spring:message code="general.cancel"/>" 
						onclick="javascript:$j('#delete-dialog').dialog('close')" /></td>
				</tr>
			</table>
			</form:form>
			</div>
			</c:if>
		</td>
		<td>
			<openmrs:hasPrivilege privilege="Purge Concept Reference Terms">
			<input type="button" value='<spring:message code="general.purge"/>' onclick="javascript:$j('#purge-dialog').dialog('open')" />
			<div id="purge-dialog" title="<spring:message code="general.purge.confirmation"/>">
				<form:form action="purgeConceptReferenceTerm.htm" method="post" modelAttribute="conceptReferenceTermModel">
				<input type="hidden" name="conceptReferenceTermId" value="${param.conceptReferenceTermId}"/>
				<br/><br/>
				<spring:message code="general.confirm.purge"/>
				<br/>
				<table cellpadding="3" cellspacing="30" align="center">
					<tr>
						<td>
							<input type="submit" value='<spring:message code="general.yes"/>' /> &nbsp; <input type="button" value="<spring:message code="general.no"/>" 
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