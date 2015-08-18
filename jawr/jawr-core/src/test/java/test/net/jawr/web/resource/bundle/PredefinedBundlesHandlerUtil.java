package test.net.jawr.web.resource.bundle;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundleDependencyException;
import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.resource.bundle.factory.BundlesHandlerFactory;
import net.jawr.web.resource.bundle.factory.util.ResourceBundleDefinition;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.handler.bundle.ResourceBundleHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

public class PredefinedBundlesHandlerUtil {

	public static final ResourceBundlesHandler buildSingleBundleHandler(
			ResourceReaderHandler handler,
			ResourceBundleHandler rsBundleHandler, JawrConfig config)
			throws DuplicateBundlePathException, BundleDependencyException {

		BundlesHandlerFactory factory = new BundlesHandlerFactory(config);
		factory.setResourceReaderHandler(handler);
		factory.setResourceBundleHandler(rsBundleHandler);
		factory.setBaseDir("/js");
		factory.setUseSingleResourceFactory(true);
		factory.setSingleFileBundleName("script");
		factory.setBundlesType("js");
		return factory.buildResourceBundlesHandler();

	}

	public static final ResourceBundlesHandler buildSimpleBundles(
			ResourceReaderHandler handler,
			ResourceBundleHandler rsBundleHandler, String baseDir, String type,
			JawrConfig config) throws DuplicateBundlePathException,
			BundleDependencyException {

		GeneratorRegistry generatorRegistry = new GeneratorRegistry(type);
		generatorRegistry.setResourceReaderHandler(handler);
		config.setGeneratorRegistry(generatorRegistry);
		BundlesHandlerFactory factory = new BundlesHandlerFactory(config);
		factory.setResourceReaderHandler(handler);
		factory.setResourceBundleHandler(rsBundleHandler);
		factory.setBaseDir(baseDir);
		factory.setBundlesType(type);

		Set<ResourceBundleDefinition> customBundles = new HashSet<ResourceBundleDefinition>();

		ResourceBundleDefinition def = new ResourceBundleDefinition();
		def.setMappings(Collections.singletonList(baseDir + "/lib/**"));
		def.setBundleName("library");
		def.setBundleId("/library." + type);
		def.setGlobal(true);
		def.setInclusionOrder(0);
		customBundles.add(def);

		def = new ResourceBundleDefinition();
		def.setMappings(Collections.singletonList(baseDir + "/global/**"));
		def.setBundleName("global");
		def.setBundleId("/global." + type);
		def.setGlobal(true);
		def.setInclusionOrder(1);
		customBundles.add(def);

		def = new ResourceBundleDefinition();
		def.setMappings(Collections.singletonList(baseDir + "/debug/on/**"));
		def.setBundleName("debugOn");
		def.setBundleId("/debugOn." + type);
		def.setGlobal(true);
		def.setInclusionOrder(2);
		def.setDebugOnly(true);
		def.setDebugNever(false);
		customBundles.add(def);

		def = new ResourceBundleDefinition();
		def.setMappings(Collections.singletonList(baseDir + "/debug/off/**"));
		def.setBundleName("debugOff");
		def.setBundleId("/debugOff." + type);
		def.setGlobal(true);
		def.setInclusionOrder(2);
		def.setDebugOnly(false);
		def.setDebugNever(true);
		customBundles.add(def);

		factory.setBundleDefinitions(customBundles);
		factory.setUseDirMapperFactory(true);
		Set<String> excludedPaths = new HashSet<String>();
		excludedPaths.add(baseDir + "/lib");
		excludedPaths.add(baseDir + "/global");
		excludedPaths.add(baseDir + "/debug");
		factory.setExludedDirMapperDirs(excludedPaths);
		return factory.buildResourceBundlesHandler();
	}

