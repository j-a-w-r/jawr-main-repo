/**
 * Copyright 2008 Jordi Hernández Sellés
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
package net.jawr.web.resource.bundle.generator;

/**
 * @author Jordi Hernández Sellés
 */
public class JavascriptStringUtil {

	/**
	 * From JSONObject.java, from which the copyright notice is the following: 
	 * 
	 * Copyright (c) 2002 JSON.org
	 * Permission is hereby granted, free of charge, to any person obtaining a copy
	 * of this software and associated documentation files (the "Software"), to deal
	 * in the Software without restriction, including without limitation the rights
	 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	 * copies of the Software, and to permit persons to whom the Software is
	 * furnished to do so, subject to the following conditions:
	 *
	 *
	 * The above copyright notice and this permission notice shall be included in all
	 * copies or substantial portions of the Software.
	 * 
	 * Produce a string in double quotes with backslash sequences in all the
	 * right places. A backslash will be inserted within </, allowing JSON
	 * text to be delivered in HTML. In JSON text, a string cannot contain a
	 * control character or an unescaped quote or backslash.
	 * @param string A String
	 * @return  A String correctly formatted for insertion in a JSON text.
	 */
	public static String quote(String string) {
	    if (string == null || string.length() == 0) {
	        return "\"\"";
	    }	
	    int          len = string.length();
	    StringBuffer sb = new StringBuffer(len + 4);
	
	    sb.append('"');
	    sb.append(escape(string));
	    sb.append('"');
	    return sb.toString();
	}

	/**
	 * Escapes a string so it is valid between double quotes in javascript. 
	 * @param string
	 * @return
	 */
	public static String escape(String string) {
		char         b;
	    char         c = 0;
	    int          i;
	    int          len = string.length();
	    StringBuffer sb = new StringBuffer(len + 2);
	    String       t;
	    for (i = 0; i < len; i += 1) {
	        b = c;
	        c = string.charAt(i);
	        switch (c) {
	        case '\\':
	        case '"':
	            sb.append('\\');
	            sb.append(c);
	            break;
	        case '/':
	            if (b == '<') {
	                sb.append('\\');
	            }
	            sb.append(c);
	            break;
	        case '\b':
	            sb.append("\\b");
	            break;
	        case '\t':
	            sb.append("\\t");
	            break;
	        case '\n':
	            sb.append("\\n");
	            break;
	        case '\f':
	            sb.append("\\f");
	            break;
	        case '\r':
	            sb.append("\\r");
	            break;
	        default:
	            if (c < ' ' || (c >= '\u0080' && c < '\u00a0') ||
	                           (c >= '\u2000' && c < '\u2100')) {
	                t = "000" + Integer.toHexString(c);
	                sb.append("\\u" + t.substring(t.length() - 4));
	            } else {
	                sb.append(c);
	            }
	        }
	    }
	    return sb.toString();
	}
}
