<%@ include file="/WEB-INF/template/include.jsp" %>

<c:if test="${model.authenticatedUser != null}">
	<c:choose>
		<c:when test="${model.size == 'columnHeaders'}">
			<c:if test="${model.addressShowExtended == 'true'}">
				<th></th>
			</c:if>
			
			<c:forEach items="${model.addressTemplate.lines}" var="line">
				<c:forEach items="${line}" var="token">
					<c:if test="${token.isToken == 'IS_ADDR_TOKEN'}">
						<th><spring:message code="${token.displayText}"/></th>
					</c:if>
				</c:forEach>
			</c:forEach>	
		</c:when>

		<c:when test="${model.size == 'inOneRow'}">
			<tr>
				<c:if test="${model.addressShowExtended == 'true'}">
					<td>
						<spring:bind path="preferred">
							<c:if test="${status.value}">*</c:if>
						</spring:bind>
					</td>
				</c:if>
				<c:forEach items="${model.addressTemplate.lines}" var="line">
					<c:forEach items="${line}" var="token">
						<c:if test="${token.isToken == 'IS_ADDR_TOKEN'}">
							<td>
								<spring:bind path="${token.codeName}">
									${status.value}
								</spring:bind>
							</td>
						</c:if>
					</c:forEach>
				</c:forEach>	
			</tr>
		</c:when>
		<c:when test="${model.size == 'compact'}">
			<div id="patientAddressPortlet">
				<table>
					<c:forEach items="${model.addressTemplate.lines}" var="line">
						<tr>
							<td>
								<c:forEach items="${line}" var="token">
									<c:if test="${token.isToken == 'IS_ADDR_TOKEN'}">
										<spring:bind path="${token.codeName}">
											${status.value}
										</spring:bind>
									</c:if>
									<c:if test="${token.isToken == 'IS_NOT_ADDR_TOKEN'}">
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
				<c:when test="${model.addressMode == 'view'}">
					<div id="patientAddressPortlet">
						<table>
							<c:forEach items="${model.addressTemplate.lines}" var="line">
								<tr>
									<td>
										<c:forEach items="${line}" var="token">
											<c:if test="${token.isToken == 'IS_ADDR_TOKEN'}">
												<spring:message code="${token.displayText}" />:
												<spring:bind path="${token.codeName}">
													${status.value}
												</spring:bind>
											</c:if>
											<c:if test="${token.isToken == 'IS_NOT_ADDR_TOKEN'}">
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
					<c:if test="${model.addressShowTable != 'false'}">
						<div id="patientAddressPortlet">
							<table>
					</c:if>
					<c:if test="${model.addressShowExtended == 'true'}">
							<tr>
								<td><spring:message code="general.preferred"/></td>
								<td>
									<spring:bind path="preferred">
										<input type="hidden" name="_${status.expression}">
										<input type="checkbox" name="${status.expression}" onclick="preferredBoxClick(this)" alt="patientAddress" <c:if test="${status.value == true}">checked</c:if> />
										<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
									</spring:bind>
								</td>
							</tr>
					</c:if>
							<c:forEach items="${model.addressTemplate.lines}" var="line">
								<tr>
									<c:forEach items="${line}" var="token">
										<c:if test="${token.isToken == 'IS_ADDR_TOKEN'}">
											<td><spring:message code="${token.displayText}" /></td>
											<td>
												<spring:bind path="${token.codeName}">
													<c:if test="${status.value == null}">
														<input type="text" name="${status.expression}" value="${model.addressTemplate.elementDefaults[token.codeName]}" size="${token.displaySize}" />
													</c:if>
													<c:if test="${status.value != null}">
														<input type="text" name="${status.expression}" value="${status.value}" size="${token.displaySize}" />
													</c:if>
													<c:if test="${model.addresShowErrors != 'false'}">
														<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
													</c:if>
												</spring:bind>
											</td>
										</c:if>
									</c:forEach>
								</tr>
							</c:forEach>
					<c:if test="${model.addressShowExtended == 'true'}">
							<spring:bind path="creator">
								<c:if test="${!(status.value == null)}">
									<tr>
										<td><spring:message code="general.createdBy" /></td>
										<td colspan="4">
											${status.value.firstName} ${status.value.lastName} -
											<spring:bind path="dateCreated">
												${status.value}
											</spring:bind>
										</td>
									</tr>
								</c:if>
							</spring:bind>
							<tr>
								<td><spring:message code="general.voided"/></td>
								<td>
									<spring:bind path="voided">
										<input type="hidden" name="_${status.expression}"/>
										<input type="checkbox" name="${status.expression}" 
											   <c:if test="${status.value == true}">checked="checked"</c:if> 
											   onClick="voidedBoxClick(this)"
										/>
									</spring:bind>
								</td>
							</tr>
							<tr>
								<td><spring:message code="general.voidReason"/></td>
								<spring:bind path="voidReason">
									<td colspan="4">
										<input type="text" name="add${status.expression}" value="${status.value}" size="43" />
										<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
									</td>
								</spring:bind>
							</tr>
							<spring:bind path="voidedBy">
								<c:if test="${!(status.value == null)}">
									<tr>
										<td><spring:message code="general.createdBy" /></td>
										<td colspan="4">
											${status.value.firstName} ${status.value.lastName} -
											<spring:bind path="dateVoided">
												${status.value}
											</spring:bind>
										</td>
									</tr>
								</c:if>
							</spring:bind>
					</c:if>
					<c:if test="${model.addressShowTable != 'false'}">
							</table>
						</div>
					</c:if>
				</c:otherwise>
			</c:choose>
		</c:when>
		<c:otherwise>
			ERROR! unknown size '${model.size}' in Address portlet
		</c:otherwise>
	</c:choose>
</c:if>
