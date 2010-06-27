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
</script>

<style>
	#newLocalizedName {
		display: none;
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
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td>
			<spring:bind path="conceptSource.description">
				<textarea name="description" rows="3" cols="40">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
		<td class="description"><spring:message code="ConceptSource.description.help"/></td>
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