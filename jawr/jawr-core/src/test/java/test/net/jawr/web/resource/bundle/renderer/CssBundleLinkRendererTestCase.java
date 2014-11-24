/**
 * Copyright 2010-2013 Ibrahim Chaehoi
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
package test.net.jawr.web.resource.bundle.renderer;

import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundleDependencyException;
import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.resource.bundle.renderer.BundleRendererContext;
import net.jawr.web.resource.bundle.renderer.CSSHTMLBundleLinkRenderer;
import net.jawr.web.resource.handler.bundle.ResourceBundleHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import org.mockito.Mockito;

import test.net.jawr.web.resource.bundle.PredefinedBundlesHandlerUtil;
import test.net.jawr.web.resource.bundle.handler.ResourceHandlerBasedTest;
import test.net.jawr.web.servlet.mock.MockServletContext;

/**
 * Test cases for the CSS link bundle renderer
 * 
 * @author Ibrahim Chaehoi
 */
public class CssBundleLinkRendererTestCase extends ResourceHandlerBasedTest {

	private static final String ROOT_TESTDIR = "/bundleLinkRenderer/";
	private static final String CSS_BASEDIR = "css/";
	private static final String CSS_CTX_PATH = "/ctxPathCss";
	
	private static final String CSS_PRE_TAG = "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"";
	private static final String CSS_PRE_PR_TAG = "<link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"";
    private static final String CSS_POST_TAG = "\" />";
	
	private BundleRendererContext bundleRendererCtx = null;
	
	private String getCssAlternatePrefixTag(String title){
		return "<link rel=\"alternate stylesheet\" type=\"text/css\" media=\"screen\" title=\""+title+"\" href=\"";
	}
	
	public CssBundleLinkRendererTestCase() {     
	    
	}
	
	public CSSHTMLBundleLinkRenderer getCssBundleLinkRenderer(boolean debugOn, boolean useRandomParam, String media, 
			boolean alternate, boolean displayAlternateStyles, String title){
		return getCssBundleLinkRenderer(new Properties(), debugOn, useRandomParam, media, alternate, displayAlternateStyles, title);
	}
	
	public CSSHTMLBundleLinkRenderer getCssBundleLinkRenderer(Properties props, boolean debugOn, boolean useRandomParam, String media, 
				boolean alternate, boolean displayAlternateStyles, String title){
		
		Charset charsetUtf = Charset.forName("UTF-8"); 
		
		props.setProperty("jawr.css.skin.default.root.dirs", "/css/themes/default");
		JawrConfig jawrConfig = new JawrConfig("css", props);
	    jawrConfig.setCharsetName("UTF-8");
	    jawrConfig.setServletMapping("/srvMapping");
	    jawrConfig.setCssLinkFlavor(CSSHTMLBundleLinkRenderer.FLAVORS_XHTML);
	    jawrConfig.setDebugModeOn(debugOn);
	    jawrConfig.setContext(new MockServletContext());
	    GeneratorRegistry generatorRegistry = new GeneratorRegistry("css");
	    jawrConfig.setGeneratorRegistry(generatorRegistry);
	    
	    ResourceReaderHandler rsHandler = createResourceReaderHandler(ROOT_TESTDIR,"css",charsetUtf,jawrConfig);
	    ResourceBundleHandler rsBundleHandler = createResourceBundleHandler(ROOT_TESTDIR,charsetUtf);
	    
	    ResourceBundlesHandler cssHandler = null;
	    try {
	    	cssHandler = PredefinedBundlesHandlerUtil.buildSimpleVariantBundles(rsHandler,rsBundleHandler,CSS_BASEDIR,"css", jawrConfig);
		} catch (DuplicateBundlePathException e) {
			// 
			throw new RuntimeException(e);
		} catch (BundleDependencyException e) {
			throw new RuntimeException(e);
		}
		
	    CSSHTMLBundleLinkRenderer renderer = new CSSHTMLBundleLinkRenderer();
	    renderer.init(cssHandler,useRandomParam,media,alternate, displayAlternateStyles, title);
		return renderer;
	}
	
