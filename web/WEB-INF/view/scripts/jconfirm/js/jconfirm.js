/*
 * Small JQuery Utility to Show a Confirmation Dialog 
 *
 */

var jConfirm = new function(){
	this.suppress;
	this.overlay;
	this.id;
	this.actioned;
		
	this.confirm = function(id,callback_affirm,callback_negate){
		this.show(id,callback_affirm,callback_negate);		
	}
	
	this.init = function(){
		this.suppress = false;
		this.overlay = '';			
	};

	this.close = function(callback){		
		$j(this.overlay).hide();
		$j('.jConfirm_Window').hide();		
		var suppressKey = $j(this.id+' #suppress_key').val();
		if(suppressKey != 'NA'){
			var dontShow = $j(this.id+' input[name=suppress_message]').is(':checked');				
			$j(this.id+' #suppress').val(dontShow);
			try{
				DWRUserService.saveUserPropertyForCurrentUser(suppressKey, dontShow, function(data){					
					if(!this.actioned && callback){
						callback();
						this.actioned = true;
					}
				});
			}catch(e){
				alert(e);
			} //Skip	
		}else{
			if(callback){
				callback();
			}
		}
	};
	
	this.show = function(id,callback_affirm,callback_negate){
		id = '#'+id;
	
		this.overlay = '#Overlay';
					
		this.suppress = $j(id+' #suppress').val();
		
		this.actioned = false;	
		
		if(this.suppress == 'false'){		
					
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
				jConfirm.close(callback_affirm);						
			});
			
			$j(id+' #jConfirm_Negate').click(function() {
				jConfirm.close(callback_negate);				
			});
		
			$j(id+' #jConfirm_Affirm').focus();
		
			this.id = id;
			
		}else{			
			if(callback_affirm){
				callback_affirm();
			}			
		}
	}	
}

$j(window).load(function(){
	jConfirm.init();
});