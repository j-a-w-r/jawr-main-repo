;(function(){	
function r(val, args){
	for(var x = 0;x<args.length;x++){
		val = val.replace('{'+x+'}', args[x]);
	}
	return val;	
}
function p() {
	var val = arguments[0];	
	var ret;
	if(val.indexOf('{0}') != -1)
		ret = function(){return r(val,arguments);}
	else ret = function(){return val;}
	for(var x = 1; x < arguments.length;x++) {
		for(var a in arguments[x])
			ret[a] = arguments[x][a];
	}
	return ret;
}
window.messages=(
{ui:{error:{panel:{title:p("Erreur")}},msg:{hello:{world:p("Â¡Bonjour $ š tout le monde!")},salut:p("Mr.")}},error:{login:p("Erreur lors de la connection")}}
)
})();