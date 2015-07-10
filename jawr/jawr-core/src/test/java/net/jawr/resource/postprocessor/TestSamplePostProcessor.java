package net.jawr.resource.postprocessor;

import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;

public class TestSamplePostProcessor implements ResourceBundlePostProcessor {

	public StringBuffer postProcessBundle(BundleProcessingStatus status, StringBuffer bundleString) {
		
		bundleString.append("\n/** Cool isn't it **/");
		return bundleString;
	}

}