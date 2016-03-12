/**
 * Copyright 2016 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.css.sass.ruby;

/**
 * Sass Ruby Utilities class
 * 
 * @author Ibrahim Chaehoi
 */
public final class SassRubyUtils {

	/**
	 * Constructor
	 */
	private SassRubyUtils() {
		
	}

	/***
	 * Normalize multibyte string for JRuby
	 * 
	 * Non-ASCII String may cause invalid multibyte char (US-ASCII) error with Ruby 1.9
	 * because Ruby 1.9 expects you to use ASCII characters in your source code.
	 * Instead we use Unicode code point representation which is usable with Ruby 1.9 and later. 
	 * Inspired from http://www.stefanwille.com/2010/08/ruby-on-rails-fix-for-invalid-multibyte-char-us-ascii/
	 * 
	 * Original code from
	 * 
	 * @param content the content to fixed
	 * @return the normalized content
	 */
	public static String normalizeMultiByteString(String content){
		
		StringBuilder sContent = new StringBuilder();

		final int BACKSLASH = 0x5c;
		for (int i = 0; i < content.length(); i++) {
			final int code = content.codePointAt(i);
			if (code < 0x80) {
				// We leave only ASCII unchanged.
				if (code == BACKSLASH) {
					// escape backslash
					sContent.append("\\");
				}
				sContent.append(content.charAt(i));
			} else {
				sContent.append(String.format("\\u%04x", code));
			}
		}
		
		return sContent.toString();
	}
}
