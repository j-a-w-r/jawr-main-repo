/**
 * Copyright 2007-2016 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.minification;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jawr.web.resource.bundle.factory.util.RegexUtil;

/**
 * Minifies CSS files by removing expendable whitespace and comments.
 * 
 * @author Jordi Hernández Sellés
 * @author ibrahim Chaehoi
 */
public class CSSMinifier {

	// This regex captures comments
	private static final String COMMENT_REGEX = "(/\\*(?!(!))[^*]*\\*+([^/][^*]*\\*+)*/)";

	// This regex captures licence (ex : /*! My licence content */
	private static final String LICENCE_REGEX = "(/\\*(?=(!))[^*]*\\*+([^/][^*]*\\*+)*/)";

	// Captures CSS strings
	private static final String QUOTED_CONTENT_REGEX = "([\"']).*?\\1";

	// A placeholder string to replace and restore CSS strings
	private static final String STRING_PLACEHOLDER = "______'JAWR_STRING'______";

	// A placeholder string to replace and restore licence comments
	private static final String LICENCE_PLACEHOLDER = "______'LICENCE'______";

	// Captured CSS rules (requires replacing CSS strings with a placeholder, or
	// quoted braces will fool it.
	private static final String RULES_REGEX = "([^\\{\\}]*)(\\{[^\\{\\}]*})";

	// Captures newlines and tabs
	private static final String NEW_LINE_TABS_REGEX = "\\r|\\n|\\t|\\f";

	/**
	 * There is a special case when the space should not be removed when
	 * preceeded by and keyword. Ex: <code>
	 * 
	 * @media only screen and (max-width:767px){ } </code>
	 */
	private static final String SPACES_REGEX = "(?ims)(\\s*\\{\\s*)|(\\s+\\-\\s+)|(\\s+\\+\\s+)|(\\s+\\*\\s+)|(\\s+\\/\\s+)|(\\s*\\}\\s*)|((?<!\\sand)\\s*\\(\\s*)|(\\s*;\\s*)|(\\s*:\\s*)|(\\s*\\))|( +)";

	private static final Pattern COMMENTS_PATTERN = Pattern.compile(COMMENT_REGEX, Pattern.DOTALL);
	private static final Pattern LICENCE_PATTERN = Pattern.compile(LICENCE_REGEX, Pattern.DOTALL);
	private static final Pattern SPACES_PATTERN = Pattern.compile(SPACES_REGEX, Pattern.DOTALL);

	private static final Pattern QUOTED_CONTENT_PATTERN = Pattern.compile(QUOTED_CONTENT_REGEX, Pattern.DOTALL);
	private static final Pattern RULES_PATTERN = Pattern.compile(RULES_REGEX, Pattern.DOTALL);
	private static final Pattern NEW_LINES_TAB_PATTERN = Pattern.compile(NEW_LINE_TABS_REGEX, Pattern.DOTALL);
	private static final Pattern STRING_PLACEHOLDER_PATTERN = Pattern.compile(STRING_PLACEHOLDER, Pattern.DOTALL);

	private static final Pattern LICENCE_PLACEHOLDER_PATTERN = Pattern.compile(LICENCE_PLACEHOLDER, Pattern.DOTALL);

	private static final String SPACE = " ";
	private static final String BRACKET_OPEN = "{";
	private static final String BRACKET_CLOSE = "}";
	private static final String PAREN_OPEN = "(";
	private static final String PAREN_CLOSE = ")";
	private static final String PLUS_OPERATOR = "+";
	private static final String PLUS_OPERATOR_REPLACEMENT = " + ";
	private static final String MINUS_OPERATOR = "-";
	private static final String MINUS_OPERATOR_REPLACEMENT = " - ";
	private static final String MULTIPLICATION_OPERATOR = "*";
	private static final String MULTIPLICATION_OPERATOR_REPLACEMENT = " * ";
	private static final String DIVISION_OPERATOR = "/";
	private static final String DIVISION_OPERATOR_REPLACEMENT = " / ";

	private static final String COLON = ":";
	private static final String SEMICOLON = ";";

	/** The flag indicating if the licence info should be kept */
	private boolean keepLicence;

	/**
	 * Template class to abstract the pattern of iterating over a Matcher and
	 * performing string replacement.
	 */
	public abstract class MatcherProcessorCallback {
		String processWithMatcher(final Matcher matcher) {
			final StringBuffer sb = new StringBuffer();
			while (matcher.find()) {
				matcher.appendReplacement(sb, RegexUtil.adaptReplacementToMatcher(matchCallback(matcher)));
			}
			matcher.appendTail(sb);
			return sb.toString();
		}

		abstract String matchCallback(Matcher matcher);
	}

