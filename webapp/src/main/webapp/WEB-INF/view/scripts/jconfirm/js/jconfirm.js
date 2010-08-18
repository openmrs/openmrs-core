/*
 * Small JQuery Utility to Show a Confirmation Dialog 
 *
 */

var jConfirm = new function(){
	this.suppress;
	this.overlay;
	this.id;
		
	this.dialog = function(id,callback1,callback2,callback3){
		this.show(id,callback1,callback2,callback3);		
	}
	
	this.init = function(){
		this.suppress = false;
		this.overlay = '';			
	};
	
	this.invokeCallback = function(callback){
		if(callback){
			callback();			
		}
	}

	this.close = function(callback){		
		$j(this.overlay).hide();
		$j('.jConfirm_Window').hide();		
		var suppressKey = $j(this.id+' #suppress_key').val();		
		if(suppressKey != 'NA'){
			var dontShow = $j(this.id+' input[name=suppress_'+this.id.substring(1)+']').is(':checked');			
			if(this.suppress != dontShow){ //If existing suppress value and dontShow value not same				
				$j(this.id+' #suppress').val(dontShow);				
				try{
					DWRUserService.saveUserPropertyForCurrentUser(suppressKey, dontShow, function(data){					
						jConfirm.invokeCallback(callback);
					});
				}catch(e){} //Skip
			}else{ 
				this.invokeCallback(callback);
			}			
		}else{
			this.invokeCallback(callback);
		}
		$j(this.overlay).remove(); 
		this.suppress = '';		
	};
	
	this.show = function(id,callback1,callback2,callback3){
		id = '#'+id;		
	
		this.overlay = '#Overlay';
		
		$j(document.body).append("<div id='Overlay'></div>");
							
		this.suppress = $j(id+' #suppress').val() == 'true';	
		
		var defaultButton = $j(id+' #default_button').val();		
		
		if(this.suppress == false){		
					
			//Set the Overlay to the whole screen
			$j(this.overlay).css({'width':$j(document).width(),'height':$j(document).height()});  
	
			//Transition of the Overlay
			$j(this.overlay).fadeIn(500);		
			$j(this.overlay).fadeTo("slow",0.5);	

			//Get the window height and width  
			var winH = $j(window).height();
			var winW = $j(window).width();  
      
			//Set the confirmation window to center  
			$j(id).css('top',  ((winH/2)-($j(id).height()/2)+($j(document).scrollTop())));  
			$j(id).css('left', ((winW/2)-($j(id).width()/2)+($j(document).scrollLeft())));		
			
			//Transition of the jConfirm
			$j(id).fadeIn(500);	
			
			$j(id).keyup(function(e){
				if(e.keyCode == 27 || e.which == 27){
					jConfirm.close();
				}
			});
		
			$j(id+' #jConfirm_Close').unbind().click(function() {
				jConfirm.close();					
			});			
		
			$j(id+' #jConfirm_Button1').unbind().click(function() {
				jConfirm.close(callback1);										
			});
			
			$j(id+' #jConfirm_Button2').unbind().click(function() {
				jConfirm.close(callback2);						
			});
			
			if(callback3 != null){
				$j(id+' #jConfirm_Button3').unbind().click(function() {
					jConfirm.close(callback3);						
				});
			}else{
				$j(id+' #jConfirm_Button3').hide();
			}
		
			$j(id+' #jConfirm_Button'+defaultButton).focus();
		
			this.id = id;			
		}else{	
			var callback;			
			if(defaultButton == '1'){
				callback = callback1;
			}else if(defaultButton == '2'){
				callback = callback2;
			}else{
				callback = callback3;
			}		
			this.invokeCallback(callback);			
		}
	}	
}

$j(window).load(function(){
	jConfirm.init();
});