/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.test.context.web;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * TODO [SPR-9864] Document ServletTestExecutionListener.
 *
 * @author Sam Brannen
 * @since 3.2
 */
public class ServletTestExecutionListener implements TestExecutionListener {

	private static final Log logger = LogFactory.getLog(ServletTestExecutionListener.class);


	/**
	 * The default implementation is <em>empty</em>. Can be overridden by
	 * subclasses as necessary.
	 *
	 * @see TestExecutionListener#beforeTestClass(TestContext)
	 */
	public void beforeTestClass(TestContext testContext) throws Exception {
		/* no-op */
	}

	/**
	 * TODO [SPR-9864] Document overridden prepareTestInstance().
	 *
	 * @see TestExecutionListener#prepareTestInstance(TestContext)
	 */
	public void prepareTestInstance(TestContext testContext) throws Exception {
		setUpRequestContextIfNecessary(testContext);
	}

	/**
	 * TODO [SPR-9864] Document overridden beforeTestMethod().
	 *
	 * @see TestExecutionListener#beforeTestMethod(TestContext)
	 */
	public void beforeTestMethod(TestContext testContext) throws Exception {
		setUpRequestContextIfNecessary(testContext);
	}

	/**
	 * TODO [SPR-9864] Document overridden afterTestMethod().
	 *
	 * @see TestExecutionListener#afterTestMethod(TestContext)
	 */
	public void afterTestMethod(TestContext testContext) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Resetting RequestContextHolder for test context %s.", testContext));
		}
		RequestContextHolder.resetRequestAttributes();
	}

	/**
	 * The default implementation is <em>empty</em>. Can be overridden by
	 * subclasses as necessary.
	 *
	 * @see TestExecutionListener#afterTestClass(TestContext)
	 */
	public void afterTestClass(TestContext testContext) throws Exception {
		/* no-op */
	}

	/**
	 * TODO [SPR-9864] Document setUpRequestContext().
	 *
	 * @param testContext
	 * @param servletContext
	 */
	private void setUpRequestContextIfNecessary(TestContext testContext) {

		ApplicationContext context = testContext.getApplicationContext();

		if (context instanceof WebApplicationContext) {
			WebApplicationContext wac = (WebApplicationContext) context;
			ServletContext servletContext = wac.getServletContext();
			if (!(servletContext instanceof MockServletContext)) {
				throw new IllegalStateException(String.format(
					"The WebApplicationContext for test context %s must be configured with a MockServletContext.",
					testContext));
			}

			if (logger.isDebugEnabled()) {
				logger.debug(String.format(
					"Setting up MockHttpServletRequest, MockHttpServletResponse, ServletWebRequest, and RequestContextHolder for test context %s.",
					testContext));
			}

			if (RequestContextHolder.getRequestAttributes() == null) {
				MockServletContext mockServletContext = (MockServletContext) servletContext;
				MockHttpServletRequest request = new MockHttpServletRequest(mockServletContext);
				MockHttpServletResponse response = new MockHttpServletResponse();
				ServletWebRequest servletWebRequest = new ServletWebRequest(request, response);

				RequestContextHolder.setRequestAttributes(servletWebRequest);

				if (wac instanceof ConfigurableApplicationContext) {
					@SuppressWarnings("resource")
					ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) wac;
					ConfigurableListableBeanFactory bf = configurableApplicationContext.getBeanFactory();
					bf.registerResolvableDependency(MockHttpServletResponse.class, response);
					bf.registerResolvableDependency(ServletWebRequest.class, servletWebRequest);
				}
			}
		}
	}

}
