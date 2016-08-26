/**
 * Copyright 2016 Danny Trunk
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

package net.jawr.web.autoconfigure;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.jawr.web.JawrConstant;
import net.jawr.web.servlet.JawrSpringController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

/**
 * {@link EnableAutoConfiguration Auto configuration} for Jawr support.
 *
 * @author Danny Trunk
 * @since 3.9
 *
 */
@Configuration
@EnableConfigurationProperties(JawrProperties.class)
@ConditionalOnClass(JawrSpringController.class)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class JawrAutoConfiguration {
	@Autowired
	private JawrProperties jawrProperties;

	@Bean
	@ConditionalOnMissingBean(name = "jawrProperties")
	public Properties jawrProperties() {
		Properties jawrProperties = new Properties();
		for (Map.Entry<String, String> entry : this.jawrProperties.getJawr().entrySet()) {
			jawrProperties.put("jawr." + entry.getKey(), entry.getValue());
		}
		return jawrProperties;
	}

	@Configuration
	@ConditionalOnMissingBean(name = "jawrBinaryController")
	public static class JawrBinaryControllerConfiguration {
		@Autowired
		private Properties jawrProperties;

		@Bean
		public JawrSpringController jawrBinaryController() {
			JawrSpringController jawrBinaryController = new JawrSpringController();
			jawrBinaryController.setConfiguration(jawrProperties);
			jawrBinaryController.setType(JawrConstant.BINARY_TYPE);
			return jawrBinaryController;
		}
	}

	@Configuration
	@ConditionalOnMissingBean(name = "jawrCssController")
	public static class JawrCssControllerConfiguration {
		@Autowired
		private Properties jawrProperties;

		@Bean
		@DependsOn("jawrBinaryController")
		public JawrSpringController jawrCssController() {
			JawrSpringController jawrCssController = new JawrSpringController();
			jawrCssController.setConfiguration(jawrProperties);
			jawrCssController.setType(JawrConstant.CSS_TYPE);
			return jawrCssController;
		}
	}

	@Configuration
	@ConditionalOnMissingBean(name = "jawrJsController")
	public static class JawrJsControllerConfiguration {
		@Autowired
		private Properties jawrProperties;

		@Bean
		public JawrSpringController jawrJsController() {
			JawrSpringController jawrJsController = new JawrSpringController();
			jawrJsController.setConfiguration(jawrProperties);
			jawrJsController.setType(JawrConstant.JS_TYPE);
			return jawrJsController;
		}
	}

	@Configuration
	@ConditionalOnMissingBean(name = "jawrHandlerMapping")
	public static class jawrHandlerMappingConfiguration {
		@Autowired
		private JawrSpringController jawrJsController;

		@Autowired
		private JawrSpringController jawrCssController;

		@Autowired
		private JawrSpringController jawrBinaryController;

		@Bean
		public HandlerMapping jawrHandlerMapping() {
			SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
			handlerMapping.setOrder(Ordered.HIGHEST_PRECEDENCE);

			Map<String, Object> urlMap = new HashMap<String, Object>();
			urlMap.put("**/*.css", jawrCssController);
			urlMap.put("**/*.eot", jawrBinaryController);
			urlMap.put("**/*.gif", jawrBinaryController);
			urlMap.put("**/*.ico", jawrBinaryController);
			urlMap.put("**/*.jpg", jawrBinaryController);
			urlMap.put("**/*.jpeg", jawrBinaryController);
			urlMap.put("**/*.js", jawrJsController);
			urlMap.put("**/*.png", jawrBinaryController);
			urlMap.put("**/*.ttf", jawrBinaryController);
			urlMap.put("**/*.woff", jawrBinaryController);
			urlMap.put("**/*.woff2", jawrBinaryController);
			urlMap.put("**/*.svg", jawrBinaryController);
			handlerMapping.setUrlMap(urlMap);

			return handlerMapping;
		}
	}
}
