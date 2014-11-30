/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package net.jawr.web.util;

import java.util.regex.Pattern;

/**
 * 
 * Utility method for String manipulation.
 * The original code is available in commons-lang.
 * 
 * Below are the original authors of StringUtils.
 * 
 * @see java.lang.String
 * @author <a href="http://jakarta.apache.org/turbine/">Apache Jakarta Turbine</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author Daniel L. Rall
 * @author <a href="mailto:gcoladonato@yahoo.com">Greg Coladonato</a>
 * @author <a href="mailto:ed@apache.org">Ed Korthof</a>
 * @author <a href="mailto:rand_mcneely@yahoo.com">Rand McNeely</a>
 * @author Stephen Colebourne
 * @author <a href="mailto:fredrik@westermarck.com">Fredrik Westermarck</a>
 * @author Holger Krauth
 * @author <a href="mailto:alex@purpletech.com">Alexander Day Chaffee</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author Arun Mammen Thomas
 * @author Gary Gregory
 * @author Phil Steitz
 * @author Al Chou
 * @author Michael Davey
 * @author Reuben Sivan
 * @author Chris Hyzer
 * @author Scott Johnson
 * @since 1.0
 * @version $Id: StringUtils.java 635447 2008-03-10 06:27:09Z bayard $
 */
public class StringUtils {

	/** The new line separator */
	public static final String LINE_SEPARATOR = "\r\n"; //System.getProperty("line.separator");
	
	/**
	 * <code>\u000a</code> linefeed LF ('\n').
	 * 
	 * @see <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#101089">JLF: Escape Sequences for Character and String
	 *      Literals</a>
	 * @since 2.2
	 */
	public static final char LF = '\n';

	/**
	 * <code>\u000d</code> carriage return CR ('\r').
	 * 
	 * @see <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#101089">JLF: Escape Sequences for Character and String
	 *      Literals</a>
	 * @since 2.2
	 */
	public static final char CR = '\r';

	/**
	 * The empty String <code>""</code>.
	 * 
	 * @since 2.0
	 */
	public static final String EMPTY = "";

	// Empty checks
	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Checks if a String is empty ("") or null.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.isEmpty(null)      = true
	 * StringUtils.isEmpty(&quot;&quot;)        = true
	 * StringUtils.isEmpty(&quot; &quot;)       = false
	 * StringUtils.isEmpty(&quot;bob&quot;)     = false
	 * StringUtils.isEmpty(&quot;  bob  &quot;) = false
	 * </pre>
	 * 
	 * <p>
	 * NOTE: This method changed in Lang version 2.0. It no longer trims the String. That functionality is available in isBlank().
	 * </p>
	 * 
	 * @param str the String to check, may be null
	 * @return <code>true</code> if the String is empty or null
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

    /**
     * <p>Checks if a String is not empty ("") and not null.</p>
     *
     * <pre>
     * StringUtils.isNotEmpty(null)      = false
     * StringUtils.isNotEmpty("")        = false
     * StringUtils.isNotEmpty(" ")       = true
     * StringUtils.isNotEmpty("bob")     = true
     * StringUtils.isNotEmpty("  bob  ") = true
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is not empty and not null
     */
    public static boolean isNotEmpty(String str) {
        return !StringUtils.isEmpty(str);
    }
    
	// Count matches
	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Counts how many times the substring appears in the larger String.
	 * </p>
	 * 
	 * <p>
	 * A <code>null</code> or empty ("") String input returns <code>0</code>.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.countMatches(null, *)       = 0
	 * StringUtils.countMatches(&quot;&quot;, *)         = 0
	 * StringUtils.countMatches(&quot;abba&quot;, null)  = 0
	 * StringUtils.countMatches(&quot;abba&quot;, &quot;&quot;)    = 0
	 * StringUtils.countMatches(&quot;abba&quot;, &quot;a&quot;)   = 2
	 * StringUtils.countMatches(&quot;abba&quot;, &quot;ab&quot;)  = 1
	 * StringUtils.countMatches(&quot;abba&quot;, &quot;xxx&quot;) = 0
	 * </pre>
	 * 
	 * @param str the String to check, may be null
	 * @param sub the substring to count, may be null
	 * @return the number of occurrences, 0 if either String is <code>null</code>
	 */
	public static int countMatches(String str, String sub) {
		if (isEmpty(str) || isEmpty(sub)) {
			return 0;
		}
		int count = 0;
		int idx = 0;
		while ((idx = str.indexOf(sub, idx)) != -1) {
			count++;
			idx += sub.length();
		}
		return count;
	}

