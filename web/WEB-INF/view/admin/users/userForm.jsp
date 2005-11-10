<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Users" otherwise="/login.htm" redirect="/admin/users/user.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="User.title"/></h2>

<spring:hasBindErrors name="user">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/>
		</c:forEach>
	</div>
	<br />
</spring:hasBindErrors>

<form method="post">
	<table>
		<tr>
			<td><spring:message code="User.username"/></td>
			<td>
				<spring:bind path="user.username">
					<input type="text" name="${status.expression}" value="${status.value}"/>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<c:if test="${user.creator == null}">
			<tr>
				<td><spring:message code="User.password"/></td>
				<td><input type="password" name="password"/></td>
	
			</tr>
			<tr>
				<td><spring:message code="User.confirm"/></td>
				<td><input type="password" name="confirm"></td>
			</tr>
		</c:if>
		<tr>
			<td><spring:message code="User.firstName"/></td>
			<td>
				<spring:bind path="user.firstName">
					<input type="text" name="${status.expression}" value="${status.value}"/>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>

		</tr>
		<tr>
			<td><spring:message code="User.middleName"/></td>
			<td>
				<spring:bind path="user.middleName">
					<input type="text" name="${status.expression}" value="${status.value}"/>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>

		</tr>
		<tr>
			<td><spring:message code="User.lastName"/></td>
			<td>
				<spring:bind path="user.lastName">
					<input type="text" name="${status.expression}" value="${status.value}"/>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>

		</tr>
		<tr>
			<td valign="top"><spring:message code="User.roles"/></td>
			<td>
				<select name="roles" multiple size="5">
					<c:forEach var="role" items="${roles}" varStatus="status">
						<option value="<c:out value="${role.role}"/>"
							<c:forEach var="r" items="${user.roles}"><c:if test="${r == role}">selected</c:if></c:forEach>>
								${role}
						</option>
					</c:forEach>
				</select>
			</td>
		</tr>
		<c:if test="${!(user.creator == null)}" >
			<tr>
				<td><spring:message code="general.creator"/></td>
				<td>
					<spring:bind path="user.creator">
						${address.creator.username}
						<input type="hidden" name="${status.expression}" value="${status.value}"/>
					</spring:bind>
				</td>
			</tr>
			<tr>
				<td><spring:message code="general.dateCreated"/></td>
				<td>
					<spring:bind path="user.dateCreated">
						<openmrs:formatDate date="${address.dateCreated}" type="long"/>
				  		<input type="hidden" name="${status.expression}" value="${status.value}">
					</spring:bind>
				</td>
			</tr>
			<spring:bind path="user.userId">
				<input type="hidden" name="${status.expression}:int" value="${status.value}">
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</c:if>
		<tr>
			<td><spring:message code="general.voided"/></td>
			<td>
				<spring:bind path="user.voided">
					<input type="hidden" name="_${status.expression}">
					<input type="checkbox" name="${status.expression}" 
						   id="${status.expression}" 
						   <c:if test="${status.value == true}">checked</c:if> 
						   onClick="voidedBoxClick(this)"
					/>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="general.voidReason"/></td>
			<spring:bind path="user.voidReason">
				<td>
					<input type="text" name="${status.expression}" id="voidReason" value="${status.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</td>
			</spring:bind>
		</tr>
			<c:if test="${!(user.voidedBy == null)}" >
				<tr>
					<td><spring:message code="general.voidedBy"/></td>
					<td>
						<spring:bind path="user.voidedBy">
							${user.voidedBy.username}
							<input type="hidden" name="${status.expression}" value="${status.value}"/>
						</spring:bind>
					</td>
				</tr>
				<tr>
					<td><spring:message code="general.dateVoided"/></td>
					<td>
						<spring:bind path="user.dateVoided">
							<openmrs:formatDate date="${user.dateVoided}" type="long"/>
							<input type="hidden" name="${status.expression}" value="${status.value}">
						</spring:bind>
					</td>
				</tr>
			</c:if>
		</tr>
	</table>
	<input type="submit" value="<spring:message code="User.save"/>" />
</form>


<%@ include file="/WEB-INF/template/footer.jsp" %>