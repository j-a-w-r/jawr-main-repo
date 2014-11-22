/**
 * 
 */
package net.jawr.dwr.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.test.AbstractPageTest;
import net.jawr.web.test.JawrIntegrationServer;

import org.apache.commons.io.IOUtils;

/**
 * @author ibrahim Chaehoi
 *
 */
public abstract class AbstractDwrPageTest extends AbstractPageTest {

	/* (non-Javadoc)
	 * @see net.jawr.web.test.AbstractPageTest#initConfigFile()
	 */
	protected void initConfigFile(){
		
		JawrDWRTestConfigFiles annotationConfig = (JawrDWRTestConfigFiles) getClass()
				.getAnnotation(JawrDWRTestConfigFiles.class);
		try{
			String currentJawrConfigPath = annotationConfig.jawrConfig();
			String webappRootDir = JawrIntegrationServer.getInstance().getWebAppRootDir();
			
			OutputStream outFile = new FileOutputStream(new File(webappRootDir, "/WEB-INF/classes/jawr.properties"));
			IOUtils.copy(getClass().getClassLoader().getResourceAsStream(
					currentJawrConfigPath), outFile);
			IOUtils.closeQuietly(outFile);
			
			String currentWebXmlPath = annotationConfig.webXml();
			outFile = new FileOutputStream(new File(webappRootDir, "/WEB-INF/web.xml"));
			IOUtils.copy(getClass().getClassLoader().getResourceAsStream(
					currentWebXmlPath), outFile);
			IOUtils.closeQuietly(outFile);	
			
			String currentDispatcherXmlPath = annotationConfig.dwrConfig();
			String name = PathNormalizer.getPathName(currentDispatcherXmlPath);
			outFile = new FileOutputStream(new File(webappRootDir, "/WEB-INF/"+name));
			IOUtils.copy(getClass().getClassLoader().getResourceAsStream(
					currentDispatcherXmlPath), outFile);
			IOUtils.closeQuietly(outFile);	
			
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

}
