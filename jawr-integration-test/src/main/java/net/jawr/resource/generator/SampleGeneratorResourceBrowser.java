/**
 * 
 */
package net.jawr.resource.generator;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.jawr.web.resource.bundle.generator.AbstractJavascriptGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.resolver.PrefixedPathResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.handler.reader.ResourceBrowser;

/**
 * @author ibrahim
 *
 */
public class SampleGeneratorResourceBrowser extends AbstractJavascriptGenerator implements ResourceBrowser {

	private Map<String, Set<String>> mappings = new HashMap<String, Set<String>>();
	
	/**
	 * 
	 */
	public SampleGeneratorResourceBrowser() {
	
		Set<String> resourceNames = new TreeSet<String>();
		resourceNames.add("tabView1.js");
		resourceNames.add("tabView2.js");
		resourceNames.add("subTabView/");
		mappings.put("browse:/js/tabView/", resourceNames);
		
		resourceNames = new TreeSet<String>();
		resourceNames.add("subTabView1.js");
		resourceNames.add("subTabView2.js");
		mappings.put("browse:/js/tabView/subTabView/", resourceNames);
		
		resourceNames = new TreeSet<String>();
		resourceNames.add("treeView1.js");
		resourceNames.add("treeView2.js");
		resourceNames.add("subTreeView/");
		mappings.put("browse:/js/treeView/", resourceNames);
		
		resourceNames = new TreeSet<String>();
		resourceNames.add("subTreeView1.js");
		resourceNames.add("subTreeView2.js");
		mappings.put("browse:/js/treeView/subTreeView/", resourceNames);
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ResourceBrowser#getResourceNames(java.lang.String)
	 */
	public Set<String> getResourceNames(String path) {
		
		String dirPath = path;
		if(!dirPath.endsWith("/")){
			dirPath = dirPath+"/";
		}
		
		return mappings.get(dirPath);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ResourceBrowser#isDirectory(java.lang.String)
	 */
	public boolean isDirectory(String path) {
		
		String dirPath = path;
		if(!dirPath.endsWith("/")){
			dirPath = dirPath+"/";
		}
		return mappings.containsKey(dirPath);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#createResource(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	public Reader createResource(GeneratorContext context) {
		
		String generatedResource = new String(";alert('GeneratedPath : "+context.getPath()+"');");
		return new StringReader(generatedResource);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.BaseResourceGenerator#getResolver()
	 */
	public ResourceGeneratorResolver getResolver() {
		return new PrefixedPathResolver("browse");
	}

}
