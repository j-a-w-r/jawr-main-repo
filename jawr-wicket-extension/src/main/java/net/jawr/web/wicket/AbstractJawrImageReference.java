/**
 * Copyright 2009-2014 Ibrahim Chaehoi
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jawr.web.JawrConstant;
import net.jawr.web.context.ThreadLocalJawrContext;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.renderer.RendererFactory;
import net.jawr.web.resource.bundle.renderer.image.ImgRenderer;
import net.jawr.web.taglib.ImageTagUtils;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.value.IValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines the abstract class for Jawr image reference for wicket
 * 
 * @author Ibrahim Chaehoi
 */
public abstract class AbstractJawrImageReference extends WebMarkupContainer {

	/** The serial version */
	private static final long serialVersionUID = 8981244472547751100L;

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AbstractJawrReference.class);

	/** The image renderer */
	private ImgRenderer renderer;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the component ID
	 */
	public AbstractJawrImageReference(String id) {
		super(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.wicket.MarkupContainer#onRender()
	 */
	protected void onRender() {

		MarkupStream markupStream = findMarkupStream();
		try {
			final ComponentTag openTag = markupStream.getTag();
			final ComponentTag tag = openTag.mutable();
			final IValueMap attributes = tag.getAttributes();

			String src = (String) attributes.get("src");
			boolean base64 = Boolean.valueOf((String) attributes.get("base64"))
					.booleanValue();

			// src is mandatory
			if (null == src) {
				throw new IllegalStateException(
						"The src attribute is mandatory for this Jawr tag. ");
			}

			// Retrieve the image resource handler
			ServletWebRequest servletWebRequest = (ServletWebRequest) getRequest();
			HttpServletRequest request = servletWebRequest
					.getContainerRequest();
			BinaryResourcesHandler binaryRsHandler = (BinaryResourcesHandler) WebApplication
					.get().getServletContext()
					.getAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE);

			if (null == binaryRsHandler)
				throw new IllegalStateException(
						"You are using a Jawr image tag while the Jawr Image servlet has not been initialized. Initialization of Jawr Image servlet either failed or never occurred.");

			final Response response = getResponse();

			src = ImageTagUtils.getImageUrl(src, base64, binaryRsHandler, request,
					getHttpServletResponseUrlEncoder(response));

			Writer writer = new RedirectWriter(response);

			this.renderer = RendererFactory.getImgRenderer(
					binaryRsHandler.getConfig(), isPlainImage());
			this.renderer.renderImage(src, attributes, writer);

		} catch (IOException ex) {
			LOGGER.error("onRender() error : ", ex);
		} finally {

			// Reset the Thread local for the Jawr context
			ThreadLocalJawrContext.reset();
		}

		markupStream.skipComponent();
	}

	/**
	 * Returns the flag indicating if the image to render is a plain image or
	 * not
	 * 
	 * @return true if the image to render is a plain image or not
	 */
	protected abstract boolean isPlainImage();

	/**
	 * Returns the HttpServletResponse which will be used to encode the URL
	 * 
	 * @param response
	 *            the response
	 * @return the HttpServletResponse which will be used to encode the URL
	 */
	private HttpServletResponse getHttpServletResponseUrlEncoder(
			final Response response) {

		return new HttpServletResponse() {

			public void setLocale(Locale loc) {

			}

			public void setContentType(String type) {

			}

			public void setContentLength(int len) {

			}

			public void setBufferSize(int size) {

			}

			public void resetBuffer() {

			}

			public void reset() {

			}

			public boolean isCommitted() {
				return false;
			}

			public PrintWriter getWriter() throws IOException {
				return new PrintWriter(new RedirectWriter(getResponse()));
			}

			public ServletOutputStream getOutputStream() throws IOException {
				return null;
			}

			public Locale getLocale() {
				return null;
			}

			public int getBufferSize() {
				return 0;
			}

			public void flushBuffer() throws IOException {

			}

			public void setStatus(int sc, String sm) {

			}

			public void setStatus(int sc) {

			}

			public void setIntHeader(String name, int value) {

			}

			public void setHeader(String name, String value) {

			}

			public void setDateHeader(String name, long date) {

			}

			public void sendRedirect(String location) throws IOException {

			}

			public void sendError(int sc, String msg) throws IOException {

			}

			public void sendError(int sc) throws IOException {

			}

			public String encodeUrl(String url) {
				return response.encodeURL(url).toString();
			}

			public String encodeURL(String url) {
				return response.encodeURL(url).toString();
			}

			public String encodeRedirectUrl(String url) {
				return null;
			}

			public String encodeRedirectURL(String url) {
				return null;
			}

			public boolean containsHeader(String name) {
				return false;
			}

			public void addIntHeader(String name, int value) {

			}

			public void addHeader(String name, String value) {

			}

			public void addDateHeader(String name, long date) {

			}

			public void addCookie(Cookie cookie) {

			}

			public String getContentType() {
				return null;
			}

			public void setCharacterEncoding(String charset) {

			}

			@Override
			public String getCharacterEncoding() {
				return "UTF-8";
			}
		};
	}
}
