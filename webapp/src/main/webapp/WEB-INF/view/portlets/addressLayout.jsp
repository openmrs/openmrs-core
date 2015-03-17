<%@ include file="/WEB-INF/template/include.jsp" %>

<script type="text/javascript">

    // The originally defined onSubmit function
    // We replace this with "return false;" until all errors are fixed
    var origOnSubmit = null;

    // to know if we have overwritten the onsubmit method already
    var overwrittenOnSubmit = false;
    
    //don't create the array variable again if it is already on the page, this is important on the 
    //long patient form where we might have multiple addresses and end up losing the mappings for earlier addresses
    if(!endDateIdValueMap){
    	//Array used to store mapping values of the endDate fields to their original values
        //the keys are the endDate input Ids while the values are the corresponding dates
    	var endDateIdValueMap = [];
    }
    
    /**
     * Validate the input format according to the regular expression.
     * If not valid, the background is highlighted and a formatting Hint is displayed.
     *
     * @param obj the input dom object
     * @param regex regular expression defined in the localized AddressTemplate in openmrs-servlet.xml
     * @param codeName the token.codeName (e.g.: "latitude")
     */
    function validateFormat(obj, regex, codeName) {
        var formatMsg = "formatMsg_" + codeName;
        var resultArray = obj.value.match(regex);
        var tips = document.getElementsByName(formatMsg);
        if (resultArray || obj.value == null || obj.value == "") {
            obj.style.background="";
            for (var i=0; i<tips.length; i++) {
                tips[i].style.display = "none";
            }
            if (overwrittenOnSubmit) {
            	// replace the parent form's onsubmit with the one
            	// we saved because we put in a temporary "return false" in the onsubmit
            	obj.form.onsubmit = origOnSubmit;
            	origOnSubmit = null;
        		overwrittenOnSubmit = false;
            }
        }
        else {
            obj.style.background="yellow";
            for (var i=0; i<tips.length; i++) {
                tips[i].style.display = "";
            }

            if (!overwrittenOnSubmit) {
        		// this is the first time there was an error, save the current
        		// onSubmit for the form and replace it with a popup error msg
        		origOnSubmit = obj.form.onsubmit;
        		obj.form.onsubmit = function() { alert('<openmrs:message code="fix.error.plain" javaScriptEscape="true"/>'); return false; };
        		overwrittenOnSubmit = true;
        	}
        }
        
    }
    
    function updateEndDate(checkboxObj, endDateInputId){
    	var endDateInputObj = document.getElementById(endDateInputId);
    	if(endDateInputObj && checkboxObj && $j(checkboxObj).attr('checked')){
    		//store the original current value so that if the user unchecks the 
    		//active box the first time after page load, we can restore the value
    		$j(endDateInputObj).val('');
    		endDateInputObj.disabled = 'disabled';
		}else{
			$j(endDateInputObj).removeAttr("disabled");
			if(endDateInputObj && endDateIdValueMap[endDateInputId]){
				//restore the original value if there was one on page load
				$j(endDateInputObj).val(endDateIdValueMap[endDateInputId]);
			}else{
				//this is the first time this address is being inactivated, set the endDate to current date
				$j(endDateInputObj).val(parseDateFromJsToString(jsDateFormat, new Date()));
			}
		}
    }
    
    function updateActiveCheckbox(endDateInputId, isActive){
    	var endDateInputObj = document.getElementById(endDateInputId);
        if(endDateInputObj){
    		var inputTags = endDateInputObj.parentNode.parentNode.parentNode.getElementsByTagName("input");
    		//find the active checkbox and uncheck it
    		for(var i in inputTags){
    			if(inputTags[i] && inputTags[i].name == 'activeCheckbox'){
    				$j(inputTags[i].parentNode.parentNode).show();
    				if(isActive == false){
    					inputTags[i].checked = false;
    					endDateIdValueMap[endDateInputId] = $j.trim($j(endDateInputObj).val());
    				}
    				inputTags[i].onclick = function(){
						updateEndDate(this, endDateInputId);
					};
    			}
    		}
        }
    }
</script>

