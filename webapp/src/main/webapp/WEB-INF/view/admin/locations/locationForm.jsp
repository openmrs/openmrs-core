<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Locations" otherwise="/login.htm" redirect="/admin/locations/location.form" />
<openmrs:message var="pageTitle" code="location.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="Location.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationForm.afterTitle" type="html" parameters="locationId=${location.locationId}" />

<c:if test="${location.retired}">
	<form action="" method="post">
		<div class="retiredMessage">
			<div>
				<openmrs:message code="general.retiredBy"/>
				<c:out value="${location.retiredBy.personName}" />
				<openmrs:formatDate date="${location.dateRetired}" type="medium" />
				-
				<c:out value="${location.retireReason}"/>
				<input type="submit" value='<openmrs:message code="Location.unretireLocation"/>' name="unretireLocation"/>
			</div>
		</div>
	</form>
</c:if>

<spring:hasBindErrors name="location">
    <openmrs_tag:errorNotify errors="${errors}" />
</spring:hasBindErrors>
<form method="post">
<fieldset>
	<table class="left-aligned-th">
		<tr>
			<th><openmrs:message code="general.name"/><span class="required">*</span></th>
			<td colspan="5">
				<spring:bind path="location.name">
					<input type="text" name="name" value="${status.value}" size="35" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th valign="top"><openmrs:message code="general.description"/></th>
			<td valign="top" colspan="5">
				<spring:bind path="location.description">
					<textarea name="description" rows="3" cols="40">${status.value}</textarea>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<spring:nestedPath path="location">
			<openmrs:portlet url="addressLayout" id="addressPortlet" size="full" parameters="layoutShowTable=false|layoutShowExtended=false|layoutShowErrors=false|isNew=${location.locationId == null}" />
		</spring:nestedPath>
		<tr>
			<th valign="top"><openmrs:message code="Location.parentLocation"/></th>
			<td colspan="5">
				<spring:bind path="location.parentLocation">
					<openmrs_tag:locationField formFieldName="parentLocation" initialValue="${status.value}" optionHeader="[blank]"/>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<spring:bind path="location.activeAttributes">
			<c:if test="${status.error}">
				<tr>
					<th></th>
					<td colspan="5">
						<span class="error">
							<c:forEach var="err" items="${status.errorMessages}">
								${ err }<br/>
							</c:forEach>
						</span>
					</td>
				</tr>
			</c:if>
		</spring:bind>
		<c:forEach var="attrType" items="${ attributeTypes }">
		    <c:if test="${ !attrType.retired }">
			    <openmrs_tag:attributesForType attributeType="${ attrType }" customizable="${ location }" formFieldNamePrefix="attribute.${ attrType.id }"/>
            </c:if>
		</c:forEach>
		<tr>
			<th valign="top"><openmrs:message code="Location.tags"/></th>
			<td colspan="5">
				<spring:bind path="location.tags">
					<input type="hidden" name="_tags"/>
					<c:forEach var="t" items="${locationTags}">
                        <c:if test="${openmrs:collectionContains(status.value, t) || !t.retired}">
                            <span <c:if test="${t.retired}">class="retired"</c:if>>
                                <input type="checkbox" name="tags" value="${t.id}" <c:if test="${openmrs:collectionContains(status.value, t)}">checked="true"</c:if>/>
                                <openmrs:format locationTag="${t}"/>
                            </span>
                        </c:if>
					</c:forEach>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<c:if test="${!(location.creator == null)}">
			<tr>
				<th><openmrs:message code="general.createdBy" /></th>
				<td colspan="5">
					<c:out value="${location.creator.personName}" /> -
					<openmrs:formatDate date="${location.dateCreated}" type="long" />
				</td>
			</tr>
		</c:if>
		<tr>
		 <c:if test="${location.locationId != null}">
           <th><font color="#D0D0D0"><sub><openmrs:message code="general.uuid" /></sub></font></th>
           <td colspan="${fn:length(locales)}"><font color="#D0D0D0"><sub>
           <spring:bind path="location.uuid">
               <c:out value="${status.value}"></c:out>
           </spring:bind></sub></font>
           </td>
         </c:if>
        </tr>
	</table>
	<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationForm.inForm" type="html" parameters="locationId=${location.locationId}" />
	<br />
	<input type="submit" value="<openmrs:message code="Location.save"/>" name="saveLocation">
</fieldset>
</form>

<br/>

<c:if test="${not location.retired && not empty location.locationId}">
	<form action="" method="post">
		<fieldset>
			<h4><openmrs:message code="Location.retireLocation"/></h4>

			<b><openmrs:message code="general.reason"/></b>
			<spring:bind path="location.retireReason">
				<input type="text" value="${status.value}" size="40" name="retireReason" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			<input type="hidden" name="retired" value="true" />
			<br/>
			<input type="submit" value='<openmrs:message code="Location.retireLocation"/>' name="retireLocation"/>
		</fieldset>
	</form>
</c:if>

<br/>

<fieldset>
	<h4><openmrs:message code="Location.childLocations"/></h4>
	<ul>
		<c:if test="${empty location.childLocations}">
			<li><openmrs:message code="general.none"/></li>
		</c:if>
		<c:forEach var="child" items="${location.childLocations}">
			<li><openmrs:format location="${child}"/></li>
		</c:forEach>
	</ul>
</fieldset>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationForm.footer" type="html" parameters="locationId=${location.locationId}" />

<%@ include file="/WEB-INF/template/footer.jsp" %>