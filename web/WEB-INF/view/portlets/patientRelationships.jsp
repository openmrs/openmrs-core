<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.User" %>
<%@ page import="org.openmrs.Person" %>
<%@ page import="org.openmrs.Relationship" %>
<%@ page import="org.openmrs.RelationshipType" %>
<%@ page import="org.openmrs.api.context.Context" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%--
showFrom=true:	show relationships where this patient is the *person*
showTo=true:	show relationships where this patient is the *relative*

showTypes=relationshipType1,relationshipType2,...,relationshipTypeN
	show a widget for each type specified, regardless of whether any relationship of that type is defined yet.
	For example:
		--------------------------------------
		| Accompagnateur         | Bob Smith |
		| Primary Care Physician |           |
		--------------------------------------

showOtherTypes=true: show a listing of relationships of types not listed in showTypes
allowEditShownTypes=true: have a [change] button for the shown types (for example to let you have just one Accompagnateur at a time)
allowAddShownTypes=true:  have an [add] button for the shown types
allowAddOtherTypes=true:  have an [add] button that lets you specify type (but not from the ones shown)
allowVoid=true:			  let the user void existing relationship

refreshOnChange=relationshipType1,...relationshipTypeN
refreshOnChange=*
	upon editing the specified relationship type (or any relationship type) reload the page. NOT YET IMPLEMENTED
--%>

<openmrs:htmlInclude file="/scripts/easyAjax.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRRelationshipService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<c:if test="${model.showTypes != null || model.showOtherTypes == 'true'}">

