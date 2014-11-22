package net.jawr.dwr.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Jawr DWR integration test config file annotation
 * 
 * @author ibrahim Chaehoi
 *
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target( {ElementType.TYPE} )
public @interface JawrDWRTestConfigFiles{

	String webXml();
	
	String jawrConfig();
	
	String dwrConfig();
	
}

