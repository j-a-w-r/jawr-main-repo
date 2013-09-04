/**
 * Copyright 2012  Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.js;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.generator.AbstractJavascriptGenerator;
import net.jawr.web.resource.bundle.generator.ConfigurationAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.PostInitializationAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;

/**
 * This class defines the coffee script generator
 * 
 * @author ibrahim Chaehoi
 */
public class CoffeeScriptGenerator extends AbstractJavascriptGenerator implements ConfigurationAwareResourceGenerator, PostInitializationAwareResourceGenerator, ICoffeeScriptGenerator {

	/** The resolver */
	private ResourceGeneratorResolver resolver;
	
	/** The JS global scope */
	private Scriptable globalScope;
	
	/** The jawr config */
	private JawrConfig config;
	
	/** The coffee script options */
	private String options;
	
	/**
	 * Constructor 
	 */
	public CoffeeScriptGenerator() {
		resolver = ResourceGeneratorResolverFactory.createSuffixResolver("coffee");
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ConfigurationAwareResourceGenerator#setConfig(net.jawr.web.config.JawrConfig)
	 */
	public void setConfig(JawrConfig config) {
		this.config = config;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.PostInitializationAwareResourceGenerator#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {
		
		options = config.getProperty("jawr.js.generator.coffee.script.options", "");
		
		// Load JavaScript Script Engine
		String script = config.getProperty("jawr.js.generator.coffee.script.location", "net/jawr/web/resource/bundle/generator/js/coffee-script.js");
		InputStream inputStream = getResourceInputStream(script);
        try {
            try {
                Reader reader = new InputStreamReader(inputStream, "UTF-8");
                try {
                    Context context = Context.enter();
                    context.setOptimizationLevel(-1); // Without this, Rhino hits a 64K bytecode limit and fails
                    try {
                        globalScope = context.initStandardObjects();
                        context.evaluateReader(globalScope, reader, "coffee-script.js", 0, null);
                    } finally {
                        Context.exit();
                    }
                } finally {
                	IOUtils.close(reader);
                }
            } catch (UnsupportedEncodingException e) {
                throw new BundlingProcessException(e); // This should never happen
            } finally {
                IOUtils.close(inputStream);
            }
        } catch (IOException e) {
            throw new BundlingProcessException(e); // This should never happen
        }
	}
	
	/**
	 * Returns the resource input stream
	 * @param path the resource path
	 * @return the resource input stream
	 */
	private InputStream getResourceInputStream(String path) {
		InputStream is = config.getContext().getResourceAsStream(path);
		if(is == null){
			try {
				is = ClassLoaderResourceUtils.getResourceAsStream(path, this);
			} catch (FileNotFoundException e) {
				throw new BundlingProcessException(e);
			}
		}
		
		return is;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.BaseResourceGenerator#getResolver()
	 */
	public ResourceGeneratorResolver getResolver() {
		return resolver;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.TextResourceGenerator#createResource(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	public Reader createResource(GeneratorContext context) {
		
		String path = context.getPath();
		Reader rd = null;
		try {
			List<Class<?>> excluded = new ArrayList<Class<?>>();
			excluded.add(ICoffeeScriptGenerator.class);
			rd = context.getResourceReaderHandler().getResource(path, false, excluded);
			StringWriter swr = new StringWriter();
			IOUtils.copy(rd, swr);
			
			String result = compile(swr.toString());
			rd = new StringReader(result);
			
		} catch (ResourceNotFoundException e) {
			throw new BundlingProcessException(e);
		} catch (IOException e) {
			throw new BundlingProcessException(e);
		}
		
		return rd;
	}
	
	/**
	 * Compile the CoffeeScript source to a JS source 
	 * @param coffeeScriptSource the CoffeeScript source
	 * @return the JS source 
	 */
	public String compile (String coffeeScriptSource) {
        
		Context context = Context.enter();
        try {
        	
            Scriptable compileScope = context.newObject(globalScope);
            compileScope.setParentScope(globalScope);
            compileScope.put("coffeeScriptSource", compileScope, coffeeScriptSource);
            try {
                return (String)context.evaluateString(compileScope, String.format("CoffeeScript.compile(coffeeScriptSource, '%s');", options),
                        "JCoffeeScriptCompiler", 0, null);
            } catch (JavaScriptException e) {
                throw new BundlingProcessException(e);
            }
        } finally {
            Context.exit();
        }
    }
}
