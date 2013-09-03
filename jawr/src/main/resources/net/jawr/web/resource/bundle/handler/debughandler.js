JAWR.loader.insert = function(bundles,func,mappingToUse,path,media) {

	for(var x = 0; x < bundles.length; x++){	
		if(bundles[x].belongsToBundle(path) && !this.usedBundles[bundles[x].name]){		
			var bundle =  bundles[x];
			this.usedBundles[bundle.name] = true;
			var pathtoRender = this.normalizePath(mappingToUse +'/'+ bundle.name);
			if(!bundle.itemPathList) {
				if(bundle.ieExpression)
					this.insertCondComment(bundle.ieExpression,func,pathtoRender,media);
				else this[func](pathtoRender,media);		
			}
			else{
				for(var i = 0; i < bundle.itemPathList.length; i++){
					var pathLink = this.addRandomParam(this.normalizePath(mappingToUse +'/'+bundle.itemPathList[i]));
					if(bundle.ieExpression)
						this.insertCondComment(bundle.ieExpression,func,pathLink,media);
					else this[func](pathLink,media);
				}
			}
		}
	}
}
JAWR.loader.addRandomParam = function(path) {
	if(JAWR.loader.disableRandomParam)
		return path;
	if(path.indexOf('?') != -1)
		path += '&' + Math.floor(Math.random()*10000);
	else path += '?' + Math.floor(Math.random()*10000);
	return path;
}