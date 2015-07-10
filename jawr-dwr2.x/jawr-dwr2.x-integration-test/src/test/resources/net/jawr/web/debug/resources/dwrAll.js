// Provide a default path to dwr.engine
if (dwr == null) var dwr = {};
if (dwr.engine == null) dwr.engine = {};
if (DWREngine == null) var DWREngine = dwr.engine;



if (Demo == null) var Demo = {};
Demo._path = ''+JAWR.jawr_dwr_path+'';
Demo.sayHello = function(p0, callback) {
  dwr.engine._execute(Demo._path, 'Demo', 'sayHello', p0, callback);
}
