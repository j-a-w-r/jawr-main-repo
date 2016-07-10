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

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jawr.web.resource.FileNameUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.mappings.PathMapping;

/**
 * The Jawr watch event processor
 * 
 * @author Ibrahim Chaehoi
 */
public class JawrWatchEventProcessor extends Thread {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(JawrWatchEventProcessor.class);

	/** The flag indicating if the watch event processor is stopped or not */
	private final AtomicBoolean stopProcessing = new AtomicBoolean(false);

	/** The resource watcher */
	private final ResourceWatcher watcher;

	/** The resource bundle handler */
	private final ResourceBundlesHandler bundlesHandler;

	/** The watch events queue */
	private final BlockingQueue<JawrWatchEvent> watchEvents;

	/** The last process time */
	private final AtomicLong lastProcessTime = new AtomicLong();

	/**
	 * Constructor
	 * 
	 * @param watcher
	 *            the resource watcher
	 * @param watchEvents
	 *            The watch event queue
	 */
	public JawrWatchEventProcessor(ResourceWatcher watcher, BlockingQueue<JawrWatchEvent> watchEvents) {
		super(watcher.getBundlesHandler().getResourceType() + " JawrWatchEventProcessor ");
		this.watcher = watcher;
		this.bundlesHandler = watcher.getBundlesHandler();
		this.watchEvents = watchEvents;
	}

	/**
	 * Sets the stop processing to true
	 */
	public void stopProcessing() {
		this.stopProcessing.set(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {

		while (!stopProcessing.get()) {

			try {
				JawrWatchEvent evt = watchEvents.take();
				AtomicBoolean processingBundle = bundlesHandler.isProcessingBundle();
				synchronized (processingBundle) {

					// Wait until processing ends
					while (processingBundle.get() && !stopProcessing.get()) {
						try {
							processingBundle.wait();
						} catch (InterruptedException e) {
							LOGGER.debug("Thread interrupted");
						}
					}
				}
				if (evt != null && !stopProcessing.get()) {
					process(evt);
				}
			} catch (InterruptedException e) {
				LOGGER.debug("Thread interrupted");
			}
		}
	}

	/**
	 * Process the event
	 * 
	 * @param evt
	 *            the jawr watch event
	 */
	public void process(JawrWatchEvent evt) {

		Path resolvedPath = evt.getResolvedPath();
		List<PathMapping> mappings = watcher.getPathToResourceBundle().get(evt.getDirPath());
		if (mappings != null) {

			boolean isDir = Files.isDirectory(resolvedPath, NOFOLLOW_LINKS);
			List<JoinableResourceBundle> bundles = new ArrayList<>();
			List<PathMapping> recursivePathMappings = new ArrayList<>();
			for (PathMapping mapping : mappings) {

				String filePath = resolvedPath.toFile().getAbsolutePath();
				if (mapping.isAsset()) {
					String fileName = FileNameUtils.getName(filePath);
					if (fileName.equals(FileNameUtils.getName(mapping.getPath()))) {
						bundles.add(mapping.getBundle());
					}
				} else {
					if (isDir) {
						if (mapping.isRecursive() && (!mapping.hasFileFilter() || mapping.accept(filePath))) {
							bundles.add(mapping.getBundle());
						}
					} else if (!mapping.hasFileFilter() || mapping.accept(filePath)) {
						bundles.add(mapping.getBundle());
					}
					if (mapping.isRecursive()) {
						recursivePathMappings.add(mapping);
					}
				}
			}

			if (!bundles.isEmpty()) {
				bundlesHandler.notifyModification(bundles);
			}

			if (!recursivePathMappings.isEmpty()) {

				// if directory is created, and watching
				// recursively,
				// then
				// register it and its sub-directories
				if (evt.getKind() == ENTRY_CREATE && isDir) {
					try {
						watcher.registerAll(resolvedPath, recursivePathMappings);
					} catch (IOException e) {
						if (LOGGER.isWarnEnabled()) {
							LOGGER.warn(e.getMessage());
						}
					}
				}
			}
		}

		lastProcessTime.set(Calendar.getInstance().getTimeInMillis());
	}

	/**
	 * Returns true if there is no more event to process
	 * 
	 * @return true if there is no more event to process
	 */
	public boolean hasNoEventToProcess() {

		long currentTime = Calendar.getInstance().getTimeInMillis();
		return watchEvents.isEmpty() && (currentTime - lastProcessTime.get() > bundlesHandler.getConfig()
				.getSmartBundlingDelayAfterLastEvent());
	}
}
