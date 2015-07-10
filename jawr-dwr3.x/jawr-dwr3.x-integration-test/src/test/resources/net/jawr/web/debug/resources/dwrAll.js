if (typeof dwr == 'undefined' || dwr.engine == undefined) throw new Error('You must include DWR engine before including this file');

(function() {
  if (dwr.engine._getObject("Demo") == undefined) {
    var p;
    
    p = {};
    p._path = '/jawr-dwr3.x-integration-test/dwr';

    /**
     * @param {class java.lang.String} p0 a param
     * @param {function|Object} callback callback function or options object
     */
    p.sayHello = function(p0, callback) {
      return dwr.engine._execute(p._path, 'Demo', 'sayHello', arguments);
    };
    
    dwr.engine._setObject("Demo", p);
  }
})();
