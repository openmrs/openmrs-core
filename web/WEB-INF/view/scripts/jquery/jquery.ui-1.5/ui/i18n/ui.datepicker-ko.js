/* Korean initialisation for the jQuery calendar extension. */
/* Written by DaeKwon Kang (ncrash.dk@gmail.com). */
jQuery(function($){
	$.datepicker.regional['ko'] = {clearText: 'ì§ì°êž°', clearStatus: '',
		closeText: 'ë«êž°', closeStatus: '',
		prevText: 'ìŽì ë¬', prevStatus: '',
		nextText: 'ë€ìë¬', nextStatus: '',
		currentText: 'ì€ë', currentStatus: '',
		monthNames: ['1ì(JAN)','2ì(FEB)','3ì(MAR)','4ì(APR)','5ì(MAY)','6ì(JUN)',
			'7ì(JUL)','8ì(AUG)','9ì(SEP)','10ì(OCT)','11ì(NOV)','12ì(DEC)'],
		monthNamesShort: ['1ì(JAN)','2ì(FEB)','3ì(MAR)','4ì(APR)','5ì(MAY)','6ì(JUN)',
			'7ì(JUL)','8ì(AUG)','9ì(SEP)','10ì(OCT)','11ì(NOV)','12ì(DEC)'],
		monthStatus: '', yearStatus: '',
		weekHeader: 'Wk', weekStatus: '',
		dayNames: ['ìŒ','ì','í','ì','ëª©','êž','í '],
		dayNamesShort: ['ìŒ','ì','í','ì','ëª©','êž','í '],
		dayNamesMin: ['ìŒ','ì','í','ì','ëª©','êž','í '],
		dayStatus: 'DD', dateStatus: 'D, M d',
		dateFormat: 'yy-mm-dd', firstDay: 0, 
		initStatus: '', isRTL: false};
	$.datepicker.setDefaults($.datepicker.regional['ko']);
});