	// Chopping
	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Remove the last character from a String.
	 * </p>
	 * 
	 * <p>
	 * If the String ends in <code>\r\n</code>, then remove both of them.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.chop(null)          = null
	 * StringUtils.chop(&quot;&quot;)            = &quot;&quot;
	 * StringUtils.chop(&quot;abc \r&quot;)      = &quot;abc &quot;
	 * StringUtils.chop(&quot;abc\n&quot;)       = &quot;abc&quot;
	 * StringUtils.chop(&quot;abc\r\n&quot;)     = &quot;abc&quot;
	 * StringUtils.chop(&quot;abc&quot;)         = &quot;ab&quot;
	 * StringUtils.chop(&quot;abc\nabc&quot;)    = &quot;abc\nab&quot;
	 * StringUtils.chop(&quot;a&quot;)           = &quot;&quot;
	 * StringUtils.chop(&quot;\r&quot;)          = &quot;&quot;
	 * StringUtils.chop(&quot;\n&quot;)          = &quot;&quot;
	 * StringUtils.chop(&quot;\r\n&quot;)        = &quot;&quot;
	 * </pre>
	 * 
	 * @param str the String to chop last character from, may be null
	 * @return String without last character, <code>null</code> if null String input
	 */
	public static String chop(String str) {
		if (str == null) {
			return null;
		}
		int strLen = str.length();
		if (strLen < 2) {
			return EMPTY;
		}
		int lastIdx = strLen - 1;
		String ret = str.substring(0, lastIdx);
		char last = str.charAt(lastIdx);
		if (last == LF) {
			if (ret.charAt(lastIdx - 1) == CR) {
				return ret.substring(0, lastIdx - 1);
			}
		}
		return ret;
	}
	
	/**
     * <p>Removes <code>separator</code> from the end of
     * <code>str</code> if it's there, otherwise leave it alone.</p>
     *
     * <p>NOTE: This method changed in version 2.0.
     * It now more closely matches Perl chomp.
     * For the previous behavior, use {@link #substringBeforeLast(String, String)}.
     * This method uses {@link String#endsWith(String)}.</p>
     *
     * <pre>
     * StringUtils.chomp(null, *)         = null
     * StringUtils.chomp("", *)           = ""
     * StringUtils.chomp("foobar", "bar") = "foo"
     * StringUtils.chomp("foobar", "baz") = "foobar"
     * StringUtils.chomp("foo", "foo")    = ""
     * StringUtils.chomp("foo ", "foo")   = "foo "
     * StringUtils.chomp(" foo", "foo")   = " "
     * StringUtils.chomp("foo", "foooo")  = "foo"
     * StringUtils.chomp("foo", "")       = "foo"
     * StringUtils.chomp("foo", null)     = "foo"
     * </pre>
     *
     * @param str  the String to chomp from, may be null
     * @param separator  separator String, may be null
     * @return String without trailing separator, <code>null</code> if null String input
     */
    public static String chomp(String str, String separator) {
        if (isEmpty(str) || separator == null) {
            return str;
        }
        if (str.endsWith(separator)) {
            return str.substring(0, str.length() - separator.length());
        }
        return str;
    }

    /**
     * <p>Remove a value if and only if the String ends with that value.</p>
     *
     * @param str  the String to chomp from, must not be null
     * @param sep  the String to chomp, must not be null
     * @return String without chomped ending
     * @throws NullPointerException if str or sep is <code>null</code>
     */
    public static String chompLast(String str, String sep) {
        if (str.length() == 0) {
            return str;
        }
        String sub = str.substring(str.length() - sep.length());
        if (sep.equals(sub)) {
            return str.substring(0, str.length() - sep.length());
        }
        return str;
    }
    
    /**
     * Split the String passed in parameter, the trailing empty string are kept. 
     * @param str the string to split
     * @param separator the separator
     * @return the splitted string
     */
    public static String[] split(String str, String separator){
    
    	return Pattern.compile(separator).split(str, -1);
    }
}
