<table>
	<tr>
		<td valign="top">
			<openmrs:message code="Field.name" /><span class="required">*</span>
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
			<openmrs:message code="general.description" />
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
			<openmrs:message code="Field.type" />
		</td>
		<td>
			<spring:bind path="field.fieldType">
				<select name="fieldTypeId" id="${status.expression}" onchange="chooseFieldType(this.value)">
					<c:forEach items="${fieldTypes}" var="ft">
						<option value="${ft.fieldTypeId}"
							<c:if test="${ft.fieldTypeId == status.value.fieldTypeId}">selected</c:if>>
							<c:out value="${ft.name}"/>
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
			<openmrs:message code="Field.concept" />
		</td>
		<td>
			<spring:bind path="field.concept">
				
				<div dojoType="ConceptSearch" widgetId="cSearch" conceptId="${status.value.conceptId}" showVerboseListing="true"></div>
				<div dojoType="OpenmrsPopup" widgetId="conceptSelection" hiddenInputName="conceptId" searchWidget="cSearch" searchTitle='<openmrs:message code="Concept.find" />'></div>
					
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
			<openmrs:message code="Field.database" />
		</td>
		<td>
			<table cellpadding="0" cellspacing="0">
				<tr>
					<td>
						<openmrs:message code="Field.tableName" />
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
						<openmrs:message code="Field.attributeName" />
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
			<openmrs:message code="Field.defaultValue" />
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
			<openmrs:message code="Field.selectMultiple" />
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
				<openmrs:message code="general.createdBy" />
			</td>
			<td>
				<c:out value="${field.creator.personName}" /> -
				<openmrs:formatDate date="${field.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
	<c:if test="${field.changedBy != null}">
		<tr>
			<td>
				<openmrs:message code="general.changedBy" />
			</td>
			<td>
				<c:out value="${field.changedBy.personName}" /> -
				<openmrs:formatDate date="${field.dateChanged}" type="long" />
			</td>
		</tr>
	</c:if>
	<tr>
         <c:if test="${field.fieldId != null}">
          <td><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/></sub></font></td>
          <td colspan="${fn:length(locales)}"><font color="#D0D0D0"><sub><spring:bind path="field.uuid">
          <c:out value="${status.value}"></c:out>
      </spring:bind>M</sub></font></td>
        </c:if>
  </tr>
</table>