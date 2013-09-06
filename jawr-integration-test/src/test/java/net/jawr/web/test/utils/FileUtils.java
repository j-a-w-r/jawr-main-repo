package net.jawr.web.test.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import net.jawr.web.resource.bundle.IOUtils;

/**
 * Utility class to retrieve file content.
 * 
 * @author ibrahim Chaehoi
 */
public class FileUtils {

	public static String readContent(File toRead) throws Exception {
		StringWriter sw = new StringWriter();
		FileInputStream fis = null;
		Reader rd = null;
		try {
			fis = new FileInputStream(toRead);
			FileChannel inchannel = fis.getChannel();
			rd = Channels.newReader(inchannel, Charset.forName("UTF-8")
					.newDecoder(), -1);
			int i;
			while ((i = rd.read()) != -1)
				sw.write(i);
		} finally {
			IOUtils.close(rd);
			IOUtils.close(fis);
			IOUtils.close(sw);
		}
		return Utils.removeCarriageReturn(sw.toString());
	}

	public static String readContent(Class<?> clazz, String path)
			throws Exception {
		InputStream is = clazz.getResourceAsStream(path);
		if (is == null) {
			throw new Exception("File '" + clazz.getPackage().getName() + "."
					+ path + "' doesn't exist");
		}
		return readContent(is);
	}

	public static String readContent(InputStream is) throws Exception {
		StringWriter sw = new StringWriter();
		Reader rd = new InputStreamReader(is, Charset.forName("UTF-8"));
		try {
			int i;
			while ((i = rd.read()) != -1)
				sw.write(i);
		} finally {
			IOUtils.close(rd);
			IOUtils.close(sw);
		}
		return Utils.removeCarriageReturn(sw.toString());
	}

	public static File getClassPathFile(String pathName) throws Exception {
		// In windows, pathnames with spaces are returned as %20
		if (pathName.indexOf("%20") != -1)
			pathName = pathName.replaceAll("%20", " ");
		File tmp = new File(getResourceURL(pathName).getFile());
		return tmp;
	}

	public static URL getResourceURL(String resource) throws IOException {
		URL url = null;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader != null)
			url = loader.getResource(resource);
		if (url == null)
			url = ClassLoader.getSystemResource(resource);
		if (url == null)
			throw new IOException("Resource " + resource + " was not found");
		return url;
	}
}
