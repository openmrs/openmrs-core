<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Location Tags" otherwise="/login.htm" redirect="/admin/locations/locationTag.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">
    var error_message = "<openmrs:message code="LocationTag.name.error" />";
	$j(document).ready(function() {
		$j('.toggleAddLocationTag').click(function(event) {
			$j('#nameError').html('');
			$j('#addLocationTag').slideToggle('fast');
			event.preventDefault();
		});

        $j('#locationTagForm').submit(function() {
            var tagName = $j('input[name="name"]').val();
            if (tagName.trim() != '') {
                $j(this).submit();
            }
            $j('#nameError').html(error_message);
            return false;
        });
	});
</script>

<h2><openmrs:message code="LocationTag.manage"/></h2>

<a class="toggleAddLocationTag" href="#"><openmrs:message code="LocationTag.add"/></a>
<div id="addLocationTag" style="border: 1px black solid; background-color: #e0e0e0; display: none">
	<form method="post" action="locationTagAdd.form" id="locationTagForm">
		<table>
			<tr>
				<th><openmrs:message code="LocationTag.name"/><span class="required">*</span></th>
				<td>
					<input type="text" name="name"/>
                    <span class="required" id="nameError"></span>
				</td>
			</tr>
			<tr>
				<th><openmrs:message code="LocationTag.description"/></th>
				<td><textarea name="description" rows="3" cols="72"></textarea></td>
			</tr>
			<tr>
				<th></th>
				<td>
					<input type="submit" value="<openmrs:message code="general.save"/>" />
					<input type="button" value="<openmrs:message code="general.cancel"/>" class="toggleAddLocationTag" />
				</td>
			</tr>
		</table>
	</form>
</div>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationTagList.afterAdd" type="html" />

<br />
<br />
<b class="boxHeader"><openmrs:message code="LocationTag.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> <openmrs:message code="general.name" /> </th>
			<th> <openmrs:message code="general.description" /> </th>
			<th> <openmrs:message code="general.creator" /> </th>
			<th> <openmrs:message code="general.dateCreated" /> </th>
		</tr>
		<c:forEach var="locationTag" items="${locationTags}">
			<tr <c:if test="${locationTag.retired == true}"> class="retired" </c:if> >
				<td valign="top">
					<a href="locationTagEdit.form?locationTagId=${locationTag.locationTagId}">
						<c:out value="${locationTag.name}"/>
					</a>
				</td>
				<td valign="top">
					<c:out value="${locationTag.description}"/>
				</td>
				<td valign="top">
					<openmrs:format user="${locationTag.creator}"/>
				</td>
				<td valign="top">
					<openmrs:formatDate date="${locationTag.dateCreated}"/>
				</td>
			</tr>
		</c:forEach>
		<c:if test="${empty locationTags}">
			<tr><td colspan="4"><openmrs:message code="general.none"/></td></tr>
		</c:if>
	</table>
	<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationTagList.inForm" type="html" />
	
</form>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationTagList.footer" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>
