/**
 * Copyright 2008-2010 Ibrahim Chaehoi
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.jawr.web.taglib.el;

import javax.servlet.jsp.JspException;

import net.jawr.web.taglib.CSSBundleTag;

import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;

/**
 * This class defines the EL version of Css Bundle tag
 * 
 * @author Ibrahim Chaehoi
 */
public class ELCSSBundleTag extends CSSBundleTag {

	/**
	 * The serial version UID
	 */
	private static final long serialVersionUID = 5248284937648399910L;

	/**
	 * Instance variable mapped to "src" tag attribute. (Mapping set in
	 * associated BeanInfo class.)
	 */
	private String srcExpr;

	/**
	 * Instance variable mapped to "useRandomParam" tag attribute. (Mapping set
	 * in associated BeanInfo class.)
	 */
	private String useRandomParamExpr;

	/**
	 * Instance variable mapped to "media" tag attribute. (Mapping set in
	 * associated BeanInfo class.)
	 */
	private String mediaExpr;

	/** 
	 * The flag indicating if we must display alternate stylesheets 
	 */
    private String displayAlternateExpr;
    
    /** 
	 * The flag indicating if it's an alternate stylesheet 
	 */
    private String alternateExpr;
    
    /** 
     * The title 
     */
    private String titleExpr;
    
    /**
	 * Returns the srcExpr
	 * 
	 * @return the srcExpr
	 */
	public String getSrcExpr() {
		return srcExpr;
	}

	/**
	 * Sets the srcExpr
	 * 
	 * @param srcExpr
	 *            the srcExpr to set
	 */
	public void setSrcExpr(String srcExpr) {
		this.srcExpr = srcExpr;
	}

	/**
	 * Returns the useRandomParamExpr
	 * 
	 * @return the useRandomParamExpr
	 */
	public String getUseRandomParamExpr() {
		return useRandomParamExpr;
	}

	/**
	 * Sets the useRandomParamExpr
	 * 
	 * @param useRandomParamExpr
	 *            the useRandomParamExpr to set
	 */
	public void setUseRandomParamExpr(String useRandomParamExpr) {
		this.useRandomParamExpr = useRandomParamExpr;
	}

	/**
	 * Returns the mediaExpr
	 * 
	 * @return the mediaExpr
	 */
	public String getMediaExpr() {
		return mediaExpr;
	}

	/**
	 * Sets the mediaExpr
	 * 
	 * @param mediaExpr
	 *            the mediaExpr to set
	 */
	public void setMediaExpr(String mediaExpr) {
		this.mediaExpr = mediaExpr;
	}

	/**
	 * Gets the alternate expression
	 * @return the alternateExpr
	 */
	public String getAlternateExpr() {
		return alternateExpr;
	}

	/**
	 * Sets the alternate expression
	 * @param alternateExpr the alternateExpr to set
	 */
	public void setAlternateExpr(String alternateExpr) {
		this.alternateExpr = alternateExpr;
	}

	/**
	 * Gets the alternate expression
	 * @return the displayAlternateExpr
	 */
	public String getDisplayAlternateExpr() {
		return displayAlternateExpr;
	}

	/**
	 * Sets the alternate expression
	 * @param getDisplayAlternateExpr the getDisplayAlternateExpr to set
	 */
	public void setDisplayAlternateExpr(String displayAlternateExpr) {
		this.displayAlternateExpr = displayAlternateExpr;
	}
	
	/**
	 * Gets the title expression
	 * @return the titleExpr
	 */
	public String getTitleExpr() {
		return titleExpr;
	}

	/**
	 * Sets the title expression
	 * @param titleExpr the titleExpr to set
	 */
	public void setTitleExpr(String titleExpr) {
		this.titleExpr = titleExpr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.taglib.AbstractResourceBundleTag#doStartTag()
	 */
	public int doStartTag() throws JspException {

		String string = null;
		Boolean bool = null;

		if (srcExpr != null) {
			string = (String) ExpressionEvaluatorManager.evaluate("srcExpr",
					srcExpr, String.class, this, pageContext);
			setSrc(string);
		}

		if (useRandomParamExpr != null) {
			string = (String) ExpressionEvaluatorManager.evaluate(
					"useRandomParamExpr", useRandomParamExpr, String.class,
					this, pageContext);
			setUseRandomParam(string);
		}

		if (mediaExpr != null) {
			string = (String) ExpressionEvaluatorManager.evaluate("mediaExpr",
					mediaExpr, String.class, this, pageContext);
			setMedia(string);
		}

		if (titleExpr != null) {
			string = (String) ExpressionEvaluatorManager.evaluate("titleExpr",
					titleExpr, String.class, this, pageContext);
			setTitle(string);
		}

		if (alternateExpr != null) {
			bool = (Boolean) ExpressionEvaluatorManager.evaluate("alternateExpr",
					alternateExpr, Boolean.class, this, pageContext);
			setAlternate(bool.booleanValue());
		}
		
		if (displayAlternateExpr != null) {
			bool = (Boolean) ExpressionEvaluatorManager.evaluate("displayAlternateExpr",
					displayAlternateExpr, Boolean.class, this, pageContext);
			setAlternate(bool.booleanValue());
		}

		return super.doStartTag();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.TagSupport#release()
	 */
	public void release() {
		super.release();
		setSrcExpr(null);
		setUseRandomParamExpr(null);
		setMediaExpr(null);
		setTitleExpr(null);
		setAlternateExpr(null);
		setDisplayAlternateExpr(null);
	}
}
