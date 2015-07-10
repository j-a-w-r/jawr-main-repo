package net.jawr.web.bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class FileUtils {

	public static File createDir(String pathName) throws Exception {
		File dir = new File(getClasspathRootDir() + pathName);
		if (!dir.exists())
			dir.mkdir();
		return dir;
	}

	public static void clearDirectory(String path) {
		deleteDirectory(path);
		File dir = new File(path);
		if (!dir.exists())
			dir.mkdir();
	}

	public static boolean deleteDirectory(String path) {
		return deleteDirectory(new File(path));
	}

	public static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	public static StringBuffer readFile(File toRead) throws Exception {
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
			rd.close();
			fis.close();
			sw.close();
		}

		return sw.getBuffer();
	}

	public static String getClasspathRootDir() throws Exception {
		File tmpRoot = getClassPathFile("test.properties").getParentFile();
		return tmpRoot.getCanonicalPath().replaceAll("%20", " ");
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
