<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Location Tags" otherwise="/login.htm" redirect="/admin/locations/locationTag.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
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

<h2><spring:message code="LocationTag.edit.title"/></h2>

<c:if test="${locationTag.retired}">
	<form method="post" action="locationTagUnretire.form">
		<input type="hidden" name="id" value="${locationTag.id}"/>
		<div class="retiredMessage">
			<div>
				<spring:message code="general.retiredBy"/>
				${locationTag.retiredBy.personName}
				<openmrs:formatDate date="${locationTag.dateRetired}" type="medium" />
				-
				${locationTag.retireReason}
				<input type="submit" value='<spring:message code="general.unretire"/>'/>
			</div>
		</div>
	</form>
</c:if>

<div class="boxHeader">
	<spring:message code="general.properties"/>
</div>
<div class="box">
	<form:form modelAttribute="locationTag">
		<table>
			<!-- localizedName related html code (begin)-->
			<spring:bind path="localizedName">
				<input type="hidden" id="localizedNameHidden" name="${status.expression}" value="${status.value}" />
			</spring:bind>
			<tr>
				<td>
					<span class="required">*</span>
					<spring:message code="LocationTag.name"/>
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
			<!-- localizedName related html code (end)-->
			
			<tr>
				<td>
					<spring:message code="LocationTag.description"/>
				</td>
				<td>
					<form:textarea path="description" rows="3" cols="72"/> <form:errors path="description" cssClass="error"/>
				</td>
			</tr>
			<tr>
				<td><spring:message code="general.createdBy"/></td>
				<td>
					<openmrs:format user="${locationTag.creator}"/>
					<openmrs:formatDate date="${locationTag.dateCreated}"/>
				</td>
			</tr>
			<tr>
				<td></td>
				<td>
					<input type="submit" value="<spring:message code="general.save"/>" />
					<input type="button" value="<spring:message code="general.cancel"/>" onClick="window.location = 'locationTag.list'"/>
				</td>
			</tr>
		</table>
	</form:form>
</div>

<br/>
<div class="boxHeader">
	<spring:message code="LocationTag.purgeRetire"/>
</div>
<div class="box">
	<%-- Purge --%>
	<c:choose>
		<c:when test="${empty locations}">
			<form method="post" action="locationTagPurge.form">
				<input type="hidden" name="id" value="${locationTag.id}"/>
				<spring:message code="LocationTag.purge.allowed"/>:
				<input type="submit" value="<spring:message code="general.purge"/>"/>
			</form>
		</c:when>
		<c:otherwise>
			<spring:message code="LocationTag.cannot.purge.in.use"/>
		</c:otherwise>
	</c:choose>
	
	<%-- Retire --%>
	<c:if test="${not locationTag.retired && not empty locationTag.id}">
		<br/>
		<form method="post" action="locationTagRetire.form">
			<input type="hidden" name="id" value="${locationTag.id}"/>
			
			<b><spring:message code="general.retire"/></b>
			<br/>
			<spring:message code="general.reason"/>:
			<input type="text" name="retireReason" size="40"/>
			<input type="submit" value='<spring:message code="general.retire"/>'/>
		</form>
	</c:if>
</div>



<c:if test="${not empty locations}">
	<br/>
	<div class="boxHeader">
		<spring:message code="LocationTag.locationsWithTag"/>
	</div>
	<div class="box">
		<ul>
			<c:forEach var="l" items="${locations}">
				<li><openmrs:format location="${l}"/></li>
			</c:forEach>
		</ul>
	</div>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>
