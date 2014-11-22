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
package net.jawr.web.resource.bundle.iterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * An paths iterator implementation based on a list of path.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class ListPathsIteratorImpl implements ResourceBundlePathsIterator {

	/**
	 * The path iterator 
	 */
	private Iterator<BundlePath> pathIterator;
	
	/**
	 * Constructor
	 * @param paths the list of path
	 */
	public ListPathsIteratorImpl(List<BundlePath> paths) {
		pathIterator = paths.iterator();
	}
	
	/**
	 * Constructor
	 * @param paths the array of path
	 */
	public ListPathsIteratorImpl(BundlePath[] paths) {
		pathIterator = Arrays.asList(paths).iterator();
	}
	
	/**
	 * Constructor
	 * @param path the path
	 */
	public ListPathsIteratorImpl(BundlePath path) {
		this(new BundlePath[]{path});
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator#nextPath()
	 */
	public BundlePath nextPath() {
		return pathIterator.next();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return pathIterator.hasNext();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public BundlePath next() {
		return pathIterator.next();
	}

	/**
	 * Unsupported method from the Iterator interface, will throw UnsupportedOperationException
	 * if called. 
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
