<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View HL7 Inbound Queue"
	otherwise="/login.htm" redirect="/admin/hl7/hl7InErrorList.htm" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<openmrs:htmlInclude file="/scripts/jquery/highlight/jquery.highlight-3.js" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.filteringDelay.js" />
<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui.custom.min.js" />
<link href="<openmrs:contextPath/>/scripts/jquery-ui/css/<spring:theme code='jqueryui.theme.name' />/jquery-ui.custom.css" type="text/css" rel="stylesheet" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css" />

<script type="text/javascript">
	var hl7table;
	var errorDetails = {};
	
	$j(document).ready(function() {
	
		$j("#popup").dialog({
			"autoOpen": false,
			"modal": true,
			"buttons": {
				"Close": function() { $j(this).dialog("close"); } 
			},
			"title": "<openmrs:message code="Hl7inError.errorList.errorDetails.header" />",
			"width": "85%"
		});

		hl7table = $j('#hl7Table').dataTable( { 
			"aoColumns": [  { "sName": "action", "bSortable": false,
					         "fnRender": function ( oObj ) {
									return '<button ' +
										'onClick="resubmitMessage(' + oObj.aData[0] + ',' + oObj.iDataRow + ')">' +
										'<openmrs:message code="Hl7inError.errorList.restore" /></button>';
								}
							},
							{ "sName": "source", "bSortable": false },
							{ "sName": "dateCreated", "bSortable": false },
							{ "sName": "data", "bSortable": false,
					         "fnRender": function ( oObj ) {
									return '<div id="hl7' + oObj.iDataRow + '" class="showmore"><pre>' + oObj.aData[3] + 
										'</pre></div><a id="show'+ oObj.iDataRow + '" href=# onClick="showMore(' + oObj.iDataRow +
										')"><openmrs:message code="Hl7inQueue.queueList.showMore" /></a>' +
										'<a id="hide'+ oObj.iDataRow + '" style="display:none;" href=# onClick="hideMore(' + oObj.iDataRow + 
										')"><openmrs:message code="Hl7inQueue.queueList.hideMore" /></a>';
								}
							},
							{ "sName": "error", "bSortable": false ,
							"fnRender": function ( oObj ) {
				         		errorDetails[oObj.iDataRow] = oObj.aData[5];
				         		var clazz = (oObj.aData[5].toUpperCase().indexOf(
						         		hl7table.fnSettings().oPreviousSearch.sSearch.toUpperCase()) > 0) ? "highlight" : "";
								return '<p>' + oObj.aData[4] + '</p>' +
									'<button class="' + clazz + '" ' + 
									'onClick="showDetails(' + oObj.iDataRow + ')">' + 
									'<openmrs:message code="Hl7inError.errorList.showDetails" /></button>';
								}
							},
							{ "sName": "errorDetails", "bSortable": false, "bVisible": false }
			  			 ],
			"sDom": '<"fg-toolbar ui-widget-header ui-corner-tl ui-corner-tr ui-helper-clearfix"flip>rt' +
				'<"fg-toolbar ui-widget-header ui-corner-bl ui-corner-br ui-helper-clearfix"i>',
			"sPaginationType": "full_numbers",
			"bAutoWidth": false,
			"bLengthChange": true,
			"bProcessing": true,
			"bServerSide": true,
			"sAjaxSource": "hl7InErrorList.json",
			"bJQueryUI": true,
			"oLanguage": {
				"sInfoFiltered": "(_MAX_ in queue)"
			},
			"fnDrawCallback": function() {
				$j("#hl7Table td").highlight(hl7table.fnSettings().oPreviousSearch.sSearch);
			}
		});
		hl7table.fnSetFilteringDelay(1000);

	} );

	function showMore(id) {
		$j('#hl7' + id).animate({height: "100%"}, "slow");
		$j('#show' + id).hide();
		$j('#hide' + id).show();
	}
	
	function hideMore(id) {
		$j('#hl7' + id).animate({height: "50px"}, "slow");
		$j('#hide' + id).hide();
		$j('#show' + id).show();
	}

	function setMessage(data) {
		$j("#message").fadeOut("fast", function() {
			if ("openmrs_msg" in data)
				$j("#message .content").html(data.openmrs_msg);
			else if ("openmrs_error" in data)
				$j("#message .content").html(data.openmrs_error);
			$j("#message").fadeIn("slow");
		});
	}
	
	function resubmitMessage(id, row) {
		$j.ajax({
			"url": "resubmitHL7InError.json",
			"data": { "hl7InErrorId": id },
			"dataType": "json",
			"error": function(XMLHttpRequest, textStatus, errorThrown) {
				setMessage('<openmrs:message code="Hl7inError.errorList.restore"/>');
			},
			"success": function(data, textStatus, XMLHttpRequest) {
				setMessage(data);
				hl7table.fnDeleteRow(row);
			}
		});
	}

	function showDetails(row) {
		$j("#popup .content").html("<pre>" + errorDetails[row] + "</pre>");
		$j("#popup .content").highlight(hl7table.fnSettings().oPreviousSearch.sSearch);
		$j("#popup").dialog("open");
	}

</script>

<style>
	.showmore { height: 50px; overflow: hidden; }
	#hl7Table button { padding: 0.5em; }
</style>

<h2><openmrs:message code="Hl7inError.header" /></h2>

<div id="message" class="ui-widget"
	style="display: none; margin-bottom: 1em;">
	<div class="ui-state-highlight ui-corner-all" style="padding: 0.5em;">
		<span class="ui-icon ui-icon-info"
			style="float: left; margin-right: 0.3em;"></span> <span class="content"></span>
	</div>
</div>

<div id="popup" style="display: none;"><span class="content"></span>
</div>

<table cellpadding="5" cellspacing="0" id="hl7Table" width="100%">
	<thead>
		<tr>
			<th></th>
			<th><openmrs:message code="Hl7inError.errorList.source.header" /></th>
			<th><openmrs:message
				code="Hl7inError.errorList.dateCreated.header" /></th>
			<th><openmrs:message code="Hl7inError.errorList.data.header" /></th>
			<th><openmrs:message code="Hl7inError.errorList.error.header" /></th>
			<th><openmrs:message
				code="Hl7inError.errorList.errorDetails.header" /></th>
		</tr>
	</thead>
	<tbody></tbody>
</table>

<%@ include file="/WEB-INF/template/footer.jsp"%>