	public static final ResourceBundlesHandler buildSimpleBundlesWithDependencies(
			ResourceReaderHandler handler,
			ResourceBundleHandler rsBundleHandler, String baseDir, String type,
			JawrConfig config) throws DuplicateBundlePathException,
			BundleDependencyException {

		GeneratorRegistry generatorRegistry = new GeneratorRegistry(type);
		generatorRegistry.setResourceReaderHandler(handler);
		config.setGeneratorRegistry(generatorRegistry);
		BundlesHandlerFactory factory = new BundlesHandlerFactory(config);
		factory.setResourceReaderHandler(handler);
		factory.setResourceBundleHandler(rsBundleHandler);
		factory.setBaseDir(baseDir);
		factory.setBundlesType(type);

		Set<ResourceBundleDefinition> customBundles = new HashSet<ResourceBundleDefinition>();

		ResourceBundleDefinition def = new ResourceBundleDefinition();
		def.setMappings(Collections.singletonList(baseDir + "/lib/**"));
		def.setBundleName("library");
		def.setBundleId("/library." + type);
		def.setGlobal(true);
		def.setInclusionOrder(0);
		customBundles.add(def);

		def = new ResourceBundleDefinition();
		def.setMappings(Collections.singletonList(baseDir + "/global/**"));
		def.setBundleName("global");
		def.setBundleId("/global." + type);
		def.setGlobal(true);
		def.setInclusionOrder(1);
		customBundles.add(def);

		def = new ResourceBundleDefinition();
		def.setMappings(Collections.singletonList(baseDir + "/debug/on/**"));
		def.setBundleName("debugOn");
		def.setBundleId("/debugOn." + type);
		def.setGlobal(true);
		def.setInclusionOrder(2);
		def.setDebugOnly(true);
		def.setDebugNever(false);
		customBundles.add(def);

		def = new ResourceBundleDefinition();
		def.setMappings(Collections.singletonList(baseDir + "/debug/off/**"));
		def.setBundleName("debugOff");
		def.setBundleId("/debugOff." + type);
		def.setGlobal(true);
		def.setInclusionOrder(2);
		def.setDebugOnly(false);
		def.setDebugNever(true);
		customBundles.add(def);
		
		def = new ResourceBundleDefinition();
		def.setMappings(Collections.singletonList(baseDir + "/prefixedBundle/**"));
		def.setBundleName("prefixedBundle");
		def.setBundleId("/prefixedBundle." + type);
		def.setBundlePrefix("/pub/");
		customBundles.add(def);
		
		def = new ResourceBundleDefinition();
		def.setMappings(Collections.singletonList(baseDir + "/dep/one/**"));
		def.setBundleName("depOne");
		def.setBundleId("/depOne." + type);
		def.setAlternateProductionURL("http://mycompany.com/one." + type);
		customBundles.add(def);
		
		def = new ResourceBundleDefinition();
		def.setMappings(Collections.singletonList(baseDir + "/dep/two/**"));
		def.setBundleName("depTwo");
		def.setBundleId("/depTwo." + type);
		def.setAlternateProductionURL("http://mycompany.com/two." + type);
		customBundles.add(def);
		
		def = new ResourceBundleDefinition();
		def.setMappings(Collections.singletonList(baseDir + "/dep/three/**"));
		def.setBundleName("depThree");
		def.setBundleId("/depThree." + type);
		def.setAlternateProductionURL("http://mycompany.com/three." + type);
		def.setDependencies(Arrays.asList("depOne", "depTwo"));
		customBundles.add(def);
		
		def = new ResourceBundleDefinition();
		def.setBundleName("externalAssetBundle");
		def.setBundleId("/externalAssetBundle." + type);
		def.setAlternateProductionURL("http://mycompany.com/externalAssetBundle.min." + type);
		def.setDebugURL("http://mycompany.com/externalAssetBundle." + type);
		customBundles.add(def);
		
		factory.setBundleDefinitions(customBundles);
		factory.setUseDirMapperFactory(true);
		
		Set<String> excludedPaths = new HashSet<String>();
		excludedPaths.add(baseDir + "/lib");
		excludedPaths.add(baseDir + "/global");
		excludedPaths.add(baseDir + "/debug");
		excludedPaths.add(baseDir + "/dep");
		excludedPaths.add(baseDir + "/prefixedBundle");
		factory.setExludedDirMapperDirs(excludedPaths);
		return factory.buildResourceBundlesHandler();
	}
	
