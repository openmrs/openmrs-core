<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View HL7 Inbound Queue"
	otherwise="/login.htm" redirect="/admin/hl7/hl7InQueuePending.htm" />

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
	
	$j(document).ready(function() {
	
		hl7table = $j('#hl7Table').dataTable( { 
			"aoColumns": [  { "sName": "action", "bSortable": false,
					         "fnRender": function ( oObj ) {
									var id = oObj.aData[0];
									return '<button onClick="toggleMessage(' + id + ',' + oObj.iDataRow + 
										')"><openmrs:message code="Hl7inQueue.queueList.hold" /></button>';
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
							}
			  			 ],
			"sDom": '<"fg-toolbar ui-widget-header ui-corner-tl ui-corner-tr ui-helper-clearfix"flip>' + 
				'rt<"fg-toolbar ui-widget-header ui-corner-bl ui-corner-br ui-helper-clearfix"i>',
			"sPaginationType": "full_numbers",
			"bAutoWidth": false,
			"bLengthChange": true,
			"bProcessing": true,
			"bServerSide": true,
			"bStateSave": false,
			"sAjaxSource": "hl7InQueueList.json?messageState=${messageState}",
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
	
	function toggleMessage(id, row) {
		$j.ajax({
			"url": "toggleHL7InQueue.json",
			"data": { "hl7InQueueId": id },
			"dataType": "json",
			"error": function(XMLHttpRequest, textStatus, errorThrown) {
				setMessage('<openmrs:message code="Hl7inQueue.queueList.error"/>');
			},
			"success": function(data, textStatus, XMLHttpRequest) {
				setMessage(data);
				hl7table.fnDeleteRow(row);
			}
		});
	}

</script>

<style>
	.showmore { height: 50px; overflow: hidden; }
	#hl7Table button { padding: 0.5em; }
</style>

<h2><openmrs:message code="Hl7inQueue.queueList.pending.title" /></h2>

<div id="message" class="ui-widget"
	style="display: none; margin-bottom: 1em;">
<div class="ui-state-highlight ui-corner-all" style="padding: 0.5em;">
<span class="ui-icon ui-icon-info"
	style="float: left; margin-right: 0.3em;"></span> <span class="content"></span>
</div>
</div>

<table cellpadding="5" cellspacing="0" id="hl7Table" width="100%">
	<thead>
		<tr>
			<th></th>
			<th><openmrs:message code="Hl7inQueue.queueList.source.header" /></th>
			<th><openmrs:message code="Hl7inQueue.queueList.dateCreated" /></th>
			<th width="80%"><openmrs:message
				code="Hl7inQueue.queueList.data.header" /></th>
		</tr>
	</thead>
	<tbody></tbody>
</table>

<%@ include file="/WEB-INF/template/footer.jsp"%>
