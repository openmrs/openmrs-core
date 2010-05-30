/*
 * Small JQuery Utility to Show a Confirmation Dialog 
 *
 */

var jConfirm = new function(){
	this.suppress;
	this.overlay;
	this.id;
	
	this.confirm = function(id,callback_affirm,callback_negate){
		this.show(id,callback_affirm,callback_negate);		
	}
	
	this.init = function(){
		this.suppress = false;
		this.overlay = '';			
	};

	this.close = function(){		
		$j(this.overlay).hide();
		$j('.jConfirm_Window').hide();		
		
	};
	
	this.show = function(id,callback_affirm,callback_negate){
		id = '#'+id;
	
		this.overlay = '#Overlay';
					
		this.suppress = $j(id+' #suppress').val();
				
		var responded = false;
		var response = false;
					
		//Set the Overlay to the whole screen
		$j(this.overlay).css({'width':$j(document).width(),'height':$j(document).height()});  
	
		//Transition of the Overlay
		$j(this.overlay).fadeIn(1000);      
		$j(this.overlay).fadeTo("slow",0.10);    

		//Get the window height and width  
		var winH = $j(window).height();
		var winW = $j(window).width();  
      
		//Set the confirmation window to center  
		$j(id).css('top',  winH/2-$j(id).height()/2);  
		$j(id).css('left', winW/2-$j(id).width()/2);		

		$j(id).fadeIn(2000);	
		
		$j(id+' #jConfirm_Close').click(function() {
			jConfirm.close();		
		});			
		
		$j(id+' #jConfirm_Affirm').click(function() {
			jConfirm.close();
			if(callback_affirm){
				callback_affirm();
			}			
		});
			
		$j(id+' #jConfirm_Negate').click(function() {
			jConfirm.close();
			if(callback_negate){
				callback_negate();
			}
		});
		
		$j(id+' #jConfirm_Affirm').focus();
		
		this.id = id;
	}
	
}

$j(window).load(function(){
	jConfirm.init();
});