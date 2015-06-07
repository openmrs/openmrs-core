<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Providers" otherwise="/login.htm" redirect="/admin/provider/provider.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
$j(document).ready(function(){
	var msgHolder = $j('#msgHolder');
	if(!$j.trim(msgHolder.html()))
		msgHolder.hide();

    var providerName = $j('#providerName');
    var person = $j('#person_id');

    providerName.on('input', function(){
        if(providerName.val().length > 0){
            $j('#personRequired').attr('style', 'display:none');
        } else {
            $j('#personRequired').attr('style', 'display:initial');
        }
    });

    $j('input').on('input focus blur', function(){
        if(person.val().length > 0){
            $j('#providerRequired').attr('style', 'display:none');
        } else {
            $j('#providerRequired').attr('style', 'display:initial');
        }
    });
});

function validateForm(){
	var providerName = $j('#providerName');
	var person = $j('#person_id');
	var provider = "${provider.providerId}";
	var msg = '<openmrs:message code="Provider.error.personAndName.provided" />';
	var result = true;
	if(providerName.val().length > 0 && (person.val().length > 0 && provider.length == 0)){
		result = false;
		$j('#msgHolder').html(msg).show();
	}
	return result;
}

function confirmPurge() {
	if (confirm('<openmrs:message code="Provider.confirmDelete"/>')) {
		return true;
	} else {
		return false;
	}
}
</script>

<style>
	#table th { text-align: left; }
	td.fieldNumber { 
		width: 5px;
		white-space: nowrap;
	}
</style>

<h2><openmrs:message code="Provider.manage.title"/></h2>

<spring:hasBindErrors name="provider">
	<openmrs:message htmlEscape="false" code="fix.error"/>
	<br />
</spring:hasBindErrors>

<b class="boxHeader">
<c:if test="${provider.providerId == null}">
	<openmrs:message code="Provider.create"/>
</c:if>
<c:if test="${provider.providerId != null}">
	<openmrs:message code="Provider.edit"/>
</c:if>
</b>

<div class="box">
	<form method="post" onSubmit="return validateForm()">
		
		<table cellpadding="3" cellspacing="0">
			<tr>
				<th><openmrs:message code="Provider.identifier"/></th>
				<td colspan="4">
					<spring:bind path="provider.identifier">			
						<input type="text" name="${status.expression}" size="10" 
							   value="${status.value}" />
					   
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
					</spring:bind>
				</td>
			</tr>
			<c:choose>
			<c:when test="${provider.providerId == null}">
			<tr>
				<th><openmrs:message code="Provider.person"/><span id="personRequired" class="required">*</span></th>
				<td>
					<spring:bind path="provider.person">
					<openmrs:fieldGen type="org.openmrs.Person" formFieldName="${status.expression}" val="${status.editor.value}"/>
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
				</td>
			</tr>
			</c:when>
			<c:otherwise>
			<tr>
				<th><openmrs:message code="Provider.person"/></th>
				<td>
					<div class="providerDetails">
						<c:out value="${provider.person.personName}" />
						<span <c:if test="${provider.person != null}">style="display:none"</c:if>>
						<spring:bind path="provider.person">
						<openmrs_tag:personField formFieldName="${status.expression}" initialValue="${status.value}" />
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>	
						</spring:bind>
						</span>
					 </div>
				</td>
			</tr>
			</c:otherwise>
			</c:choose>

			<spring:bind path="provider.activeAttributes">
				<c:if test="${status.error}">
					<tr>
						<th></th>
						<td>
							<span class="error">
								<c:forEach var="err" items="${status.errorMessages}">
									${ err }<br/>
								</c:forEach>
							</span>
						</td>
					</tr>
				</c:if>
			</spring:bind>
            <c:if test="${ not empty providerAttributeTypes }">
				<c:forEach var="attrType" items="${ providerAttributeTypes }">
					<openmrs_tag:attributesForType attributeType="${ attrType }" customizable="${ provider }" formFieldNamePrefix="attribute.${ attrType.providerAttributeTypeId }"/>
				</c:forEach>
			</c:if>
			<tr>
         	<c:if test="${provider.providerId != null}">
           		<td><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/></sub></font></td>
           		<td colspan="${fn:length(locales)}"><font color="#D0D0D0"><sub>
           	<spring:bind path="provider.uuid">
               <c:out value="${status.value}"></c:out>
           </spring:bind>
           </sub></font>
           </td>
           </c:if>
    	   </tr>
		</table>

		<br/>
	
	<input type="hidden" name="phrase" value='<request:parameter name="phrase" />'/>
	<input type="submit" name="saveProviderButton" value='<openmrs:message code="Provider.save"/>'>
	&nbsp;
	<input type="button" value='<openmrs:message code="general.cancel"/>' onclick="document.location='index.htm'">
	
	</form>
	</div>
	
	<br/>

	<c:if test="${provider.providerId != null}">
		<div class="box">
			<form method="post">
				<table cellpadding="3" cellspacing="0">
					<tr>
						<th><openmrs:message code="general.createdBy" /></th>
						<td>
							<a href="#View User" onclick="return gotoUser(null, '${provider.creator.userId}')"><c:out value="${provider.creator.personName}" /></a> -
							<openmrs:formatDate date="${provider.dateCreated}" type="medium" />
						</td>
					</tr>
					<c:if test="${provider.retiredBy == null}">
						<tr id="retiredReason">
							<th><openmrs:message code="general.retiredReason" /></th>
							<td>
								<spring:bind path="provider.retired">
									<input type="hidden" name="${status.expression}" value="true"/>
								</spring:bind>

								<spring:bind path="provider.retireReason">
									<input type="text" id="retire" value="${status.value}" name="${status.expression}" size="40" />
									<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
								</spring:bind>
							</td>
						</tr>
						<tr>
							<td><input type="submit" name="retireProviderButton"
								value='<openmrs:message code="Provider.retire"/>'></td>
						</tr>
					</c:if>	
														
					<c:if test="${provider.retiredBy != null}">
						<tr id="retiredBy">
							<th><openmrs:message code="general.retiredBy" /></th>
							<td>
								<a href="#View User" onclick="return gotoUser(null, '${provider.retiredBy.userId}')"><c:out value="${provider.retiredBy.personName}" /></a> -
								<openmrs:formatDate date="${provider.dateRetired}" type="medium" />
							</td>
						</tr>
						<tr>
							<th><openmrs:message code="general.retiredReason" /></th>
							<td><c:out value="${provider.retireReason}"/></td>
						</tr>
						<tr>
							<td><input type="submit" name="unretireProviderButton"
								value='<openmrs:message code="Provider.unretire"/>'></td>
						</tr>
					</c:if>
				</table>
			</form>
		</div>

		<br/>

		<div class="box">
			<form method="post" onsubmit="return confirmPurge()">
				<table cellpadding="3" cellspacing="0">
					<tr>
						<th><openmrs:message code="Provider.purge" /></th>
					</tr>
					<tr>
						<td><input type="submit" name="purgeProviderButton"
						value='<openmrs:message code="Provider.purge"/>'></td>
					</tr>
				</table>
			</form>
		</div>
	</c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>