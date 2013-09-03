/**
 * Copyright 2008 Ibrahim Chaehoi
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

import net.jawr.web.taglib.JavascriptBundleTag;

import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;

/**
 * This class defines the EL version of Javascript Bundle tag
 * 
 * @author Ibrahim Chaehoi
 */
public class ELJavascriptBundleTag extends JavascriptBundleTag {

	/** The serial version UID */
	private static final long serialVersionUID = -5893064781111690672L;

	/** The source expression */
	private String srcExpr;

	/** The use random parameter expression */
	private String useRandomParamExpr;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.taglib.AbstractResourceBundleTag#doStartTag()
	 */
	public int doStartTag() throws JspException {

		String string = null;
		
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
	}
}