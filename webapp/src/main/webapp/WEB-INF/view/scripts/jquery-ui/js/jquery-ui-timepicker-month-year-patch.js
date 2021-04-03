/*
 * jQuery UI Datepicker Patch for IE for month year dropdown
 *
 * Depends:
 *    jquery.ui.datepicker.js
 *
 *********************************************************************
 *  NOTE: THIS FILE MUST BE DELETED AFTER JQUERY UPGRADE
 *********************************************************************
 * THIS IS A PATCH TO FIX BUG IN JQUERY FOR DATEPICKER ON IE FOR MONTH YEAR DROPDOWN
 * IT'S ALREADY FIXED IN NEW VERSION OF JQUEYR  TICKET #6198
 * PLEASE, REMOVE THIS INCLUDE AFTER JQUERY UPGRADE
 */

(function( $, undefined ) {
    var dpuuid = new Date().getTime();
    $.extend($.datepicker, {
        __updateDatepicker: $.datepicker._updateDatepicker,
        _updateDatepicker: function(inst) {
            inst.dpDiv.find("select.ui-datepicker-year, select.ui-datepicker-month").removeAttr("onclick");
            this.__updateDatepicker(inst);
        }
    });
    window['DP_jQuery_' + dpuuid] = $;
})(jQuery);

