/* Ukrainian (UTF-8) initialisation for the jQuery UI date picker plugin. */
/* Written by Maxim Drogobitskiy (maxdao@gmail.com). */
jQuery(function($){
	$.datepicker.regional['ua'] = {clearText: 'ÐÑÐžÑÑÐžÑÐž', clearStatus: '',
		closeText: 'ÐÐ°ÐºÑÐžÑÐž', closeStatus: '',
		prevText: '&lt;&lt;',  prevStatus: '',
		nextText: '&gt;&gt;', nextStatus: '',
		currentText: 'Ð¡ÑÐŸÐ³ÐŸÐŽÐœÑ', currentStatus: '',
		monthNames: ['Ð¡ÑÑÐµÐœÑ','ÐÑÑÐžÐ¹','ÐÐµÑÐµÐ·ÐµÐœÑ','ÐÐ²ÑÑÐµÐœÑ','Ð¢ÑÐ°Ð²ÐµÐœÑ','Ð§ÐµÑÐ²ÐµÐœÑ',
		'ÐÐžÐ¿ÐµÐœÑ','Ð¡ÐµÑÐ¿ÐµÐœÑ','ÐÐµÑÐµÑÐµÐœÑ','ÐÐŸÐ²ÑÐµÐœÑ','ÐÐžÑÑÐŸÐ¿Ð°ÐŽ','ÐÑÑÐŽÐµÐœÑ'],
		monthNamesShort: ['Ð¡ÑÑ','ÐÑÑ','ÐÐµÑ','ÐÐ²Ñ','Ð¢ÑÐ°','Ð§ÐµÑ',
		'ÐÐžÐ¿','Ð¡ÐµÑ','ÐÐµÑ','ÐÐŸÐ²','ÐÐžÑ','ÐÑÑ'],
		monthStatus: '', yearStatus: '',
		weekHeader: 'ÐÐµ', weekStatus: '',
		dayNames: ['ÐœÐµÐŽÑÐ»Ñ','Ð¿ÐŸÐœÐµÐŽÑÐ»ÐŸÐº','Ð²ÑÐ²ÑÐŸÑÐŸÐº','ÑÐµÑÐµÐŽÐ°','ÑÐµÑÐ²ÐµÑ','Ð¿ÑÑÐœÐžÑÑ','ÑÑÐ±Ð±ÐŸÑÐ°'],
		dayNamesShort: ['ÐœÐµÐŽ','Ð¿ÐœÐŽ','Ð²ÑÐ²','ÑÑÐŽ','ÑÑÐ²','Ð¿ÑÐœ','ÑÐ±Ñ'],
		dayNamesMin: ['ÐÐŽ','ÐÐœ','ÐÑ','Ð¡Ñ','Ð§Ñ','ÐÑ','Ð¡Ð±'],
		dayStatus: 'DD', dateStatus: 'D, M d',
		dateFormat: 'dd.mm.yy', firstDay: 1, 
		initStatus: '', isRTL: false};
	$.datepicker.setDefaults($.datepicker.regional['ua']);
});
