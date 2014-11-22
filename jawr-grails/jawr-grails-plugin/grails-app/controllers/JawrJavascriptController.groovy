import net.jawr.web.JawrGrailsConstant;
import net.jawr.web.servlet.JawrRequestHandler;


/**
 * Jawr controller for javascript requests. 
 * It will delegate in the corresponding requestHandler to attend requests. 
 */
class JawrJavascriptController {
	def defaultAction = "doGet"
	JawrRequestHandler requestHandler;
	
	def doGet = {
		
		if(null == requestHandler)
			requestHandler = servletContext.getAttribute(JawrGrailsConstant.JAWR_GRAILS_JS_REQUEST_HANDLER);
		
			// In grails the request is always internally forwarded. This takes account for that. 
			String path = request['javax.servlet.forward.servlet_path'];			
			if(grailsApplication.config.jawr.js.mapping){
				path = path.replaceFirst(grailsApplication.config.jawr.js.mapping, '');
				
			}
			
		render "";	
		requestHandler.processRequest(path,request, response );
		
		return null;
	}
	
	
}