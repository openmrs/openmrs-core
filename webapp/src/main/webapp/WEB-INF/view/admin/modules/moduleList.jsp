<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Modules" otherwise="/login.htm" redirect="/admin/modules/module.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />
<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<link href="<openmrs:contextPath/>/scripts/jquery-ui/css/<spring:theme code='jqueryui.theme.name' />/jquery-ui.custom.css" type="text/css" rel="stylesheet" />
<script type="text/javascript">
	var oTable;
	
	 function escapeSpecialCharacters(moduleId){
		 var segments = moduleId.split(".");
         if(segments.length > 1){
         moduleId = segments[0] + ('\\.') + segments[1];
         }
        return moduleId;
	 }
	
	 function getDependencies(module, isUnloadFlag){
		 var moduleId = module.parentNode.parentNode.id;
	     var path = "${pageContext.request.contextPath}/admin/modules/manage/checkdependencies.form";
   		 path = path + "?moduleId=" + moduleId;
	$j.ajax({
		async : false,
		type : "GET",
		url : path,
		dataType : "text",
		success : function(data) {
			if(data != ""){
				var message;
				message = '<openmrs:message code="Module.dependencyShutdownNotice" javaScriptEscape="true"/>';				
				message += '<br/><br/>' + JSON.parse(data);
				
				document.getElementById('dependency-confirmation-message').innerHTML = message;
			    $j( "#dialog-confirm" ).dialog({
			        resizable: false,
			        width: '50%',
			        modal: true,
			        buttons: {
			          "Ok": function() {
			            $j( this ).dialog( "close" );	
			            
			            moduleId = escapeSpecialCharacters(moduleId);	
			            if(isUnloadFlag == false){
			            $j('#' + moduleId + '-form').append('<input type="hidden" name="stop.x" value="stop.x">');
			            }else{
				            $j('#' + moduleId + '-form').append('<input type="hidden" name="unload.x" value="unload.x">');
     	
			            }
						$j('#' + moduleId + '-form').submit();
			          },
			          Cancel: function() {			        	  
			            $j( this ).dialog( "close" );
			          }
			        }
			      });
			}else{
	            moduleId = escapeSpecialCharacters(moduleId);	
	            if(isUnloadFlag == false){
				$j('#' + moduleId + '-form').append('<input type="hidden" name="stop.x" value="stop.x">');
	            }else{
					$j('#' + moduleId + '-form').append('<input type="hidden" name="unload.x" value="unload.x">');
	            }
				$j('#' + moduleId + '-form').submit();
			}
		}
	});
	return false;
	}
	 
	$j(document).ready(function() {
		$j('#addUpgradePopup').dialog({
			autoOpen: false,
			modal: true,
			title: '<openmrs:message code="Module.addOrUpgrade" javaScriptEscape="true"/>',
			width: '90%'
		});
				
		$j('#addUpgradeButton').click(function() {
			$j('#addUpgradePopup').dialog('open');
		});

		$j('.errorDetailsButton').click(function() {
			var detailsNum = $j(this).attr('id').substring(18); // strip 'errorDetailsButton'
			$j('#errorDetails' + detailsNum).dialog('open');
		});

		oTable = $j('#findModuleTable').dataTable({
			"aoColumns": [ { "sName": "Action", "bSortable": false,
					         "fnRender": function ( oObj ) {
									var downloadURL = oObj.aData[0];
									return '<form action="module.list" method="post"><input type="hidden" name="download" value="true" /><input type="hidden" name="action" value="upload" /><input type="hidden" name="downloadURL" value="' + downloadURL + '" /><input type="submit" value="<openmrs:message code="Module.install" />" /></form>';
								}
							},
							{ "sName": "Name" },
							{ "sName": "Version" },
							{ "sName": "Author" },
							{ "sName": "Description" }
			  			 ],
			"bLengthChange": false,			  			 
			"aaSorting": [[1,'asc'], [2,'desc']],
			"bAutoWidth": false,
			"sPaginationType": "two_button",
			"bProcessing": true,
			"bServerSide": true,
			"fnServerData": function ( sSource, aoData, fnCallback ) {
								aoData.push( { "name": "openmrs_version", "value": "${openmrsVersion}" } );

								<c:forEach var="module" items="${loadedModules}">
								  aoData.push( { "name": "excludeModule", "value": "${module.moduleId}" } );
								</c:forEach>

								$j.ajax( {
					                "dataType": 'jsonp',
				                	"type": "GET",
				                	"url": "${moduleRepositoryURL}/findModules",
				                	"data": aoData,
				                	"success": fnCallback
				            	} );
				        	}
		});
	});
