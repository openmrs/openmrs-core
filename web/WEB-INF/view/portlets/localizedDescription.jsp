<%@ include file="/WEB-INF/template/include.jsp" %>

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
	/*
	* Update the value of hidden input 'localizedDescriptionHidden' in simple mode.
	* Note: In simple mode, there is only one text input for description in UI.
	*/
	function updateUnlocalizedValueInSimpleMode(obj) {
		// update contents of those unvisiable tabs in advanced mode
		// and also the value of hidden input 'localizedDescriptionHidden'
		var textareaOfDefault = document.getElementById('textarea_default');
		textareaOfDefault.value = escapeDelimiter(obj.value);
		updateDescriptionVariantInAdvancedMode(textareaOfDefault, 'default');
	}

	/*
	* Update description value and other descriptions referring to this value
	* @param obj - the textarea in which the updated description value is stored
	* @param lang - language in which the description value is modified 
	*/
	function updateDescriptionVariantInAdvancedMode(obj, lang) {
		// firstly, update current description itself
		var currentValue = document.getElementById('localizedDescriptionHidden').value;
		if (lang == 'default') {
			if (currentValue.indexOf('i18n:v1;') == -1)
				document.getElementById('localizedDescriptionHidden').value = escapeDelimiter(obj.value);
			else
				updateDescription('unlocalized', obj.value);
		} else {
			var pattern = ";" + lang + ":";
			if (currentValue.indexOf('i18n:v1;') == -1)
				document.getElementById('localizedDescriptionHidden').value = "i18n:v1;unlocalized:" + currentValue + ";" + lang + ":" + escapeDelimiter(obj.value) + ";";
			else if (currentValue.indexOf(pattern) == -1)
				document.getElementById('localizedDescriptionHidden').value = currentValue + lang + ":" + escapeDelimiter(obj.value) + ";";
		    else
				updateDescription(lang, obj.value);
		}

		// secondly, update related other description variants(same as XXX), also may need to do recursively
		updateRelatedDescriptionVariants(lang, obj.value);
	}

	function updateRelatedDescriptionVariants(lang, val) {
		// update descriptions which refer to the passed lang
		var pattern = "same as " + lang;
		var allCheckBoxes = document.getElementsByName(pattern);
		for (var i = 0;i < allCheckBoxes.length;i++) {
			if (allCheckBoxes[i].checked == true) {
				var pNode = allCheckBoxes[i].parentNode;
				(pNode.getElementsByTagName('textarea'))[0].value = val;
				updateRelatedDescriptionVariants(pNode.className, val);
			}
		}
	}

	/*
	* Update description when belonged metedata has been localized
	* @param loc - language in which the modified value is stored
	* @param value - modified description value
	*/
	function updateDescription(loc, value) {
		var localizedDescriptionValue = document.getElementById("localizedDescriptionHidden").value;
		var pattern = ";" + loc + ":";
		var pos = localizedDescriptionValue.indexOf(pattern);
		if (pos != -1) {
			var prefix = localizedDescriptionValue.substring(0, pos + pattern.length);
			var suffix = "";
			//cut out the sub string behind "pattern"
			var temp = localizedDescriptionValue.substr(pos + pattern.length);
			//search for the next sub string like form ";xx:"
			pattern = ";[^:;\\\\]*:";
			var reg = new RegExp(pattern);
			if (temp.match(reg) == null) {/*cann't find the next sub string*/
				//the passed loc is the locale of last variant description
				document.getElementById("localizedDescriptionHidden").value = prefix + escapeDelimiter(value) + ";";
			} else {
				//cut out the sub string behind the second "pattern"
				pos = temp.match(reg).index;
				suffix = temp.substr(pos);
				document.getElementById("localizedDescriptionHidden").value = prefix + escapeDelimiter(value) + suffix;
			}
		}
	}

	/*
	* Remove an existed variant description
	*/
	function removeVariantdescription(loc, value){
		var localizedDescriptionValue = document.getElementById("localizedDescriptionHidden").value;
		//pattern will be used in regular expression, so we should use escapeDelimiter two times to escapse ";" to be "\\\\;" 
		var pattern = ";" + loc + ":" + escapeDelimiter(escapeDelimiter(value)) + ";";
		var reg = new RegExp(pattern);
		document.getElementById("localizedDescriptionHidden").value = localizedDescriptionValue.replace(reg, ";");
	}

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

	// switch mode from simple to advanced
	function switchToAdvancedMode() {
		document.getElementById('simpleMode').style.display="none";
		document.getElementById('advancedModeTROne').style.display="";
		document.getElementById('advancedModeTRTwo').style.display="";
	}

	// change the status of checkbox 'same as xxx' 
	function changeSameStatus(obj, lang) {
		var udpatedNode = obj.parentNode.getElementsByTagName('textarea')[0];
		if (obj.checked == true) {
			removeVariantdescription(lang, udpatedNode.value);//remove this description variant from hidden input 'localizedDescriptionHidden'
			var pNode = obj.parentNode.parentNode;
			var sameAsWho = obj.name.substr(8);//'same as 's length is 8
			var allTds = pNode.getElementsByTagName('td');
			for (var i = 0;i < allTds.length;i++) {
				var tdNode = allTds[i];
				if (tdNode.className == sameAsWho) {
					var textareaNode = tdNode.getElementsByTagName('textarea')[0];
					udpatedNode.value = textareaNode.value;
					udpatedNode.disabled = true;
					updateRelatedDescriptionVariants(udpatedNode.parentNode.className, udpatedNode.value);// update other description variants same as this one
					return;
				}
			}
		} else {
			udpatedNode.value = "";
			udpatedNode.disabled = false;
			updateRelatedDescriptionVariants(udpatedNode.parentNode.className, udpatedNode.value);// update other description variants same as this one
		}
	}
