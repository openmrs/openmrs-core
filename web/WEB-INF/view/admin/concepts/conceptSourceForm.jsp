<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Sources" otherwise="/login.htm" redirect="/admin/concepts/conceptSource.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">
	/*
	 * Clone the element given by the id and put the newly cloned
	 * element right before said id.
	 * 
	 * @param id the string id of the element to clone
	 */
	function cloneElement(id) {
		var elementToClone = document.getElementById(id);
		var clone = elementToClone.cloneNode(true);
		clone.id = "";
		elementToClone.parentNode.insertBefore(clone, elementToClone);
		clone.style.display = "";
	}

	/*
	* Remove the related span for deleted variant name and also delete variant name's value stored in "localizedNameHidden" input
	*/
	function removeParentElement(btn) {
		//delete the related variant name from "localizedNameHidden" input
		var currentLocale = btn.parentNode.getElementsByTagName("input")[0].value;
		if (currentLocale.length != 0) {
			var nameVal = btn.parentNode.getElementsByTagName("input")[1].value;
			removeVariantName(currentLocale, nameVal);
		}
		btn.parentNode.parentNode.removeChild(btn.parentNode);
	}	

	/*
	* Onchange Event function for text input which stores the unlocalized value
	*/
	function updateUnlocalizedValue(obj){
		var newUnlocalizedValue = obj.value;
		var localizedNameValue = document.getElementById("localizedNameHidden").value;
		var pos = localizedNameValue.indexOf("i18n:v1;");
		if (pos == -1)
			document.getElementById("localizedNameHidden").value = escapeDelimiter(newUnlocalizedValue);
		else
			updateName("unlocalized", newUnlocalizedValue);// A hack way to update unlocalized name by method "updateName"
	}	

	/*
	* Onchange Event function for select input which stores the locale of a variant name
	*/
	function updateLocale(obj){
		var selectedLocale = obj.value;
		var currentLocale = obj.parentNode.getElementsByTagName("input")[0].value;
		//check whether there is already one exist name defined in selected locale
		if (validateSelectedLocale(selectedLocale, currentLocale, obj)) {
			if (currentLocale.length == 0){/*only new variant name's current locale is empty*/
				//add a new variant name
				var nameVal = obj.parentNode.getElementsByTagName("input")[1].value;
				addVariantName(selectedLocale, nameVal);
				//update current locale to equal with selectedLocale
				obj.parentNode.getElementsByTagName("input")[0].value = selectedLocale;
			} else {/*update locale for those existed variant name*/
				//just update locale in the existed match variant name(e.g., es:Hello --> en:Hello)
				//this case mostly happen when end-user define a wrong-match variant name at first and correct later
				var fromStr = ";" + currentLocale + ":";
				var toStr = ";" + selectedLocale + ":";
				var reg = new RegExp(fromStr);
				var localizedNameValue = document.getElementById("localizedNameHidden").value;
				document.getElementById("localizedNameHidden").value = localizedNameValue.replace(reg, toStr);
				//update current locale to equal with selectedLocale
				obj.parentNode.getElementsByTagName("input")[0].value = selectedLocale;
			}
		}
	}

	/*
	* Onchange Event function for text input which stores the string value of a variant name
	*/
	function addOrUpdateVariantName(obj){
		var currentLocale = obj.parentNode.getElementsByTagName("input")[0].value;
		if (currentLocale.length == 0) {/*add a new variant name*/
			//this case only happen when end-user firstly to fill in name value not select a locale for creating a variant name
			var selectedLocale = obj.parentNode.getElementsByTagName("select")[0].value; 
			if (validateSelectedLocale(selectedLocale, currentLocale, obj)) {
				addVariantName(selectedLocale, obj.value);
				obj.parentNode.getElementsByTagName("input")[0].value = selectedLocale;
			}
		} else {/*update a existed variant name*/
			updateName(currentLocale, obj.value);
		}
	}

	/*
	* Add a new variant name
	*/
	function addVariantName(loc, value){
		var localizedNameValue = document.getElementById("localizedNameHidden").value;
		if (localizedNameValue.indexOf("i18n:v1;") == -1) /*e.g., Hello --> i18n:v1;unlocalized:Hello;en_UK:Hello;*/
			document.getElementById("localizedNameHidden").value = "i18n:v1;unlocalized:" + localizedNameValue + ";";
		document.getElementById("localizedNameHidden").value += (loc + ":" + escapeDelimiter(value) + ";");
	}

	/*
	* Update unlocalized name(when already added localization) or an existed variant name
	* Here can update unlocalized name is because "unlocalized" also can be consider as a locale name for hacky.
	*/
	function updateName(loc, value){
		var localizedNameValue = document.getElementById("localizedNameHidden").value;
		var pattern = ";" + loc + ":";
		var pos = localizedNameValue.indexOf(pattern);
		if (pos != -1) {
			var prefix = localizedNameValue.substring(0, pos + pattern.length);
			var suffix = "";
			//cut out the sub string behind "pattern"
			var temp = localizedNameValue.substr(pos + pattern.length);
			//search for the next sub string like form ";xx:"
			pattern = ";[^:;\\\\]*:";
			var reg = new RegExp(pattern);
			if (temp.match(reg) == null) {/*cann't find the next sub string*/
				//the passed loc is the locale of last variant name
				document.getElementById("localizedNameHidden").value = prefix + escapeDelimiter(value) + ";";
			} else {
				//cut out the sub string behind the second "pattern"
				pos = temp.match(reg).index;
				suffix = temp.substr(pos);
				document.getElementById("localizedNameHidden").value = prefix + escapeDelimiter(value) + suffix;
			}
		}
	}

	/*
	* Remove an existed variant name
	*/
	function removeVariantName(loc, value){
		var localizedNameValue = document.getElementById("localizedNameHidden").value;
		//pattern will be used in regular expression, so we should use escapeDelimiter two times to escapse ";" to be "\\\\;" 
		var pattern = ";" + loc + ":" + escapeDelimiter(escapeDelimiter(value)) + ";";
		var reg = new RegExp(pattern);
		document.getElementById("localizedNameHidden").value = localizedNameValue.replace(reg, ";");
	}

	/*
	* Check whether selectedLocale of updated/added variant name has already been used by another existed variant name.
	* @param selectedLocale - selected locale of updated/added variant name
	* @param oldLocale - for added variant name, it's "";for updated variant name, it's old locale before changing locale select input
	* @param obj - it can be either select input or text input in one span related to a variant name;it's used to locate error span
	*/
	function validateSelectedLocale(selectedLocale, oldLocale, obj){
		var localizedNameValue = document.getElementById("localizedNameHidden").value;
		var searchText = selectedLocale + ":";
		var errorSpan = obj.parentNode.getElementsByTagName("span")[0];
		if (localizedNameValue.indexOf(searchText) != -1 && selectedLocale != oldLocale) {
			errorSpan.style.display = "";
			return false;
		}
		else {
			errorSpan.style.display = "none";
			return true;
		}
	}

	/*
	* escape ":" or ";" occur in passed text
	*/
	function escapeDelimiter(text) {
		var reg = new RegExp(":", "g");
		text = text.replace(reg, "\\:");
		reg = new RegExp(";", "g");
		text = text.replace(reg, "\\;");
		return text;
	}	


	// following are functions related to localizable description
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

