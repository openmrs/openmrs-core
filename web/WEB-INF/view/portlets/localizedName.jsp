<%@ include file="/WEB-INF/template/include.jsp" %>

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
		var pos = localizedNameValue.indexOf("\^v1\^");
		if (pos == -1)
			document.getElementById("localizedNameHidden").value = newUnlocalizedValue;
		else
			document.getElementById("localizedNameHidden").value = newUnlocalizedValue + localizedNameValue.substr(pos);
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
				var fromStr = currentLocale + ":";
				var toStr = selectedLocale + ":";
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
			updateVariantName(currentLocale, obj.value);
		}
	}

	/*
	* Add a new variant name
	*/
	function addVariantName(loc, value){
		var localizedNameValue = document.getElementById("localizedNameHidden").value;
		if (localizedNameValue.indexOf("\^v1\^") == -1)/*e.g., Hello --> Hello^v1^en_UK:Hello*/
			document.getElementById("localizedNameHidden").value = localizedNameValue + "^v1^" + loc + ":" + value;
		else/*e.g., Hello^v1^en_UK:Hello --> Hello^v1^en_UK:Hello;es:Hola*/
			document.getElementById("localizedNameHidden").value = localizedNameValue + ";" + loc + ":" + value;
	}

	/*
	* Update an existed variant name
	*/
	function updateVariantName(loc, value){
		var localizedNameValue = document.getElementById("localizedNameHidden").value;
		var searchText = loc + ":";
		var start = localizedNameValue.indexOf(searchText);
		var end = localizedNameValue.indexOf(";", start);
		var fromStr = "";
		if (end != -1)
			fromStr = localizedNameValue.substring(start, end);
		else
			fromStr = localizedNameValue.substr(start);
		var toStr = loc + ":" + value;
		var reg = new RegExp(fromStr);
		document.getElementById("localizedNameHidden").value = localizedNameValue.replace(reg, toStr);
	}

	/*
	* Remove an existed variant name
	*/
	function removeVariantName(loc, value){
		var localizedNameValue = document.getElementById("localizedNameHidden").value;
		var deletedValue = loc + ":" + value + ";";;
		var reg;
		if (localizedNameValue.indexOf(deletedValue) != -1){
			reg = new RegExp(deletedValue);
			document.getElementById("localizedNameHidden").value = localizedNameValue.replace(reg, "");
			return;
		}

		deletedValue = ";" + loc + ":" + value;//may be the match variant name is in the end of "localizedNameValue"
		if (localizedNameValue.indexOf(deletedValue) != -1) {
			reg = new RegExp(deletedValue);
			document.getElementById("localizedNameHidden").value = localizedNameValue.replace(reg, "");
			return;
		} 

		deletedValue = "\^v1\^" + loc + ":" + value;//may be the match variant name is only variant name.
		var pos = localizedNameValue.indexOf(deletedValue);
		if (pos != -1) {
			document.getElementById("localizedNameHidden").value = localizedNameValue.substring(0, pos);
		} 
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
	
</script>

<style>
	#newLocalizedName {
		display: none;
	}
</style>

<spring:bind path="localizedName">
	<input type="hidden" id="localizedNameHidden" name="${status.expression}" value="${status.value}" />
</spring:bind>
<tr>
	<td>
		<spring:message code="general.name"/>
	</td>
	<td>
		<spring:bind path="localizedName.unlocalizedValue">
			<input type="text" value="${status.value}" onchange="updateUnlocalizedValue(this)" />
			<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
		</spring:bind>
	</td>
</tr>
<tr>	
	<td></td>	
	<td>
		<spring:bind path="localizedName.variants">
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
</tr>