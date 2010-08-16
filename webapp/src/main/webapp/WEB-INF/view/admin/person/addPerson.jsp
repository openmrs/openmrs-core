<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Add People" otherwise="/login.htm" redirect="/admin/person/addPerson.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>

<c:choose>
	<c:when test="${empty param.addName}">
		
		<h2>
			<c:choose>
				<c:when test="${param.personType != null}">
					<spring:message code="Person.add.${param.personType}" />
				</c:when>
				<c:otherwise>
					<spring:message code="Person.add"/>
				</c:otherwise>
			</c:choose>
		</h2>
		<openmrs:portlet id="createPerson" url="addPersonForm" parameters="personType=${param.personType}|postURL=addPerson.htm|viewType=${param.viewType}" />
		<script type="text/javascript">
			document.getElementById("personName").focus();
		</script>
	</c:when>
	<c:otherwise>
		
		<form method="post" action="" id="addPersonForm">
				
		<openmrs:htmlInclude file="/dwr/interface/DWRPersonService.js" />
		
		<openmrs:htmlInclude file="/dwr/util.js" />
		
		<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />
				
		<script type="text/javascript">
			dojo.require("dojo.widget.openmrs.PersonSearch");
			
			dojo.addOnLoad( function() {
		
				searchWidget = dojo.widget.manager.getWidgetById("pSearch");			
				
				dojo.event.topic.subscribe("pSearch/select", 
					function(msg) {
						document.getElementById("personId").value = msg.objs[0].personId;
						document.getElementById("addPersonForm").submit();
					}
				);
				
				searchWidget.allowNewSearch = function() {
					return false;
				};
				
				var personName = "${param.addName}";
				var birthdate = "${param.addBirthdate}";
				var age = "${param.addAge}";
				var gender = "${param.addGender}";
				DWRPersonService.getSimilarPeople(personName, birthdate, age, gender, searchWidget.simpleClosure(searchWidget, "doObjectsFound"));
				
				searchWidget.allowAutoJump = function() { return false; };
			});
		</script>
		
		<style type="text/css">
			#openmrsSearchTable th {
				text-align: left;
			}
			#pSearchInput {
				display: none;
			}
		</style>
		
		<h2><spring:message code="Person.search.similarPerson"/></h2>
		<b id="similarPeopleInstructions"><spring:message code="Person.search.similarPersonInstructions"/></b>
		
		<br/><br/>
		
		<div dojoType="PersonSearch" widgetId="pSearch" inputId="pSearchInput" ></div>
		
		<br/>
		<input type="hidden" name="personId" id="personId" />
		<input type="hidden" name="personType" value="${param.personType}" />
		<input type="hidden" name="viewType" value="${param.viewType}" />
		
		<input type="submit" value='<spring:message code="Person.search.similarPersonNotOnList"/>' />
		&nbsp;
		<input type="button" value='<spring:message code="general.back"/>' onClick="history.go(-1)" />
		
		<br/><br/>
		
		</form>
		
	</c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/template/footer.jsp" %>