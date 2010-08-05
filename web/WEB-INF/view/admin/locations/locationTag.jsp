<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Location Tags" otherwise="/login.htm" redirect="/admin/locations/locationTag.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">
	$j(document).ready(function() {
		$j('.toggleAddLocationTag').click(function(event) {
			$j('#addLocationTag').slideToggle('fast');
			event.preventDefault();
		});
	});
</script>

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
			document.getElementById("localizedNameHidden").value = escapeDelimter(newUnlocalizedValue);
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
		document.getElementById("localizedNameHidden").value += (loc + ":" + escapeDelimter(value) + ";");
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
				document.getElementById("localizedNameHidden").value = prefix + escapeDelimter(value) + ";";
			} else {
				//cut out the sub string behind the second "pattern"
				pos = temp.match(reg).index;
				suffix = temp.substr(pos);
				document.getElementById("localizedNameHidden").value = prefix + escapeDelimter(value) + suffix;
			}
		}
	}

	/*
	* Remove an existed variant name
	*/
	function removeVariantName(loc, value){
		var localizedNameValue = document.getElementById("localizedNameHidden").value;
		//pattern will be used in regular expression, so we should use escapeDelimter two times to escapse ";" to be "\\\\;" 
		var pattern = ";" + loc + ":" + escapeDelimter(escapeDelimter(value)) + ";";
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
	function escapeDelimter(text) {
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
		textareaOfDefault.value = escapeDelimter(obj.value);
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
				document.getElementById('localizedDescriptionHidden').value = escapeDelimter(obj.value);
			else
				updateDescription('unlocalized', obj.value);
		} else {
			var pattern = ";" + lang + ":";
			if (currentValue.indexOf('i18n:v1;') == -1)
				document.getElementById('localizedDescriptionHidden').value = "i18n:v1;unlocalized:" + currentValue + ";" + lang + ":" + escapeDelimter(obj.value) + ";";
			else if (currentValue.indexOf(pattern) == -1)
				document.getElementById('localizedDescriptionHidden').value = currentValue + lang + ":" + escapeDelimter(obj.value) + ";";
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
				document.getElementById("localizedDescriptionHidden").value = prefix + escapeDelimter(value) + ";";
			} else {
				//cut out the sub string behind the second "pattern"
				pos = temp.match(reg).index;
				suffix = temp.substr(pos);
				document.getElementById("localizedDescriptionHidden").value = prefix + escapeDelimter(value) + suffix;
			}
		}
	}

	/*
	* Remove an existed variant description
	*/
	function removeVariantdescription(loc, value){
		var localizedDescriptionValue = document.getElementById("localizedDescriptionHidden").value;
		//pattern will be used in regular expression, so we should use escapeDelimter two times to escapse ";" to be "\\\\;" 
		var pattern = ";" + loc + ":" + escapeDelimter(escapeDelimter(value)) + ";";
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


<h2><spring:message code="LocationTag.manage"/></h2>

<a class="toggleAddLocationTag" href="#"><spring:message code="LocationTag.add"/></a>
<div id="addLocationTag" style="border: 1px black solid; background-color: #e0e0e0; display: none">
	<form method="post" action="locationTagAdd.form">
		<input type="hidden" id="localizedNameHidden" name="localizedName" value="" />
		<input type="hidden" id="localizedDescriptionHidden" name="localizedDescription" value="" />
		<table>
			<!-- localizedName related html code (begin)-->
			<tr>
				<th><spring:message code="LocationTag.name"/></th>
				<td>
					<input type="text" value="" onchange="updateUnlocalizedValue(this)" />
					<span class="required">*</span>
				</td>
			</tr>
			<tr>	
				<td></td>	
				<td>
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
			</tr>
			<!-- localizedName related html code (end)-->
			
			<!-- localizedDescription related html code (begin)-->
			<tr id="simpleMode">
				<th valign="top"><spring:message code="LocationTag.description"/></th>
				<td>
					<textarea rows="3" cols="40" onkeypress="return forceMaxLength(this, 1024);" onchange="updateUnlocalizedValueInSimpleMode(this)" ></textarea>
				</td>
				<td valign="top">
					<input type="button" style="float:right;" value='<spring:message code="LocalizedDescription.add"/>' class="smallButton" style="width:90px;" onclick="return switchToAdvancedMode();" />
				</td>
			</tr>			
			<tr id="advancedModeTROne" style="display:none;">
				<td valign="top">
					<spring:message code="LocationTag.description"/>
				</td>
				<td>
					<a id="defaultTab" class="tab selectedTab default" href="#selectDefault" onclick="return selectTab(this)"><spring:message code="LocalizedDescription.add.firstTabName" /></a>&nbsp;&nbsp;
					<openmrs:forEachRecord name="allowedLocale">
						<a id="${record}Tab" class="tab ${record}" href="#select${record.displayName}" onclick="return selectTab(this)">${record.displayName}</a>&nbsp;&nbsp;
					</openmrs:forEachRecord>
				</td>
			</tr>
			<tr id="advancedModeTRTwo" style="display:none;">
				<td></td>
				<!-- Deal with the unlocalized description -->
				<td class="default">
					<textarea id="textarea_default" rows="3" cols="40" onkeypress="return forceMaxLength(this, 1024);" onchange="updateDescriptionVariantInAdvancedMode(this, 'default')" ></textarea>
				</td>
				<openmrs:forEachRecord name="allowedLocale">
					<!-- Deal with the description within each allowed locale -->
					<td class="${record}" style="display:none;">
						<c:set var="isSame" value="yes" />
						<c:set var="sameAsLanguage" value="default" />
						<c:set var="sameAsDisplayLanguage" value="default" />
						<c:set var="descriptionValue" value="" />
						<c:if test="${!(record.country == null or record.country == '')}">
							<c:set var="languageName" value="${record.language}" />
							<openmrs:forEachRecord name="allowedLocale">
								<c:if test="${(record.country == null or record.country == '') and (record.language == languageName)}">
									<c:set var="sameAsLanguage" value="${languageName}" />
									<c:set var="sameAsDisplayLanguage" value="${record.displayLanguage}" />
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
						<textarea rows="3" cols="40" onkeypress="return forceMaxLength(this, 1024);" onchange="updateDescriptionVariantInAdvancedMode(this, '${localeValue}')" <c:if test="${isSame == 'yes'}">disabled</c:if> >${descriptionValue}</textarea>
					</td>
				</openmrs:forEachRecord>
			</tr>
			<!-- localizedDescription related html code (end)-->
			<tr>
				<th></th>
				<td>
					<input type="submit" value="<spring:message code="general.save"/>" />
					<input type="button" value="<spring:message code="general.cancel"/>" class="toggleAddLocationTag" />
				</td>
			</tr>
		</table>
	</form>
</div>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationTagList.afterAdd" type="html" />

<br />
<br />
<b class="boxHeader"><spring:message code="LocationTag.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> <spring:message code="general.name" /> </th>
			<th> <spring:message code="general.description" /> </th>
			<th> <spring:message code="general.creator" /> </th>
			<th> <spring:message code="general.dateCreated" /> </th>
		</tr>
		<c:forEach var="locationTag" items="${locationTags}">
			<tr <c:if test="${locationTag.retired == true}"> class="retired" </c:if> >
				<td valign="top">
					<a href="locationTagEdit.form?locationTagId=${locationTag.locationTagId}">
						${locationTag.name}
					</a>
				</td>
				<td valign="top">
					${locationTag.description}
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
			<tr><td colspan="4"><spring:message code="general.none"/></td></tr>
		</c:if>
	</table>
	<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationTagList.inForm" type="html" />
	
</form>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationTagList.footer" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>
