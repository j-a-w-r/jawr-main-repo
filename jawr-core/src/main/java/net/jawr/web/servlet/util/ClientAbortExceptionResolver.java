/**
 * Copyright 2015 Ibrahim Chaehoi
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
package net.jawr.web.servlet.util;

import java.io.IOException;

/**
 * This class is used to determine if an exception is a client Abort Exception or not.
 * The Exception used can be different depending on the application container.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class ClientAbortExceptionResolver {

	/**
	 * Checks if the exception is a client abort exception
	 * @param e
	 * @return true if the exception is a client abort exception
	 */
	public static boolean isClientAbortException(IOException e) {
		
		String exceptionClassName = e.getClass().getName();
		return exceptionClassName.endsWith(".EofException")
				|| exceptionClassName.endsWith(".ClientAbortException");
	}
}
