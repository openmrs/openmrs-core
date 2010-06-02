/*
 * Small JQuery Utility to Show a Confirmation Dialog 
 *
 */

var jConfirm = new function(){
	this.suppress;
	this.overlay;
	this.id;
	this.actioned;
		
	this.dialog = function(id,callback_affirm,callback_negate){
		this.show(id,callback_affirm,callback_negate);		
	}
	
	this.init = function(){
		this.suppress = false;
		this.overlay = '';			
	};
	
	this.invokeCallback = function(callback){
		if(callback && !this.actioned){
			callback();
			this.actioned = true;
		}
	}

	this.close = function(callback){		
		$j(this.overlay).hide();
		$j('.jConfirm_Window').hide();		
		var suppressKey = $j(this.id+' #suppress_key').val();		
		if(suppressKey != 'NA'){
			var dontShow = $j(this.id+' input[name=suppress_message]').is(':checked');			
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
	
	this.show = function(id,callback_affirm,callback_negate){
		id = '#'+id;
	
		this.overlay = '#Overlay';
		
		$j(document.body).append("<div id='Overlay'></div>");
					
		this.suppress = $j(id+' #suppress').val() == 'true';	
		
		this.actioned = false;	
		
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
			$j(id).css('top',  winH/2-$j(id).height()/2);  
			$j(id).css('left', winW/2-$j(id).width()/2);		
			
			//Transition of the jConfirm
			$j(id).fadeIn(500);	
		
			$j(id+' #jConfirm_Close').click(function() {
				jConfirm.close();		
			});			
		
			$j(id+' #jConfirm_Affirm').click(function() {
				jConfirm.close(callback_affirm);						
			});
			
			$j(id+' #jConfirm_Negate').click(function() {
				jConfirm.close(callback_negate);				
			});
		
			$j(id+' #jConfirm_Affirm').focus();
		
			this.id = id;
			
		}else{			
			this.invokeCallback(callback_affirm);			
		}
	}	
}

$j(window).load(function(){
	jConfirm.init();
});