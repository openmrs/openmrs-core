<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Relationship Types" otherwise="/login.htm" redirect="/admin/person/relationshipType.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">

	function confirmPurge() {
		if (confirm("Are you sure you want to purge this object? It will be permanently removed from the system.")) {
			return true;
		} else {
			return false;
		}
	}
	
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
	* Remove the related span for deleted variant aIsToB/bIsToA value and also delete related value stored in hidden "localizedAIsToB/localizedBIsToA" input
	*/
	function removeParentElement(btn, hiddenInputId) {
		//delete the related variant values from "localizedXXXHidden" input
		var currentLocale = btn.parentNode.getElementsByTagName("input")[0].value;
		if (currentLocale.length != 0) {
			var delValue = btn.parentNode.getElementsByTagName("input")[1].value;
			removeVariantValue(currentLocale, delValue, hiddenInputId);
		}
		btn.parentNode.parentNode.removeChild(btn.parentNode);
	}	

	/*
	* Onchange Event function for text input which stores the unlocalized value
	*/
	function updateUnlocalizedValue(obj, hiddenInputId){
		var newUnlocalizedValue = obj.value;
		var localizedValue = document.getElementById(hiddenInputId).value;
		var pos = localizedValue.indexOf("i18n:v1;");
		if (pos == -1)
			document.getElementById(hiddenInputId).value = escapeDelimiter(newUnlocalizedValue);
		else
			updateValue("unlocalized", newUnlocalizedValue);// A hack way to update unlocalized value by method "updateValue"
	}	

	/*
	* Onchange Event function for select input which stores the locale of a variant value
	*/
	function updateLocale(obj, hiddenInputId){
		var selectedLocale = obj.value;
		var currentLocale = obj.parentNode.getElementsByTagName("input")[0].value;
		//check whether there is already one exist value defined in selected locale
		if (validateSelectedLocale(selectedLocale, currentLocale, obj, hiddenInputId)) {
			if (currentLocale.length == 0){/*only new variant value's current locale is empty*/
				//add a new variant value
				var newVal = obj.parentNode.getElementsByTagName("input")[1].value;
				addVariantValue(selectedLocale, newVal, hiddenInputId);
				//update current locale to equal with selectedLocale
				obj.parentNode.getElementsByTagName("input")[0].value = selectedLocale;
			} else {/*update locale for those existed variant value*/
				//just update locale in the existed match variant value(e.g., es:Hello --> en:Hello)
				//this case mostly happen when end-user define a wrong-match variant value at first and correct later
				var fromStr = ";" + currentLocale + ":";
				var toStr = ";" + selectedLocale + ":";
				var reg = new RegExp(fromStr);
				var localizedValue = document.getElementById(hiddenInputId).value;
				document.getElementById(hiddenInputId).value = localizedValue.replace(reg, toStr);
				//update current locale to equal with selectedLocale
				obj.parentNode.getElementsByTagName("input")[0].value = selectedLocale;
			}
		}
	}

	/*
	* Onchange Event function for text input which stores the string value of a variant aIsToB/bIsToA
	*/
	function addOrUpdateVariantValue(obj, hiddenInputId){
		var currentLocale = obj.parentNode.getElementsByTagName("input")[0].value;
		if (currentLocale.length == 0) {/*add a new variant value*/
			//this case only happen when end-user firstly to fill in value not select a locale for creating a variant aIsToB/bIsToA
			var selectedLocale = obj.parentNode.getElementsByTagName("select")[0].value; 
			if (validateSelectedLocale(selectedLocale, currentLocale, obj, hiddenInputId)) {
				addVariantValue(selectedLocale, obj.value, hiddenInputId);
				obj.parentNode.getElementsByTagName("input")[0].value = selectedLocale;
			}
		} else {/*update a existed variant value*/
			updateValue(currentLocale, obj.value, hiddenInputId);
		}
	}

	/*
	* Add a new variant aIsToB/bIsToA
	*/
	function addVariantValue(loc, value, hiddenInputId){
		var localizedValue = document.getElementById(hiddenInputId).value;
		if (localizedValue.indexOf("i18n:v1;") == -1) 
			document.getElementById(hiddenInputId).value = "i18n:v1;unlocalized:" + localizedValue + ";";
		document.getElementById(hiddenInputId).value += (loc + ":" + escapeDelimiter(value) + ";");
	}

	/*
	* Update unlocalized value(when already added localization) or an existed variant value
	* Here can update unlocalized value is because "unlocalized" also can be consider as a locale name for hacky.
	*/
	function updateValue(loc, value, hiddenInputId){
		var localizedValue = document.getElementById(hiddenInputId).value;
		var pattern = ";" + loc + ":";
		var pos = localizedValue.indexOf(pattern);
		if (pos != -1) {
			var prefix = localizedValue.substring(0, pos + pattern.length);
			var suffix = "";
			//cut out the sub string behind "pattern"
			var temp = localizedValue.substr(pos + pattern.length);
			//search for the next sub string like form ";xx:"
			pattern = ";[^:;\\\\]*:";
			var reg = new RegExp(pattern);
			if (temp.match(reg) == null) {/*cann't find the next sub string*/
				//the passed loc is the locale of last variant value
				document.getElementById(hiddenInputId).value = prefix + escapeDelimiter(value) + ";";
			} else {
				//cut out the sub string behind the second "pattern"
				pos = temp.match(reg).index;
				suffix = temp.substr(pos);
				document.getElementById(hiddenInputId).value = prefix + escapeDelimiter(value) + suffix;
			}
		}
	}

	/*
	* Remove an existed variant value(for aIsToB/bIsToA)
	*/
	function removeVariantValue(loc, value, hiddenInputId){
		var localizedValue = document.getElementById(hiddenInputId).value;
		//pattern will be used in regular expression, so we should use escapeDelimiter two times to escapse ";" to be "\\\\;" 
		var pattern = ";" + loc + ":" + escapeDelimiter(escapeDelimiter(value)) + ";";
		var reg = new RegExp(pattern);
		document.getElementById(hiddenInputId).value = localizedValue.replace(reg, ";");
	}

	/*
	* Check whether selectedLocale of updated/added variant aIsToB/bIsToA has already been used by another existed variant aIsToB/bIsToA.
	* @param selectedLocale - selected locale of updated/added variant value
	* @param oldLocale - for added variant value, it's "";for updated variant value, it's old locale before changing locale select input
	* @param obj - it can be either select input or text input in one span related to a variant aIsToB/bIsToA.;it's used to locate error span
	*/
	function validateSelectedLocale(selectedLocale, oldLocale, obj, hiddenInputId){
		var localizedValue = document.getElementById(hiddenInputId).value;
		var searchText = selectedLocale + ":";
		var errorSpan = obj.parentNode.getElementsByTagName("span")[0];
		if (localizedValue.indexOf(searchText) != -1 && selectedLocale != oldLocale) {
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
</script>

<style>
	#newLocalizedAIsToB {
		display: none;
	}
	#newLocalizedBIsToA {
		display: none;
	}
