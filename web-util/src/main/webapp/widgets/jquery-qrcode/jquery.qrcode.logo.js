/**
 * Extend JQuery QR code for logo
 * Add logo parameter in option like below:
 * logo : {width:50px; height:50px; src: "./logo.png"}
 */
var QR_LOGO_DEFALT = {
	WIDTH : 50,
	HEIGHT : 50,
};

(function( $ ){
	$.fn.qrcodeWithLogo = function(options) {
		
		return this.each(function(){
			$(this).qrcode(options);
			$(this).css("position", "relative");
			
			if (options.logo && options.logo.src) {
				var logo = options.logo;
				var isCanvas = (options.render == "canvas");
				var $qrcodeE = $(this).find( isCanvas ? "canvas" : "table");
				var qrcodeWidth = $qrcodeE.css("width").replace("px","");
				var qrcodeHeight = $qrcodeE.css("height").replace("px","");
				var logoWidth = logo.width ? logo.width : QR_LOGO_DEFALT.WIDTH;
				var logoHeight = logo.height ? logo.height : QR_LOGO_DEFALT.HEIGHT;
				var logoTop = (qrcodeHeight - logoHeight) / 2;
				var logoLeft = (qrcodeWidth - logoWidth) / 2;
				
				if (isCanvas) {
					var ctx = $qrcodeE[0].getContext("2d");
					
					var img = new Image();
					img.src = logo.src;
					img.onload = function(){//图片具有lazy loading，需要加载完才能画
						ctx.drawImage(img, logoLeft,logoTop,logoWidth,logoHeight);
					}
				} else {
					var qrCodePosition = $qrcodeE.position();//增加table偏移量
					var $logoDiv = $("<img></img>")
						.css("position", "absolute")
						.css("width", logoWidth+"px")
						.css("height", logoHeight+"px")
						.css("top", (qrCodePosition.top+logoTop)+"px")
						.css("left", (qrCodePosition.left+logoLeft)+"px")
						.prop("src", logo.src);
					$logoDiv.appendTo(this);
				}
			}
		});
	};
})( jQuery );

QRCodeUtil = {
	/**
	 * 解决qrCode中文问题
	 */
	toUtf8 : function (str) {
		var out, i, len, c;    
	    out = "";    
	    len = str.length;    
	    for(i = 0; i < len; i++) {    
	        c = str.charCodeAt(i);    
	        if ((c >= 0x0001) && (c <= 0x007F)) {    
	            out += str.charAt(i);    
	        } else if (c > 0x07FF) {    
	            out += String.fromCharCode(0xE0 | ((c >> 12) & 0x0F));    
	            out += String.fromCharCode(0x80 | ((c >>  6) & 0x3F));    
	            out += String.fromCharCode(0x80 | ((c >>  0) & 0x3F));    
	        } else {    
	            out += String.fromCharCode(0xC0 | ((c >>  6) & 0x1F));    
	            out += String.fromCharCode(0x80 | ((c >>  0) & 0x3F));    
	        }    
	    }    
	    return out;
	}	
};
