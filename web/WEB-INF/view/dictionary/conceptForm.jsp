<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="Edit Dictionary" otherwise="/login.htm"
	redirect="/dictionary/concept.form" />

<h2><spring:message code="Concept.title" /></h2>

<c:if test="${concept.conceptId != null}">
	<a href="concept.form?conceptId=${concept.conceptId - 1}">&laquo; Previous</a> |
	<a href="concept.htm?conceptId=${concept.conceptId}">View</a> |
	<a href="concept.form?conceptId=${concept.conceptId + 1}">Next &raquo;</a>
</c:if>

<br/><br/>

<script>
	function removeItem(nameList, idList)
	{
		var input = document.getElementById(idList);
		var sel = document.getElementById(nameList);
		var optList = sel.options;
		var remaining = new Array();
		var i = optList.selectedIndex;
		if (i >=0 )
			optList[i] = null;
		copyIds(nameList, idList);
	}
	function addMember(nameList, idList)
	{
		window.open(
			'concept_selector?field=set_members',
			'add_to_set',
			'width=600,height=800,scrollbars=yes,resizable=yes,toolbar=no,location=no,directories=no,status=yes,menubar=no,copyhistory=no');
	}
	function addAnswer(nameList, idList)
	{
	
	}
	function moveUp(nameList, idList)
	{
		var input = document.getElementById(idList);
		var sel = document.getElementById(nameList);
		var i = sel.selectedIndex;
		if ( i > 0 ) 
		{
			var optList = sel.options;
			var id   = optList[i].value;
			var name = optList[i].text;
			optList[i].value = optList[i-1].value;
			optList[i].text  = optList[i-1].text;
			optList[i].selected = false;
			optList[i-1].value = id;
			optList[i-1].text  = name;
			optList[i-1].selected = true;
			copyIds(nameList, idList);
		}
	}
	function moveDown(nameList, idList)
	{
		var input = document.getElementById(idList);
		var sel = document.getElementById(nameList);
		var i = sel.selectedIndex;
		var optList = sel.options;
		if ( i >= 0 && i != (optList.length - 1)) 
		{
			var id   = optList[i].value;
			var name = optList[i].text;
			optList[i].value = optList[i+1].value;
			optList[i].text  = optList[i+1].text;
			optList[i].selected = false;
			optList[i+1].value = id;
			optList[i+1].text  = name;
			optList[i+1].selected = true;
			copyIds(nameList, idList);
		}
	}
	function copyIds(nameList, idList)
	{
		var input = document.getElementById(idList);
		var sel = document.getElementById(nameList);
		var optList = sel.options;
		var remaining = new Array();
		var i=0;
		while (i < optList.length)
		{
			remaining.push(optList[i].value);
			i++;
		}
		input.value = remaining.join(' ');
	}
</script>

<style>
	.smallButton {
		border: 1px solid lightgrey;
		background-color: whitesmoke;
		cursor: pointer;
		width: 75px;
		margin: 2px;
	}
</style>

<form method="post" action="">
<table>
	<tr>
		<td><spring:message code="general.name" /></td>
		<td><spring:bind path="conceptName.name">
			<input type="text" name="${status.expression}"
				value="${status.value}" size="45" />
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<tr>
		<td><spring:message code="Concept.shortName" /></td>
		<td><spring:bind path="conceptName.shortName">
			<input type="text" name="${status.expression}"
				value="${status.value}" size="10" />
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description" /></td>
		<td valign="top"><spring:bind path="concept.description">
			<textarea name="${status.expression}" rows="3" cols="60">${status.value}</textarea>
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="Concept.synonyms" /></td>
		<td valign="top"><spring:bind path="concept.synonyms">
			<textarea name="syns" rows="6" cols="25"><c:forEach
				items="${status.value}" var="syn">${syn}