</style>

<h2><spring:message code="RelationshipType.title"/></h2>

<form method="post">
<fieldset>
<table>
	<!-- Html Code for localizedAIsToB(begin) -->
	<spring:bind path="relationshipType.localizedAIsToB">
		<input type="hidden" id="localizedAIsToBHidden" name="${status.expression}" value="${status.value}" />
	</spring:bind>
	<tr>
		<td>
			<spring:message code="RelationshipType.aIsToB"/>
		</td>
		<td>
			<spring:bind path="relationshipType.localizedAIsToB.unlocalizedValue">
				<input type="text" value="${status.value}" onchange="updateUnlocalizedValue(this, 'localizedAIsToBHidden')" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>	
		<td></td>	
		<td>
			<spring:bind path="relationshipType.localizedAIsToB.variants">
				<c:forEach var="entry" items="${status.value}">
					<span>
						<input type="hidden" name="currentLocale" value="${entry.key}" />
						<spring:message code="general.language"/>
						<select onchange="updateLocale(this, 'localizedAIsToBHidden')">
							<openmrs:forEachRecord name="allowedLocale">
								<option value="${record}" <c:if test="${record == entry.key}">selected</c:if> >
									${record.displayName}
								</option>
							</openmrs:forEachRecord>
						</select>
						<span class="error" style="display:none;"><spring:message code="LocalizedString.locale.duplicate" /></span>
						<spring:message code="LocalizedString.title"/>
						<input type="text" value="${entry.value}" class="smallWidth" onchange="addOrUpdateVariantValue(this, 'localizedAIsToBHidden')" />
						<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" onClick="removeParentElement(this, 'localizedAIsToBHidden')" />
						<br/>
					</span>
				</c:forEach>
			</spring:bind>
			<span id="newLocalizedAIsToB">
				<input type="hidden" name="currentLocale" value="" />
				<spring:message code="general.language"/>
				<select onchange="updateLocale(this, 'localizedAIsToBHidden')">
					<openmrs:forEachRecord name="allowedLocale">
						<option value="${record}">
							${record.displayName}
						</option>
					</openmrs:forEachRecord>
				</select>
				<span class="error" style="display:none;"><spring:message code="LocalizedString.locale.duplicate" /></span>
				<spring:message code="LocalizedString.title"/>
				<input type="text" value="" class="smallWidth" onchange="addOrUpdateVariantValue(this, 'localizedAIsToBHidden')" />
				<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" onClick="removeParentElement(this, 'localizedAIsToBHidden')" />
				<br/>
			</span>
			<input type="button" value='<spring:message code="LocalizedString.add"/>' class="smallButton" style="width:90px;" onClick="cloneElement('newLocalizedAIsToB')" />
			<br/>
		</td>		
	</tr>
	<!-- Html Code for localizedAIsToB (end) -->
	
	<!-- Html Code for localizedBIsToA(begin) -->
	<spring:bind path="relationshipType.localizedBIsToA">
		<input type="hidden" id="localizedBIsToAHidden" name="${status.expression}" value="${status.value}" />
	</spring:bind>
	<tr>
		<td>
			<spring:message code="RelationshipType.bIsToA"/>
		</td>
		<td>
			<spring:bind path="relationshipType.localizedBIsToA.unlocalizedValue">
				<input type="text" value="${status.value}" onchange="updateUnlocalizedValue(this, 'localizedBIsToAHidden')" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>	
		<td></td>	
		<td>
			<spring:bind path="relationshipType.localizedBIsToA.variants">
				<c:forEach var="entry" items="${status.value}">
					<span>
						<input type="hidden" name="currentLocale" value="${entry.key}" />
						<spring:message code="general.language"/>
						<select onchange="updateLocale(this, 'localizedBIsToAHidden')">
							<openmrs:forEachRecord name="allowedLocale">
								<option value="${record}" <c:if test="${record == entry.key}">selected</c:if> >
									${record.displayName}
								</option>
							</openmrs:forEachRecord>
						</select>
						<span class="error" style="display:none;"><spring:message code="LocalizedString.locale.duplicate" /></span>
						<spring:message code="LocalizedString.title"/>
						<input type="text" value="${entry.value}" class="smallWidth" onchange="addOrUpdateVariantValue(this, 'localizedBIsToAHidden')" />
						<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" onClick="removeParentElement(this, 'localizedBIsToAHidden')" />
						<br/>
					</span>
				</c:forEach>
			</spring:bind>
			<span id="newLocalizedBIsToA">
				<input type="hidden" name="currentLocale" value="" />
				<spring:message code="general.language"/>
				<select onchange="updateLocale(this, 'localizedBIsToAHidden')">
					<openmrs:forEachRecord name="allowedLocale">
						<option value="${record}">
							${record.displayName}
						</option>
					</openmrs:forEachRecord>
				</select>
				<span class="error" style="display:none;"><spring:message code="LocalizedString.locale.duplicate" /></span>
				<spring:message code="LocalizedString.title"/>
				<input type="text" value="" class="smallWidth" onchange="addOrUpdateVariantValue(this, 'localizedBIsToAHidden')" />
				<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" onClick="removeParentElement(this, 'localizedBIsToAHidden')" />
				<br/>
			</span>
			<input type="button" value='<spring:message code="LocalizedString.add"/>' class="smallButton" style="width:90px;" onClick="cloneElement('newLocalizedBIsToA')" />
			<br/>
		</td>		
	</tr>
	<!-- Html Code for localizedBIsToA (end) -->
	
	<spring:nestedPath path="relationshipType">
		<openmrs:portlet url="localizedDescription" id="localizedDescriptionLayout" /> 
	</spring:nestedPath>
	
	<c:if test="${relationshipType.creator != null}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${relationshipType.creator.personName} -
				<openmrs:formatDate date="${relationshipType.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
