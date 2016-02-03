/**
 * Copyright 2007-2016 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.iterator.BundlePath;
import net.jawr.web.resource.bundle.mappings.BundlePathMapping;
import net.jawr.web.resource.bundle.mappings.BundlePathMappingBuilder;
import net.jawr.web.resource.bundle.mappings.FilePathMapping;
import net.jawr.web.resource.bundle.mappings.PathMapping;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.bundle.variant.VariantUtils;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.util.StringUtils;

/**
 * Basic implementation of JoinableResourceBundle.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 * 
 */
public class JoinableResourceBundleImpl implements JoinableResourceBundle {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(JoinableResourceBundleImpl.class);

	/** The name of the bundle used in the configuration properties */
	private String name;

	/** The ID for this bundle. The URL, which will identify the bundle. */
	private String id;

	/** The inclusion pattern */
	private InclusionPattern inclusionPattern;

	/** The generator Registry */
	private GeneratorRegistry generatorRegistry;

	/** The bundle path mapping */
	protected BundlePathMappingBuilder bundlePathMappingBuilder;

	/** The bundle path mapping */
	protected BundlePathMapping bundlePathMapping;

	/** The flag indicating if the resource bundle is dirty */
	private boolean dirty;

	/** The bundle prefix */
	private String bundlePrefix;

	/** The URL prefix */
	private String urlPrefix;

	/** The IE conditional expression */
	private String explorerConditionalExpression;

	/** The alternate URL for the bundle */
	private String alternateProductionURL;

	/** The static URL to use in debug mode */
	private String debugURL;

	/** The prefix mapping for locale variant version */
	private Map<String, String> prefixMap;

	/** The map of variants */
	protected Map<String, VariantSet> variants;

	/** The list of variant keys */
	protected List<String> variantKeys;

	/** The list of bundle dependencies */
	protected List<JoinableResourceBundle> dependencies;

	/** The file post processor */
	private ResourceBundlePostProcessor unitaryPostProcessor;

	/** The bundle post processor */
	private ResourceBundlePostProcessor bundlePostProcessor;

