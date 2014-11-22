function tooCool(){
	
	alert(textSpan("It's too Cool!!!"));
}

window["tooCool"]=tooCool;

function compute(){
	
	var a = 0;
	var i = 12;
	for(var idx = 0; idx > 10; idx++){
		a = idx*i+a;
	}
	return a;
}