package test.net.jawr.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

import net.jawr.web.resource.bundle.IOUtils;

public class FileUtils {
	
	public static final int BUFFER_SIZE = 128 * 1024;

	public static File createDir(String pathName) throws Exception {
		File dir = new File(getClasspathRootDir(), pathName);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new RuntimeException("Impossible to create the directory : " + pathName);
			}
		}
		return dir;
	}

	public static void clearDirectory(String path) {
		deleteDirectory(path);
		File dir = new File(path);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new RuntimeException("Impossible to create the directory : " + path);
			}
		}
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

	/**
	 * Removes the carriage return from he string passed in parameter.
	 * 
	 * @param str
	 *            the string
	 * @return a string without carriage return.
	 */
	public static String removeCarriageReturn(String str) {
		return str.replaceAll("\r\n", "\n");
	}

	public static String readClassPathFile(String path) throws Exception {

		return removeCarriageReturn(readFile(getClassPathFile(path)).toString());
	}

	public static String readFile(String filePath) throws Exception {

		return readFile(new File(filePath));
	}

	public static String readFile(File toRead) throws Exception {

		return readFile(toRead, "UTF-8");
	}

	public static String readFile(File toRead, String charset) throws Exception {

		StringWriter sw = new StringWriter();
		FileInputStream fis = null;
		FileChannel inchannel = null;
		Reader rd = null;

		try {

			fis = new FileInputStream(toRead);
			inchannel = fis.getChannel();
			rd = Channels.newReader(inchannel, charset);
			int i;
			while ((i = rd.read()) != -1) {
				sw.write(i);
			}
		} finally {
			IOUtils.close(rd);
			IOUtils.close(inchannel);
			IOUtils.close(fis);
			IOUtils.close(sw);
		}

		return removeCarriageReturn(sw.getBuffer().toString());
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

	public static String getClassPathFileAbsolutePath(String pathName) throws Exception {
		// In windows, pathnames with spaces are returned as %20
		if (pathName.indexOf("%20") != -1)
			pathName = pathName.replaceAll("%20", " ");
		File tmp = new File(getResourceURL(pathName).getFile());
		return tmp.getAbsolutePath();
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

	@SuppressWarnings("resource")
	public static void copyFile(File from, File to) throws IOException {
		FileChannel in = null;
		FileChannel out = null;
		try {
			in = new FileInputStream(from).getChannel();
			out = new FileOutputStream(to).getChannel();

			long size = in.size();
			long position = 0;
			while (position < size) {
				position += in.transferTo(position, BUFFER_SIZE, out);
			}
		} finally {
			IOUtils.close(in);
			IOUtils.close(out);
		}
	}

	public static void copyFile(String srcFile, String destFile) throws Exception {
		File destF = new File(FileUtils.getClassPathFileAbsolutePath(destFile));
		if (!destF.exists() && !destF.getParentFile().exists()) {
			destF.getParentFile().mkdirs();
		}

		FileWriter fw = new FileWriter(destF);
		FileReader fr = new FileReader(new File(FileUtils.getClassPathFileAbsolutePath(srcFile)));
		IOUtils.copy(fr, fw, true);
	}
}
