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
package net.jawr.web.taglib.el;

import javax.servlet.jsp.JspException;

import net.jawr.web.taglib.ImageInputTag;

/**
 * This class defines the EL version of image input HTML tag
 * 
 * @author Ibrahim Chaehoi
 */
public class ELImageInputTag extends ImageInputTag {
	
	/** The serial version UID */
	private static final long serialVersionUID = -7065803916220144608L;

	/**
     * Instance variable mapped to "base64" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String base64Expr;
    
	/**
     * Instance variable mapped to "align" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String alignExpr;

    /**
     * Instance variable mapped to "alt" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String altExpr;

    /**
     * Instance variable mapped to "border" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String borderExpr;

    /**
     * Instance variable mapped to "dir" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String dirExpr;

    /**
     * Instance variable mapped to "disabled" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String disabledExpr;

    /**
     * Instance variable mapped to "lang" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String langExpr;

    /**
     * Instance variable mapped to "onblur" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String onblurExpr;

    /**
     * Instance variable mapped to "onchange" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String onchangeExpr;

    /**
     * Instance variable mapped to "onclick" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String onclickExpr;

    /**
     * Instance variable mapped to "ondblclick" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String ondblclickExpr;

    /**
     * Instance variable mapped to "onfocus" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String onfocusExpr;

    /**
     * Instance variable mapped to "onkeydown" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String onkeydownExpr;

    /**
     * Instance variable mapped to "onkeypress" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String onkeypressExpr;

    /**
     * Instance variable mapped to "onkeyup" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String onkeyupExpr;

    /**
     * Instance variable mapped to "onmousedown" tag attribute. (Mapping set
     * in associated BeanInfo class.)
     */
    private String onmousedownExpr;

    /**
     * Instance variable mapped to "onmousemove" tag attribute. (Mapping set
     * in associated BeanInfo class.)
     */
    private String onmousemoveExpr;

    /**
     * Instance variable mapped to "onmouseout" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String onmouseoutExpr;

    /**
     * Instance variable mapped to "onmouseover" tag attribute. (Mapping set
     * in associated BeanInfo class.)
     */
    private String onmouseoverExpr;

    /**
     * Instance variable mapped to "onmouseup" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String onmouseupExpr;

    /**
     * Instance variable mapped to "src" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String srcExpr;

    /**
     * Instance variable mapped to "style" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String styleExpr;

    /**
     * Instance variable mapped to "styleClass" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String styleClassExpr;

    /**
     * Instance variable mapped to "styleId" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String styleIdExpr;

    /**
     * Instance variable mapped to "tabindex" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String tabindexExpr;

    /**
     * Instance variable mapped to "title" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String titleExpr;

    /**
     * Instance variable mapped to "value" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    private String valueExpr;

    /**
	 * Returns the base64 flag expression
	 * @return the base64 flag expression
	 */
	public String getBase64Expr() {
		return base64Expr;
	}

