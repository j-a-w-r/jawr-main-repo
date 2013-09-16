/**
 * Copyright 2009-2013 Jordi Hern�ndez, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.renderer.image;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Renders an img or input type img tag in HTML. 
 * 
 * @author Jordi Hernandez
 * @author ibrahim Chaehoi
 */
public class ImgHTMLRenderer implements ImgRenderer, Serializable {
	
	/** The serial versio UID */
	private static final long serialVersionUID = 5070489835130503527L;
	
	/** The image start tag */
	private static final String IMG_TAG_START = "<img ";
	
	/** The input image start tag */
	private static final String INPUT_TAG_START = "<input type=\"image\" ";
	
	/** The start tag prefix used by the renderer */
	private String tagStart;
	
	/**
	 * Constructor
	 * @param isPlainImage If true, will render an IMG tag, otherwise an input type="image" is rendered instead
	 */
	public ImgHTMLRenderer(){
		
	}
	
	/**
	 * Initializes the image renderer
	 * @param isPlainImage
	 */
	public void init(boolean isPlainImage) {
		this.tagStart = isPlainImage ? IMG_TAG_START : INPUT_TAG_START;
	}
	
	/**
	 * Render the actual tag
	 * @param imgSource Source of the image
	 * @param attributes Attributes for the tag. 
	 * @param writer Writer to render the HTML into. Will NOT be closed or flushed. 
	 * 
	 * @throws IOException if an exception occurs
	 */
	public void renderImage(String imgSource, Map<String, Object> attributes, final Writer writer) throws IOException{
		StringBuffer sb = new StringBuffer(tagStart);
		sb.append("src=\"").append(imgSource).append("\" ");
		for(Iterator<Entry<String, Object>> it = attributes.entrySet().iterator();it.hasNext();) {
			Entry<String, Object> mapEntry = it.next();
			sb.append(mapEntry.getKey())
				.append("=\"")
				.append(mapEntry.getValue())
				.append("\" ");
			
		}
		sb.append("/>");
		
		writer.write(sb.toString());
		
	}

}
