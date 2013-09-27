/**
 * Copyright 2007-2012 Jordi Hernández Sellés, Ibrahim Chaehoi
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

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.sorting.SortFileParser;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.bundle.variant.VariantUtils;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.util.StringUtils;

import org.apache.log4j.Logger;

/**
 * Basic implementation of JoinableResourceBundle.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 * 
 */
public class JoinableResourceBundleImpl implements JoinableResourceBundle {

	/** The logger */
	private static final Logger LOGGER = Logger
			.getLogger(JoinableResourceBundleImpl.class);

	/** The name of the bundle used in the configuration properties */
	private String name;

	/** The ID for this bundle. The URL, which will identify the bundle. */
	private String id;

	/** The inclusion pattern */
	private InclusionPattern inclusionPattern;

	/** The list of path mappings. It could contains directory mapping like 'myPath/**' */
	private List<String> pathMappings;

	/** The final item path list containing all the resource linked to this bundle */
	protected List<String> itemPathList;

	/** The final item path list containing all the resource linked to this bundle for debug mode */
	protected List<String> itemDebugPathList;

	/** The resource reader handle */
	private ResourceReaderHandler resourceReaderHandler;
	
	/** The generator Registry */
	private GeneratorRegistry generatorRegistry;

	/** The licence path list */
	protected Set<String> licensesPathList;

	/** The file extensions allowed in the bundle */
	private String fileExtension;

	/** The URL prefix */
	private String urlPrefix;

	/** The IE conditional expression */
	private String explorerConditionalExpression;

	/** The alternate URL for the bundle */
	private String alternateProductionURL;

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
	 * @param id the ID for this bundle.
	 * @param name String Unique name for this bundle.
	 * @param fileExtension String File extensions for this bundle.
	 * @param inclusionPattern InclusionPattern Strategy for including this bundle.
	 * @param resourceReaderHandler ResourceHandler Used to access the files and folders.
	 * @param generatorRegistry The generator registry.
	 */
	public JoinableResourceBundleImpl(String id, String name,
			String fileExtension, InclusionPattern inclusionPattern,
			ResourceReaderHandler resourceReaderHandler, GeneratorRegistry generatorRegistry) {
		super();

		this.inclusionPattern = inclusionPattern;
		this.generatorRegistry = generatorRegistry;
		if(generatorRegistry.isPathGenerated(id)){
			this.id = id;
		}else{
			this.id = PathNormalizer.asPath(id);
		}
		this.name = name;
		this.resourceReaderHandler = resourceReaderHandler;
		this.itemPathList = new CopyOnWriteArrayList<String>();
		this.itemDebugPathList = new CopyOnWriteArrayList<String>();
		this.licensesPathList = new HashSet<String>();
		if(fileExtension != null && fileExtension.length() > 0 && fileExtension.charAt(0) != '.'){
			this.fileExtension = "."+fileExtension;
		}else{
			this.fileExtension = fileExtension;
		}
		prefixMap = new ConcurrentHashMap<String, String>();

	}

