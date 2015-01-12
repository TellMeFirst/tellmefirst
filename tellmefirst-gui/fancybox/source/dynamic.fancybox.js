(function ($, F) {
	F.transitions.resizeIn = function() {
        var endPos   = $.extend({}, F.current.dim, F._getPosition(true));
        			var startPos;
        
       				startPos = F.tmpWrap.position();
        			startPos.width  = F.tmpWrap.width();
        			startPos.height = F.tmpWrap.height();

        			F.inner
            			.css('overflow', 'hidden')
            			.width( F.tmpInner.width() )
            			.height( F.tmpInner.height() )
            			.css('opacity', 0);

        			F.wrap.css(startPos).show();
        			F.tmpWrap.trigger('onReset').remove();
        			F.wrap.animate(endPos, {
            			duration: F.current.nextSpeed,
            			step    : F.transitions.step,
            			complete: function() {
                			setTimeout(function() {
                    			F.inner.fadeTo("fast", 1, F._afterZoomIn);
                			}, 1);
            			}
        			});
    			};
 
    			F.transitions.resizeOut = function() {
        		if (F.tmpWrap) {
            		F.tmpWrap.stop(true).trigger('onReset').remove();
        		}       
       		 	F.tmpWrap  = F.wrap.stop(true, true);
       	 		F.tmpInner = F.inner.stop(true, true);
    			};
			}(jQuery, jQuery.fancybox));