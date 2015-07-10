package test.net.jawr.web.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;

import net.jawr.web.util.ServletContextUtils;

import org.junit.Test;
import org.mockito.Mockito;

public class ServletContextUtilsTestCase {

	@Test
	public void testGetContextPathWithNullServletCtx() {
		
		assertEquals(ServletContextUtils.DEFAULT_CONTEXT_PATH, ServletContextUtils.getContextPath(null));
	}
	
	@Test
	public void testGetContextPath(){
		ServletContext ctx = Mockito.mock(ServletContext.class);
		when(ctx.getContextPath()).thenReturn("/myWebApp");
		assertEquals("/myWebApp", ServletContextUtils.getContextPath(ctx));
	}

	@Test
	public void testGetContextPathWithEmptyPath(){
		ServletContext ctx = Mockito.mock(ServletContext.class);
		when(ctx.getContextPath()).thenReturn("");
		assertEquals(ServletContextUtils.DEFAULT_CONTEXT_PATH, ServletContextUtils.getContextPath(ctx));
	}

}

