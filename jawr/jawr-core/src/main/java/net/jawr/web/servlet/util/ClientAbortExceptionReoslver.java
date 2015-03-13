package net.jawr.web.servlet.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ClientAbortExceptionReoslver {

	private static final List<String> exceptions = Arrays.asList(
			"org.apache.catalina.connector.ClientAbortException",
			"org.mortbay.jetty.io.EofException",
			"org.eclipse.jetty.io.EofException");

	public static boolean isClientAbortException(IOException e) {
		return exceptions.contains(e.getClass().getName());
	}
}
