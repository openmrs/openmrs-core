<table>
	<tr>
		<td valign="top">
			<spring:message code="Field.name" />
		</td>
		<td>
			<spring:bind path="field.name">
				<input type="text" name="${status.expression}" id="${status.expression}" value="${status.value}" size="55" />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">
						${status.errorMessage}
					</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top">
			<spring:message code="general.description" />
		</td>
		<td>
			<spring:bind path="field.description">
				<textarea name="${status.expression}" id="${status.expression}" rows="2" cols="40">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}">
					<span class="error">
						${status.errorMessage}
					</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>
			<spring:message code="Field.type" />
		</td>
		<td>
			<spring:bind path="field.fieldType">
				<select name="fieldTypeId" id="${status.expression}" onchange="chooseFieldType(this.value)">
					<c:forEach items="${fieldTypes}" var="ft">
						<option value="${ft.fieldTypeId}"
							<c:if test="${ft.fieldTypeId == status.value.fieldTypeId}">selected</c:if>>
							${ft.name}
						</option>
					</c:forEach>
				</select>
				<c:if test="${status.errorMessage != ''}">
					<span class="error">
						${status.errorMessage}
					</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr id="concept">
		<td>
			<spring:message code="Field.concept" />
		</td>
		<td>
			<spring:bind path="field.concept">
				<div id="conceptName">
					${conceptName}
				</div>
				<input type="hidden" id="conceptId" value="${status.value.conceptId}" name="conceptId" />
				<input type="button" id="conceptButton" class="smallButton" value='<spring:message code="general.change"/>' onclick="showConceptSearch(this)" />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">
						${status.errorMessage}
					</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr id="database">
		<td valign="top">
			<spring:message code="Field.database" />
		</td>
		<td>
			<table cellpadding="0" cellspacing="0">
				<tr>
					<td>
						<spring:message code="Field.tableName" />
						<br />
						<spring:bind path="field.tableName">
							<input type="input" name="${status.expression}" id="${status.expression}" value="${status.value}"/>
							<c:if test="${status.errorMessage != ''}">
								<span class="error">
									${status.errorMessage}
								</span>
							</c:if>
						</spring:bind>
					</td>
					<td>
						<spring:message code="Field.attributeName" />
						<br />
						<spring:bind path="field.attributeName">
							<input type="input" name="${status.expression}" id="${status.expression}" value="${status.value}"/>
							<c:if test="${status.errorMessage != ''}">
								<span class="error">
									${status.errorMessage}
								</span>
							</c:if>
						</spring:bind>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td valign="top">
			<spring:message code="Field.defaultValue" />
		</td>
		<td>
			<spring:bind path="field.defaultValue">
				<input type="text" name="${status.expression}" id="${status.expression}" value="${status.value}" size="55" />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">
						${status.errorMessage}
					</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td>
			<spring:message code="Field.selectMultiple" />
		</td>
		<td>
			<spring:bind path="field.selectMultiple">
				<input type="checkbox" name="${status.expression}" id="${status.expression}" value="on" 
					<c:if test="${status.value == true}">selected</c:if> />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">
						${status.errorMessage}
					</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${field.creator != null}">
		<tr>
			<td>
				<spring:message code="general.createdBy" />
			</td>
			<td>
				${field.creator.firstName} ${field.creator.lastName} -
				<openmrs:formatDate date="${field.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
	<c:if test="${field.changedBy != null}">
		<tr>
			<td>
				<spring:message code="general.changedBy" />
			</td>
			<td>
				${field.changedBy.firstName} ${field.changedBy.lastName} -
				<openmrs:formatDate date="${field.dateChanged}" type="long" />
			</td>
		</tr>
	</c:if>
</table>
<div id="searchForm" class="searchForm">
	<div class="wrapper">
		<input type="button" onclick="return closeBox();" class="closeButton" value="X" />
		<form method="get" onsubmit="return searchBoxChange('conceptSearchBody', searchText, null, false, 0); return false;" action="">
			<h3>
				<spring:message code="Concept.find" />
			</h3>
			<input type="text" id="searchText" size="35" onkeyup="return searchBoxChange('conceptSearchBody', this, event, false, 400);">
			<input type="checkbox" id="verboseListing" value="true" <c:if test="${defaultVerbose == true}">checked</c:if> onclick="searchBoxChange('searchBody', searchText, event, false, 0); searchText.focus();"><label for="verboseListing"><spring:message code="dictionary.verboseListing"/></label>
		</form>
		<div id="conceptSearchResults" class="searchResults">
			<table>
				<tbody id="conceptSearchBody">
				</tbody>
			</table>
		</div>
	</div>
</div>