	/**
     * Getter method for "align" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getAlignExpr() {
        return (alignExpr);
    }

    /**
     * Getter method for "alt" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getAltExpr() {
        return (altExpr);
    }

    /**
     * Getter method for "border" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getBorderExpr() {
        return (borderExpr);
    }

    /**
     * Getter method for "dir" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getDirExpr() {
        return (dirExpr);
    }

    /**
     * Getter method for "disabled" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getDisabledExpr() {
        return (disabledExpr);
    }

    /**
     * Getter method for "lang" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getLangExpr() {
        return (langExpr);
    }

    /**
     * Getter method for "onblur" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getOnblurExpr() {
        return (onblurExpr);
    }

    /**
     * Getter method for "onchange" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getOnchangeExpr() {
        return (onchangeExpr);
    }

    /**
     * Getter method for "onclick" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getOnclickExpr() {
        return (onclickExpr);
    }

    /**
     * Getter method for "ondblclick" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    public String getOndblclickExpr() {
        return (ondblclickExpr);
    }

    /**
     * Getter method for "onfocus" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getOnfocusExpr() {
        return (onfocusExpr);
    }

    /**
     * Getter method for "onkeydown" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getOnkeydownExpr() {
        return (onkeydownExpr);
    }

    /**
     * Getter method for "onkeypress" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    public String getOnkeypressExpr() {
        return (onkeypressExpr);
    }

    /**
     * Getter method for "onkeyup" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getOnkeyupExpr() {
        return (onkeyupExpr);
    }

    /**
     * Getter method for "onmousedown" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    public String getOnmousedownExpr() {
        return (onmousedownExpr);
    }

    /**
     * Getter method for "onmousemove" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    public String getOnmousemoveExpr() {
        return (onmousemoveExpr);
    }

    /**
     * Getter method for "onmouseout" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    public String getOnmouseoutExpr() {
        return (onmouseoutExpr);
    }

    /**
     * Getter method for "onmouseover" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    public String getOnmouseoverExpr() {
        return (onmouseoverExpr);
    }

    /**
     * Getter method for "onmouseup" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getOnmouseupExpr() {
        return (onmouseupExpr);
    }

    /**
     * Getter method for "src" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getSrcExpr() {
        return (srcExpr);
    }

    /**
     * Getter method for "style" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getStyleExpr() {
        return (styleExpr);
    }

    /**
     * Getter method for "styleClass" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    public String getStyleClassExpr() {
        return (styleClassExpr);
    }

    /**
     * Getter method for "styleId" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getStyleIdExpr() {
        return (styleIdExpr);
    }

    /**
     * Getter method for "tabindex" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getTabindexExpr() {
        return (tabindexExpr);
    }

    /**
     * Getter method for "title" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getTitleExpr() {
        return (titleExpr);
    }

    /**
     * Getter method for "value" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public String getValueExpr() {
        return (valueExpr);
    }

    /**
     * Setter method for "base64" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setBase64Expr(String base64Expr) {
		this.base64Expr = base64Expr;
	}

	/**
     * Setter method for "align" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setAlignExpr(String alignExpr) {
        this.alignExpr = alignExpr;
    }

    /**
     * Setter method for "alt" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setAltExpr(String altExpr) {
        this.altExpr = altExpr;
    }

    /**
     * Setter method for "border" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setBorderExpr(String borderExpr) {
        this.borderExpr = borderExpr;
    }

    /**
     * Setter method for "dir" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setDirExpr(String dirExpr) {
        this.dirExpr = dirExpr;
    }

    /**
     * Setter method for "disabled" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setDisabledExpr(String disabledExpr) {
        this.disabledExpr = disabledExpr;
    }

    /**
     * Setter method for "lang" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setLangExpr(String langExpr) {
        this.langExpr = langExpr;
    }

    /**
     * Setter method for "onblur" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setOnblurExpr(String onblurExpr) {
        this.onblurExpr = onblurExpr;
    }

    /**
     * Setter method for "onchange" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setOnchangeExpr(String onchangeExpr) {
        this.onchangeExpr = onchangeExpr;
    }

    /**
     * Setter method for "onclick" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setOnclickExpr(String onclickExpr) {
        this.onclickExpr = onclickExpr;
    }

    /**
     * Setter method for "ondblclick" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    public void setOndblclickExpr(String ondblclickExpr) {
        this.ondblclickExpr = ondblclickExpr;
    }

    /**
     * Setter method for "onfocus" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setOnfocusExpr(String onfocusExpr) {
        this.onfocusExpr = onfocusExpr;
    }

    /**
     * Setter method for "onkeydown" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setOnkeydownExpr(String onkeydownExpr) {
        this.onkeydownExpr = onkeydownExpr;
    }

    /**
     * Setter method for "onkeypress" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    public void setOnkeypressExpr(String onkeypressExpr) {
        this.onkeypressExpr = onkeypressExpr;
    }

    /**
     * Setter method for "onkeyup" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setOnkeyupExpr(String onkeyupExpr) {
        this.onkeyupExpr = onkeyupExpr;
    }

    /**
     * Setter method for "onmousedown" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    public void setOnmousedownExpr(String onmousedownExpr) {
        this.onmousedownExpr = onmousedownExpr;
    }

    /**
     * Setter method for "onmousemove" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    public void setOnmousemoveExpr(String onmousemoveExpr) {
        this.onmousemoveExpr = onmousemoveExpr;
    }

    /**
     * Setter method for "onmouseout" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    public void setOnmouseoutExpr(String onmouseoutExpr) {
        this.onmouseoutExpr = onmouseoutExpr;
    }

    /**
     * Setter method for "onmouseover" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    public void setOnmouseoverExpr(String onmouseoverExpr) {
        this.onmouseoverExpr = onmouseoverExpr;
    }

    /**
     * Setter method for "onmouseup" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setOnmouseupExpr(String onmouseupExpr) {
        this.onmouseupExpr = onmouseupExpr;
    }

    /**
     * Setter method for "src" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setSrcExpr(String srcExpr) {
        this.srcExpr = srcExpr;
    }

    /**
     * Setter method for "style" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setStyleExpr(String styleExpr) {
        this.styleExpr = styleExpr;
    }

    /**
     * Setter method for "styleClass" tag attribute. (Mapping set in
     * associated BeanInfo class.)
     */
    public void setStyleClassExpr(String styleClassExpr) {
        this.styleClassExpr = styleClassExpr;
    }

