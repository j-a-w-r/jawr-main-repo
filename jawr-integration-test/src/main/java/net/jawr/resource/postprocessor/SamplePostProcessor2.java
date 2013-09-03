/**
 * 
 */
package net.jawr.resource.postprocessor;

import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;

/**
 * A sample post processor
 * @author Ibrahim Chaehoi
 *
 */
public class SamplePostProcessor2 implements ResourceBundlePostProcessor {

	public StringBuffer postProcessBundle(BundleProcessingStatus status, StringBuffer bundleString) {
		
		if(status.getLastPathAdded().equals("/css/one.css")){
			bundleString.append("\n.generated_class2 { color : #FFAD00; }\n");
		}
		return bundleString;
	}

}
