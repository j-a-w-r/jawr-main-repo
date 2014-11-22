import net.jawr.web.JawrGrailsConstant;
import net.jawr.web.servlet.JawrBinaryResourceRequestHandler;

/**
 * Jawr controller for images requests. 
 * It will delegate in the corresponding requestHandler to attend requests. 
 */
class JawrBinaryController {
	def defaultAction = "doGet"
	JawrBinaryResourceRequestHandler requestHandler;
	
	def doGet = {
		
		if(null == requestHandler)
			requestHandler = servletContext.getAttribute(JawrGrailsConstant.JAWR_GRAILS_BINARY_REQUEST_HANDLER);
		
			// In grails the request is always internally forwarded. This takes account for that. 
			String path = request['javax.servlet.forward.servlet_path'];			
			if(grailsApplication.config.jawr.binary.mapping){
				path = path.replaceFirst(grailsApplication.config.jawr.binary.mapping, '');
				
			}
			
		render "";	
		requestHandler.processRequest(path,request, response );
		
		return null;
	}
	
	
}