<style>
	#newLocalizedName {
		display: none;
	}
	a.selectedTab {
		background-color: whitesmoke;
	}
	a.tab{
		border-bottom: 1px solid whitesmoke;
		padding-left: 3px;
		padding-right: 3px;
	}
</style>

<h2><spring:message code="ConceptSource.title"/></h2>

<c:if test="${isImplementationId}">
<br/><spring:message code="ConceptSource.isImplementationId"/><br/><br/>
</c:if>

<c:if test="${conceptSource.conceptSourceId == null}">
	<form method="post">
</c:if>

<table>
	<spring:bind path="conceptSource.localizedName">
		<input type="hidden" id="localizedNameHidden" name="${status.expression}" value="${status.value}" />
	</spring:bind>
	<tr>
		<td>
			<spring:message code="general.name"/>
		</td>
		<td>
			<spring:bind path="conceptSource.localizedName.unlocalizedValue">
				<input type="text" value="${status.value}" onchange="updateUnlocalizedValue(this)" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
		<td class="description">
			<spring:message code="ConceptSource.name.help"/>
		</td>
	</tr>
	<tr>	
		<td></td>	
		<td>
			<spring:bind path="conceptSource.localizedName.variants">
				<c:forEach var="entry" items="${status.value}">
					<span>
						<input type="hidden" name="currentLocale" value="${entry.key}" />
						<spring:message code="general.language"/>
						<select onchange="updateLocale(this)">
							<openmrs:forEachRecord name="allowedLocale">
								<option value="${record}" <c:if test="${record == entry.key}">selected</c:if> >
									${record.displayName}
								</option>
							</openmrs:forEachRecord>
						</select>
						<span class="error" style="display:none;"><spring:message code="LocalizedName.locale.duplicate" /></span>
						<spring:message code="LocalizedName.title"/>
						<input type="text" value="${entry.value}" class="smallWidth" onchange="addOrUpdateVariantName(this)" />
						<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" onClick="removeParentElement(this)" />
						<br/>
					</span>
				</c:forEach>
			</spring:bind>
			<span id="newLocalizedName">
				<input type="hidden" name="currentLocale" value="" />
				<spring:message code="general.language"/>
				<select onchange="updateLocale(this)">
					<openmrs:forEachRecord name="allowedLocale">
						<option value="${record}">
							${record.displayName}
						</option>
					</openmrs:forEachRecord>
				</select>
				<span class="error" style="display:none;"><spring:message code="LocalizedName.locale.duplicate" /></span>
				<spring:message code="LocalizedName.title"/>
				<input type="text" value="" class="smallWidth" onchange="addOrUpdateVariantName(this)" />
				<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" onClick="removeParentElement(this)" />
				<br/>
			</span>
			<input type="button" value='<spring:message code="LocalizedName.add"/>' class="smallButton" style="width:90px;" onClick="cloneElement('newLocalizedName')" />
			<br/>
		</td>	
		<td></td>		
	</tr>
	
	<tr>
		<td><spring:message code="ConceptSource.hl7Code"/></td>
		<td>
			<spring:bind path="conceptSource.hl7Code">
				<input type="text" name="hl7Code" value="${status.value}" size="35" maxlength="5" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
		<td class="description"><spring:message code="ConceptSource.hl7Code.help"/></td>
	</tr>
	
	<!-- Copy contents of localizedDescription.jsp into here, in order this page has three tds in tr -->
	<spring:bind path="conceptSource.localizedDescription">
		<input type="hidden" id="localizedDescriptionHidden" name="${status.expression}" value="${status.value}" />
	</spring:bind>
	
	<spring:bind path="conceptSource.localizedDescription.variants">
		<c:set var="variants" value="${status.value}" />
	</spring:bind>
	
	<c:choose>
		<c:when test="${(variants == null) or (variants.size == 0)}">
			<tr id="simpleMode">
				<td valign="top">
					<spring:message code="general.description" />
				</td>
				<td valign="top">
					<spring:bind path="conceptSource.localizedDescription.unlocalizedValue">
						<textarea rows="3" cols="45" onkeypress="return forceMaxLength(this, 1024);" onchange="updateUnlocalizedValueInSimpleMode(this)" >${status.value}</textarea>
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
					<br />
					<input type="button" value='<spring:message code="LocalizedDescription.add"/>' class="smallButton" style="width:90px;" onclick="return switchToAdvancedMode();" />
				</td>
				<td class="description"><spring:message code="ConceptSource.description.help"/></td>
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
		<td class="description"><spring:message code="ConceptSource.description.help"/></td>
	</tr>
	<tr id="advancedModeTRTwo" style="${showAdvancedModeFirst}">
		<td></td>
		<!-- Deal with the unlocalized description -->
		<td class="default">
			<spring:bind path="conceptSource.localizedDescription.unlocalizedValue">
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
				<spring:bind path="conceptSource.localizedDescription.unlocalizedValue">
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
		<td></td>
	</tr>

	<c:if test="${conceptSource.creator != null}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${conceptSource.creator.personName} -
				<openmrs:formatDate date="${conceptSource.dateCreated}" type="long" />
			</td>
			<td class="description"></td>
		</tr>
	</c:if>
</table>
<br />
<c:choose>
	<c:when test="${conceptSource.conceptSourceId == null}">
		<input type="submit" value='<spring:message code="ConceptSource.save"/>'>
		</form>
	</c:when>
	<c:otherwise>
		<spring:message code="ConceptSource.cannotBeEdited"/>
	</c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/template/footer.jsp" %>