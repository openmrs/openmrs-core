/* Norwegian initialisation for the jQuery UI date picker plugin. */
/* Written by Naimdjon Takhirov (naimdjon@gmail.com). */

$(document).ready(function(){
    $.datepicker.regional['no'] = {clearText: 'TÃžm', clearStatus: '',
		closeText: 'Lukk', closeStatus: '',
        prevText: '&laquo;Forrige',  prevStatus: '',
		nextText: 'Neste&raquo;', nextStatus: '',
		currentText: 'I dag', currentStatus: '',
        monthNames: ['Januar','Februar','Mars','April','Mai','Juni', 
        'Juli','August','September','Oktober','November','Desember'],
        monthNamesShort: ['Jan','Feb','Mar','Apr','Mai','Jun', 
        'Jul','Aug','Sep','Okt','Nov','Des'],
		monthStatus: '', yearStatus: '',
		weekHeader: 'Uke', weekStatus: '',
		dayNamesShort: ['SÃžn','Man','Tir','Ons','Tor','Fre','LÃžr'],
		dayNames: ['SÃžndag','Mandag','Tirsdag','Onsdag','Torsdag','Fredag','LÃžrdag'],
		dayNamesMin: ['SÃž','Ma','Ti','On','To','Fr','LÃž'],
		dayStatus: 'DD', dateStatus: 'D, M d',
        dateFormat: 'yy-mm-dd', firstDay: 0, 
		initStatus: '', isRTL: false};
    $.datepicker.setDefaults($.datepicker.regional['no']); 
});
