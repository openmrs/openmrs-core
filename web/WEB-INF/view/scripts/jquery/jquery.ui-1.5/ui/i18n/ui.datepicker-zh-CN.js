/* Chinese initialisation for the jQuery UI date picker plugin. */
/* Written by Cloudream (cloudream@gmail.com). */
jQuery(function($){
	$.datepicker.regional['zh-CN'] = {clearText: 'æž
é€', clearStatus: 'æž
é€å·²éæ¥æ',
		closeText: 'å
³é­', closeStatus: 'äžæ¹ååœåéæ©',
		prevText: '&lt;äžæ', prevStatus: 'æŸç€ºäžæ',
		nextText: 'äžæ&gt;', nextStatus: 'æŸç€ºäžæ',
		currentText: 'ä»å€©', currentStatus: 'æŸç€ºæ¬æ',
		monthNames: ['äžæ','äºæ','äžæ','åæ','äºæ','å
­æ',
		'äžæ','å
«æ','ä¹æ','åæ','åäžæ','åäºæ'],
		monthNamesShort: ['äž','äº','äž','å','äº','å
­',
		'äž','å
«','ä¹','å','åäž','åäº'],
		monthStatus: 'éæ©æä»œ', yearStatus: 'éæ©å¹Žä»œ',
		weekHeader: 'åš', weekStatus: 'å¹Žå
åšæ¬¡',
		dayNames: ['æææ¥','ææäž','ææäº','ææäž','ææå','ææäº','ææå
­'],
		dayNamesShort: ['åšæ¥','åšäž','åšäº','åšäž','åšå','åšäº','åšå
­'],
		dayNamesMin: ['æ¥','äž','äº','äž','å','äº','å
­'],
		dayStatus: 'è®Ÿçœ® DD äžºäžåšèµ·å§', dateStatus: 'éæ© mæ dæ¥, DD',
		dateFormat: 'yy-mm-dd', firstDay: 1, 
		initStatus: 'è¯·éæ©æ¥æ', isRTL: false};
	$.datepicker.setDefaults($.datepicker.regional['zh-CN']);
});