	public static final ResourceBundlesHandler buildSimpleVariantBundles(
			ResourceReaderHandler handler,
			ResourceBundleHandler rsBundleHandler, String baseDir, String type,
			JawrConfig config) throws DuplicateBundlePathException,
			BundleDependencyException {

		config.getConfigProperties().setProperty("jawr.css.skin.default.root.dirs", "/css/themes/default/");
		if(config.getGeneratorRegistry() == null){
			GeneratorRegistry generatorRegistry = new GeneratorRegistry(type);
			generatorRegistry.setResourceReaderHandler(handler);
			config.setGeneratorRegistry(generatorRegistry);
		}
		
		// Load the skin generator
		config.getGeneratorRegistry().loadGeneratorIfNeeded("skin:/temp.css");
	    
	    BundlesHandlerFactory factory = new BundlesHandlerFactory(config);
		factory.setResourceReaderHandler(handler);
		factory.setResourceBundleHandler(rsBundleHandler);
		factory.setBaseDir(baseDir);
		factory.setBundlesType(type);

		Set<ResourceBundleDefinition> customBundles = new HashSet<ResourceBundleDefinition>();

		ResourceBundleDefinition def = new ResourceBundleDefinition();
		def.setBundleName("library");
		def.setMappings(Collections.singletonList(baseDir + "/lib/**"));
		def.setBundleId("/library." + type);
		def.setGlobal(true);
		def.setInclusionOrder(0);
		customBundles.add(def);

		def = new ResourceBundleDefinition();
		def.setBundleName("global");
		def.setMappings(Collections.singletonList(baseDir + "/global/**"));
		def.setBundleId("/global." + type);
		def.setGlobal(true);
		def.setInclusionOrder(1);
		customBundles.add(def);

		def = new ResourceBundleDefinition();
		def.setBundleName("debugOn");
		def.setMappings(Collections.singletonList(baseDir + "/debug/on/**"));
		def.setBundleId("/debugOn." + type);
		def.setGlobal(true);
		def.setInclusionOrder(2);
		def.setDebugOnly(true);
		def.setDebugNever(false);
		customBundles.add(def);

		def = new ResourceBundleDefinition();
		def.setBundleName("debugOff");
		def.setMappings(Collections.singletonList(baseDir + "/debug/off/**"));
		def.setBundleId("/debugOff." + type);
		def.setGlobal(true);
		def.setInclusionOrder(2);
		def.setDebugOnly(false);
		def.setDebugNever(true);
		customBundles.add(def);
		
		def = new ResourceBundleDefinition();
		def.setBundleName("theme");
		def.setMappings(Collections.singletonList("skin:/css/themes/default/**"));
		def.setBundleId("/theme." + type);
		Map<String, VariantSet> variants = new HashMap<String, VariantSet>();
		variants.put(JawrConstant.SKIN_VARIANT_TYPE, new VariantSet(JawrConstant.SKIN_VARIANT_TYPE, "default", Arrays.asList("default", "winter", "summer")));
		def.setVariants(variants);
		def.setInclusionOrder(2);
		customBundles.add(def);

		def = new ResourceBundleDefinition();
		def.setBundleName("bundle1");
		def.setMappings(Collections.singletonList("/css/dependencies/related/"));
		def.setBundleId("/bundle1." + type);
		customBundles.add(def);

		def = new ResourceBundleDefinition();
		def.setBundleName("bundle1_1");
		def.setMappings(Collections.singletonList("/css/dependencies/"));
		def.setBundleId("/bundle1_1." + type);
		def.setDependencies(Collections.singletonList("bundle1"));
		customBundles.add(def);
		
		factory.setBundleDefinitions(customBundles);
		factory.setUseDirMapperFactory(true);
		Set<String> excludedPaths = new HashSet<String>();
		excludedPaths.add(baseDir + "/lib");
		excludedPaths.add(baseDir + "/global");
		excludedPaths.add(baseDir + "/debug");
		factory.setExludedDirMapperDirs(excludedPaths);
		return factory.buildResourceBundlesHandler();
	}
}