</c:forEach></textarea>
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<tr>
		<td><spring:message code="Concept.conceptClass" /></td>
		<td valign="top"><spring:bind path="concept.conceptClass">
			<select name="${status.expression}">
				<c:forEach items="${classes}" var="cc">
					<option value="${cc.conceptClassId}"
						<c:if test="${cc.conceptClassId == status.value}">selected="selected"</c:if>>${cc.name}</option>
				</c:forEach>
			</select>
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<c:if
		test="${concept.conceptClass != null && concept.conceptClass.set}">
		<tr id="setOptions">
			<td valign="top"><spring:message code="Concept.conceptSets"/></td>
			<td valign="top">
				<input type="text" name="conceptSets" id="conceptSets" size="40" value='<c:forEach items="${concept.conceptSets}" var="set">${set.concept.conceptId} </c:forEach>' />
				<table cellpadding="0" cellspacing="0">
					<tr>
						<td valign="top">
							<select size="6" id="conceptSetsNames">
								<c:forEach items="${concept.conceptSets}" var="set">
									<option value="${set.concept.conceptId}">
												<%=((org.openmrs.ConceptSet) pageContext
													.getAttribute("set")).getConcept().getName(
													request.getLocale()).getName()%>
									</option>
								</c:forEach>
							</select>
						</td>
						<td valign="top" class="buttons">
							<input type="button" value="<spring:message code="general.add"/>" class="smallButton" onClick="addMember('conceptSetsNames', 'conceptSets');" /> <br/>
							<input type="button" value="<spring:message code="general.remove"/>" class="smallButton" onClick="removeItem('conceptSetsNames', 'conceptSets');" /> <br/>
							<input type="button" value="<spring:message code="general.move_up"/>" class="smallButton" onClick="moveUp('conceptSetsNames', 'conceptSets');" /><br/>
							<input type="button" value="<spring:message code="general.move_down"/>" class="smallButton" onClick="moveDown('conceptSetsNames', 'conceptSets');" /><br/>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</c:if>
	<tr>
		<td><spring:message code="Concept.datatype" /></td>
		<td valign="top"><spring:bind path="concept.datatype">
			<select name="${status.expression}" onChange="changeDatatype(this);">
				<c:forEach items="${datatypes}" var="cd">
					<option value="${cd.conceptDatatypeId}"
						<c:if test="${cd.conceptDatatypeId == status.value}">selected="selected"</c:if>>${cd.name}</option>
				</c:forEach>
			</select>
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<c:if test="${concept.datatype != null && concept.datatype.name == 'Coded'}">
		<tr>
			<td valign="top"><spring:message code="Concept.answers"/></td>
			<td>
				<input type="text" name="answers" id="answerIds" size="40" value='<c:forEach items="${concept.answers}" var="answer">${answer.answerConcept.conceptId} </c:forEach>' />
				<table cellspacing="0" cellpadding="0">
					<tr>
						<td valign="top">
							<select size="6" id="answerNames">
								<c:forEach items="${concept.answers}" var="answer">
									<option value="${answer.answerConcept.conceptId}">
										<%= ((org.openmrs.ConceptAnswer) pageContext.getAttribute("answer")).getAnswerConcept().getName(request.getLocale()).getName() %>
									</option>
								</c:forEach>
							</select>
						</td>
						<td valign="top" class="buttons">
							<input type="button" value="<spring:message code="general.add"/>" class="smallButton" onClick="addAnswer('answerNames', 'answerIds');"/><br/>
							<input type="button" value="<spring:message code="general.remove"/>" class="smallButton" onClick="removeItem('answerNames', 'answerIds');"/><br/>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</c:if>
	<c:if test="${concept.numeric}">
		<tr>
			<td valign="top"><spring:message code="ConceptNumeric.name"/></td>
			<td>
				<spring:nestedPath path="concept.conceptNumeric">
					<table border="0">
						<tr>
							<th></th>
							<th><spring:message code="ConceptNumeric.low"/></th>
							<th><spring:message code="ConceptNumeric.high"/></th>
						</tr>
						<tr>
							<th valign="middle"><spring:message code="ConceptNumeric.absolute"/></th>
							<td valign="middle">
								<spring:bind path="lowAbsolute">
									<input type="text" name="${status.expression}" value="${status.value}" size="10" />
									<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
								</spring:bind>
							</td>
							<td valign="middle">
								<spring:bind path="hiAbsolute">
									<input type="text" name="${status.expression}" value="${status.value}" size="10"/>
									<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
								</spring:bind>
							</td>
						</tr>
						<tr>
							<th valign="middle"><spring:message code="ConceptNumeric.critical"/></th>
							<td valign="middle">
								<spring:bind path="lowCritical">
									<input type="text" name="${status.expression}" value="${status.value}" size="10" />
									<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
								</spring:bind>
							</td>
							<td valign="middle">
								<spring:bind path="hiCritical">
									<input type="text" name="${status.expression}" value="${status.value}" size="10"/>
									<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
								</spring:bind>
							</td>
						</tr>
						<tr>
							<th valign="middle"><spring:message code="ConceptNumeric.normal"/></th>
							<td valign="middle">
								<spring:bind path="lowNormal">
									<input type="text" name="${status.expression}" value="${status.value}" size="10" />
									<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
								</spring:bind>
							</td>
							<td valign="middle">
								<spring:bind path="hiNormal">
									<input type="text" name="${status.expression}" value="${status.value}" size="10"/>
									<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
								</spring:bind>
							</td>
						</tr>
						<tr>
							<td></td>
							<td colspan="2"><small><em>(<spring:message code="ConceptNumeric.inclusive"/>)</em></small>
							</td>
						</tr>
						<tr>
							<td><spring:message code="ConceptNumeric.units"/></td>
							<td colspan="2">
								<spring:bind path="units">
									<input type="text" name="${status.expression}" value="${status.value}" size="15"/>
									<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
								</spring:bind>
							</td>
						</tr>
						<tr>
							<td><spring:message code="ConceptNumeric.precise"/></td>
							<td colspan="2">
								<spring:bind path="precise">
									<input type="hidden" name="_${status.expression}" value=""/>
									<input type="checkbox" name="${status.expression}" <c:if test="${status.value}">checked="checked"</c:if>/>
									<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
								</spring:bind>
							</td>
						</tr>
					</table>
				</spring:nestedPath>
			</td>
	</c:if>
	<tr>
		<td><spring:message code="Concept.icd10"/></td>
		<td><spring:bind path="concept.icd10">
			<input type="text" name="${status.expression}"
				value="${status.value}" size="10" />
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<tr>
		<td><spring:message code="Concept.loinc" /></td>
		<td><spring:bind path="concept.loinc">
			<input type="text" name="${status.expression}"
				value="${status.value}" size="10" />
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<tr>
		<td><spring:message code="Concept.version" /></td>
		<td><spring:bind path="concept.version">
			<input type="text" name="${status.expression}"
				value="${status.value}" size="10" />
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<tr>
		<td><spring:message code="general.retired" /></td>
		<td><spring:bind path="concept.retired">
			<input type="hidden" name="_${status.expression}">
			<input type="checkbox" name="${status.expression}" value="true"
				<c:if test="${status.value == true}">checked</c:if> />
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<c:if test="${!(concept.creator == null)}">
		<tr>
			<td><spring:message code="general.creator" /></td>
			<td><spring:bind path="concept.creator">
						${concept.creator.username}
					</spring:bind></td>
		</tr>
		<tr>
			<td><spring:message code="general.dateCreated" /></td>
			<td><spring:bind path="concept.dateCreated">
				<openmrs:formatDate date="${concept.dateCreated}" type="long" />
			</spring:bind></td>
		</tr>
	</c:if>
	<c:if test="${!(concept.changedBy == null)}">
		<tr>
			<td><spring:message code="general.changedBy" /></td>
			<td><spring:bind path="concept.changedBy">
						${concept.changedBy.username}
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="general.dateChanged" /></td>
			<td><spring:bind path="concept.dateChanged">
				<openmrs:formatDate date="${concept.dateChanged}" type="long" />
			</spring:bind></td>
		</tr>
	</c:if>
</table>
<input type="submit" value="<spring:message code="Concept.save"/>" /></form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
