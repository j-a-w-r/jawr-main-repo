/**
 * 
 */
package net.jawr.resource.preprocessor;

import java.util.List;

import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.global.preprocessor.GlobalPreprocessingContext;
import net.jawr.web.resource.bundle.global.processor.AbstractChainedGlobalProcessor;

/**
 * @author ibrahim Chaehoi
 *
 */
public class TestSampleGlobalPreProcessor extends
AbstractChainedGlobalProcessor<GlobalPreprocessingContext> {

	public TestSampleGlobalPreProcessor() {
		super("sampleGPre");
	}

	@Override
	public void processBundles(GlobalPreprocessingContext ctx,
			List<JoinableResourceBundle> bundles) {
		// DO Nothing
		
	}

	

}