    /**
     * Setter method for "styleId" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setStyleIdExpr(String styleIdExpr) {
        this.styleIdExpr = styleIdExpr;
    }

    /**
     * Setter method for "tabindex" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setTabindexExpr(String tabindexExpr) {
        this.tabindexExpr = tabindexExpr;
    }

    /**
     * Setter method for "title" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setTitleExpr(String titleExpr) {
        this.titleExpr = titleExpr;
    }

    /**
     * Setter method for "value" tag attribute. (Mapping set in associated
     * BeanInfo class.)
     */
    public void setValueExpr(String valueExpr) {
        this.valueExpr = valueExpr;
    }

    /**
     * Resets attribute values for tag reuse.
     */
    public void release() {
        super.release();
        setBase64Expr(null);
        setAlignExpr(null);
        setAltExpr(null);
        setBorderExpr(null);
        setDirExpr(null);
        setDisabledExpr(null);
        setLangExpr(null);
        setOnblurExpr(null);
        setOnchangeExpr(null);
        setOnclickExpr(null);
        setOndblclickExpr(null);
        setOnfocusExpr(null);
        setOnkeydownExpr(null);
        setOnkeypressExpr(null);
        setOnkeyupExpr(null);
        setOnmousedownExpr(null);
        setOnmousemoveExpr(null);
        setOnmouseoutExpr(null);
        setOnmouseoverExpr(null);
        setOnmouseupExpr(null);
        setSrcExpr(null);
        setStyleExpr(null);
        setStyleClassExpr(null);
        setStyleIdExpr(null);
        setTabindexExpr(null);
        setTitleExpr(null);
        setValueExpr(null);
    }

    /**
     * Process the start tag.
     *
     * @throws JspException if a JSP exception has occurred
     */
    public int doStartTag() throws JspException {
        evaluateExpressions();

        return (super.doStartTag());
    }

