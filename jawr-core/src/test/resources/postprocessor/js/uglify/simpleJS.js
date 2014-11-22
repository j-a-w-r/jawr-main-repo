/** 
 * @preserve This is a usefull comment 
 * So please don't remove it 
 */
(function() {
	var cube, square, increment;

	square = function(x) {
		return x * x;
	};

	cube = function(x) {
		if(false){
			alert("This code should be removed with dead_code compress option");
		}
		debugger;
		return square(x) * x;
	};

	increment = function(x) {
		var arr = new Array(1, 2, 3); // This array is used to check unsafe compression mode
		return x+arr.length;
	};
	
}).call(this);