	/**
	 * Constructor
	 * 
	 * @param id the ID of this bundle
	 * @param name Unique name for this bundle.
	 * @param fileExtension File extensions for this bundle.
	 * @param inclusionPattern Strategy for including this bundle.
	 * @param pathMappings Set Strings representing the folders or files to include, possibly with wildcards.
	 * @param resourceReaderHandler Used to access the files and folders.
	 * @param generatorRegistry the generator registry
	 */
	public JoinableResourceBundleImpl(String id, String name,
			String fileExtension, InclusionPattern inclusionPattern,
			List<String> pathMappings, ResourceReaderHandler resourceReaderHandler, GeneratorRegistry generatorRegistry) {
		this(id, name, fileExtension, inclusionPattern, resourceReaderHandler, generatorRegistry);

		if (LOGGER.isDebugEnabled()){
			LOGGER.debug("Adding mapped files for bundle " + id);
		}
		this.pathMappings = pathMappings;

		initPathList();
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug("Added " + this.itemPathList.size() + " files and "
					+ licensesPathList.size() + " licenses for the bundle "
					+ id);
		}
	
	}

	/**
	 * Detects all files that belong to this bundle and adds them to the items path list.
	 */
	private void initPathList() {
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug("Creating bundle path List for " + this.id);
		}
		
		for (Iterator<String> it = pathMappings.iterator(); it.hasNext();) {
			String pathMapping = it.next();
			boolean isGeneratedPath = generatorRegistry.isPathGenerated(pathMapping);
			
			// Handle generated resources
			// path ends in /, the folder is included without subfolders
			if (pathMapping.endsWith("/")) {
				addItemsFromDir(pathMapping, false);
			}
			// path ends in /, the folder is included with all subfolders
			else if (pathMapping.endsWith("/**")) {
				addItemsFromDir(pathMapping.substring(0, pathMapping
						.lastIndexOf("**")), true);
			} else if (pathMapping.endsWith(fileExtension)) {
				addPathMapping(asPath(pathMapping, isGeneratedPath));
			} else if (generatorRegistry.isPathGenerated(pathMapping)) {
				addPathMapping(pathMapping);
			}else if (pathMapping.endsWith(LICENSES_FILENAME)) {
				licensesPathList.add(asPath(pathMapping, isGeneratedPath));
			} else
				throw new BundlingProcessException("Wrong mapping [" + pathMapping + "] for bundle ["
						+ this.name + "]. Please check configuration. ");
		}
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug("Finished creating bundle path List for " + this.id);
		}
	}

	private void addPathMapping(String pathMapping) {
		if(!getInclusionPattern().isIncludeOnlyOnDebug()){
			itemPathList.add(pathMapping);
		}
			
		if(!getInclusionPattern().isExcludeOnDebug()){
			itemDebugPathList.add(pathMapping);
		}
	}

	/**
	 * Adds all the resources within a path to the item path list.
	 * 
	 * @param dirName
	 * @param addSubDirs boolean If subfolders will be included. In such case, every folder below the path is included.
	 */
	protected void addItemsFromDir(String dirName, boolean addSubDirs) {
		Set<String> resources = resourceReaderHandler.getResourceNames(dirName);
		boolean isGeneratedPath = generatorRegistry.isPathGenerated(dirName);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Adding " + resources.size() + " resources from path ["
					+ dirName + "] to bundle " + getId());
		}

		// If the directory contains a sorting file, it is used to order the resources.
		if (resources.contains(SORT_FILE_NAME)
				|| resources.contains("/" + SORT_FILE_NAME)) {
			
			String sortFilePath = joinPaths(dirName,
					SORT_FILE_NAME, isGeneratedPath);
			
			Reader reader;
			try {
				reader = resourceReaderHandler.getResource(sortFilePath);
			} catch (ResourceNotFoundException e) {
				throw new BundlingProcessException(
						"Unexpected ResourceNotFoundException when reading a sorting file["
								+ sortFilePath + "]", e);
			}
			
			SortFileParser parser = new SortFileParser(reader, resources, dirName);

			List<String> sortedResources = parser.getSortedResources();
			for (Iterator<String> it = sortedResources.iterator(); it.hasNext();) {
				String resourceName = (String) it.next();
				
				// Add subfolders or files
				if (resourceName.endsWith(fileExtension) || generatorRegistry.isPathGenerated(resourceName)) {
					addPathMapping(asPath(resourceName, isGeneratedPath));
					
					if (LOGGER.isDebugEnabled())
						LOGGER
								.debug("Added to item path list from the sorting file:"
										+ resourceName);
				} else if (addSubDirs
						&& resourceReaderHandler.isDirectory(resourceName))
					addItemsFromDir(resourceName, true);
			}
		}

		// Add licenses file
		if (resources.contains(LICENSES_FILENAME)
				|| resources.contains("/" + LICENSES_FILENAME)) {
			licensesPathList.add(joinPaths(dirName,
					LICENSES_FILENAME, isGeneratedPath));
		}

		// Add remaining resources (remaining after sorting, or all if no sort file present)
		List<String> folders = new ArrayList<String>();
		for (Iterator<String> it = resources.iterator(); it.hasNext();) {
			String resourceName = (String) it.next();
			String resourcePath = joinPaths(dirName,
					resourceName, isGeneratedPath);
			
			boolean resourceIsDir = resourceReaderHandler.isDirectory(resourcePath);
			if (addSubDirs
					&& resourceIsDir){
				folders.add(resourceName);
			}else if (resourcePath.endsWith(fileExtension) || (generatorRegistry.isPathGenerated(resourcePath) && !resourceIsDir)) {
				addPathMapping(asPath(resourcePath, isGeneratedPath));
				
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Added to item path list:"
							+ asPath(resourcePath, isGeneratedPath));
			} 
		}

		// Add subfolders if requested. Subfolders are added last unless specified in sorting file.
		if (addSubDirs) {
			for (Iterator<String> it = folders.iterator(); it.hasNext();) {
				String folderName = (String) it.next();
				addItemsFromDir(joinPaths(dirName, folderName, isGeneratedPath),
						true);
			}
		}
	}

	/**
	 * Normalizes a path and adds a separator at its start, if it's not a generated resource. 
	 * @param path the path
	 * @param generatedResource the flag indicating if the resource has been generated
	 * @return the normalized path
	 */
	private String asPath(String path, boolean generatedResource){
		
		String result = path;
		if(!generatedResource){
			result = PathNormalizer.asPath(path);
		}
		return result;
	}
	
	/**
	 * Normalizes two paths and joins them as a single path. 
	 * @param prefix the path prefix
	 * @param path the path
	 * @param generatedResource the flag indicating if the resource has been generated
	 * @return the normalized path
	 */
	private String joinPaths(String dirName, String folderName, boolean generatedResource){
		
		return PathNormalizer.joinPaths(dirName, folderName, generatedResource);
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
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#isComposite()
	 */
	public boolean isComposite() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getUnitaryPostProcessor()
	 */
	public ResourceBundlePostProcessor getUnitaryPostProcessor() {
		return unitaryPostProcessor;
	}

	/**
	 * Sets the unitary post processor
	 * 
	 * @param unitaryPostProcessor the unitary post processor
	 */
	public void setUnitaryPostProcessor(
			ResourceBundlePostProcessor unitaryPostProcessor) {
		this.unitaryPostProcessor = unitaryPostProcessor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getBundlePostProcessor()
	 */
	public ResourceBundlePostProcessor getBundlePostProcessor() {
		return bundlePostProcessor;
	}

	/**
	 * Sets the bundle post processor
	 * 
	 * @param bundlePostProcessor the post processor to set
	 */
	public void setBundlePostProcessor(
			ResourceBundlePostProcessor bundlePostProcessor) {
		this.bundlePostProcessor = bundlePostProcessor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getExplorerConditionalExpression()
	 */
	public String getExplorerConditionalExpression() {
		return explorerConditionalExpression;
	}

	/**
	 * Set the conditional comment expression.
	 * 
	 * @param explorerConditionalExpression
	 */
	public void setExplorerConditionalExpression(
			String explorerConditionalExpression) {
		this.explorerConditionalExpression = explorerConditionalExpression;
	}

	/**
	 * Set the list of variants for variant resources
	 * 
	 * @param variantSets
	 */
	public void setVariants(Map<String, VariantSet> variantSets) {
		
		if(variantSets != null){
			this.variants = new TreeMap<String, VariantSet>(variantSets);
			variantKeys = VariantUtils.getAllVariantKeys(this.variants);	
		}
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getVariants()
	 */
	public Map<String, VariantSet> getVariants() {
		
		return variants;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getLocaleVariantKeys()
	 */
	public List<String> getVariantKeys() {
		
		return variantKeys;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getAlternateProductionURL()
	 */
	public String getAlternateProductionURL() {
		return this.alternateProductionURL;
	}

	/**
	 * Sets the alternate production URL
	 * 
	 * @param alternateProductionURL the alternateProductionURL to set
	 */
	public void setAlternateProductionURL(String alternateProductionURL) {
		this.alternateProductionURL = alternateProductionURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#belongsTobundle(java.lang.String)
	 */
	public boolean belongsToBundle(String itemPath) {
		return itemPathList.contains(itemPath) || itemDebugPathList.contains(itemPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getInclusionPattern()
	 */
	public InclusionPattern getInclusionPattern() {
		return this.inclusionPattern;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#setMappings(java.util.List)
	 */
	public void setMappings(List<String> pathMappings) {

		this.pathMappings = pathMappings;
		initPathList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getItemPathList()
	 */
	public List<String> getItemPathList() {
		return itemPathList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getItemDebugPathList()
	 */
	public List<String> getItemDebugPathList() {
		return itemDebugPathList;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getItemPathList(java.lang.String)
	 */
	public List<String> getItemDebugPathList(Map<String,String> variants) {
		return getItemPathList(itemDebugPathList, variants);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getItemPathList(java.lang.String)
	 */
	public List<String> getItemPathList(Map<String,String> variants) {
		return getItemPathList(itemPathList, variants);
	}
	
	
	private List<String> getItemPathList(List<String> itemList, Map<String,String> variants) {
			if (variants == null || variants.isEmpty())
			return itemList;

		List<String> rets = new ArrayList<String>();
		
		for (Iterator<String> it = itemList.iterator(); it.hasNext();) {
			String path = (String) it.next();
			if (generatorRegistry.isPathGenerated(path)) {
				Set<String> variantTypes = generatorRegistry.getGeneratedResourceVariantTypes(path);
				String variantKey = VariantUtils.getVariantKey(variants, variantTypes);
				if(StringUtils.isNotEmpty(variantKey)){
					rets.add(VariantUtils.getVariantBundleName(path, variantKey));
				}else{
					rets.add(path);
				}
			} else{
				rets.add(path);
			}
		}
		return rets;
	}

	/**
	 * Sets the bundle dependencies
	 * @param dependencies the bundle dependencies
	 */
	public void setDependencies(List<JoinableResourceBundle> dependencies) {
		this.dependencies = dependencies;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getDependencies()
	 */
	public List<JoinableResourceBundle> getDependencies() {
		return dependencies;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getLicensesPathList()
	 */
	public Set<String> getLicensesPathList() {
		return this.licensesPathList;
	}

	/**
	 * Sets the licence path list
	 * 
	 * @param licencePathList the list to set
	 */
	public void setLicensesPathList(Set<String> licencePathList) {
		this.licensesPathList = licencePathList;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getURLPrefix(java.util.Map)
	 */
	public String getURLPrefix(Map<String, String> variants) {
		
		if (null == this.urlPrefix)
			throw new IllegalStateException(
					"The bundleDataHashCode must be set before accessing the url prefix.");

		if (variants != null && !variants.isEmpty()) {
			String key = getAvailableVariant(variants);
			if (StringUtils.isNotEmpty(key)){
				return prefixMap.get(key) + "." + key + "/";
			}
		}
		return this.urlPrefix + "/";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getBundleDataHashCode()
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
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#setBundleDataHashCode(java.lang.String, java.lang.String)
	 */
	public void setBundleDataHashCode(String variantKey,
			String bundleDataHashCode) {

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
	 * @param variantKey the requested variant key
	 * @return the variant key to use
	 */
	private String getAvailableVariant(Map<String, String> curVariants) {
		
		String variantKey = null;
		if(variants != null){
			Map<String, String> availableVariants = generatorRegistry.getAvailableVariantMap(variants, curVariants);
			variantKey = VariantUtils.getVariantKey(availableVariants);
		}
		
		return variantKey;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "JoinableResourceBundleImpl [id=" + id + ", name=" + name + "]";
	}
	
}