</script>

<spring:bind path="localizedDescription">
	<input type="hidden" id="localizedDescriptionHidden" name="${status.expression}" value="${status.value}" />
</spring:bind>

<spring:bind path="localizedDescription.variants">
	<c:set var="variants" value="${status.value}" />
</spring:bind>

<c:choose>
	<c:when test="${(variants == null) or (variants.size == 0)}">
		<tr id="simpleMode">
			<td valign="top">
				<spring:message code="general.description" />
			</td>
			<td valign="top">
				<spring:bind path="localizedDescription.unlocalizedValue">
					<textarea rows="3" cols="45" onkeypress="return forceMaxLength(this, 1024);" onchange="updateUnlocalizedValueInSimpleMode(this)" >${status.value}</textarea>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
				<input type="button" style="float:right;" value='<spring:message code="LocalizedDescription.add"/>' class="smallButton" style="width:90px;" onclick="return switchToAdvancedMode();" />
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
		<spring:bind path="localizedDescription.unlocalizedValue">
			<textarea id="textarea_default" rows="3" cols="45" onkeypress="return forceMaxLength(this, 1024);" onchange="updateDescriptionVariantInAdvancedMode(this, 'default')" >${status.value}</textarea>
			<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
		</spring:bind>	
	</td>
	<openmrs:forEachRecord name="allowedLocale">
		<!-- Deal with the description within each allowed locale -->
		<td class="${record}" style="display:none;">
			<c:set var="isSame" value="yes" />
			<c:set var="sameAsLanguage" value="default" />
			<c:set var="sameAsDisplayLanguage" value="default" />
			<spring:bind path="localizedDescription.unlocalizedValue">
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
			<input type="checkbox" name="same as ${sameAsLanguage}" <c:if test="${isSame == 'yes'}">checked</c:if> onclick="changeSameStatus(this, '${localeValue}')" />
			<!-- use spring message tag here for internalization-->
			<spring:message code="LocalizedDescription.add.sameAs" />&nbsp;
			<c:choose>
				<c:when test="${sameAsLanguage == 'default'}"><spring:message code="LocalizedDescription.add.firstTabName" /></c:when>
				<c:otherwise>${sameAsDisplayLanguage}</c:otherwise>
			</c:choose>
			<br />
			<textarea rows="3" cols="45" onkeypress="return forceMaxLength(this, 1024);" onchange="updateDescriptionVariantInAdvancedMode(this, '${localeValue}')" <c:if test="${isSame == 'yes'}">disabled</c:if> >${descriptionValue}</textarea>
		</td>
	</openmrs:forEachRecord>
</tr>