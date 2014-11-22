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

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.resolver.IComponentResolver;

/**
 * This class defines a tag resolver which handles &lt;wicket:jawr&gt; tags. The
 * tasks of this resolver will be to : - add a "transparent" WebMarkupContainer
 * to transparently handling child components. - create the right component
 * depending on the tag defined inside &lt;wicket:jawr&gt;
 * 
 * @author Ibrahim Chaehoi
 * 
 */
public class JawrWicketLinkResolver implements IComponentResolver {

	private static final String IMAGE_TAG_NAME = "image";
	private static final String IMG_TAG_NAME = "img";
	/** The serial version UID */
	private static final long serialVersionUID = -2106412613442819122L;

	@Override
	public Component resolve(MarkupContainer container,
			MarkupStream markupStream, ComponentTag tag) {

		Component component = null;

		// Only component tags have the id == "_jawrAutolink_"
		String tagName = tag.getName();
		if (tag.getId().equals(JawrWicketLinkTagHandler.AUTOLINK_ID)) {

			// create the right component depending on the tag name
			WebMarkupContainer jawrTag = null;
			final String id = tag.getId() + container.getPage().getAutoIndex();
			if (tagName.equalsIgnoreCase(IMG_TAG_NAME)) {
				jawrTag = new JawrImageReference(id);
			} else if (tagName.equalsIgnoreCase("input")
					&& tag.getAttribute("type").equals(IMAGE_TAG_NAME)) {
				jawrTag = new JawrHtmlImageReference(id);
			} else if (tagName.equalsIgnoreCase("script")) {
				jawrTag = new JawrJavascriptReference(id);
			} else if (tagName.equalsIgnoreCase("link")) {
				jawrTag = new JawrStylesheetReference(id);
			}

			if (jawrTag != null) {
				container.autoAdd(jawrTag, markupStream);
			}

			// Yes, we handled the tag
			return jawrTag;
		} else if (tag instanceof WicketTag) {

			// For tag wicket:jawr
			if (tagName.equals("jawr")) {

				final String id = tag.getId()
						+ container.getPage().getAutoIndex();
				component = new TransparentWebMarkupContainer(id);
				component.setRenderBodyOnly(true);
				container.autoAdd(component, markupStream);

				// Yes, we handled the tag
				return component;
			}
		}

		// We were not able to handle the tag
		return null;
	}

}
