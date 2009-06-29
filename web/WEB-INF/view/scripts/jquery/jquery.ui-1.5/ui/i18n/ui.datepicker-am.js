/* Armenian(UTF-8) initialisation for the jQuery UI date picker plugin. */
/* Written by Levon Zakaryan (levon.zakaryan@gmail.com)*/
jQuery(function($){
	$.datepicker.regional['am'] = {clearText: 'ÕÕ¡ÖÖÕ¥Õ¬', clearStatus: '',
		closeText: 'ÕÕ¡Õ¯Õ¥Õ¬', closeStatus: '',
		prevText: '&lt;ÕÕ¡Õ­.',  prevStatus: '',
		nextText: 'ÕÕ¡Õ».&gt;', nextStatus: '',
		currentText: 'Ô±ÕµÕœÖ
Ö', currentStatus: '',
		monthNames: ['ÕÕžÖÕ¶ÕŸÕ¡Ö','ÕÕ¥Õ¿ÖÕŸÕ¡Ö','ÕÕ¡ÖÕ¿','Ô±ÕºÖÕ«Õ¬','ÕÕ¡ÕµÕ«Õœ','ÕÕžÖÕ¶Õ«Õœ',
		'ÕÕžÖÕ¬Õ«Õœ','ÕÕ£ÕžÕœÕ¿ÕžÕœ','ÕÕ¥ÕºÕ¿Õ¥ÕŽÕ¢Õ¥Ö','ÕÕžÕ¯Õ¿Õ¥ÕŽÕ¢Õ¥Ö','ÕÕžÕµÕ¥ÕŽÕ¢Õ¥Ö','ÔŽÕ¥Õ¯Õ¿Õ¥ÕŽÕ¢Õ¥Ö'],
		monthNamesShort: ['ÕÕžÖÕ¶ÕŸ','ÕÕ¥Õ¿Ö','ÕÕ¡ÖÕ¿','Ô±ÕºÖ','ÕÕ¡ÕµÕ«Õœ','ÕÕžÖÕ¶Õ«Õœ',
		'ÕÕžÖÕ¬','ÕÕ£Õœ','ÕÕ¥Õº','ÕÕžÕ¯','ÕÕžÕµ','ÔŽÕ¥Õ¯'],
		monthStatus: '', yearStatus: '',
		weekHeader: 'ÕÔ²Õ', weekStatus: '',
		dayNames: ['Õ¯Õ«ÖÕ¡Õ¯Õ«','Õ¥Õ¯ÕžÖÕ·Õ¡Õ¢Õ©Õ«','Õ¥ÖÕ¥ÖÕ·Õ¡Õ¢Õ©Õ«','Õ¹ÕžÖÕ¥ÖÕ·Õ¡Õ¢Õ©Õ«','Õ°Õ«Õ¶Õ£Õ·Õ¡Õ¢Õ©Õ«','ÕžÖÖÕ¢Õ¡Õ©','Õ·Õ¡Õ¢Õ¡Õ©'],
		dayNamesShort: ['Õ¯Õ«Ö','Õ¥ÖÕ¯','Õ¥ÖÖ','Õ¹ÖÖ','Õ°Õ¶Õ£','ÕžÖÖÕ¢','Õ·Õ¢Õ©'],
		dayNamesMin: ['Õ¯Õ«Ö','Õ¥ÖÕ¯','Õ¥ÖÖ','Õ¹ÖÖ','Õ°Õ¶Õ£','ÕžÖÖÕ¢','Õ·Õ¢Õ©'],
		dayStatus: 'DD', dateStatus: 'D, M d',
		dateFormat: 'dd.mm.yy', firstDay: 1, 
		initStatus: '', isRTL: false};
	$.datepicker.setDefaults($.datepicker.regional['am']);
});
