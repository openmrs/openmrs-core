<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Sources" otherwise="/login.htm" redirect="/admin/concepts/conceptSource.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="ConceptSource.title"/></h2>

<c:if test="${isImplementationId}">
<br/><openmrs:message code="ConceptSource.isImplementationId"/><br/><br/>
</c:if>

<c:if test="${conceptSource.conceptSourceId == null}">
	<form method="post">
</c:if>

<spring:hasBindErrors name="conceptSource">
    <openmrs_tag:errorNotify errors="${errors}" />
</spring:hasBindErrors>

<fieldset>
<table>
	<tr>
		<td><openmrs:message code="general.name"/><span class="required">*</span></td>
		<td>
			<spring:bind path="conceptSource.name">
				<input type="text" name="name" value="<c:out value="${status.value}" />" size="35" />
				<c:if test="${status.errorMessage != ''}"><br/><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
		<td class="description"><openmrs:message code="ConceptSource.name.help"/></td>
	</tr>
	<tr>
		<td><openmrs:message code="ConceptSource.hl7Code"/></td>
		<td>
			<spring:bind path="conceptSource.hl7Code">
				<input type="text" name="hl7Code" value="<c:out value="${status.value}" />" size="35" maxlength="20" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
		<td class="description"><openmrs:message code="ConceptSource.hl7Code.help"/></td>
	</tr>
	<tr>
		<td valign="top"><openmrs:message code="general.description"/><span class="required">*</span></td>
		<td>
			<spring:bind path="conceptSource.description">
				<textarea name="description" rows="3" cols="40"><c:out value="${status.value}" /></textarea>
				<c:if test="${status.errorMessage != ''}"><br/><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
		<td class="description"><openmrs:message code="ConceptSource.description.help"/></td>
	</tr>
	<c:if test="${conceptSource.creator != null}">
		<tr>
			<td><openmrs:message code="general.createdBy" /></td>
			<td>
				<c:out value="${conceptSource.creator.personName}" /> -
				<openmrs:formatDate date="${conceptSource.dateCreated}" type="long" />
			</td>
			<td class="description"></td>
		</tr>
	</c:if>
	<tr>
 		 <c:if test="${conceptSource.conceptSourceId != null}">
           <td><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/></sub></font></td>
           <td colspan="${fn:length(locales)}"><font color="#D0D0D0"><sub>${conceptSource.uuid}</sub></font></td>
         </c:if>
   </tr>
</table>
</fieldset>


<c:if test="${not conceptSource.retired && not empty conceptSource.conceptSourceId}">
<br/>
<fieldset>
	<form method="post">
			<h4><openmrs:message code="general.retire"/></h4>
			
			<b><openmrs:message code="general.reason"/></b>
			<input type="text" value="" size="40" name="retireReason" />
			<spring:hasBindErrors name="conceptSource">
				<c:forEach items="${errors.allErrors}" var="error">
					<c:if test="${error.code == 'retireReason'}"><span class="error"><openmrs:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span></c:if>
				</c:forEach>
			</spring:hasBindErrors>
			<br/>
			<input type="submit" value='<openmrs:message code="general.retire"/>' name="retire"/>
	</form>
</fieldset>
</c:if>

<c:if test="${conceptSource.voided && not empty conceptSource.conceptSourceId}">
<br/>
<fieldset>
	<form method="post">
			<h4><openmrs:message code="general.restore"/></h4>
			<input type="submit" value='<openmrs:message code="general.restore"/>' name="restore"/>
	</form>
</fieldset>
</c:if>

<c:if test="${not empty conceptSource.conceptSourceId }">
<br/>
<fieldset>
	<form method="post">
			<h4><openmrs:message code="general.purge"/></h4>
			<input type="submit" value='<openmrs:message code="general.purge"/>' name="purge"/>
	</form>
</fieldset>
</c:if>

<br />
<c:choose>
	<c:when test="${conceptSource.conceptSourceId == null}">
		<input type="submit" value='<openmrs:message code="ConceptSource.save"/>'>
		</form>
	</c:when>
	<c:otherwise>
		<openmrs:message code="ConceptSource.cannotBeEdited"/>
	</c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/template/footer.jsp" %>