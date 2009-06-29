/**
 * @author Arturas Paleicikas <arturas.paleicikas@metasite.net>
 */
jQuery(function($){
	$.datepicker.regional['lv'] = {
		clearText: 'NotÄ«rÄ«t', clearStatus: '',
		closeText: 'AizvÄrt', closeStatus: '',
		prevText: 'Iepr',  prevStatus: '',
		nextText: 'NÄka', nextStatus: '',
		currentText: 'Å odien', currentStatus: '',
		monthNames: ['JanvÄris','FebruÄris','Marts','AprÄ«lis','Maijs','JÅ«nijs',
		'JÅ«lijs','Augusts','Septembris','Oktobris','Novembris','Decembris'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Mai','JÅ«n',
		'JÅ«l','Aug','Sep','Okt','Nov','Dec'],
		monthStatus: '', yearStatus: '',
		weekHeader: 'Nav', weekStatus: '',
		dayNames: ['svÄtdiena','pirmdiena','otrdiena','treÅ¡diena','ceturtdiena','piektdiena','sestdiena'],
		dayNamesShort: ['svt','prm','otr','tre','ctr','pkt','sst'],
		dayNamesMin: ['Sv','Pr','Ot','Tr','Ct','Pk','Ss'],
		dayStatus: 'DD', dateStatus: 'D, M d',
		dateFormat: 'dd-mm-yy', firstDay: 1, 
		initStatus: '', isRTL: false};
	$.datepicker.setDefaults($.datepicker.regional['lv']);
});