</script>

<h2><openmrs:message code="Module.header" /></h2>

<p><openmrs:message code="Module.notice" /></p>
<div id="dialog-confirm" title="<openmrs:message code="Module.dependencyValidationNotice"/>"><p id="dependency-confirmation-message"></p></div>

<c:choose>
	<c:when test="${allowAdmin == 'true'}">
		<div id="buttonPanel">
			<div style="float:left">
				<input type="button" id="addUpgradeButton" value="<openmrs:message code="Module.addOrUpgrade" javaScriptEscape="true"/>"/>
				<div id="addUpgradePopup">
					<b class="boxHeader"><openmrs:message code="Module.add"/></b>
					<div class="box">
						<form id="moduleAddForm" action="module.list" method="post" enctype="multipart/form-data">
							<input type="file" name="moduleFile" size="40" <c:if test="${allowAdmin!='true'}">disabled="disabled"</c:if> />
							<input type="hidden" name="action" value="upload"/>
							<input type="submit" value='<openmrs:message code="Module.upload"/>'/>
						</form>
					</div>
					<br/>
		
					<b class="boxHeader"><openmrs:message code="Module.upgrade"/></b>
					<div class="box">
						<form method="post" id="uploadUpdateForm" enctype="multipart/form-data">
							<input type="file" name="moduleFile" size="40" />
							<input type="hidden" name="action" value="upload"/>
							<input type="hidden" name="update" value="true"/>
							<input type="submit" value='<openmrs:message code="Module.upload"/>'/>
						</form>
					</div>
					<br/>
		
					<div id="findModule">
						<b class="boxHeader"><openmrs:message code="Module.findAndDownload" arguments="${moduleRepositoryURL}" /></b>
						<div class="box">
							<table id="findModuleTable" cellpadding="5" cellspacing="0">
					    		<thead>
					       			<tr>
										<th><openmrs:message code="general.action"/></th>
										<th><openmrs:message code="general.name"/></th>
										<th><openmrs:message code="general.version"/></th>
										<th><openmrs:message code="general.author"/></th>
										<th><openmrs:message code="general.description"/></th>
					       			</tr>
					   			</thead>
					   			<tbody>
					    		</tbody>
							</table>
						</div>
					</div>
					<br/>
				</div>
			</div>
			<div style="float:left">
				<form method="post"><input type="submit" value='<openmrs:message code="Module.checkForUpdates"/>'/></form>
			</div>
			<div style="float:right">
				<form method="post"><input type="submit" name="action" value='<openmrs:message code="Module.startAll"/>'/></form>
			</div>
			<div style="clear:both">&nbsp;</div>
		</div>	
	</c:when>
	<c:otherwise>
		${disallowUploads}
	</c:otherwise>
</c:choose>

