/*
 * ConsolePostProcessor.java
 *
 * Created on June 28, 2008, 5:15 PM
 *
 * Copyright (c) 2008 FooBrew, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.j2free.jawr;

import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;

/**
 * @author Ryan Wilson (http://blog.augmentedfragments.com)
 */
public class ConsolePostProcessor implements ResourceBundlePostProcessor {
    
    private static final String CONSOLE_LOG_REGEX = "window\\.console\\.log\\([^)]*\\);";
    
    public ConsolePostProcessor() {
    }
    
    public StringBuffer postProcessBundle(BundleProcessingStatus status,StringBuffer bundleString) {
        StringBuffer ret = new StringBuffer();
        ret.append(removeLogStatements(bundleString.toString()));
        return ret;
    }
    
    public String removeLogStatements(String string) {
        return string.replaceAll(CONSOLE_LOG_REGEX,"");
    }
}
