/* Russian (UTF-8) initialisation for the jQuery UI date picker plugin. */
/* Written by Andrew Stromnov (stromnov@gmail.com). */
jQuery(function($){
	$.datepicker.regional['ru'] = {clearText: 'ÐÑÐžÑÑÐžÑÑ', clearStatus: '',
		closeText: 'ÐÐ°ÐºÑÑÑÑ', closeStatus: '',
		prevText: '&lt;ÐÑÐµÐŽ',  prevStatus: '',
		nextText: 'Ð¡Ð»ÐµÐŽ&gt;', nextStatus: '',
		currentText: 'Ð¡ÐµÐ³ÐŸÐŽÐœÑ', currentStatus: '',
		monthNames: ['Ð¯ÐœÐ²Ð°ÑÑ','Ð€ÐµÐ²ÑÐ°Ð»Ñ','ÐÐ°ÑÑ','ÐÐ¿ÑÐµÐ»Ñ','ÐÐ°Ð¹','ÐÑÐœÑ',
		'ÐÑÐ»Ñ','ÐÐ²Ð³ÑÑÑ','Ð¡ÐµÐœÑÑÐ±ÑÑ','ÐÐºÑÑÐ±ÑÑ','ÐÐŸÑÐ±ÑÑ','ÐÐµÐºÐ°Ð±ÑÑ'],
		monthNamesShort: ['Ð¯ÐœÐ²','Ð€ÐµÐ²','ÐÐ°Ñ','ÐÐ¿Ñ','ÐÐ°Ð¹','ÐÑÐœ',
		'ÐÑÐ»','ÐÐ²Ð³','Ð¡ÐµÐœ','ÐÐºÑ','ÐÐŸÑ','ÐÐµÐº'],
		monthStatus: '', yearStatus: '',
		weekHeader: 'ÐÐµ', weekStatus: '',
		dayNames: ['Ð²ÐŸÑÐºÑÐµÑÐµÐœÑÐµ','Ð¿ÐŸÐœÐµÐŽÐµÐ»ÑÐœÐžÐº','Ð²ÑÐŸÑÐœÐžÐº','ÑÑÐµÐŽÐ°','ÑÐµÑÐ²ÐµÑÐ³','Ð¿ÑÑÐœÐžÑÐ°','ÑÑÐ±Ð±ÐŸÑÐ°'],
		dayNamesShort: ['Ð²ÑÐº','Ð¿ÐœÐŽ','Ð²ÑÑ','ÑÑÐŽ','ÑÑÐ²','Ð¿ÑÐœ','ÑÐ±Ñ'],
		dayNamesMin: ['ÐÑ','ÐÐœ','ÐÑ','Ð¡Ñ','Ð§Ñ','ÐÑ','Ð¡Ð±'],
		dayStatus: 'DD', dateStatus: 'D, M d',
		dateFormat: 'dd.mm.yy', firstDay: 1, 
		initStatus: '', isRTL: false};
	$.datepicker.setDefaults($.datepicker.regional['ru']);
});
