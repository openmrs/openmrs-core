<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="Dictionary" otherwise="/login.htm" redirect="/dictionary/concept.form"/>

<h2><spring:message code="Concept.title"/></h2>

<form method="post" action="">	
	<table>
		<tr>
			<td><spring:message code="general.name"/></td>
			<td>
				<spring:bind path="conceptName.name">
					<input type="text" name="${status.expression}" value="${status.value}" size="45" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="Concept.shortName"/></td>
			<td>
				<spring:bind path="conceptName.shortName">
					<input type="text" name="${status.expression}" value="${status.value}" size="10" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="general.description"/></td>
			<td valign="top">
				<spring:bind path="concept.description">
					<textarea name="${status.expression}" rows="3" cols="60">${status.value}</textarea>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="Concept.synonyms"/></td>
			<td valign="top">
				<spring:bind path="concept.synonyms">
					<textarea name="syns" rows="6" cols="20"><c:forEach items="${status.value}" var="syn">${syn}
</c:forEach></textarea>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="Concept.conceptClass"/></td>
			<td valign="top">
				<spring:bind path="concept.conceptClass">
					<select name="${status.expression}"
							onChange="changeClass(this);">
						<c:forEach items="${classes}" var="cc">
							<option value="${cc.conceptClassId}" <c:if test="${cc.conceptClassId == status.value}">selected="selected"</c:if>>${cc.name}</option>
						</c:forEach>
					</select>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr id="classDetails">
			<td></td>
			<td id="classDetailsElement"></td>
		</tr>
		<tr>
			<td><spring:message code="Concept.datatype"/></td>
			<td valign="top">
				<spring:bind path="concept.datatype">
					<select name="${status.expression}"
							onChange="changeDatatype(this);">
						<c:forEach items="${datatypes}" var="cd">
							<option value="${cd.conceptDatatypeId}" <c:if test="${cd.conceptDatatypeId == status.value}">selected="selected"</c:if>>${cd.name}</option>
						</c:forEach>
					</select>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr id="datatypeDetails">
			<td></td>
			<td id="datatypeDetailsElement"></td>
		</tr>
		<tr>
			<td><spring:message code="Concept.icd10"/></td>
			<td>
				<spring:bind path="concept.icd10">
					<input type="text" name="${status.expression}" value="${status.value}" size="10"/>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="Concept.loinc"/></td>
			<td>
				<spring:bind path="concept.loinc">
					<input type="text" name="${status.expression}" value="${status.value}" size="10" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="Concept.version"/></td>
			<td>
				<spring:bind path="concept.version">
					<input type="text" name="${status.expression}" value="${status.value}" size="10" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="general.retired"/></td>
			<td>
				<spring:bind path="concept.retired">
					<input type="hidden" name="_${status.expression}">
					<input type="checkbox" name="${status.expression}" value="true" <c:if test="${status.value == true}">checked</c:if> />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<c:if test="${!(concept.creator == null)}" >
			<tr>
				<td><spring:message code="general.creator"/></td>
				<td>
					<spring:bind path="concept.creator">
						${concept.creator.username}
					</spring:bind>
				</td>
			</tr>
			<tr>
				<td><spring:message code="general.dateCreated"/></td>
				<td>
					<spring:bind path="concept.dateCreated">
						<openmrs:formatDate date="${concept.dateCreated}" type="long"/>
					</spring:bind>
				</td>
			</tr>
		</c:if>
		<c:if test="${!(concept.changedBy == null)}" >
			<tr>
				<td><spring:message code="general.changedBy"/></td>
				<td>
					<spring:bind path="concept.changedBy">
						${concept.changedBy.username}
					</spring:bind>
				</td>
			</tr>
			<tr>
				<td><spring:message code="general.dateChanged"/></td>
				<td>
					<spring:bind path="concept.dateChanged">
						<openmrs:formatDate date="${concept.dateChanged}" type="long"/>
					</spring:bind>
				</td>
			</tr>
		</c:if>
	</table>
<input type="submit" value=" Save Concept "/>

</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>