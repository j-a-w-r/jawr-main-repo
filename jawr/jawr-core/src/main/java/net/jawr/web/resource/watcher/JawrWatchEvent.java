/**
 * Copyright 2016 Ibrahim Chaehoi
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
package net.jawr.web.resource.watcher;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * This class defines a Jawr Watch event, an event which occurs on a watched
 * resource
 * 
 * @author Ibrahim Chaehoi
 */
public class JawrWatchEvent {

	private final WatchEvent.Kind<?> kind;

	private final Path resolvedPath;

	private final Path dirPath;

	/**
	 * Constructor
	 * 
	 * @param kind
	 *            the type of watch event
	 * @param resolvedPath
	 *            the path on which the event happens
	 * @param dirPath
	 *            the directory path
	 */
	public JawrWatchEvent(WatchEvent.Kind<?> kind, Path resolvedPath, Path dirPath) {
		this.kind = kind;
		this.resolvedPath = resolvedPath;
		this.dirPath = dirPath;
	}

	/**
	 * @return the kind
	 */
	public WatchEvent.Kind<?> getKind() {
		return kind;
	}

	/**
	 * @return the resolvedPath
	 */
	public Path getResolvedPath() {
		return resolvedPath;
	}

	/**
	 * @return the dirPath
	 */
	public Path getDirPath() {
		return dirPath;
	}

}