	private String renderToString(BundleRenderer renderer, String path, BundleRendererContext ctx){
		
		ByteArrayOutputStream baOs = new ByteArrayOutputStream();
	    WritableByteChannel wrChannel = Channels.newChannel(baOs);
	    Writer writer = Channels.newWriter(wrChannel, "UTF-8");
	    String result = null;
	    try {
			renderer.renderBundleLinks(path, ctx, writer);
		    writer.close();
		    result = baOs.toString("UTF-8");
		    result = getCssLinks(result);
		} catch (IOException e) {
			fail("Exception rendering tags:" + e.getMessage());
		}
	    return result;
	}
	
	private String getCssLinks(String content) throws IOException{
		
		StringBuffer result = new StringBuffer();
		
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		while((line = reader.readLine()) != null){
			if(line.indexOf("<link") != -1){
				result.append(line.replaceAll("(\\?d=\\d+&)", "?").replaceAll("(&d=\\d+)", "")+"\n");
			}
		}
		
		return result.toString();
	}
	
	
	public void testWriteCSSBundleLinks()
	{
		CSSHTMLBundleLinkRenderer renderer = getCssBundleLinkRenderer(false, false, null, false, false, null);
		
		// Test regular link creation
	    bundleRendererCtx = new BundleRendererContext(CSS_CTX_PATH, null, false, false);
	    String result = renderToString(renderer,"/css/lib/lib.css", bundleRendererCtx);
		
		assertNotSame("No css tag written ", "", result.trim());
			
		StringTokenizer tk = new StringTokenizer(result,"\n");
		String next;
		
		assertEquals("Invalid number of tags written. ",3, tk.countTokens());
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 0:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 0:" + next, next.endsWith("/library.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 1:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 1:" + next, next.endsWith("/global.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 2:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 2:" + next, next.endsWith("/debugOff.css" + CSS_POST_TAG));
		
		// Reusing the set, we test that no repeats are allowed. 
		//result = renderToString(cssRenderer,"/css/lib/lib.css", CSS_CTX_PATH, includedBundles, false, false);
		result = renderToString(renderer,"/css/lib/lib.css", bundleRendererCtx);
		assertEquals("Tags were repeated","", result.trim());
	}
	
	public void testWriteDebugCSSBundleLinks()
	{
		CSSHTMLBundleLinkRenderer renderer = getCssBundleLinkRenderer(true, false, null, false, false, null);
		// Test regular link creation
	    bundleRendererCtx = new BundleRendererContext(CSS_CTX_PATH, null, false, false);
	    String result = renderToString(renderer,"/css/lib/lib.css", bundleRendererCtx);
		
	    assertNotSame("No css tag written ", "", result.trim());
			
		StringTokenizer tk = new StringTokenizer(result,"\n");
		String next;
		
		assertEquals("Invalid number of tags written. ",3, tk.countTokens());
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 0:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 0:" + next, next.endsWith("/css/lib/lib.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 1:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 1:" + next, next.endsWith("/css/global/global.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 2:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 2:" + next, next.endsWith("/css/debug/on/debugOn.css" + CSS_POST_TAG));
		
		// Reusing the set, we test that no repeats are allowed. 
		//result = renderToString(cssRenderer,"/css/lib/lib.css", CSS_CTX_PATH, includedBundles, false, false);
		result = renderToString(renderer,"/css/lib/lib.css", bundleRendererCtx);
		assertEquals("Tags were repeated","", result.trim());
	}
	
