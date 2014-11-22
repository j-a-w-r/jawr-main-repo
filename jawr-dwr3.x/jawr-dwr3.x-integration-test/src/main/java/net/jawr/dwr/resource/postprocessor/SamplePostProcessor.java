/**
 * 
 */
package net.jawr.dwr.resource.postprocessor;

import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;

/**
 * A sample post processor
 * @author Ibrahim Chaehoi
 */
public class SamplePostProcessor implements ResourceBundlePostProcessor {

	public StringBuffer postProcessBundle(BundleProcessingStatus status, StringBuffer bundleString) {
		
		if(status.getLastPathAdded().equals("jar:fwk/css/temp.css")){
			bundleString.append("\n.generated_class { color : #DA00FF; }\n");
		}
		return bundleString;
	}

}
