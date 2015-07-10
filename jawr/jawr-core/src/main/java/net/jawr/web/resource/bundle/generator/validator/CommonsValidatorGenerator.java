/**
 * Copyright 2008-2012  Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.validator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.generator.AbstractJavascriptGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.JavascriptStringUtil;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;
import net.jawr.web.resource.bundle.locale.message.MessageBundleScriptCreator;

import org.apache.commons.validator.Arg;
import org.apache.commons.validator.Field;
import org.apache.commons.validator.Form;
import org.apache.commons.validator.Msg;
import org.apache.commons.validator.ValidatorAction;
import org.apache.commons.validator.ValidatorResources;
import org.apache.commons.validator.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Generates validation javascript using the apache commons validator.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 * 
 */
public class CommonsValidatorGenerator extends AbstractJavascriptGenerator {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CommonsValidatorGenerator.class.getName());
	private static final String STATIC_JAVASCRIPT_KEY = "_static";

	private Map<String, ValidatorResources> validatorResourcesMap;

	/** The resolver */
	private ResourceGeneratorResolver resolver;

	public CommonsValidatorGenerator() {
		validatorResourcesMap = new HashMap<String, ValidatorResources>();
		resolver = ResourceGeneratorResolverFactory
				.createPrefixResolver(GeneratorRegistry.COMMONS_VALIDATOR_PREFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.ResourceGenerator#createResource
	 * (java.lang.String, javax.servlet.ServletContext,
	 * java.nio.charset.Charset)
	 */
	public Reader createResource(GeneratorContext context) {
		Locale locale = null == context.getLocale() ? Locale.getDefault()
				: context.getLocale();
		String validators = context.getPath();
		String validatorParams = context.getParenthesesParam();

		String messagesNS = MessageBundleScriptCreator.DEFAULT_NAMESPACE;
		boolean stopOnErrors = true;

		StringTokenizer valparams = new StringTokenizer(validatorParams, "|");
		String validatorMappingKey = valparams.nextToken();
		if (valparams.hasMoreTokens())
			messagesNS = valparams.nextToken();
		if (valparams.hasMoreTokens())
			stopOnErrors = Boolean.valueOf(valparams.nextToken())
					.booleanValue();

		// Build the ValidatorResources if it doesn't exist yet
		if (!validatorResourcesMap.containsKey(validatorMappingKey)) {
			createValidatorResources(validatorMappingKey, context.getConfig());
		}

		ValidatorResources validatorResources = (ValidatorResources) validatorResourcesMap
				.get(validatorMappingKey);
		StringBuffer sb = new StringBuffer();
		StringTokenizer tk = new StringTokenizer(validators, "|");
		while (tk.hasMoreTokens()) {
			String validator = tk.nextToken();
			if (STATIC_JAVASCRIPT_KEY.equals(validator)) {
				sb.append(buildStaticJavascript(validatorResources));
			} else
				sb.append(buildFormJavascript(validatorResources, validator,
						locale, messagesNS, stopOnErrors));

		}

		return new StringReader(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.BaseResourceGenerator#getPathMatcher
	 * ()
	 */
	public ResourceGeneratorResolver getResolver() {

		return resolver;
	}

	/**
	 * Creates a validator for a specific form.
	 * 
	 * @param validatorResources
	 * @param formName
	 * @param locale
	 * @return
	 */
	private StringBuffer buildFormJavascript(
			ValidatorResources validatorResources, String formName,
			Locale locale, String messageNS, boolean stopOnErrors) {
		Form form = validatorResources.getForm(locale, formName);
		return createDynamicJavascript(validatorResources, form, messageNS,
				stopOnErrors);
	}

	@SuppressWarnings("unchecked")
	private StringBuffer createDynamicJavascript(ValidatorResources resources,
			Form form, String messageNS, boolean stopOnErrors) {
		StringBuffer results = new StringBuffer();

		List<ValidatorAction> actions = this.createActionList(resources, form);

		final String methods = this.createMethods(actions, stopOnErrors);

		String jsFormName = form.getName();

		results.append(this.getJavascriptBegin(jsFormName, methods));

		for (Iterator<ValidatorAction> i = actions.iterator(); i.hasNext();) {
			ValidatorAction va = i.next();
			int jscriptVar = 0;
			String functionName = null;

			if ((va.getJsFunctionName() != null)
					&& (va.getJsFunctionName().length() > 0)) {
				functionName = va.getJsFunctionName();
			} else {
				functionName = va.getName();
			}

			results.append("    function " + jsFormName + "_" + functionName
					+ " () { \n");

			for (Iterator<Field> x = form.getFields().iterator(); x.hasNext();) {
				Field field = x.next();

				// Skip indexed fields for now until there is a good way to
				// handle error messages (and the length of the list (could
				// retrieve from scope?))
				if (field.isIndexed() || !field.isDependency(va.getName())) {
					continue;
				}

				String message = null;
				Msg msg = field.getMessage(va.getName());
				if ((msg != null) && !msg.isResource()) {
					message = JavascriptStringUtil.quote(msg.toString());
				} else {
					if (msg == null) {
						message = va.getMsg();
					} else {
						message = msg.getKey();
					}
					Arg[] args = field.getArgs(va.getName());

					message = messageNS + "." + message + "(";
					for (int a = 0; a < args.length; a++) {
						if (args[a] != null) {
							if (args[a].isResource()) {
								message += messageNS + "." + args[a].getKey()
										+ "()";
							} else {
								message += "\"" + args[a].getKey() + "\"";
							}
							message += ",";
						}
					}
					message += "null)";
				}

				message = (message != null) ? message : "";

				// prefix variable with 'a' to make it a legal identifier
				results.append("     this.a" + jscriptVar++ + " = [\""
						+ field.getKey() + "\", " + (message) + ", ");

				results.append("function(varName){");

				Map<String, Var> vars = field.getVars();

				// Loop through the field's variables.
				Iterator<Entry<String, Var>> varsIterator = vars.entrySet()
						.iterator();

				while (varsIterator.hasNext()) {

					Entry<String, Var> varEntry = varsIterator.next();
					String varName = varEntry.getKey();
					Var var = varEntry.getValue();
					String varValue = var.getValue();

					// Non-resource variable
					if (var.isResource()) {
						varValue = messageNS + "." + varValue + "()";
					} else
						varValue = "'" + varValue + "'";
					String jsType = var.getJsType();

					// skip requiredif variables field, fieldIndexed, fieldTest,
					// fieldValue
					if (varName.startsWith("field")) {
						continue;
					}

					String varValueEscaped = JavascriptStringUtil
							.escape(varValue);

					if (Var.JSTYPE_INT.equalsIgnoreCase(jsType)) {
						results.append("this." + varName + "=+"
								+ varValueEscaped + "; ");
					} else if (Var.JSTYPE_REGEXP.equalsIgnoreCase(jsType)) {
						results.append("this." + varName + "=eval('/'+"
								+ varValueEscaped + "+'/'); ");
					} else if (Var.JSTYPE_STRING.equalsIgnoreCase(jsType)) {
						results.append("this." + varName + "="
								+ varValueEscaped + "; ");

						// So everyone using the latest format doesn't need to
						// change their xml files immediately.
					} else if ("mask".equalsIgnoreCase(varName)) {
						results.append("this." + varName + "=eval('/'+"
								+ varValueEscaped + "+'/'); ");
					} else {
						results.append("this." + varName + "="
								+ varValueEscaped + "; ");
					}
				}

				results.append(" return this[varName];}];\n");
			}

			results.append("    } \n\n");
		}
		return results;
	}

	/**
	 * Returns the opening script element and some initial javascript.
	 */
	protected String getJavascriptBegin(String jsFormName, String methods) {
		StringBuffer sb = new StringBuffer();
		String name = jsFormName.replace('/', '_'); // remove any '/' characters

		name = jsFormName.substring(0, 1).toUpperCase()
				+ jsFormName.substring(1, jsFormName.length());

		sb.append("\n    var bCancel = false; \n\n");
		sb.append("    function validate" + name + "(form) { \n");
		sb.append("        if (bCancel) { \n");
		sb.append("            return true; \n");
		sb.append("        } else { \n");

		// Always return true if there aren't any Javascript validation methods
		if ((methods == null) || (methods.length() == 0)) {
			sb.append("            return true; \n");
		} else {
			sb.append("            var formValidationResult; \n");
			sb.append("            formValidationResult = " + methods + "; \n");
			if (methods.indexOf("&&") >= 0) {
				sb.append("            return (formValidationResult); \n");
			} else {
				// Making Sure that Bitwise operator works:
				sb.append("            return (formValidationResult == 1); \n");
			}
		}
		sb.append("        } \n");
		sb.append("    } \n\n");

		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	private List<ValidatorAction> createActionList(
			ValidatorResources resources, Form form) {
		List<String> actionMethods = new ArrayList<String>();

		Iterator<Field> itFields = form.getFields().iterator();

		while (itFields.hasNext()) {
			Field field = itFields.next();

			for (Iterator<String> x = field.getDependencyList().iterator(); x
					.hasNext();) {
				String o = x.next();

				if ((o != null) && !actionMethods.contains(o)) {
					actionMethods.add(o);
				}
			}
		}

		List<ValidatorAction> actions = new ArrayList<ValidatorAction>();

		// Create list of ValidatorActions based on actionMethods
		Iterator<String> iterator = actionMethods.iterator();

		while (iterator.hasNext()) {
			String depends = iterator.next();
			ValidatorAction va = resources.getValidatorAction(depends);

			// throw nicer NPE for easier debugging
			if (va == null) {
				throw new NullPointerException("Depends string \"" + depends
						+ "\" was not found in validator-rules.xml.");
			}

			if ((va.getJavascript() != null)
					&& (va.getJavascript().length() > 0)) {
				actions.add(va);
			} else {
				iterator.remove();
			}
		}

		Collections.sort(actions, ACTION_COMPARATOR);

		return actions;
	}

	private String createMethods(List<ValidatorAction> actions,
			boolean stopOnErrors) {
		StringBuffer methods = new StringBuffer();
		final String methodOperator = stopOnErrors ? " && " : " & ";

		Iterator<ValidatorAction> iter = actions.iterator();

		while (iter.hasNext()) {
			ValidatorAction va = iter.next();

			if (methods.length() > 0) {
				methods.append(methodOperator);
			}

			methods.append(va.getMethod()).append("(form)");
		}

		return methods.toString();
	}

	@SuppressWarnings("unchecked")
	private StringBuffer buildStaticJavascript(
			ValidatorResources validatorResources) {
		StringBuffer sb = new StringBuffer();

		Iterator<ValidatorAction> actions = validatorResources
				.getValidatorActions().values().iterator();

		while (actions.hasNext()) {
			ValidatorAction va = actions.next();

			if (va != null) {
				String javascript = va.getJavascript();

				if ((javascript != null) && (javascript.length() > 0)) {
					sb.append(javascript).append("\n");
				}
			}
		}

		return sb;
	}

	private void createValidatorResources(String path, JawrConfig config) {

		ValidatorResources validatorResources = null;
		String configPaths = config.getConfigProperties().getProperty(path);

		StringTokenizer st = new StringTokenizer(configPaths, ",");
		InputStream[] inputStreams = new InputStream[st.countTokens()];
		int pos = 0;

		try {
			while (st.hasMoreTokens()) {
				String validatorRules = st.nextToken().trim();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Validation rules file from '"
							+ validatorRules + "'");
				}
				InputStream is = null;
				try {
					is = ClassLoaderResourceUtils.getResourceAsStream(
							validatorRules, this);
				} catch (FileNotFoundException fos) {
					is = config.getContext()
							.getResourceAsStream(validatorRules);
				}
				inputStreams[pos] = is;
				pos++;
			}
			validatorResources = new ValidatorResources(inputStreams);
		} catch (SAXException ex) {
			throw new BundlingProcessException(ex);
		} catch (IOException e) {
			throw new BundlingProcessException(e);
		}
		validatorResourcesMap.put(path, validatorResources);
	}

	private static final Comparator<ValidatorAction> ACTION_COMPARATOR = new Comparator<ValidatorAction>() {
		public int compare(ValidatorAction va1, ValidatorAction va2) {

			if (((va1.getDepends() == null) || (va1.getDepends().length() == 0))
					&& ((va2.getDepends() == null) || (va2.getDepends()
							.length() == 0))) {
				return 0;
			} else if (((va1.getDepends() != null) && (va1.getDepends()
					.length() > 0))
					&& ((va2.getDepends() == null) || (va2.getDepends()
							.length() == 0))) {
				return 1;
			} else if (((va1.getDepends() == null) || (va1.getDepends()
					.length() == 0))
					&& ((va2.getDepends() != null) && (va2.getDepends()
							.length() > 0))) {
				return -1;
			} else {
				return va1.getDependencyList().size()
						- va2.getDependencyList().size();
			}
		}
	};

}
