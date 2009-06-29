/**
 * Lithuanian (UTF-8) initialisation for the jQuery UI date picker plugin.
 *
 * @author Arturas Paleicikas <arturas@avalon.lt>
 */
jQuery(function($){
	$.datepicker.regional['lt'] = {clearText: 'IÅ¡valyti', clearStatus: '',
		closeText: 'UÅŸdaryti', closeStatus: '',
		prevText: '&lt;Atgal',  prevStatus: '',
		nextText: 'Pirmyn&gt;', nextStatus: '',
		currentText: 'Å iandien', currentStatus: '',
		monthNames: ['Sausis','Vasaris','Kovas','Balandis','GeguÅŸÄ','BirÅŸelis',
		'Liepa','RugpjÅ«tis','RugsÄjis','Spalis','Lapkritis','Gruodis'],
		monthNamesShort: ['Sau','Vas','Kov','Bal','Geg','Bir',
		'Lie','Rugp','Rugs','Spa','Lap','Gru'],
		monthStatus: '', yearStatus: '',
		weekHeader: '', weekStatus: '',
		dayNames: ['sekmadienis','pirmadienis','antradienis','treÄiadienis','ketvirtadienis','penktadienis','Å¡eÅ¡tadienis'],
		dayNamesShort: ['sek','pir','ant','tre','ket','pen','Å¡eÅ¡'],
		dayNamesMin: ['Se','Pr','An','Tr','Ke','Pe','Å e'],
		dayStatus: 'DD', dateStatus: 'D, M d',
		dateFormat: 'yy-mm-dd', firstDay: 1, 
		initStatus: '', isRTL: false};
	$.datepicker.setDefaults($.datepicker.regional['lt']);
});
