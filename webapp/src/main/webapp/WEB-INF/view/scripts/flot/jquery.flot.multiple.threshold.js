/*
Flot plugin for specifying range of thresolds on data. 
Controlled through the option "constraints" in a specific series

usage -
  $.plot($("#placeholder"), [{ data: [ ... ], constraints: [constraint1, constraint2]},{data:[...],constraints:[...]}])
  
  Example:
  $.plot($("#placeholder"),[{data: d1,
					    constraints: [	
					    {
						threshold: {above:2},
						color: "rgb(0,0,0)"
					    },
					    {
						threshold: {below:-2},
						color: "rgb(0,0,255)"
					    }
					]
					}]);
	threshold -> y-limit on the plot.
	color     -> the color with which to plot portion of the graph which satisfies the limit condition.
	 


Internally, the plugin works by splitting the data into different series, one for each constraint.

*/
(function($){
  
    
  function init(plot){
   
    function plotWithMultipleThresholds(plot,s,datapoints){
			if(s.data && s.data.length > 0 && s.constraints && s.constraints.length>0){
			   var series = createPlotData(s.data,s.constraints);
				for(var i=0;i<series.length;i++){
				var ss = $.extend({},s);
				ss.constraints = [];
				ss.data = series[i].data;
				ss.color = series[i].color;
				ss.lines = series[i].lines;
				plot.getData().push(ss);
			}
		}
    }
	
    function createPlotData(dataset,constraints) {
    	plotData=[];
    	
    	var above=constraints.filter(aboveConstraints);
		var below=constraints.filter(belowConstraints);
		
		rangeY=_findMaxAndMinY(dataset);
		rangeX=_findMaxAndMinX(dataset);
		
		//creating data for the above plot
		above.sort(function(p1,p2){return p1.threshold.above - p2.threshold.above;});
		
		if(above.length>0) {
			var aboveMax={};
			$.extend(true, aboveMax,above[above.length-1]);
			aboveMax.threshold.above = aboveMax.threshold.above+10;
			if(aboveMax.threshold.above<rangeY.max) {
				aboveMax.threshold.above=rangeY.max+10;
			}
			above.push(aboveMax);
		}
		
		for ( var i = 0; i < above.length-1; i++) { 
			plotData.push( { 
			data : [[rangeX.min,above[i].threshold.above],[rangeX.max,above[i].threshold.above],[rangeX.max,above[i+1].threshold.above],[rangeX.min,above[i+1].threshold.above]], 
			color : above[i].color,
			lines : {show: true, fill: true,lineWidth:0}
			}); 
		}
		
		//creating data for the below plot
		below.sort(function(p1,p2){return p2.threshold.below - p1.threshold.below;});
		
		if(below.length>0) {
			var belowMin={};
			$.extend(true, belowMin,below[below.length-1]);
			belowMin.threshold.below=belowMin.threshold.below-10;
			if(belowMin.threshold.below>rangeY.min) {
				belowMin.threshold.below=rangeY.min-10;
			}
			below.push(belowMin);
		}

		for ( var i = 0; i < below.length-1; i++) { 
			plotData.push( { 
			data : [[rangeX.min,below[i].threshold.below],[rangeX.max,below[i].threshold.below],[rangeX.max,below[i+1].threshold.below],[rangeX.min,below[i+1].threshold.below]], 
			color : below[i].color,
			lines : {show: true, fill: true, lineWidth:0}
			}); 
		}
		
		return plotData;
    }
    
	function aboveConstraints(element, index, array) {
		return (!isNaN(element.threshold.above) && element.threshold.above!=null);
	}
	
	function belowConstraints(element, index, array) {
		return (!isNaN(element.threshold.below) && element.threshold.below!=null);
	}

	function _findMaxAndMinX(dataset){
		if(undefined == dataset)return undefined;
		var arr = [];
		for( var i=0;i<dataset.length;i++){
		   arr[i] = dataset[i][0];
		}
		arr.sort();
		return { min:arr[0],max:arr[arr.length-1]};
	}

	function _findMaxAndMinY(dataset){
		if(undefined == dataset)return undefined;
		var arr = [];
		for( var i=0;i<dataset.length;i++){
		   arr[i] = dataset[i][1];
		}
		arr.sort(function(p1,p2){return p1-p2});
		return { min:arr[0],max:arr[arr.length-1]};
	}
	
    plot.hooks.processRawData.push(plotWithMultipleThresholds);
  }
  
$.plot.plugins.push({
        init: init,
        name: 'multiple.threshold',
        version: '1.0'
    });
})(jQuery);


