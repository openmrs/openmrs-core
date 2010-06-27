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
</script>

<style>
	#newLocalizedName {
		display: none;
	}
</style>


<h2><spring:message code="LocationTag.manage"/></h2>

<a class="toggleAddLocationTag" href="#"><spring:message code="LocationTag.add"/></a>
<div id="addLocationTag" style="border: 1px black solid; background-color: #e0e0e0; display: none">
	<form method="post" action="locationTagAdd.form">
		<input type="hidden" id="localizedNameHidden" name="localizedName" value="" />
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
			
			<tr>
				<th><spring:message code="LocationTag.description"/></th>
				<td><textarea name="description" rows="3" cols="72"></textarea></td>
			</tr>
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
