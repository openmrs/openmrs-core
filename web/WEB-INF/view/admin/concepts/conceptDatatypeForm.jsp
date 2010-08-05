<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Datatypes" otherwise="/login.htm" redirect="/admin/concepts/conceptDatatype.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<style>
	a.selectedTab {
		background-color: whitesmoke;
	}
	a.tab{
		border-bottom: 1px solid whitesmoke;
		padding-left: 3px;
		padding-right: 3px;
	}
</style>

<script type="text/javascript">
	function selectTab(tab) {
		var displays = new Array();
		
		var tabs = tab.parentNode.getElementsByTagName("a");
		for (var tabIndex=0; tabIndex<tabs.length; tabIndex++) {
			var index = tabs[tabIndex].id.indexOf("Tab");
			var tabName = tabs[tabIndex].id.substr(0, index);
			if (tabs[tabIndex] == tab) {
				displays[tabName] = "";
				addClass(tabs[tabIndex], 'selectedTab');
			}
			else {
				displays[tabName] = "none";
				removeClass(tabs[tabIndex], 'selectedTab');
			}
		}
		
		var parent = tab.parentNode.parentNode.parentNode;
		var elements = parent.getElementsByTagName("td");	
		for (var i=0; i<elements.length; i++) {
			if (displays[elements[i].className] != null)
				elements[i].style.display = displays[elements[i].className];
		}
		
		tab.blur();
		return false;
	}	
</script>

<h2><spring:message code="ConceptDatatype.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptDatatypeForm.afterTitle" type="html" parameters="conceptDatatypeId=${conceptDatatype.conceptDatatypeId}" />

<%--  <form method="post"> --%>
<table>
	<tr>
		<td><spring:message code="general.name"/></td>
		<td>
			<spring:bind path="conceptDatatype.name">
				<input type="text" name="name" value="${status.value}" size="35" readonly="1"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<!-- 
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td>
			<spring:bind path="conceptDatatype.description">
				<textarea name="description" rows="3" cols="40" type="_moz" readonly="1">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	-->
	<spring:bind path="conceptDatatype.localizedDescription">
		<input type="hidden" id="localizedDescriptionHidden" name="${status.expression}" value="${status.value}" />
	</spring:bind>
	
	<spring:bind path="conceptDatatype.localizedDescription.variants">
		<c:set var="variants" value="${status.value}" />
	</spring:bind>
	
	<c:choose>
		<c:when test="${(variants == null) or (variants.size == 0)}">
			<tr id="simpleMode">
				<td valign="top">
					<spring:message code="general.description" />
				</td>
				<td valign="top">
					<spring:bind path="conceptDatatype.localizedDescription.unlocalizedValue">
						<textarea rows="3" cols="45" type="_moz" readonly="1">${status.value}</textarea>
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
				</td>
			</tr>
			<c:set var="showAdvancedModeFirst" value="display:none;" /> 
		</c:when>
		<c:otherwise>
			<c:set var="showAdvancedModeFirst" value="" />
		</c:otherwise>
	</c:choose>
	
	<tr id="advancedModeTROne" style="${showAdvancedModeFirst}">
		<td>
			<spring:message code="general.description"/>
		</td>
		<td>
			<a id="defaultTab" class="tab selectedTab default" href="#selectDefault" onclick="return selectTab(this)"><spring:message code="LocalizedDescription.add.firstTabName" /></a>&nbsp;&nbsp;
			<openmrs:forEachRecord name="allowedLocale">
				<a id="${record}Tab" class="tab ${record}" href="#select${record.displayName}" onclick="return selectTab(this)">${record.displayName}</a>&nbsp;&nbsp;
			</openmrs:forEachRecord>
		</td>
	</tr>
	<tr id="advancedModeTRTwo" style="${showAdvancedModeFirst}">
		<td></td>
		<!-- Deal with the unlocalized description -->
		<td class="default">
			<spring:bind path="conceptDatatype.localizedDescription.unlocalizedValue">
				<textarea id="textarea_default" rows="3" cols="45" type="_moz" readonly="1">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>	
		</td>
		<openmrs:forEachRecord name="allowedLocale">
			<!-- Deal with the description within each allowed locale -->
			<td class="${record}" style="display:none;">
				<c:set var="isSame" value="yes" />
				<c:set var="sameAsLanguage" value="default" />
				<c:set var="sameAsDisplayLanguage" value="default" />
				<spring:bind path="conceptDatatype.localizedDescription.unlocalizedValue">
					<c:set var="descriptionValue" value="${status.value}" />
				</spring:bind>	
				<c:if test="${variants[record] != null}">
					<c:set var="isSame" value="no" />
					<c:set var="descriptionValue" value="${variants[record]}" />
				</c:if>
				<c:if test="${!(record.country == null or record.country == '')}">
					<c:set var="languageName" value="${record.language}" />
					<openmrs:forEachRecord name="allowedLocale">
						<c:if test="${(record.country == null or record.country == '') and (record.language == languageName)}">
							<c:set var="sameAsLanguage" value="${languageName}" />
							<c:set var="sameAsDisplayLanguage" value="${record.displayLanguage}" />
							<c:if test="${(isSame == 'yes') and (variants[record] != null)}">
								<!-- update current description's value to that value within its language-only locale -->
								<c:set var="descriptionValue" value="${variants[record]}" />
							</c:if>
						</c:if>
					</openmrs:forEachRecord>
				</c:if>
				<c:choose>
					<c:when test="${record.country != null and record.country != ''}">
						<c:set var="localeValue" value="${record.language}_${record.country}" />
					</c:when>
					<c:otherwise>
						<c:set var="localeValue" value="${record.language}" />
					</c:otherwise>
				</c:choose>
				<input type="checkbox" name="same as ${sameAsLanguage}" <c:if test="${isSame == 'yes'}">checked</c:if> readonly="1"/>
				<!-- use spring message tag here for internalization-->
				<spring:message code="LocalizedDescription.add.sameAs" />&nbsp;
				<c:choose>
					<c:when test="${sameAsLanguage == 'default'}"><spring:message code="LocalizedDescription.add.firstTabName" /></c:when>
					<c:otherwise>${sameAsDisplayLanguage}</c:otherwise>
				</c:choose>
				<br />
				<textarea rows="3" cols="45" type="_moz" readonly="1">${descriptionValue}</textarea>
			</td>
		</openmrs:forEachRecord>
	</tr>
	<tr>
		<td><spring:message code="ConceptDatatype.hl7Abbreviation"/></td>
		<td>
			<spring:bind path="conceptDatatype.hl7Abbreviation">
				<input type="text" name="hl7Abbreviation" value="${status.value}" size="5" readonly="1"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(conceptDatatype.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${conceptDatatype.creator.personName} -
				<openmrs:formatDate date="${conceptDatatype.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
</table>
<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptDatatypeForm.inForm" type="html" parameters="conceptDatatypeId=${conceptDatatype.conceptDatatypeId}" />
<br />
<%-- <input type="submit" value="<spring:message code="ConceptDatatype.save"/>">
</form> --%>

<div id="conceptDatatypeFormReadOnly">(<spring:message code="general.readonly"/>)</div>

<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.conceptDatatypeForm.footer" type="html" parameters="conceptDatatypeId=${conceptDatatype.conceptDatatypeId}" />

<%@ include file="/WEB-INF/template/footer.jsp" %>