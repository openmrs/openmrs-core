/* Hungarian initialisation for the jQuery UI date picker plugin. */
/* Written by Istvan Karaszi (jquerycalendar@spam.raszi.hu). */
jQuery(function($){
	$.datepicker.regional['hu'] = {clearText: 'tÃ¶rlÃ©s', clearStatus: '',
		closeText: 'bezÃ¡rÃ¡s', closeStatus: '',
		prevText: '&laquo;&nbsp;vissza', prevStatus: '',
		nextText: 'elÅre&nbsp;&raquo;', nextStatus: '',
		currentText: 'ma', currentStatus: '',
		monthNames: ['JanuÃ¡r', 'FebruÃ¡r', 'MÃ¡rcius', 'Ãprilis', 'MÃ¡jus', 'JÃºnius',
		'JÃºlius', 'Augusztus', 'Szeptember', 'OktÃ³ber', 'November', 'December'],
		monthNamesShort: ['Jan', 'Feb', 'MÃ¡r', 'Ãpr', 'MÃ¡j', 'JÃºn',
		'JÃºl', 'Aug', 'Szep', 'Okt', 'Nov', 'Dec'],
		monthStatus: '', yearStatus: '',
		weekHeader: 'HÃ©', weekStatus: '',
		dayNames: ['VasÃ¡map', 'HÃ©tfÃ¶', 'Kedd', 'Szerda', 'CsÃŒtÃ¶rtÃ¶k', 'PÃ©ntek', 'Szombat'],
		dayNamesShort: ['Vas', 'HÃ©t', 'Ked', 'Sze', 'CsÃŒ', 'PÃ©n', 'Szo'],
		dayNamesMin: ['V', 'H', 'K', 'Sze', 'Cs', 'P', 'Szo'],
		dayStatus: 'DD', dateStatus: 'D, M d',
		dateFormat: 'yy-mm-dd', firstDay: 1, 
		initStatus: '', isRTL: false};
	$.datepicker.setDefaults($.datepicker.regional['hu']);
});