    /**
     * Processes all attribute values which use the JSTL expression evaluation
     * engine to determine their values.
     *
     * @throws JspException if a JSP exception has occurred
     */
    private void evaluateExpressions()
        throws JspException {
        String string = null;
        Boolean bool = null;

        if (getBase64Expr() != null && (bool =
	            EvalHelper.evalBoolean("base64", getBase64Expr(), this,
	                pageContext)) != null) {
	        setBase64(bool.booleanValue());
	    }
        
        //  The "align" attribute is deprecated.  This needs to be removed when
        //  the "align" attribute is finally removed.
        if (getAlignExpr() != null && (string =
                EvalHelper.evalString("align", getAlignExpr(), this, pageContext)) != null) {
            setAlign(string);
        }

        if (getAltExpr() != null && (string =
                EvalHelper.evalString("alt", getAltExpr(), this, pageContext)) != null) {
            setAlt(string);
        }

        if (getBorderExpr() != null && (string =
                EvalHelper.evalString("border", getBorderExpr(), this,
                    pageContext)) != null) {
            setBorder(string);
        }

        if (getDirExpr() != null && (string =
        		EvalHelper.evalString("dir", getDirExpr(), this,
        			pageContext)) != null) {
        	setDir(string);
        }
        
        if (getDisabledExpr() != null && (bool =
                EvalHelper.evalBoolean("disabled", getDisabledExpr(), this,
                    pageContext)) != null) {
            setDisabled(bool.booleanValue());
        }

        if (getLangExpr() != null && (string =
            	EvalHelper.evalString("lang", getLangExpr(), this,
            		pageContext)) != null) {
        	setLang(string);
        }

        if (getOnblurExpr() != null && (string =
                EvalHelper.evalString("onblur", getOnblurExpr(), this,
                    pageContext)) != null) {
            setOnblur(string);
        }

        if (getOnchangeExpr() != null && (string =
                EvalHelper.evalString("onchange", getOnchangeExpr(), this,
                    pageContext)) != null) {
            setOnchange(string);
        }

        if (getOnclickExpr() != null && (string =
                EvalHelper.evalString("onclick", getOnclickExpr(), this,
                    pageContext)) != null) {
            setOnclick(string);
        }

        if (getOndblclickExpr() != null && (string =
                EvalHelper.evalString("ondblclick", getOndblclickExpr(), this,
                    pageContext)) != null) {
            setOndblclick(string);
        }

        if (getOnfocusExpr() != null && (string =
                EvalHelper.evalString("onfocus", getOnfocusExpr(), this,
                    pageContext)) != null) {
            setOnfocus(string);
        }

        if (getOnkeydownExpr() != null && (string =
                EvalHelper.evalString("onkeydown", getOnkeydownExpr(), this,
                    pageContext)) != null) {
            setOnkeydown(string);
        }

        if (getOnkeypressExpr() != null && (string =
                EvalHelper.evalString("onkeypress", getOnkeypressExpr(), this,
                    pageContext)) != null) {
            setOnkeypress(string);
        }

        if (getOnkeyupExpr() != null && (string =
                EvalHelper.evalString("onkeyup", getOnkeyupExpr(), this,
                    pageContext)) != null) {
            setOnkeyup(string);
        }

        if (getOnmousedownExpr() != null && (string =
                EvalHelper.evalString("onmousedown", getOnmousedownExpr(),
                    this, pageContext)) != null) {
            setOnmousedown(string);
        }

        if (getOnmousemoveExpr() != null && (string =
                EvalHelper.evalString("onmousemove", getOnmousemoveExpr(),
                    this, pageContext)) != null) {
            setOnmousemove(string);
        }

        if (getOnmouseoutExpr() != null && (string =
                EvalHelper.evalString("onmouseout", getOnmouseoutExpr(), this,
                    pageContext)) != null) {
            setOnmouseout(string);
        }

        if (getOnmouseoverExpr() != null && (string =
                EvalHelper.evalString("onmouseover", getOnmouseoverExpr(),
                    this, pageContext)) != null) {
            setOnmouseover(string);
        }

        if (getOnmouseupExpr() != null && (string =
                EvalHelper.evalString("onmouseup", getOnmouseupExpr(), this,
                    pageContext)) != null) {
            setOnmouseup(string);
        }

        if (getSrcExpr() != null && (string =
                EvalHelper.evalString("src", getSrcExpr(), this, pageContext)) != null) {
            setSrc(string);
        }

        if (getStyleExpr() != null && (string =
                EvalHelper.evalString("style", getStyleExpr(), this, pageContext)) != null) {
            setStyle(string);
        }

        if (getStyleClassExpr() != null && (string =
                EvalHelper.evalString("styleClass", getStyleClassExpr(), this,
                    pageContext)) != null) {
            setStyleClass(string);
        }

        if (getStyleIdExpr() != null && (string =
                EvalHelper.evalString("styleId", getStyleIdExpr(), this,
                    pageContext)) != null) {
            setStyleId(string);
        }

        if (getTabindexExpr() != null && (string =
                EvalHelper.evalString("tabindex", getTabindexExpr(), this,
                    pageContext)) != null) {
            setTabindex(string);
        }

        if (getTitleExpr() != null && (string =
                EvalHelper.evalString("title", getTitleExpr(), this, pageContext)) != null) {
            setTitle(string);
        }

       if (getValueExpr() != null && (string =
                EvalHelper.evalString("value", getValueExpr(), this, pageContext)) != null) {
            setValue(string);
        }
    }
    
}
