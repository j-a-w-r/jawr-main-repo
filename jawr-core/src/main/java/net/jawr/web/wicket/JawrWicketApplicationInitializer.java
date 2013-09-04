/**
 * Copyright 2009 Ibrahim Chaehoi
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

import org.apache.wicket.Application;
import org.apache.wicket.markup.MarkupParser;
import org.apache.wicket.markup.MarkupParserFactory;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.parser.XmlPullParser;

/**
 * Utility class to initialize wicket application for Jawr
 * 
 * @author Ibrahim Chaehoi
 */
public final class JawrWicketApplicationInitializer {

	/**
	 * Initialize the wicket application
	 * 
	 * @param app the aplpication to initialize
	 */
	public static void initApplication(Application app){
		
		// Add the Jawr tag handler to the MarkupParserFactory 
		MarkupParserFactory factory = new MarkupParserFactory(){
			
			public MarkupParser newMarkupParser(final MarkupResourceStream resource)
		    {
		       MarkupParser parser = new MarkupParser(new XmlPullParser(), resource);
		       parser.appendMarkupFilter(new JawrWicketLinkTagHandler());
		       return parser;
		    }
		};
		
		app.getMarkupSettings().setMarkupParserFactory(factory);
		
		// Add the Jawr link resolver
		app.getPageSettings().addComponentResolver(new JawrWicketLinkResolver());
	}
}
