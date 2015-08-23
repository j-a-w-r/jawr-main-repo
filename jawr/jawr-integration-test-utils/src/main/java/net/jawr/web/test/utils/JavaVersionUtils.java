/**
 * 
 */
package net.jawr.web.test.utils;

/**
 * Utility class for java version info.
 * 
 * @author Ibrahim Chaehoi
 */
public final class JavaVersionUtils {

	public static boolean isVersionInferiorToJava8(){
		String version = System.getProperty("java.version");
		int idx = version.lastIndexOf(".");
		float fVersion;
		if(idx == -1){
			fVersion = Float.parseFloat(version);
		}else{
			fVersion = Float.parseFloat(version.substring(0, idx));
		}
		
		return fVersion < 1.8f;
	}
}