</table>
<br />
<input type="submit" value="<spring:message code="RelationshipType.save"/>" name="save">
</fieldset>
</form>

<br/>

<c:if test="${not relationshipType.retired && not empty relationshipType.relationshipTypeId}">
	<form method="post">
		<fieldset>
			<h4><spring:message code="RelationshipType.retireRelationshipType"/></h4>
			
			<b><spring:message code="general.reason"/></b>
			<input type="text" value="" size="40" name="retireReason" />
			<spring:hasBindErrors name="relationshipType">
				<c:forEach items="${errors.allErrors}" var="error">
					<c:if test="${error.code == 'retireReason'}"><span class="error"><spring:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span></c:if>
				</c:forEach>
			</spring:hasBindErrors>
			<br/>
			<input type="submit" value='<spring:message code="RelationshipType.retireRelationshipType"/>' name="retire"/>
		</fieldset>
	</form>
</c:if>

<br/>

<c:if test="${not empty relationshipType.relationshipTypeId}">
	<openmrs:hasPrivilege privilege="Purge Relationship Types">
		<form id="purge" method="post" onsubmit="return confirmPurge()">
			<fieldset>
				<h4><spring:message code="RelationshipType.purgeRelationshipType"/></h4>
				<input type="submit" value='<spring:message code="RelationshipType.purgeRelationshipType"/>' name="purge" />
			</fieldset>
		</form>
	</openmrs:hasPrivilege>
</c:if>

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>