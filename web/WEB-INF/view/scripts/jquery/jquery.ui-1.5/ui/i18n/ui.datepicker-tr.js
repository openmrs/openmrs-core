/* Turkish initialisation for the jQuery UI date picker plugin. */
/* Written by Izzet Emre Erkan (kara@karalamalar.net). */
jQuery(function($){
	$.datepicker.regional['tr'] = {clearText: 'temizle', clearStatus: 'geÃ§erli tarihi temizler',
		closeText: 'kapat', closeStatus: 'sadece gÃ¶stergeyi kapat',
		prevText: '&#x3c;geri', prevStatus: 'Ã¶nceki ayÄ± gÃ¶ster',
		nextText: 'ileri&#x3e', nextStatus: 'sonraki ayÄ± gÃ¶ster',
		currentText: 'bugÃŒn', currentStatus: '',
		monthNames: ['Ocak','Åubat','Mart','Nisan','MayÄ±s','Haziran',
		'Temmuz','AÄustos','EylÃŒl','Ekim','KasÄ±m','AralÄ±k'],
		monthNamesShort: ['Oca','Åub','Mar','Nis','May','Haz',
		'Tem','AÄu','Eyl','Eki','Kas','Ara'],
		monthStatus: 'baÅka ay', yearStatus: 'baÅka yÄ±l',
		weekHeader: 'Hf', weekStatus: 'AyÄ±n haftalarÄ±',
		dayNames: ['Pazar','Pazartesi','SalÄ±','ÃarÅamba','PerÅembe','Cuma','Cumartesi'],
		dayNamesShort: ['Pz','Pt','Sa','Ãa','Pe','Cu','Ct'],
		dayNamesMin: ['Pz','Pt','Sa','Ãa','Pe','Cu','Ct'],
		dayStatus: 'HaftanÄ±n ilk gÃŒnÃŒnÃŒ belirleyin', dateStatus: 'D, M d seÃ§iniz',
		dateFormat: 'dd.mm.yy', firstDay: 1, 
		initStatus: 'Bir tarih seÃ§iniz', isRTL: false};
	$.datepicker.setDefaults($.datepicker.regional['tr']);
});
