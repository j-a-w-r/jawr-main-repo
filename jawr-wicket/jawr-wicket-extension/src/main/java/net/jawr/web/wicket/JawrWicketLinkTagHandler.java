/**
 * Copyright 2009-2013 Ibrahim Chaehoi
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
package net.jawr.web.wicket;

import java.text.ParseException;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;
import org.apache.wicket.util.value.IValueMap;

/**
 * This filter is highly inspired from the WicketLinkTagHandler.
 * 
 * This is a markup inline filter. It identifies xml tags which include a href
 * attribute and which are not Wicket specific components and flags these tags
 * (ComponentTag) as modified component. A component resolver
 * (JawrWicketLinkResolver) will later resolve the href and automatically
 * generate the right component wich will be rendered on the page.
 * <p>
 * 
 * @author Ibrahim Chaehoi
 */
public class JawrWicketLinkTagHandler extends AbstractMarkupFilter {
	/** The id of autolink components */
	public static final String AUTOLINK_ID = "_jawrAutolink_";

	static {
		// register "wicket:jawr"
		WicketTagIdentifier.registerWellKnownTagName("jawr");
	}

	/**
	 * Construct.
	 */
	public JawrWicketLinkTagHandler() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.wicket.markup.parser.AbstractMarkupFilter#onComponentTag(org
	 * .apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected MarkupElement onComponentTag(ComponentTag tag)
			throws ParseException {
		if (tag == null) {
			return tag;
		}

		// Only xml tags not already identified as Wicket components will be
		// considered for autolinking. This is because it is assumed that Wicket
		// components like images or all other kind of Wicket Links will handle
		// it themselves.
		// Subclass analyzeAutolinkCondition() to implement you own
		// implementation and register the new tag handler with the markup
		// parser through Application.newMarkupParser().
		if (analyzeAutolinkCondition(tag) == true) {
			// Just a dummy name. The ComponentTag will not be forwarded.
			tag.setId(AUTOLINK_ID);
			tag.setAutoComponentTag(true);
			tag.setModified(true);
			return tag;
		}

		return tag;
	}

	/**
	 * Analyze the tag. If return value == true, a jawr component will be
	 * created.
	 * 
	 * @param tag
	 *            The current tag being parsed
	 * @return If true, tag will become auto-component
	 */
	protected boolean analyzeAutolinkCondition(final ComponentTag tag) {
		if (tag.getId() == null) {
			if (checkRef(tag)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if if tag ref is a correct one or not
	 * 
	 * @param tag
	 *            the component tag
	 * @return true if if tag ref is a correct one or not and that a component
	 *         should be created
	 */
	private final boolean checkRef(ComponentTag tag) {
		boolean ok = false;
		if (!tag.getName().equals("a")) {
			IValueMap attributes = tag.getAttributes();
			String ref = attributes.getString("href");
			if (ref == null) {
				ref = attributes.getString("src");
			}

			if ((ref != null)
					&& (isJawrImageTag(tag) || (ref.indexOf(":") == -1))) {
				ok = true;
			}

		}

		return ok;
	}

	/**
	 * Checks if it's a Jawr image tag or not
	 * 
	 * @param tag
	 *            the Component tag
	 * @return true if it's a Jawr image tag or not
	 */
	private boolean isJawrImageTag(ComponentTag tag) {
		String tagName = tag.getName();
		return (tagName.equalsIgnoreCase("img") || (tagName
				.equalsIgnoreCase("input") && tag.getAttribute("type").equals(
				"image")));
	}

}