<c:if test="${model.authenticatedUser != null}">
	<c:choose>
		<c:when test="${model.size == 'columnHeaders'}">
			<c:if test="${model.layoutShowExtended == 'true'}">
				<th></th>
			</c:if>
			
			<c:forEach items="${model.layoutTemplate.lines}" var="line">
				<c:forEach items="${line}" var="token">
					<c:if test="${token.isToken == model.layoutTemplate.layoutToken}">
						<th><openmrs:message code="${token.displayText}"/></th>
					</c:if>
				</c:forEach>
			</c:forEach>	
		</c:when>

		<c:when test="${model.size == 'inOneRow'}">
			<tr>
				<c:if test="${model.layoutShowExtended == 'true'}">
					<td>
						<spring:bind path="preferred">
							<c:if test="${status.value}">*</c:if>
						</spring:bind>
					</td>
				</c:if>
				<c:forEach items="${model.layoutTemplate.lines}" var="line">
					<c:forEach items="${line}" var="token">
						<c:if test="${token.isToken == model.layoutTemplate.layoutToken}">
							<td>
								<spring:bind path="${token.codeName}">
									<c:out value="${status.value}"/>
								</spring:bind>
							</td>
						</c:if>
					</c:forEach>
				</c:forEach>	
			</tr>
		</c:when>
		<c:when test="${model.size == 'compact'}">
			<div id="${model.portletDivName}">
				<table>
					<c:forEach items="${model.layoutTemplate.lines}" var="line">
						<tr>
							<td>
								<c:forEach items="${line}" var="token">
									<c:if test="${token.isToken == model.layoutTemplate.layoutToken}">
										<spring:bind path="${token.codeName}">
											<c:out value="${status.value}"/>
										</spring:bind>
									</c:if>
									<c:if test="${token.isToken == model.layoutTemplate.nonLayoutToken}">
										${token.displayText}
									</c:if>
								</c:forEach>
							</td>
						</tr>
					</c:forEach>
				</table>
			</div>
		</c:when>
		<c:when test="${model.size == 'full'}">
			<c:choose>
				<c:when test="${model.layoutMode == 'view'}">
					<div id="${model.portletDivName}">
						<table>
							<c:forEach items="${model.layoutTemplate.lines}" var="line">
								<tr>
									<td>
										<c:forEach items="${line}" var="token">
											<c:if test="${token.isToken == model.layoutTemplate.layoutToken}">
												<openmrs:message code="${token.displayText}" />:
												<spring:bind path="${token.codeName}">
													<c:out value="${status.value}"/>
												</spring:bind>
											</c:if>
											<c:if test="${token.isToken == model.layoutTemplate.nonLayoutToken}">
												${token.displayText}
											</c:if>
										</c:forEach>
									</td>
								</tr>
							</c:forEach>
						</table>
					</div>
				</c:when>
				<c:otherwise>
					<c:if test="${model.layoutShowTable != 'false'}">
						<div id="${model.portletDivName}">
							<table>
					</c:if>
					<c:if test="${model.layoutShowExtended == 'true'}">
							<tr>
								<td><openmrs:message code="general.preferred"/></td>
								<td>
									<spring:bind path="preferred">
										<input type="hidden" name="_${status.expression}">
										<input type="checkbox" name="${status.expression}" onclick="if (preferredBoxClick) preferredBoxClick(this)" value="true" alt="personAddress" <c:if test="${status.value == true}">checked</c:if> />
										<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
									</spring:bind>
								</td>
							</tr>
					</c:if>
					<tr style="display:none">
						<td><openmrs:message code="PersonAddress.isActive" /></td>
						<td>
							<input name="activeCheckbox" type="checkbox" checked="checked"/>
						</td>
					</tr>
					<c:forEach items="${model.layoutTemplate.lines}" var="line">
						<tr>
							<c:forEach items="${line}" var="token" varStatus="tokenStatus">
								<c:if test="${token.isToken == model.layoutTemplate.layoutToken}">
									<td id="${token.codeName}_label"><openmrs:message code="${token.displayText}" /></td>
									<td <c:if test="${tokenStatus.last && tokenStatus.index < model.layoutTemplate.maxTokens}">colspan="${model.layoutTemplate.maxTokens - tokenStatus.index}"</c:if>>
									<c:catch var="exp">
										<spring:bind path="${token.codeName}">
											<c:if test="${token.codeName == 'endDate'}"><input type="hidden" name="_${status.expression}"></c:if>
											<c:set var="elementValue" value="${status.value}" />
											<c:if test="${model.isNew && empty elementValue && not empty model.layoutTemplate.elementDefaults && not empty model.layoutTemplate.elementDefaults[token.codeName]}">
												<c:set var="elementValue" value="${model.layoutTemplate.elementDefaults[token.codeName]}" />
											</c:if>
											<input id="${status.expression}" type="text" name="${status.expression}" value="${elementValue}" size="${token.displaySize}" 
                                            	<c:if test="${token.codeName == 'startDate' || token.codeName == 'endDate'}">onfocus='showCalendar(this,60)'</c:if> 
                                            	<c:if test="${token.codeName == 'endDate' && status.value == ''}">disabled="disabled" </c:if>
                                                onkeyup="<c:if test='${model.layoutTemplate.elementRegex[token.codeName] !="" }'>validateFormat(this, '${model.layoutTemplate.elementRegex[token.codeName]}','${token.codeName}' )</c:if>"
                                            />
                                            <c:remove var="elementValue" scope="page" />
                                            <c:if test="${token.codeName == 'endDate'}">
                                            <script type="text/javascript">updateActiveCheckbox('${status.expression}', ${status.value == ''});</script>
                                            </c:if>
                                           <i name="formatMsg_${token.codeName}" style="font-weight: normal; font-size: xx-small; color: red; display: none">
                                                 <c:choose>
                                                     <c:when test="${model.layoutTemplate.elementRegexFormats[token.codeName] != null }" >
                                                        (<openmrs:message code="general.format" />: ${model.layoutTemplate.elementRegexFormats[token.codeName]})
                                                    </c:when>
                                                    <c:otherwise>
                                                        <openmrs:message code="general.invalid" />&nbsp;<openmrs:message code="general.format" />
                                                    </c:otherwise>
                                                </c:choose>
                                            </i>
											<c:if test="${model.layoutShowErrors != 'false'}">
												<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
											</c:if>
										</spring:bind>
									  </c:catch>
									  <c:if test="${not empty exp}">
									  	<%--hide the label for the token's field since we are not displaying the input for this missing property --%>
									  	<script>$j("#${token.codeName}_label").hide()</script>
									  	<c:if test="${model.layoutShowErrors == true}">
											<span class="error">${exp.message}</span>
									  	</c:if>
									  </c:if>
									</td>
								</c:if>
								<c:if test="${token.isToken == model.layoutTemplate.nonLayoutToken}">
									<td>${token.displayText}</td>
								</c:if>
							</c:forEach>
						</tr>
					</c:forEach>
					<c:if test="${model.layoutShowExtended == 'true'}">
							<spring:bind path="creator">
								<c:if test="${!(status.value == null)}">
									<tr>
										<td><openmrs:message code="general.createdBy" /></td>
										<td colspan="4">
											<c:out value="${status.value.personName}" /> -
											<openmrs:formatDate path="dateCreated" type="long" />
										</td>
									</tr>
								</c:if>
							</spring:bind>
							<spring:bind path="changedBy">
								<c:if test="${!(status.value == null)}">
									<tr>
										<td><openmrs:message code="general.changedBy" /></td>
										<td colspan="4">
											<c:out value="${status.value.personName}" /> -
											<openmrs:formatDate path="dateChanged" type="long" />
										</td>
									</tr>
								</c:if>
							</spring:bind>
                            <c:if test="${model.layoutHideVoidOption != 'true'}">
								<tr>
									<td><openmrs:message code="general.voided"/></td>
									<td>
										<spring:bind path="voided">
											<input type="hidden" name="_${status.expression}"/>
											<input type="checkbox" name="${status.expression}" 
												   <c:if test="${status.value == true}">checked="checked"</c:if> 
												   onClick="toggleLayer('<spring:bind path="personAddressId">voidReasonAddressRow-${status.value}</spring:bind>'); if (voidedBoxClicked) voidedBoxClicked(this); "
											/>
										</spring:bind>
									</td>
								</tr>
								<tr id="<spring:bind path="personAddressId">voidReasonAddressRow-${status.value}</spring:bind>"
									style="<spring:bind path="voided"><c:if test="${status.value == false}">display: none;</c:if></spring:bind>">
									<td><openmrs:message code="general.voidReason"/></td>
									<spring:bind path="voidReason">
										<td colspan="4">
											<input type="text" name="${status.expression}" value="${status.value}" size="43" />
											<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
										</td>
									</spring:bind>
								</tr>
								<spring:bind path="voidedBy">
									<c:if test="${!(status.value == null)}">
										<tr>
											<td><openmrs:message code="general.voidedBy" /></td>
											<td colspan="4">
												<c:out value="${status.value.personName}" /> -
												<openmrs:formatDate path="dateVoided" type="long" />
											</td>
										</tr>
									</c:if>
								</spring:bind>
                            </c:if>
					</c:if>
					<c:if test="${model.layoutShowTable != 'false'}">
							</table>
						</div>
					</c:if>
				</c:otherwise>
			</c:choose>
		</c:when>
		<c:otherwise>
			<openmrs:message code="Portlet.addressLayout.error" arguments="${model.size}"/>
		</c:otherwise>
	</c:choose>
</c:if>
