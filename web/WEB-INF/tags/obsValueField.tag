<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="conceptId" required="true" %>
<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="initialValue" required="false" %>
<%@ attribute name="rows" required="false" %>
<%@ attribute name="cols" required="false" %>
<%@ attribute name="size" required="false" %>
<%@ attribute name="showUnits" required="false" %>
<%@ attribute name="showFormat" required="false" %>

<openmrs:htmlInclude file="/dwr/interface/DWRConceptService.js" />

<script type="text/javascript">
	
	function validateNumericRange(conceptId, field) {
		var errorTag = $('numericRangeError'+conceptId);
		if (field.value != '') {
			if (isNaN(field.value)) {
				errorTag.innerHTML = '<spring:message code="error.number" />';
				errorTag.className = 'error';
				return false;
			}
			else {
				DWRConceptService.isValidNumericValue(field.value, conceptId, function(validValue) {
					if (validValue == false) {
						errorTag.innerHTML = '<spring:message code="error.numberOutsideRange" />';
						errorTag.className = 'error';
						return false;
					}
				});
			}
		}
		errorTag.className = errorTag.innerHTML = '';
		return true;
	}
</script>

<openmrs:concept conceptId="${conceptId}" var="c" nameVar="n" numericVar="num">

	<c:choose>
		<c:when test="${c.datatype.boolean}">
			<span id="valueBooleanRow${conceptId}" class="obsValue">
				<select name="${formFieldName}" id="valueBooleanField${conceptId}">
					<option value="" <c:if test="${initialValue == null}">selected</c:if>></option>
					<option value="1" <c:if test="${initialValue != null && initialValue != 0}">selected</c:if>><spring:message code="general.true"/></option>
					<option value="0" <c:if test="${initialValue == 0}">selected</c:if>><spring:message code="general.false"/></option>
				</select>
			</span>
		</c:when>
		<c:when test="${c.datatype.text}">
			<span id="valueTextRow${conceptId}" class="obsValue">
				<textarea name="${formFieldName}" id="valueTextField${conceptId}" rows="${empty rows ? 3 : rows}" cols="${empty cols ? 35 : cols}">${initialValue}</textarea>
			</span>
		</c:when>
		<c:when test="${c.datatype.numeric}">
			<span id="valueNumericRow${conceptId}" class="obsValue">
				<input type="text" name="${formFieldName}" id="valueNumericField${conceptId}" value="${initialValue}" size="${empty size ? 10 : size}" onblur="validateNumericRange('${c.conceptId}', this)"/>
				<c:if test="${empty showUnits || showUnits=='true'}"><span id="numericUnits${conceptId}">${num.units}</span></c:if>
				<span id="numericRangeError${c.conceptId}"></span>
			</span>
		</c:when>
		<c:when test="${c.datatype.date}">
			<span id="valueDatetimeRow${conceptId}" class="obsValue">
				<input type="text" name="${formFieldName}" id="valueDatetimeField${conceptId}" value="${initialValue}" size="${empty size ? 10 : size}" onFocus="showCalendar(this)"/>
				<c:if test="${empty showFormat || showFormat=='true'}">(<openmrs:datePattern />)</c:if>
			</span>
		</c:when>
		<c:when test="${c.datatype.coded}">
			<span id="valueCodedRow${conceptId}" class="obsValue">
				<c:choose>
					<c:when test="${!empty c.answers}">
						<openmrs_tag:conceptAnswerField concept="${c}" formFieldName="${formFieldName}" initialValue="${initialValue}" />
					</c:when>
					<c:when test="${empty c.answers && c.set}">
						<openmrs_tag:conceptSetField conceptId="${conceptId}" formFieldName="${formFieldName}" initialValue="${initialValue}" />
					</c:when>
					<c:otherwise>
						<openmrs_tag:conceptField formFieldName="${formFieldName}" initialValue="${initialValue}" />
					</c:otherwise>
				</c:choose>
			</span>
		</c:when>					
		<c:otherwise>
			<span id="unsupportedRow${conceptId}" class="obsValue">Not Yet Supported</span>
		</c:otherwise>
	</c:choose>
</openmrs:concept>