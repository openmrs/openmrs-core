/* Polish initialisation for the jQuery UI date picker plugin. */
/* Written by Jacek Wysocki (jacek.wysocki@gmail.com). */
jQuery(function($){
	$.datepicker.regional['pl'] = {clearText: 'WyczyÅÄ', clearStatus: 'WyczyÅÄ obecnÄ
 datÄ',
		closeText: 'Zamknij', closeStatus: 'Zamknij bez zapisywania',
		prevText: '&#x3c;Poprzedni', prevStatus: 'PokaÅŒ poprzedni miesiÄ
c',
		nextText: 'NastÄpny&#x3e;', nextStatus: 'PokaÅŒ nastÄpny miesiÄ
c',
		currentText: 'DziÅ', currentStatus: 'PokaÅŒ aktualny miesiÄ
c',
		monthNames: ['StyczeÅ','Luty','Marzec','KwiecieÅ','Maj','Czerwiec',
		'Lipiec','SierpieÅ','WrzesieÅ','PaÅºdziernik','Listopad','GrudzieÅ'],
		monthNamesShort: ['Sty','Lu','Mar','Kw','Maj','Cze',
		'Lip','Sie','Wrz','Pa','Lis','Gru'],
		monthStatus: 'PokaÅŒ inny miesiÄ
c', yearStatus: 'PokaÅŒ inny rok',
		weekHeader: 'Tydz', weekStatus: 'TydzieÅ roku',
		dayNames: ['Niedziela','Poniedzialek','Wtorek','Åroda','Czwartek','PiÄ
tek','Sobota'],
		dayNamesShort: ['Nie','Pn','Wt','År','Czw','Pt','So'],
		dayNamesMin: ['N','Pn','Wt','År','Cz','Pt','So'],
		dayStatus: 'Ustaw DD jako pierwszy dzieÅ tygodnia', dateStatus: 'Wybierz D, M d',
		dateFormat: 'yy-mm-dd', firstDay: 1, 
		initStatus: 'Wybierz datÄ', isRTL: false};
	$.datepicker.setDefaults($.datepicker.regional['pl']);
});
