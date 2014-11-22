/**
 * 
 */
package net.jawr.web.resource.postprocessor;

import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;

/**
 * @author ibrahim
 *
 */
public class SamplePostProcessor2 implements ResourceBundlePostProcessor {

	public StringBuffer postProcessBundle(BundleProcessingStatus status, StringBuffer bundleString) {
		
		bundleString.append("\n/** Too Cool isn't it **/");
		return bundleString;
	}

}
