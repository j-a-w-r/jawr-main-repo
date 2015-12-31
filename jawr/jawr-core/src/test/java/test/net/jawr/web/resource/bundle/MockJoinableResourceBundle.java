/**
 * Copyright 2010 Ibrahim Chaehoi
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package test.net.jawr.web.resource.bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.PathMapping;
import net.jawr.web.resource.bundle.iterator.BundlePath;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.variant.VariantSet;

/**
 * @author Ibrahim Chaehoi
 *
 */
//TODO Remove this and use a Mockito.Mock instead
public class MockJoinableResourceBundle implements JoinableResourceBundle {

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#belongsToBundle(java.lang.String)
	 */
	@Override
	public boolean belongsToBundle(String itemPath) {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getAlternateProductionURL()
	 */
	@Override
	public String getAlternateProductionURL() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getDebugURL()
	 */
	@Override
	public String getDebugURL() {
		return null;
	}
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getBundleDataHashCode(java.lang.String)
	 */
	@Override
	public String getBundleDataHashCode(String variantKey) {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getBundlePostProcessor()
	 */
	@Override
	public ResourceBundlePostProcessor getBundlePostProcessor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getDependencies()
	 */
	@Override
	public List<JoinableResourceBundle> getDependencies() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getExplorerConditionalExpression()
	 */
	@Override
	public String getExplorerConditionalExpression() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getId()
	 */
	@Override
	public String getId() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getInclusionPattern()
	 */
	@Override
	public InclusionPattern getInclusionPattern() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getItemPathList()
	 */
	@Override
	public List<BundlePath> getItemPathList() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getItemPathList(java.util.Map)
	 */
	@Override
	public List<BundlePath> getItemPathList(Map<String, String> variants) {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getLicensesPathList()
	 */
	@Override
	public Set<String> getLicensesPathList() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getName()
	 */
	@Override
	public String getName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getURLPrefix(java.util.Map)
	 */
	@Override
	public String getURLPrefix(Map<String, String> variants) {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getUnitaryPostProcessor()
	 */
	@Override
	public ResourceBundlePostProcessor getUnitaryPostProcessor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getVariantKeys()
	 */
	@Override
	public List<String> getVariantKeys() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getVariants()
	 */
	@Override
	public Map<String, VariantSet> getVariants() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#isComposite()
	 */
	@Override
	public boolean isComposite() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#setBundleDataHashCode(java.lang.String, java.lang.String)
	 */
	@Override
	public void setBundleDataHashCode(String variantKey,
			String bundleDataHashCode) {
	
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#setDependencies(java.util.List)
	 */
	@Override
	public void setDependencies(List<JoinableResourceBundle> bundleDependencies) {
	
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#setMappings(java.util.List)
	 */
	@Override
	public void setMappings(List<String> mappings) {
	
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#setVariants(java.util.Map)
	 */
	@Override
	public void setVariants(Map<String, VariantSet> variants) {
		
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#setBundlePostProcessor(net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor)
	 */
	@Override
	public void setBundlePostProcessor(
			ResourceBundlePostProcessor bundlePostProcessor) {
		
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#setUnitaryPostProcessor(net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor)
	 */
	@Override
	public void setUnitaryPostProcessor(
			ResourceBundlePostProcessor unitaryPostProcessor) {
		
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getItemDebugPathList()
	 */
	@Override
	public List<BundlePath> getItemDebugPathList() {
		return new ArrayList<BundlePath>();
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getItemDebugPathList(java.util.Map)
	 */
	@Override
	public List<BundlePath> getItemDebugPathList(Map<String, String> variants) {
		return new ArrayList<BundlePath>();
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getBundlePrefix()
	 */
	@Override
	public String getBundlePrefix() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getMappings()
	 */
	@Override
	public List<PathMapping> getMappings() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#setDirty(boolean)
	 */
	@Override
	public void setDirty(boolean dirty) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#isDirty()
	 */
	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

}
