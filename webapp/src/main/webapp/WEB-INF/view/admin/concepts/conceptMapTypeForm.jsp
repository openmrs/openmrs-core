<%@page import="java.util.Locale" %>
<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Map Types" otherwise="/login.htm"
                 redirect="/admin/concepts/conceptMapType.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">
<c:if test="${conceptMapType.conceptMapTypeId != null}">
$j(document).ready( function() {
	$j("#retire-dialog").dialog({
		autoOpen: false,
		resizable: false,
		width:'auto',
		height:'auto',
		modal: true,
		beforeClose: function(event, ui){
			$j('#retireReason').val('');
		}
	});
	
	$j("#retire-dialog").addClass("content_align_center");
	
	$j("#purge-dialog").dialog({
		autoOpen: false,
		resizable: false,
		width:'auto',
		height:'auto',
		modal: true
	});
	
	$j("#purge-dialog").addClass("content_align_center");
});
</c:if>
</script>

<h2><openmrs:message code="ConceptMapType.form.title"/></h2>
<br/>

<c:if test="${conceptMapType.conceptMapTypeId != null && conceptMapType.retired}">
<form:form action="unretireConceptMapType.htm" method="post" modelAttribute="conceptMapType">
	<c:if test="${param.conceptMapTypeId != null}">
		<input type="hidden" name="conceptMapTypeId" value="${param.conceptMapTypeId}"/>
	</c:if>
	<div class="voidedMessage">
		<div>
		<openmrs:message code="ConceptMapType.retiredMessage"/> &nbsp;<input type="submit" value='<openmrs:message code="general.restore"/>'/>
		</div>
	</div>
</form:form>
</c:if>

<form:form method="post" action="conceptMapType.form" modelAttribute="conceptMapType">
	<c:if test="${param.conceptMapTypeId != null}">
		<input type="hidden" name="conceptMapTypeId" value="${param.conceptMapTypeId}"/>
	</c:if>
	<fieldset>
	<legend><openmrs:message code="ConceptMapType.details" /></legend>
	<br/>
    <table cellpadding="3" cellspacing="3" class="form_align_left">
        <tr>
            <th class="alignRight"><openmrs:message code="general.name"/><span class="required">*</span></th>
            <td>
                <spring:bind path="name">
                	<input type="text" name="${status.expression}" value="<c:out value="${status.value}" />"/>
                    <c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
                </spring:bind>
            </td>
        </tr>
        <tr>
       		<th class="alignRight" valign="top"><openmrs:message code="general.description"/></th>
            <td valign="top">
                <spring:bind path="description">
                	<textarea name="${status.expression}" rows="3" cols="50"><c:out value="${status.value}" /></textarea>
                    <c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
                </spring:bind>
            </td>
        </tr>
        <tr>
       		<th class="alignRight">
       			<openmrs:message code="ConceptMapType.hidden" /> <img class="help_icon" src="${pageContext.request.contextPath}/images/help.gif" border="0" title="<openmrs:message code="ConceptMapType.hidden.help"/>"/>
       		</th>
            <td>
                <spring:bind path="isHidden">
                	<input type="hidden" name="_${status.expression}" value="" />
                	<input type="checkbox" name="${status.expression}" <c:if test="${status.value}">checked="checked"</c:if> />
                    <c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
                </spring:bind>
            </td>
        </tr>
        <c:if test="${conceptMapType.creator != null}">
		<tr>
			<th class="alignRight"><openmrs:message code="general.createdBy" /></th>
			<td>
				<c:out value="${conceptMapType.creator.personName}" /> -
				<openmrs:formatDate date="${conceptMapType.dateCreated}" type="long" />
			</td>
		</tr>
		</c:if>
		<c:if test="${conceptMapType.changedBy != null}">
		<tr>
			<th class="alignRight"><openmrs:message code="general.changedBy" /></th>
			<td>
				<c:out value="${conceptMapType.changedBy.personName}" /> -
				<openmrs:formatDate date="${conceptMapType.dateChanged}" type="long" />
			</td>
		</tr>
		</c:if>
		<c:if test="${conceptMapType.retiredBy != null}">
		<tr>
			<th class="alignRight"><openmrs:message code="general.retiredBy" /></th>
			<td>
				<c:out value="${conceptMapType.retiredBy.personName}" /> -
				<openmrs:formatDate date="${conceptMapType.dateRetired}" type="long" />
			</td>
		</tr>
		</c:if>
		<c:if test="${conceptMapType.retireReason != null}">
		<tr>
			<th class="alignRight"><openmrs:message code="general.retireReason" /></th>
			<td>
				${conceptMapType.retireReason}
			</td>
		</tr>
		</c:if>
		<tr>
			<td colspan="2">
				<table cellpadding="0" cellspacing="20" align="center">
        			<tr>
        				<td><input type="submit" value="<openmrs:message code="general.save"/>"></td>
        				<td><input type="button" value="<openmrs:message code="general.cancel"/>" onclick="javascript:window.location='conceptMapTypeList.list'"></td>
        			</tr>
     			</table>
			</td>
		</tr>
    </table>
    </fieldset>
</form:form>

<c:if test="${conceptMapType.conceptMapTypeId != null}">
<br/>
<table cellpadding="3" cellspacing="3">
	<tr>
		<td>
			<c:if test="${conceptMapType.retired == false }">
			<input type="submit" value='<openmrs:message code="general.retire"/>' onclick="javascript:$j('#retire-dialog').dialog('open')"/>
			<div id="retire-dialog" title="<openmrs:message code="general.retire.confirmation"/>">
			<form:form action="retireConceptMapType.htm" method="post" modelAttribute="conceptMapType">
			<input type="hidden" name="conceptMapTypeId" value="${param.conceptMapTypeId}"/>
			<br/><br/>
			<table cellpadding="3" cellspacing="3" align="center">
				<tr>
					<th><openmrs:message code="general.reason"/></th>
					<td>
						<input id="retireReason" type="text" name="retireReason" size="40" />
					</td>
				</tr>
				<tr height="20"></tr>
				<tr>
					<td colspan="2" style="text-align: center">
						<input type="submit" value="<openmrs:message code="general.retire"/>" /> &nbsp; <input type="button" value="<openmrs:message code="general.cancel"/>" 
						onclick="javascript:$j('#retire-dialog').dialog('close')" /></td>
				</tr>
			</table>
			</form:form>
			</div>
			</c:if>
		</td>
		<td>
			<openmrs:hasPrivilege privilege="Purge Concept Map Types">
			<input type="button" value='<openmrs:message code="general.purge"/>' onclick="javascript:$j('#purge-dialog').dialog('open')" />
			<div id="purge-dialog" title="<openmrs:message code="general.purge.confirmation"/>">
				<form:form action="purgeConceptMapType.htm" method="post" modelAttribute="conceptMapType">
				<input type="hidden" name="conceptMapTypeId" value="${param.conceptMapTypeId}"/>
				<br/><br/>
				<openmrs:message code="general.confirm.purge"/>
				<br/>
				<table cellpadding="3" cellspacing="30" align="center">
					<tr>
						<td>
							<input type="submit" value='<openmrs:message code="general.yes"/>' /> &nbsp; <input type="button" value="<openmrs:message code="general.no"/>" 
							onclick="javascript:$j('#purge-dialog').dialog('close')" />
						</td>
					</tr>
				</table>
				</form:form>
			</div>
			</openmrs:hasPrivilege>
		</td>
	</tr>
</table>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>