<c:forEach var="module" items="${moduleList}" varStatus="varStatus">
	<c:if test="${varStatus.first}">
		<b class="boxHeader"><openmrs:message code="Module.manage" /></b>
		<div class="box" id="moduleListing">
			<table cellpadding="5" cellspacing="0">
				<thead>
					<tr>
						<c:if test="${allowAdmin=='true'}">
							<th colspan="2"><openmrs:message code="general.action"/></th>
						</c:if>
						<th><openmrs:message code="general.name"/></th>
						<th><openmrs:message code="general.version"/></th>
						<th><openmrs:message code="general.author"/></th>
						<th><openmrs:message code="general.description"/></th>
						<th></th>
						<th></th>
					</tr>
				</thead>
				<tbody>
	</c:if>
			
				<form id="${module.moduleId}-form" method="post">
					<input type="hidden" name="moduleId" value="${module.moduleId}" />
					<tr class='${varStatus.index % 2 == 0 ? "oddRow" : "evenRow" }' id="${module.moduleId}">
						<c:choose>
							<c:when test="${allowAdmin=='true' && module.mandatory == false && module.coreModule == false}">
								<td valign="top">
									<c:choose>
										<c:when test="${not module.started}">
											<input type="image" src="${pageContext.request.contextPath}/images/play.gif" name="start" onclick="document.getElementById('hiddenAction').value = this.value" title="<openmrs:message code="Module.start.help"/>" alt="<openmrs:message code="Module.start"/>" />
										</c:when>
										<c:otherwise>
											<input type="image" src="${pageContext.request.contextPath}/images/stop.gif" name="stop" onclick="return getDependencies(this, false);" title="<openmrs:message code="Module.stop.help"/>" alt="<openmrs:message code="Module.stop"/>" />
										</c:otherwise>
									</c:choose>
								</td>
								<td valign="top"><input type="image" src="${pageContext.request.contextPath}/images/trash.gif" name="unload" onclick="return getDependencies(this, true);" title="<openmrs:message code="Module.unload.help"/>" title="<openmrs:message code="Module.unload"/>" alt="<openmrs:message code="Module.unload"/>" /></td>
							</c:when>
							<c:otherwise>
								<td valign="top">
									<img src="${pageContext.request.contextPath}/images/lock.gif" title="<openmrs:message code="Module.locked.help"/>" alt="<openmrs:message code="Module.locked"/>" />
								</td>
								<td></td>
							</c:otherwise>
						</c:choose>
						<td valign="top">${module.name} <c:if test="${not module.started}"><b id="moduleNotStarted" style="white-space: nowrap">[<openmrs:message code="Module.notStarted"/>]</b></c:if></td>
						<td valign="top">${module.version}</td>
						<td valign="top">${module.author}</td>
						<td valign="top">${fn:substring(fn:escapeXml(module.description),0, 200)}...</td>
						<td valign="top"<c:if test="${module.startupErrorMessage != null}">class="error"</c:if> >
							<c:if test="${module.startupErrorMessage != null}">
								<span class="errorDetailsButton" id="errorDetailsButton${varStatus.index}">
									<openmrs:message code="Module.errorClickForDetails"/>
								</span>
								<div class="errorDetailsDialog" id="errorDetails${varStatus.index}">
									<pre style="margin: 0px;">${module.startupErrorMessage}</pre>
								</div>
								<script type="text/javascript">
									$j('#errorDetails${varStatus.index}').dialog({
										autoOpen: false,
										modal: true,
										title: '<openmrs:message code="Module.errorStarting" arguments="${module.name}" javaScriptEscape="true"/>',
										width: '90%'
									});
								</script>
							</c:if>
						</td>
						<td>
							<c:if test="${module.downloadURL != null}">
								${module.updateVersion}
								<openmrs:message code="Module.updateAvailable" /> 
								<c:if test="${allowAdmin=='true'}">
									<input type="submit" name="action" value='<openmrs:message code="Module.installUpdate"/>'>
									<openmrs:message code="general.or"/>
								</c:if>
								<a href="${module.downloadURL}"><openmrs:message code="Module.downloadUpdate"/></a>
							</c:if>
						</td>
					</tr>
				</form>
				
	<c:if test="${varStatus.last}">
			</tbody>	
			</table>
		</div>

	</c:if>
	
</c:forEach>

<c:if test="${fn:length(moduleList) == 0}">
	<i> &nbsp; <openmrs:message code="Module.noLoadedModules"/></i><br/>
</c:if>

<br/>

<b class="boxHeader"><openmrs:message code="Module.help" /></b>
<div class="box">
	<ul>
		<li><i><openmrs:message code="Module.help.load"/></i></li>
		<c:if test="${fn:length(moduleList) > 0}">
			<li><i><openmrs:message code="Module.help.unload"/></i></li>
			<li><i><openmrs:message code="Module.help.startStop"/></i></li>
			<li><i><openmrs:message code="Module.help.update"/></i></li>
		</c:if>
		<li><i><openmrs:message htmlEscape="false" code="Module.help.findMore"/></i></li>
	</ul>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>