<div id="patientRelationshipPortlet">
	<div class="box">
		<table>
		<c:if test="${model.showTypes != null}">
			<tbody>
			<c:forTokens var="relType" items="${model.showTypes}" delims=",">
				<%
					String relType = (String)pageContext.getAttribute("relType");
					if ( relType != null ) {
						List<RelationshipType> rtList = (List<RelationshipType>)Context.getPatientService().getRelationshipTypes();
						
						if ( rtList != null ) {
							RelationshipType rt = null;

							for ( int i = 0; i < rtList.size(); i++ ) {
								RelationshipType currType = rtList.get(i);
								if ( currType != null ) {
									if (currType.getName().equals(relType)) rt = currType;
								}
							}
							
							if ( rt != null ) {
								Map<String, Object> model = (Map<String, Object>)request.getAttribute("model");
								if ( model != null ) {
									Map<RelationshipType, List<Relationship>> relationshipsByType = (Map<RelationshipType, List<Relationship>>)model.get("patientRelationshipsByType");
									if ( relationshipsByType != null ) {
										List<Relationship> relList = (List)relationshipsByType.get(rt);
										if ( relList != null ) {
											Relationship r = (Relationship)relList.get(0);
											if ( r != null ) {
												Person p = r.getPerson();
												if ( p != null ) {
													User u = p.getUser();
													if ( u != null ) {
														Integer userId = u.getUserId();
														if ( userId != null ) {
															pageContext.setAttribute("currUserId", userId.toString());
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				%>
				<tr valign="bottom">
					<td class="patientSummaryLabel">${relType}:</td>
					<c:choose>
						<c:when test="${model.allowEditShownTypes == 'true'}">
							<td>
								<openmrs_tag:userField formFieldName="boxForRelType_${relType}" roles="${relType}" initialValue="${currUserId}" searchLabel="" searchLabelCode="Relationship.instructions.select.accompagnateur" callback="handleChangeRelationById" /> 
							</td>
						</c:when>
						<c:otherwise>
							<td id="boxForRelType_${relType}" class="patientSummaryValue"></td>
						</c:otherwise>
					</c:choose>
				</tr>
			</c:forTokens>
			</tbody>
		</c:if>
		<c:if test="${model.showOtherTypes == 'true'}">
			<tbody id="PR_otherRelationshipsBody"></tbody>
		</c:if>
		</table>
		<c:if test="${model.showOtherTypes == 'true' && model.allowAddOtherTypes == 'true'}">
			Add relationship:
			<small>(personId or Patient.patientId or User.userId)</small>
			<input type="text" id="PR_otherAddRelFromPerson"/>
			<%--
				<small>User</small><openmrs:fieldGen type="org.openmrs.User" formFieldName="PR_otherAddRelFromUser" val="" />
				<br/>or<br/>
				<small>Patient</small>
				<openmrs:fieldGen type="org.openmrs.Patient" formFieldName="PR_otherAddRelFromPatient" val="" />
				// And change the call to handleAddRelationship to add Patient and User fields
			--%>
			is this patient's <select id="PR_otherAddRelType"></select>
			<input type="button" value="<spring:message code="general.add"/>" onClick="handleAddRelationship('PR_otherAddRelFromPerson', 'PR_otherAddRelType')"/>
		</c:if>
	</div>
</div>
</c:if>

<script type="text/javascript">
	var typesToShow = "${model.showTypes}".split(",");
	var typesToRefresh = "${model.refreshOnChange}".split(",");
	
	<c:if test="${model.showOtherTypes == 'true' && model.allowAddOtherTypes=='true'}">
		DWRRelationshipService.getRelationshipTypes(refreshAddRelationshipBoxes);
	</c:if>
	
	function refreshShownTypes() {
		for (var i = 0; i < typesToShow.length; ++i) {
			var relType = typesToShow[i];
			//document.getElementById('boxForRelType_' + relType).innerHTML = '<spring:message code="general.none"/>';
		}
		
		for (var i = 0; i < typesToShow.length; ++i) {
			var relType = typesToShow[i];

			//alert('calling grtp with [${model.personId}][' + relType + ']');
			DWRRelationshipService.getRelationshipsToPerson(${model.personId}, relType, function(relationships) {

					//alert('this many rels: ' + relationships.length);

					for (var j = 0; j < relationships.length; ++j) {

						var rel = relationships[j];	
						
						var tmp = '';
						//var tmp = $('boxForRelType_' + rel.relationshipType).innerHTML;
						if (j == 0) {
							tmp = '';
						}
						if (tmp != '') { tmp += '<br/>'; }
						tmp += rel.fromName;
						<c:if test="${model.allowVoid}">
							tmp += ' <a href="javascript:handleVoidRelationship(' + rel.relationshipId + ')" class="voidButton">[x]</a>';
						</c:if>

						var userPopup = dojo.widget.manager.getWidgetById("boxForRelType_" + rel.relationshipType + "_selection");
						alert('about to try to set innerHTML for ' + userPopup);
						userPopup.displayNode.innerHTML = rel.toName;
						alert('done trying');

						//$('boxForRelType_' + rel.relationshipType).innerHTML = tmp;
					}
				});
		}
	}
	
	var otherTypesCellFuncs = [
		function(rel) { return rel.relationshipType + ':'; },
		function(rel) { return rel.fromName
							<c:if test="${model.allowVoid == 'true'}">
								+ '<a href="javascript:handleVoidRelationship(' + rel.relationshipId + ')" class="voidButton">[x]</a>'
							</c:if>
						; }
	];

	function refreshOtherTypes() {
		DWRRelationshipService.getRelationshipsToPerson(${model.personId}, null, handleRefreshOtherTypes);
	}
	
	function contains(collection, value) {
		for (var i = 0; i < collection.length; ++i)
			if (collection[i] == value)
				return true;
		return false;
	}
	
	function handleRefreshOtherTypes(rels) {
		var notShownAlready = new Array();
		for (var i = 0; i < rels.length; ++i) {
			if (!contains(typesToShow, rels[i].relationshipType)) {
				notShownAlready.push(rels[i]);
			}
		}
		DWRUtil.removeAllRows('PR_otherRelationshipsBody');
		DWRUtil.addRows('PR_otherRelationshipsBody', notShownAlready, otherTypesCellFuncs,
			{
				cellCreator:function(options) {
				    var td = document.createElement("td");
				    // TODO: why is options not what it's documented to be?
				    if (options.cellNum == 0) {
				    	td.className = "patientSummaryLabel";
				    } else if (options.cellNum == 1) {
				    	td.className = "patientSummaryValue";
				    }
				    return td;
				}
			});
	}
	
	function handleVoidRelationship(relationshipId) {
		DWRRelationshipService.voidRelationship(relationshipId, refreshAllRelationships);
	}
	
	function handleChangeRelationById(relType, fromPersonId) {
		var toPersonId = ${model.personId};
		DWRRelationshipService.setRelationshipTo("User." + fromPersonId, toPersonId, relType, finishedChangeRelation);
	}
	
	function finishedChangeRelation() {
		//alert('done');
		// do nothing - dojo has already taken care of it
	}

	function handleChangeRelation(relType, fromBoxId) {
		var fromPersonId = DWRUtil.getValue($(fromBoxId));
		handleChangeRelationById(relType, fromPersonId);
	}
	
	//function handleAddRelationship(fromBoxId, fromPatientBoxId, fromUserBoxId, relTypeBoxId) {
	function handleAddRelationship(fromBoxId, relTypeBoxId) {
		var fromPersonId = DWRUtil.getValue($(fromBoxId));
		<%--
			var fromPatientId = DWRUtil.getValue($(fromPatientBoxId));
			var fromUserId = DWRUtil.getValue($(fromUserBoxId));
			if (fromPatientId != null && fromUserId != null)
				window.alert("You can't specify both a patient and a user");
		--%>
		var toPersonId = ${model.personId};
		var relTypeId = DWRUtil.getValue($(relTypeBoxId));
		$(fromBoxId).value = '';
		<%--
			DWRUtil.setValue(fromPatientBoxId, '');
			DWRUtil.setValue(fromUserBoxId, '');
			if (fromPatientId != null)
				fromPersonId = 'Patient.' + fromPatientId;
			else if (fromUserId != null)
				fromPersonid = 'User.' + fromUserId;
		--%>
		DWRRelationshipService.createRelationship(fromPersonId, toPersonId, relTypeId, refreshAllRelationships);
	}
	
	function refreshAllRelationships() {
		<c:if test="${model.showTypes != null}">
			refreshShownTypes();
		</c:if>
		<c:if test="${model.showOtherTypes == 'true'}">
			refreshOtherTypes();
		</c:if>
	}
	
	function refreshAddRelationshipBoxes(relTypes) {
		var typesNotAlreadyShown = new Array();
		for (var i = 0; i < relTypes.length; ++i) {
			if (!contains(typesToShow, relTypes[i].name)) {
				typesNotAlreadyShown.push(relTypes[i]);
			}
		}
		DWRUtil.removeAllOptions('PR_otherAddRelType');
		DWRUtil.addOptions('PR_otherAddRelType', ["Select a relationship type..."]);
		DWRUtil.addOptions('PR_otherAddRelType', typesNotAlreadyShown, 'id', 'name');
		if (typesNotAlreadyShown.length == 1) {
			DWRUtil.setValue('PR_otherAddRelType', typesNotAlreadyShown[0].id);
		}
	}
	
	//refreshAllRelationships();

</script>