	public void testWriteCSSPrintBundleLinks(){

		CSSHTMLBundleLinkRenderer renderer = getCssBundleLinkRenderer(false, false, "print", false, false, null);
		bundleRendererCtx = new BundleRendererContext(CSS_CTX_PATH, null, false, false);
	    
		//globalBundleAdded = false;
		String result = renderToString(renderer,"/css/lib/lib.css", bundleRendererCtx);

		assertNotSame("No css tag written ", "", result.trim());
			
		StringTokenizer tk = new StringTokenizer(result,"\n");
		assertEquals("Invalid number of tags written. ",3, tk.countTokens());
		
		String next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 0:" + next, next.startsWith(CSS_PRE_PR_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 0:" + next, next.endsWith("library.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 1:" + next, next.startsWith(CSS_PRE_PR_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 1:" + next, next.endsWith("global.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 2:" + next, next.startsWith(CSS_PRE_PR_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 2:" + next, next.endsWith("debugOff.css" + CSS_POST_TAG));
		
		// Reusing the context, we test that no repeats are allowed. 
		result = renderToString(renderer,"/css/lib/lib.css", bundleRendererCtx);
		assertEquals("Tags were repeated","", result.trim());
	}
	
	public void testWriteDebugCSSPrintBundleLinks(){

		CSSHTMLBundleLinkRenderer renderer = getCssBundleLinkRenderer(true, false, "print", false, false, null);
		bundleRendererCtx = new BundleRendererContext(CSS_CTX_PATH, null, false, false);
	    
		//globalBundleAdded = false;
		String result = renderToString(renderer,"/css/lib/lib.css", bundleRendererCtx);

		assertNotSame("No css tag written ", "", result.trim());
			
		StringTokenizer tk = new StringTokenizer(result,"\n");
		assertEquals("Invalid number of tags written. ",3, tk.countTokens());
		
		String next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 0:" + next, next.startsWith(CSS_PRE_PR_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 0:" + next, next.endsWith("/css/lib/lib.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 1:" + next, next.startsWith(CSS_PRE_PR_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 1:" + next, next.endsWith("/css/global/global.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 2:" + next, next.startsWith(CSS_PRE_PR_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 2:" + next, next.endsWith("/css/debug/on/debugOn.css" + CSS_POST_TAG));
		
		// Reusing the context, we test that no repeats are allowed. 
		result = renderToString(renderer,"/css/lib/lib.css", bundleRendererCtx);
		assertEquals("Tags were repeated","", result.trim());
	}
	
	public void testWriteAlternateCSSBundleLinks()
	{
		CSSHTMLBundleLinkRenderer renderer = getCssBundleLinkRenderer(false, false, null, true, false, "aTitle");
		// Test regular link creation
	    bundleRendererCtx = new BundleRendererContext(CSS_CTX_PATH, new HashMap<String, String>(), false, false);
	    String result = renderToString(renderer,"/css/lib/lib.css", bundleRendererCtx);
	    
	    assertNotSame("No css tag written ", "", result.trim());
		
		String cssLinkPrefix = getCssAlternatePrefixTag("aTitle");
		StringTokenizer tk = new StringTokenizer(result,"\n");
		String next;
		
		assertEquals("Invalid number of tags written. ",3, tk.countTokens());
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 0:" + next, next.startsWith(cssLinkPrefix + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 0:" + next, next.endsWith("/library.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 1:" + next, next.startsWith( cssLinkPrefix + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 1:" + next, next.endsWith("/global.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 2:" + next, next.startsWith(cssLinkPrefix + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 2:" + next, next.endsWith("/debugOff.css" + CSS_POST_TAG));
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(renderer,"/css/lib/lib.css", bundleRendererCtx);
		assertEquals("Tags were repeated","", result.trim());
	}
	
	public void testDebugWriteAlternateCSSBundleLinks()
	{
		CSSHTMLBundleLinkRenderer renderer = getCssBundleLinkRenderer(true, false, null, true, false, "aTitle");
		// Test regular link creation
	    bundleRendererCtx = new BundleRendererContext(CSS_CTX_PATH, new HashMap<String, String>(), false, false);
	    String result = renderToString(renderer,"/css/lib/lib.css", bundleRendererCtx);
	    
	    assertNotSame("No css tag written ", "", result.trim());
		
		String cssLinkPrefix = getCssAlternatePrefixTag("aTitle");
		StringTokenizer tk = new StringTokenizer(result,"\n");
		String next;
		
		assertEquals("Invalid number of tags written. ",3, tk.countTokens());
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 0:" + next, next.startsWith(cssLinkPrefix + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 0:" + next, next.endsWith("/css/lib/lib.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 1:" + next, next.startsWith(cssLinkPrefix + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 1:" + next, next.endsWith("/css/global/global.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 2:" + next, next.startsWith(cssLinkPrefix + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 2:" + next, next.endsWith("/css/debug/on/debugOn.css" + CSS_POST_TAG));
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(renderer,"/css/lib/lib.css", bundleRendererCtx);
		assertEquals("Tags were repeated","", result.trim());
	}
	
	public void testWriteCSSBundleWithVariantLinks()
	{
		CSSHTMLBundleLinkRenderer renderer = getCssBundleLinkRenderer(false, false, null, false, true, null);
		// Test regular link creation
		Map<String, String> variants = new HashMap<String, String>();
		variants.put(JawrConstant.SKIN_VARIANT_TYPE, "winter");
		variants.put(JawrConstant.LOCALE_VARIANT_TYPE, "fr_FR");
		
	    bundleRendererCtx = new BundleRendererContext(CSS_CTX_PATH, variants, false, false);
	    String result = renderToString(renderer,"/theme.css", bundleRendererCtx);
		
	    assertNotSame("No css tag written ", "", result.trim());
			
		StringTokenizer tk = new StringTokenizer(result,"\n");
		String next;
		
		assertEquals("Invalid number of tags written. ",6, tk.countTokens());
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 0:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 0:" + next, next.endsWith("/library.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 1:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 1:" + next, next.endsWith("/global.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 2:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 2:" + next, next.endsWith("/debugOff.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 2:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 2:" + next, next.endsWith(".winter/theme.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 2:" + next, next.startsWith(getCssAlternatePrefixTag("default") + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 2:" + next, next.endsWith(".default/theme.css"+ CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 2:" + next, next.startsWith(getCssAlternatePrefixTag("summer") + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 2:" + next, next.endsWith(".summer/theme.css" + CSS_POST_TAG));
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(renderer,"/theme.css", bundleRendererCtx);
		assertEquals("Tags were repeated","", result.trim());
	}
	
	
	public void testDebugWriteCSSBundleWithVariantLinks()
	{
		CSSHTMLBundleLinkRenderer renderer = getCssBundleLinkRenderer(true, false, null, false, true, null);
		// Test regular link creation
		Map<String, String> variants = new HashMap<String, String>();
		variants.put(JawrConstant.SKIN_VARIANT_TYPE, "winter");
		variants.put(JawrConstant.LOCALE_VARIANT_TYPE, "fr_FR");
		
	    bundleRendererCtx = new BundleRendererContext(CSS_CTX_PATH, variants, false, false);
	    String result = renderToString(renderer,"/theme.css", bundleRendererCtx);
	    
	    assertNotSame("No css tag written ", "", result.trim());
			
		StringTokenizer tk = new StringTokenizer(result,"\n");
		String next;
		
		assertEquals("Invalid number of tags written. ",9, tk.countTokens());
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 0:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 0:" + next, next.endsWith("/css/lib/lib.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 1:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 1:" + next, next.endsWith("/css/global/global.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 2:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 2:" + next, next.endsWith("/css/debug/on/debugOn.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 3:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 3:" + next, next.endsWith("/jawr_generator.css?generationConfigParam=skin%3A%2Fcss%2Fthemes%2Fdefault%2Ftemp1.css%40winter" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 4:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 4:" + next, next.endsWith("/jawr_generator.css?generationConfigParam=skin%3A%2Fcss%2Fthemes%2Fdefault%2Fborder%2Fborder1.css%40winter" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 5:" + next, next.startsWith(getCssAlternatePrefixTag("default") + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 5:" + next, next.endsWith("/jawr_generator.css?generationConfigParam=skin%3A%2Fcss%2Fthemes%2Fdefault%2Ftemp1.css%40default"+ CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 6:" + next, next.startsWith(getCssAlternatePrefixTag("default") + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 6:" + next, next.endsWith("/jawr_generator.css?generationConfigParam=skin%3A%2Fcss%2Fthemes%2Fdefault%2Fborder%2Fborder1.css%40default"+ CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 7:" + next, next.startsWith(getCssAlternatePrefixTag("summer") + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 7:" + next, next.endsWith("/jawr_generator.css?generationConfigParam=skin%3A%2Fcss%2Fthemes%2Fdefault%2Ftemp1.css%40summer" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 8:" + next, next.startsWith(getCssAlternatePrefixTag("summer") + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 8:" + next, next.endsWith("/jawr_generator.css?generationConfigParam=skin%3A%2Fcss%2Fthemes%2Fdefault%2Fborder%2Fborder1.css%40summer" + CSS_POST_TAG));
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(renderer,"/theme.css", bundleRendererCtx);
		assertEquals("Tags were repeated","", result.trim());
	}
	
	public void testWriteCSSBundleLinksWithDependencies()
	{
		CSSHTMLBundleLinkRenderer renderer = getCssBundleLinkRenderer(false, false, null, false, false, null);
		// Test regular link creation
		
		bundleRendererCtx = new BundleRendererContext(CSS_CTX_PATH, null, false, false);
	    String result = renderToString(renderer,"/bundle1_1.css", bundleRendererCtx);
	    
	    assertNotSame("No css tag written ", "", result.trim());
			
		StringTokenizer tk = new StringTokenizer(result,"\n");
		String next;
		
		assertEquals("Invalid number of tags written. ",5, tk.countTokens());
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 0:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 0:" + next, next.endsWith("/library.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 1:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 1:" + next, next.endsWith("/global.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 2:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 2:" + next, next.endsWith("/debugOff.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 3:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 3:" + next, next.endsWith("/bundle1.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 4:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 4:" + next, next.endsWith("/bundle1_1.css" + CSS_POST_TAG));
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(renderer,"/bundle1_1.css", bundleRendererCtx);
		assertEquals("Tags were repeated","", result.trim());
	}
	
	public void testDebugWriteCSSBundleLinksWithDependencies()
	{
		CSSHTMLBundleLinkRenderer renderer = getCssBundleLinkRenderer(true, false, null, false, false, null);
		// Test regular link creation
		
		bundleRendererCtx = new BundleRendererContext(CSS_CTX_PATH, null, false, false);
	    String result = renderToString(renderer,"/bundle1_1.css", bundleRendererCtx);
	    
	    assertNotSame("No css tag written ", "", result.trim());
			
		StringTokenizer tk = new StringTokenizer(result,"\n");
		String next;
		
		assertEquals("Invalid number of tags written. ",5, tk.countTokens());
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 0:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 0:" + next, next.endsWith("/css/lib/lib.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 1:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 1:" + next, next.endsWith("/css/global/global.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 2:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 2:" + next, next.endsWith("/css/debug/on/debugOn.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 3:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 3:" + next, next.endsWith("/css/dependencies/related/mainStyle.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 4:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 4:" + next, next.endsWith("/css/dependencies/styleWithDependency.css" + CSS_POST_TAG));
		
		// Reusing the set, we test that no repeats are allowed. 
		result = renderToString(renderer,"/bundle1_1.css", bundleRendererCtx);
		assertEquals("Tags were repeated","", result.trim());
	}
	
	public void testWriteCSSBundleLinksForceRenderIEBundleInDebug()
	{
		Properties props = new Properties();
		props.setProperty("jawr.debug.ie.force.css.bundle", "true");
		CSSHTMLBundleLinkRenderer renderer = getCssBundleLinkRenderer(props, true, false, null, false, false, null);
		
		// Test regular link creation
	    bundleRendererCtx = new BundleRendererContext(CSS_CTX_PATH, null, false, false);
	    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		when(request.getHeader("User-Agent")).thenReturn("MSIE 6");
	    bundleRendererCtx.setRequest(request);
	    
	    String result = renderToString(renderer,"/css/lib/lib.css", bundleRendererCtx);

	    assertNotSame("No css tag written ", "", result.trim());
			
		StringTokenizer tk = new StringTokenizer(result,"\n");
		String next;
		
		assertEquals("Invalid number of tags written. ",3, tk.countTokens());
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 0:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 0:" + next, next.endsWith("/jawr_generator.css?generationConfigParam=ieCssGen%3A%2F546923304%2Flibrary.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 1:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 1:" + next, next.endsWith("/jawr_generator.css?generationConfigParam=ieCssGen%3A%2F1133437087%2Fglobal.css" + CSS_POST_TAG));
		
		next = tk.nextElement().toString();
		assertTrue("Unexpected tag added at position 2:" + next, next.startsWith(CSS_PRE_TAG + "/ctxPathCss/srvMapping/"));
		assertTrue("Unexpected tag added at position 2:" + next, next.endsWith("/jawr_generator.css?generationConfigParam=ieCssGen%3A%2FN806385619%2FdebugOn.css" + CSS_POST_TAG));
		
		// Reusing the set, we test that no repeats are allowed. 
		//result = renderToString(cssRenderer,"/css/lib/lib.css", CSS_CTX_PATH, includedBundles, false, false);
		result = renderToString(renderer,"/css/lib/lib.css", bundleRendererCtx);
		assertEquals("Tags were repeated","", result.trim());
	}
	
}
