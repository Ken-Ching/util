$(function(){
	var cssContent = document.getElementById("cssContent");
	cssContent.onmouseover = function () {
		this.style = "color:red;";
	}
	cssContent.onmouseout = function () {
		this.style = "color:black;";
	}
});