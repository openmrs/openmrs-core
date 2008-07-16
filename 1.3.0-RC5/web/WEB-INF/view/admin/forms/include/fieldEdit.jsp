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
				<textarea name="${status.expression}" id="${status.expression}" rows="2" cols="40" type="_moz">${status.value}</textarea>
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
				
				<div dojoType="ConceptSearch" widgetId="cSearch" conceptId="${status.value.conceptId}" showVerboseListing="true"></div>
				<div dojoType="OpenmrsPopup" widgetId="conceptSelection" hiddenInputName="conceptId" searchWidget="cSearch" searchTitle='<spring:message code="Concept.find" />'></div>
					
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
				<input type="hidden" name="_${status.expression}">
				<input type="checkbox" name="${status.expression}" id="${status.expression}" value="on" 
					<c:if test="${status.value == true}">checked</c:if> />
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
				${field.creator.personName} -
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
				${field.changedBy.personName} -
				<openmrs:formatDate date="${field.dateChanged}" type="long" />
			</td>
		</tr>
	</c:if>
</table>