	/**
	 * Constructor
	 */
	public CSSMinifier() {
		this(false);
	}

	/**
	 * Constructor
	 * 
	 * @param keepLicence
	 *            the flag indicating if we should kept the licence
	 */
	public CSSMinifier(boolean keepLicence) {
		this.keepLicence = keepLicence;
	}

	/**
	 * @param data
	 *            CSS to minify
	 * @return StringBuffer Minified CSS.
	 */
	public StringBuffer minifyCSS(final StringBuffer data) {
		// Remove comments and carriage returns
		String compressed = COMMENTS_PATTERN.matcher(data.toString()).replaceAll("");

		// Temporarily replace the strings with a placeholder
		final List<String> licences = new ArrayList<>();
		final Matcher licenceMatcher = LICENCE_PATTERN.matcher(compressed);

		compressed = new MatcherProcessorCallback() {
			@Override
			String matchCallback(final Matcher matcher) {
				final String match = matcher.group();
				String replacement = "";
				if (keepLicence) {
					licences.add(match);
					replacement = LICENCE_PLACEHOLDER;
				}
				return replacement;
			}
		}.processWithMatcher(licenceMatcher);

		// Temporarily replace the strings with a placeholder
		final List<String> strings = new ArrayList<>();
		final Matcher stringMatcher = QUOTED_CONTENT_PATTERN.matcher(compressed);

		compressed = new MatcherProcessorCallback() {
			@Override
			String matchCallback(final Matcher matcher) {
				final String match = matcher.group();
				strings.add(match);
				return STRING_PLACEHOLDER;
			}
		}.processWithMatcher(stringMatcher);

		// Grab all rules and replace whitespace in selectors
		final Matcher rulesmatcher = RULES_PATTERN.matcher(compressed);
		compressed = new MatcherProcessorCallback() {
			@Override
			String matchCallback(final Matcher matcher) {
				final String match = matcher.group(1);
				final String spaced = NEW_LINES_TAB_PATTERN.matcher(match).replaceAll(SPACE).trim();
				return spaced + matcher.group(2);
			}
		}.processWithMatcher(rulesmatcher);

		// Replace all linefeeds and tabs
		compressed = NEW_LINES_TAB_PATTERN.matcher(compressed).replaceAll(" ");

		// Match all empty space we can minify
		final Matcher matcher = SPACES_PATTERN.matcher(compressed);
		compressed = new MatcherProcessorCallback() {
			@Override
			String matchCallback(final Matcher matcher) {
				String replacement = SPACE;
				final String match = matcher.group();

				if (match.contains(PLUS_OPERATOR)) {
					replacement = PLUS_OPERATOR_REPLACEMENT;
				} else if (match.contains(MINUS_OPERATOR)) {
					replacement = MINUS_OPERATOR_REPLACEMENT;
				} else if (match.contains(MULTIPLICATION_OPERATOR)) {
					replacement = MULTIPLICATION_OPERATOR_REPLACEMENT;
				} else if (match.contains(DIVISION_OPERATOR)) {
					replacement = DIVISION_OPERATOR_REPLACEMENT;
				} else if (match.contains(PLUS_OPERATOR)) {
					replacement = PLUS_OPERATOR_REPLACEMENT;
				} else if (match.contains(BRACKET_OPEN)) {
					replacement = BRACKET_OPEN;
				} else if (match.contains(BRACKET_CLOSE)) {
					replacement = BRACKET_CLOSE;
				} else if (match.contains(PAREN_OPEN)) {
					replacement = PAREN_OPEN;
				} else if (match.contains(COLON)) {
					replacement = COLON;
				} else if (match.contains(SEMICOLON)) {
					replacement = SEMICOLON;
				} else if (match.contains(PAREN_CLOSE)) {
					replacement = PAREN_CLOSE;
				}
				return replacement;
			}
		}.processWithMatcher(matcher);

		// Restore all Strings
		final Matcher restoreStringMatcher = STRING_PLACEHOLDER_PATTERN.matcher(compressed);
		final Iterator<String> itStr = strings.iterator();
		compressed = new MatcherProcessorCallback() {
			@Override
			String matchCallback(final Matcher matcher) {

				final String replacement = itStr.next();
				return replacement;
			}
		}.processWithMatcher(restoreStringMatcher);

		// Restore all licences
		final Matcher restoreLicenceMatcher = LICENCE_PLACEHOLDER_PATTERN.matcher(compressed);
		final Iterator<String> itLicence = licences.iterator();
		compressed = new MatcherProcessorCallback() {
			@Override
			String matchCallback(final Matcher matcher) {

				final String replacement = itLicence.next();
				return replacement;
			}
		}.processWithMatcher(restoreLicenceMatcher);

		return new StringBuffer(compressed);
	}

}
