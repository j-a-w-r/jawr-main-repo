/**
 * Copyright 2009-2016 Ibrahim Chaehoi
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
package net.jawr.web.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.context.ThreadLocalJawrContext;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.renderer.RendererFactory;
import net.jawr.web.resource.bundle.renderer.image.ImgRenderer;

/**
 * This tag defines the base class for HTML tags
 * 
 * @author Ibrahim Chaehoi
 * 
 */
public abstract class AbstractImageTag extends ImagePathTag {

	/** The serial version UID */
	private static final long serialVersionUID = 1085874354131806795L;

	/** The image renderer */
	private ImgRenderer renderer;

	/** The attribute map */
	private Map<String, Object> attributeMap;

	/**
	 * Constructor
	 */
	public AbstractImageTag() {
		super();
		this.attributeMap = new HashMap<>();
	}

	/**
	 * Returns the attribute map
	 * 
	 * @return the attribute map
	 */
	protected Map<String, Object> getAttributeMap() {
		return this.attributeMap;
	}

	/**
	 * @param align
	 *            the align to set
	 */
	public void setAlign(String align) {
		getAttributeMap().put("align", align);
	}

	/**
	 * @param border
	 *            the border to set
	 */
	public void setBorder(String border) {
		getAttributeMap().put("border", border);
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		getAttributeMap().put("name", name);
	}

	/**
	 * @param style
	 *            the style to set
	 */
	public void setStyle(String style) {
		getAttributeMap().put("style", style);
	}

	/**
	 * @param styleClass
	 *            the styleClass to set
	 */
	public void setStyleClass(String styleClass) {
		getAttributeMap().put("class", styleClass);
	}

	/**
	 * @param styleId
	 *            the styleId to set
	 */
	public void setStyleId(String styleId) {
		getAttributeMap().put("id", styleId);
	}

	/**
	 * @param alt
	 *            the alt to set
	 */
	public void setAlt(String alt) {
		getAttributeMap().put("alt", alt);
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		getAttributeMap().put("title", title);
	}

	/**
	 * @param lang
	 *            the lang to set
	 */
	public void setLang(String lang) {
		getAttributeMap().put("lang", lang);
	}

	/**
	 * @param dir
	 *            the dir to set
	 */
	public void setDir(String dir) {
		getAttributeMap().put("dir", dir);
	}

	/**
	 * @param onclick
	 *            the onclick to set
	 */
	public void setOnclick(String onclick) {
		getAttributeMap().put("onclick", onclick);
	}

	/**
	 * @param ondblclick
	 *            the ondblclick to set
	 */
	public void setOndblclick(String ondblclick) {
		getAttributeMap().put("ondblclick", ondblclick);
	}

	/**
	 * @param onmouseover
	 *            the onmouseover to set
	 */
	public void setOnmouseover(String onmouseover) {
		getAttributeMap().put("onmouseover", onmouseover);
	}

	/**
	 * @param onmouseout
	 *            the onmouseout to set
	 */
	public void setOnmouseout(String onmouseout) {
		getAttributeMap().put("onmouseout", onmouseout);
	}

	/**
	 * @param onmousemove
	 *            the onmousemove to set
	 */
	public void setOnmousemove(String onmousemove) {
		getAttributeMap().put("onmousemove", onmousemove);
	}

	/**
	 * @param onmousedown
	 *            the onmousedown to set
	 */
	public void setOnmousedown(String onmousedown) {
		getAttributeMap().put("onmousedown", onmousedown);
	}

	/**
	 * @param onmouseup
	 *            the onmouseup to set
	 */
	public void setOnmouseup(String onmouseup) {
		getAttributeMap().put("onmouseup", onmouseup);
	}

	/**
	 * @param onkeydown
	 *            the onkeydown to set
	 */
	public void setOnkeydown(String onkeydown) {
		getAttributeMap().put("onkeydown", onkeydown);
	}

	/**
	 * @param onkeyup
	 *            the onkeyup to set
	 */
	public void setOnkeyup(String onkeyup) {
		getAttributeMap().put("onkeyup", onkeyup);
	}

	/**
	 * @param onkeypress
	 *            the onkeypress to set
	 */
	public void setOnkeypress(String onkeypress) {
		getAttributeMap().put("onkeypress", onkeypress);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.TagSupport#release()
	 */
	@Override
	public void release() {
		super.release();
		this.attributeMap = new HashMap<>();
	}

	/**
	 * Render the IMG tag.
	 * 
	 * @throws JspException
	 *             if a JSP exception has occurred
	 */
	@Override
	public int doEndTag() throws JspException {

		try {

			BinaryResourcesHandler rsHandler = null;
			if ((rsHandler = (BinaryResourcesHandler) pageContext.getServletContext()
					.getAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE)) == null)
				throw new IllegalStateException(
						"Binary ResourceBundlesHandler not present in servlet context. Initialization of Jawr either failed or never occurred.");

			JawrConfig jawrConfig = rsHandler.getConfig();
			this.renderer = RendererFactory.getImgRenderer(jawrConfig, isPlainImage());
			this.renderer.renderImage(getImgSrcToRender(), getAttributeMap(), pageContext.getOut());
		} catch (IOException e) {
			throw new JspException(e);
		} finally {
			// Reset the Thread local for the Jawr context
			ThreadLocalJawrContext.reset();
		}

		return (EVAL_PAGE);
	}

	/**
	 * Returns the flag indicating if the image to render is a plain image or
	 * not
	 * 
	 * @return true if the image to render is a plain image or not
	 */
	protected abstract boolean isPlainImage();

}