	/**
	 * Protected access constructor, which omits the mappings parameter.
	 * 
	 * @param id
	 *            the ID for this bundle.
	 * @param name
	 *            The unique name for this bundle.
	 * @param bundlePrefix
	 *            The bundle prefix
	 * @param fileExtension
	 *            The File extensions for this bundle.
	 * @param inclusionPattern
	 *            The Strategy for including this bundle.
	 * @param resourceReaderHandler
	 *            ResourceHandler Used to access the files and folders.
	 * @param generatorRegistry
	 *            The generator registry.
	 */
	public JoinableResourceBundleImpl(String id, String name, String bundlePrefix, String fileExtension,
			InclusionPattern inclusionPattern, ResourceReaderHandler resourceReaderHandler,
			GeneratorRegistry generatorRegistry) {
		super();

		this.inclusionPattern = inclusionPattern;
		this.generatorRegistry = generatorRegistry;
		if (generatorRegistry.isPathGenerated(id)) {
			this.id = id;
		} else {
			this.id = PathNormalizer.asPath(id);
		}
		this.name = name;
		
		if (bundlePrefix != null) {
			this.bundlePrefix = PathNormalizer.asDirPath(bundlePrefix);
		}
		
		this.bundlePathMappingBuilder = createBundlePathMappingBuilder(fileExtension, resourceReaderHandler, generatorRegistry);
		this.bundlePathMapping = new BundlePathMapping(this);

		prefixMap = new ConcurrentHashMap<String, String>();
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID of this bundle
	 * @param name
	 *            Unique name for this bundle.
	 * @param bundlePrefix
	 *            The bundle prefix
	 * @param fileExtension
	 *            File extensions for this bundle.
	 * @param inclusionPattern
	 *            Strategy for including this bundle.
	 * @param pathMappings
	 *            Set Strings representing the folders or files to include,
	 *            possibly with wildcards.
	 * @param resourceReaderHandler
	 *            Used to access the files and folders.
	 * @param generatorRegistry
	 *            the generator registry
	 */
	public JoinableResourceBundleImpl(String id, String name, String bundlePrefix, String fileExtension,
			InclusionPattern inclusionPattern, List<String> pathMappings, ResourceReaderHandler resourceReaderHandler,
			GeneratorRegistry generatorRegistry) {
		this(id, name, bundlePrefix, fileExtension, inclusionPattern, resourceReaderHandler, generatorRegistry);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Adding mapped files for bundle " + id);
		}

		this.bundlePathMappingBuilder = createBundlePathMappingBuilder(fileExtension, resourceReaderHandler, generatorRegistry);
		this.bundlePathMapping = bundlePathMappingBuilder.build(pathMappings);
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Added " + this.bundlePathMapping.getItemPathList().size() + " files and "
					+ bundlePathMapping.getLicensesPathList().size() + " licenses for the bundle " + id);
		}

	}

	/**
	 * Returns the bundle path mapping builder
	 * @param fileExtension the file extension
	 * @param resourceReaderHandler the resource reader handler
	 * @param generatorRegistry the generator registry
	 */
	protected BundlePathMappingBuilder createBundlePathMappingBuilder(String fileExtension, ResourceReaderHandler resourceReaderHandler,
			GeneratorRegistry generatorRegistry) {
		return new BundlePathMappingBuilder(this, fileExtension, generatorRegistry, resourceReaderHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getId()
	 */
	public String getId() {
		return this.id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.JoinableResourceBundle#getBundlePrefix()
	 */
	public String getBundlePrefix() {
		return bundlePrefix;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#isComposite()
	 */
	public boolean isComposite() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#
	 * getUnitaryPostProcessor ()
	 */
	public ResourceBundlePostProcessor getUnitaryPostProcessor() {
		return unitaryPostProcessor;
	}

	/**
	 * Sets the unitary post processor
	 * 
	 * @param unitaryPostProcessor
	 *            the unitary post processor
	 */
	public void setUnitaryPostProcessor(ResourceBundlePostProcessor unitaryPostProcessor) {
		this.unitaryPostProcessor = unitaryPostProcessor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#
	 * getBundlePostProcessor ()
	 */
	public ResourceBundlePostProcessor getBundlePostProcessor() {
		return bundlePostProcessor;
	}

	/**
	 * Sets the bundle post processor
	 * 
	 * @param bundlePostProcessor
	 *            the post processor to set
	 */
	public void setBundlePostProcessor(ResourceBundlePostProcessor bundlePostProcessor) {
		this.bundlePostProcessor = bundlePostProcessor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#
	 * getExplorerConditionalExpression()
	 */
	public String getExplorerConditionalExpression() {
		return explorerConditionalExpression;
	}

	/**
	 * Set the conditional comment expression.
	 * 
	 * @param explorerConditionalExpression
	 */
	public void setExplorerConditionalExpression(String explorerConditionalExpression) {
		this.explorerConditionalExpression = explorerConditionalExpression;
	}

	/**
	 * Set the list of variants for variant resources
	 * 
	 * @param variantSets
	 */
	public void setVariants(Map<String, VariantSet> variantSets) {

		if (variantSets != null) {
			this.variants = new TreeMap<String, VariantSet>(variantSets);
			variantKeys = VariantUtils.getAllVariantKeys(this.variants);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getVariants()
	 */
	public Map<String, VariantSet> getVariants() {

		return variants;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.JoinableResourceBundle#getLocaleVariantKeys
	 * ()
	 */
	public List<String> getVariantKeys() {

		return variantKeys;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#
	 * getAlternateProductionURL ()
	 */
	public String getAlternateProductionURL() {
		return this.alternateProductionURL;
	}

	/**
	 * Sets the alternate production URL
	 * 
	 * @param alternateProductionURL
	 *            the alternateProductionURL to set
	 */
	public void setAlternateProductionURL(String alternateProductionURL) {
		this.alternateProductionURL = alternateProductionURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getDebugURL()
	 */
	@Override
	public String getDebugURL() {
		return this.debugURL;
	}

	/**
	 * Sets the debug URL
	 * 
	 * @param debugURL
	 *            the debugURL to set
	 */
	public void setDebugURL(String debugURL) {
		this.debugURL = debugURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.JoinableResourceBundle#belongsTobundle(java
	 * .lang.String)
	 */
	public boolean belongsToBundle(String itemPath) {

		boolean belongsToBundle = false;

		for (BundlePath path : bundlePathMapping.getItemPathList()) {
			if (path.getPath().equals(itemPath)) {
				belongsToBundle = true;
				break;
			}
		}
		if (!belongsToBundle) {
			for (BundlePath path : bundlePathMapping.getItemDebugPathList()) {
				if (path.getPath().equals(itemPath)) {
					belongsToBundle = true;
					break;
				}
			}
		}

		return belongsToBundle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.JoinableResourceBundle#getInclusionPattern()
	 */
	public InclusionPattern getInclusionPattern() {
		return this.inclusionPattern;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.JoinableResourceBundle#setMappings(java.
	 * util.List)
	 */
	public void setMappings(List<String> pathMappings) {

		this.bundlePathMapping = this.bundlePathMappingBuilder.build(pathMappings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getMappings()
	 */
	@Override
	public List<PathMapping> getMappings() {
		return this.bundlePathMapping.getPathMappings();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.JoinableResourceBundle#getItemPathList()
	 */
	public List<BundlePath> getItemPathList() {
		return bundlePathMapping.getItemPathList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.JoinableResourceBundle#getItemDebugPathList
	 * ()
	 */
	public List<BundlePath> getItemDebugPathList() {
		return bundlePathMapping.getItemDebugPathList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.JoinableResourceBundle#getFilePathMappings
	 * ()
	 */
	public List<FilePathMapping> getFilePathMappings() {
		return bundlePathMapping.getFilePathMappings();
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getLinkedFilePathMappings()
	 */
	@Override
	public List<FilePathMapping> getLinkedFilePathMappings() {
		return bundlePathMapping.getLinkedFilePathMappings();
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#setLinkedFilePathMappings(java.util.List)
	 */
	@Override
	public void setLinkedFilePathMappings(List<FilePathMapping> mappings) {
		this.bundlePathMapping.setLinkedFilePathMappings(mappings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.JoinableResourceBundle#getItemPathList(java
	 * .lang.String)
	 */
	public List<BundlePath> getItemDebugPathList(Map<String, String> variants) {

		if (StringUtils.isNotEmpty(debugURL)) {
			return bundlePathMapping.getItemDebugPathList();
		}
		return getItemPathList(bundlePathMapping.getItemDebugPathList(), variants);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.JoinableResourceBundle#getItemPathList(java
	 * .lang.String)
	 */
	public List<BundlePath> getItemPathList(Map<String, String> variants) {
		return getItemPathList(bundlePathMapping.getItemPathList(), variants);
	}

	/**
	 * Filters the bundlePath list given in parameter using the specified
	 * variants
	 * 
	 * @param itemList
	 *            the list of bundlePath
	 * @param variants
	 *            the variants
	 * @return the filtered list of bundlePath
	 */
	private List<BundlePath> getItemPathList(List<BundlePath> itemList, Map<String, String> variants) {
		if (variants == null || variants.isEmpty()) {
			return itemList;
		}

		List<BundlePath> rets = new ArrayList<BundlePath>();

		for (Iterator<BundlePath> it = itemList.iterator(); it.hasNext();) {
			BundlePath bundlePath = it.next();
			String path = bundlePath.getPath();
			if (generatorRegistry.isPathGenerated(path)) {
				Set<String> variantTypes = generatorRegistry.getGeneratedResourceVariantTypes(path);
				String variantKey = VariantUtils.getVariantKey(variants, variantTypes);
				if (StringUtils.isNotEmpty(variantKey)) {
					rets.add(new BundlePath(bundlePath.getBundlePrefix(),
							VariantUtils.getVariantBundleName(path, variantKey, true)));
				} else {
					rets.add(bundlePath);
				}
			} else {
				rets.add(bundlePath);
			}
		}
		return rets;
	}

	/**
	 * Sets the bundle dependencies
	 * 
	 * @param dependencies
	 *            the bundle dependencies
	 */
	public void setDependencies(List<JoinableResourceBundle> dependencies) {
		this.dependencies = dependencies;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.JoinableResourceBundle#getDependencies()
	 */
	public List<JoinableResourceBundle> getDependencies() {
		return dependencies;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.JoinableResourceBundle#getLicensesPathList()
	 */
	public Set<String> getLicensesPathList() {
		return this.bundlePathMapping.getLicensesPathList();
	}

	/**
	 * Sets the licence path list
	 * 
	 * @param licencePathList
	 *            the list to set
	 */
	public void setLicensesPathList(Set<String> licencePathList) {
		this.bundlePathMapping.setLicensesPathList(licencePathList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.JoinableResourceBundle#getURLPrefix(java
	 * .util.Map)
	 */
	public String getURLPrefix(Map<String, String> variants) {

		if (null == this.urlPrefix)
			throw new IllegalStateException("The bundleDataHashCode must be set before accessing the url prefix.");

		if (variants != null && !variants.isEmpty()) {
			String key = getAvailableVariant(variants);
			if (StringUtils.isNotEmpty(key)) {
				return prefixMap.get(key) + "." + key + "/";
			}
		}
		return this.urlPrefix + "/";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.JoinableResourceBundle#getBundleDataHashCode
	 * ()
	 */
	public String getBundleDataHashCode(String variantKey) {
		if (StringUtils.isEmpty(variantKey)) {
			return this.urlPrefix;
		} else {
			return (String) prefixMap.get(variantKey);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.JoinableResourceBundle#setBundleDataHashCode
	 * (java.lang.String, java.lang.String)
	 */
	public void setBundleDataHashCode(String variantKey, String bundleDataHashCode) {

		String prefix = bundleDataHashCode;

		if (StringUtils.isEmpty(variantKey)) {
			this.urlPrefix = prefix;
		} else {
			prefixMap.put(variantKey, prefix);
		}
	}

	/**
	 * Resolves a registered path from a variant key.
	 * 
	 * @param variantKey
	 *            the requested variant key
	 * @return the variant key to use
	 */
	private String getAvailableVariant(Map<String, String> curVariants) {

		String variantKey = null;
		if (variants != null) {
			Map<String, String> availableVariants = generatorRegistry.getAvailableVariantMap(variants, curVariants);
			variantKey = VariantUtils.getVariantKey(availableVariants);
		}

		return variantKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "JoinableResourceBundleImpl [id=" + id + ", name=" + name + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.JoinableResourceBundle#setDirty(boolean)
	 */
	@Override
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return dirty;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#resetBundleMapping()
	 */
	public void resetBundleMapping(){
		bundlePathMapping = bundlePathMappingBuilder.build();
	}
}
