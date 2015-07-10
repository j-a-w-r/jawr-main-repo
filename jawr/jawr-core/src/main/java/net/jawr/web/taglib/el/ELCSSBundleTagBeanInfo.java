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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * This class defines the bean information for the ELCSSBundleTag class.
 * 
 * @author Ibrahim Chaehoi
 */
public class ELCSSBundleTagBeanInfo extends SimpleBeanInfo {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		List<PropertyDescriptor> proplist = new ArrayList<PropertyDescriptor>();

		try {
			proplist.add(new PropertyDescriptor("src", ELCSSBundleTag.class,
					null, "setSrcExpr"));
		} catch (IntrospectionException ex) {
		}
		try {
			proplist.add(new PropertyDescriptor("useRandomParam",
					ELCSSBundleTag.class, null, "setUseRandomParamExpr"));
		} catch (IntrospectionException ex) {
		}
		try {
			proplist.add(new PropertyDescriptor("media", ELCSSBundleTag.class,
					null, "setMediaExpr"));
		} catch (IntrospectionException ex) {
		}
		try {
			proplist.add(new PropertyDescriptor("title", ELCSSBundleTag.class,
					null, "setTitleExpr"));
		} catch (IntrospectionException ex) {
		}
		try {
			proplist.add(new PropertyDescriptor("alternate", ELCSSBundleTag.class,
					null, "setAlternateExpr"));
		} catch (IntrospectionException ex) {
		}
		try {
			proplist.add(new PropertyDescriptor("displayAlternate", ELCSSBundleTag.class,
					null, "setDisplayAlternateExpr"));
		} catch (IntrospectionException ex) {
		}
		
		PropertyDescriptor[] result = new PropertyDescriptor[proplist.size()];
		return ((PropertyDescriptor[]) proplist.toArray(result));
	}

}
