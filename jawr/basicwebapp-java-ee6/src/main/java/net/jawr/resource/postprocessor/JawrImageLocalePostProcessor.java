package net.jawr.resource.postprocessor;

import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.variant.VariantSet;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author timdegrande
 * @since 11/04/11
 */
public class JawrImageLocalePostProcessor implements ResourceBundlePostProcessor {
	Pattern pattern = Pattern.compile("/images/([a-z]{2}/)?");

	@Override
	public StringBuffer postProcessBundle(BundleProcessingStatus status, StringBuffer bundleString) {
		if (status.isSearchingPostProcessorVariants()) {
			List<String> langs = Arrays.asList("nl", "de", "fr", "en");
			VariantSet variantSet = new VariantSet("locale", "en", langs);

			status.addPostProcessVariant("locale", variantSet);
		} else {
			String locale = status.getVariant("locale");
			StringBuilder imageLocation = new StringBuilder("/images");
			if (locale != null) {
				imageLocation.append("/")
					.append(locale);
			}
			imageLocation.append('/');
			String result = updateField(bundleString.toString(), imageLocation.toString());

			bundleString = new StringBuffer(result);
		}

		return bundleString;
	}

	private String updateField(String bundle, String key) {
		Matcher m = pattern.matcher(bundle);

		if (m.find()) {
			return m.replaceAll(key);
		}

		return bundle;
	}
}
