/* Arabic Translation for jQuery UI date picker plugin. */
/* Khaled Al Horani -- koko.dw@gmail.com */
jQuery(function($){
	$.datepicker.regional['ar'] = {
		clearText: 'äÙíÝ',
		clearStatus: 'ÇãÓÍ ÇáÊÇÑíÎ ÇáÍÇáí',
		closeText: 'ÅÛáÇÞ',
		closeStatus: 'ÅÛáÇÞ ÈÏæä ÍÝÙ',
		prevText: '<ÇáÓÇÈÞ',
		prevStatus: 'ÚÑÖ ÇáÔåÑ ÇáÓÇÈÞ',
		nextText: 'ÇáÊÇáí>',
		nextStatus: 'ÚÑÖ ÇáÔåÑ ÇáÞÇÏã',
		currentText: 'Çáíæã',
		currentStatus: 'ÚÑÖ ÇáÔåÑ ÇáÍÇáí',
		monthNames: ['ßÇäæä ÇáËÇäí', 'ÔÈÇØ', 'ÂÐÇÑ', 'äíÓÇä', 'ÂÐÇÑ', 'ÍÒíÑÇä', 'ÊãæÒ', 'ÂÈ', 'Ãíáæá',	'ÊÔÑíä ÇáÃæá', 'ÊÔÑíä ÇáËÇäí', 'ßÇäæä ÇáÃæá'],
		monthNamesShort: ['ßÇäæä ÇáËÇäí', 'ÔÈÇØ', 'ÂÐÇÑ', 'äíÓÇä', 'ÂÐÇÑ', 'ÍÒíÑÇä', 'ÊãæÒ', 'ÂÈ', 'Ãíáæá',	'ÊÔÑíä ÇáÃæá', 'ÊÔÑíä ÇáËÇäí', 'ßÇäæä ÇáÃæá'],
		monthStatus: 'ÚÑÖ ÔåÑ ÂÎÑ',
		yearStatus: 'ÚÑÖ ÓäÉ ÂÎÑì',
		weekHeader: 'ÃÓÈæÚ',
		weekStatus: 'ÃÓÈæÚ ÇáÓäÉ',
		dayNames: ['ÇáÓÈÊ', 'ÇáÃÍÏ', 'ÇáÇËäíä', 'ÇáËáÇËÇÁ', 'ÇáÃÑÈÚÇÁ', 'ÇáÎãíÓ', 'ÇáÌãÚÉ'],
		dayNamesShort: ['ÇáÓÈÊ', 'ÇáÃÍÏ', 'ÇáÇËäíä', 'ÇáËáÇËÇÁ', 'ÇáÃÑÈÚÇÁ', 'ÇáÎãíÓ', 'ÇáÌãÚÉ'],
		dayNamesMin: ['ÇáÓÈÊ', 'ÇáÃÍÏ', 'ÇáÇËäíä', 'ÇáËáÇËÇÁ', 'ÇáÃÑÈÚÇÁ', 'ÇáÎãíÓ', 'ÇáÌãÚÉ'],
		dayStatus: 'ÇÎÊÑ DD ááíæã ÇáÃæá ãä ÇáÃÓÈæÚ',
		dateStatus: 'ÇÎÊÑ D, M d',
		dateFormat: 'dd/mm/yy', firstDay: 0, 
		initStatus: 'ÇÎÊÑ íæã',
		isRTL: true
	};
	$.datepicker.setDefaults($.datepicker.regional['ar']);
});
