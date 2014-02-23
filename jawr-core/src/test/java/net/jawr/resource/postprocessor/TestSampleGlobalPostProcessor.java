/**
 * 
 */
package net.jawr.resource.postprocessor;

import java.util.List;

import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.global.postprocessor.GlobalPostProcessingContext;
import net.jawr.web.resource.bundle.global.processor.AbstractChainedGlobalProcessor;

/**
 * @author ibrahim Chaehoi
 *
 */
public class TestSampleGlobalPostProcessor extends
AbstractChainedGlobalProcessor<GlobalPostProcessingContext> {

	public TestSampleGlobalPostProcessor() {
		super("sampleGPost");
	}

	@Override
	public void processBundles(GlobalPostProcessingContext ctx,
			List<JoinableResourceBundle> bundles) {
		// DO Nothing
		
	}

	

}
