/**
 * Copyright 2015-2016 Ibrahim Chaehoi
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
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.mappings.FilePathMapping;
import net.jawr.web.resource.bundle.mappings.PathMapping;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * This class defines the resource watcher
 * 
 * @author Ibrahim Chaehoi
 */
public class ResourceWatcher extends Thread {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceWatcher.class);

	/** The watch service */
	private final WatchService watchService;

	/** The bundles handler */
	private ResourceBundlesHandler bundlesHandler;

	/** The bundles handler */
	private ResourceReaderHandler rsReader;

	/** The keys map */
	private final Map<WatchKey, Path> keys;

	/** The flag indicating if the watcher is stopped or not */
	private AtomicBoolean stopWatching = new AtomicBoolean(false);

	/** The map between path and resource bundle */
	private final Map<Path, List<PathMapping>> pathToResourceBundle = new ConcurrentHashMap<>();

	/** The Jawr watch event processor */
	private final JawrWatchEventProcessor jawrEvtProcessor;

	/** The watch events */
	private BlockingQueue<JawrWatchEvent> watchEvents = new LinkedBlockingQueue<>();

	/**
	 * Constructor
	 * 
	 * @param bundlesHandler
	 *            the bundles handler
	 * @param rsReader
	 *            the resource reader handler
	 */
	public ResourceWatcher(ResourceBundlesHandler bundlesHandler, ResourceReaderHandler rsReader) {
		super(bundlesHandler.getResourceType() + " JawrResourceWatcher");
		this.bundlesHandler = bundlesHandler;
		this.rsReader = rsReader;
		this.keys = new HashMap<>();

		try {
			this.watchService = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			throw new BundlingProcessException(e);
		}

		jawrEvtProcessor = new JawrWatchEventProcessor(this, watchEvents);
		jawrEvtProcessor.start();

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				close();
			}
		}));
	}

	/**
	 * Returns the ResourceBundlesHandler
	 * 
	 * @return the ResourceBundlesHandler
	 */
	public ResourceBundlesHandler getBundlesHandler() {
		return bundlesHandler;
	}

	/**
	 * Returns the map which links java.nio.file.Path to a list of PathMapping
	 * 
	 * @return the map which links java.nio.file.Path to a list of PathMapping
	 */
	public Map<Path, List<PathMapping>> getPathToResourceBundle() {
		return pathToResourceBundle;
	}

	/**
	 * Sets the flag indicating if we must stop the resource watching
	 */
	public void stopWatching() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Stopping resource watching");
		}

		this.stopWatching.set(true);
		jawrEvtProcessor.stopProcessing();
		jawrEvtProcessor.interrupt();
	}

	/**
	 * Initialize the map which links path to asset bundle
	 * 
	 * @param bundles
	 *            the list of bundles
	 * @throws IOException
	 *             if an {@link IOException} occurs
	 */
	public synchronized void initPathToResourceBundleMap(List<JoinableResourceBundle> bundles) throws IOException {

		for (JoinableResourceBundle bundle : bundles) {

			// Remove bundle reference from existing mapping if exists
			removePathMappingFromPathMap(bundle);

			List<PathMapping> mappings = bundle.getMappings();
			for (PathMapping pathMapping : mappings) {
				register(pathMapping);
			}

			// Register file path mapping for linked resources
			List<FilePathMapping> fMappings = bundle.getLinkedFilePathMappings();
			for (FilePathMapping fMapping : fMappings) {
				register(fMapping);
			}
		}
	}

	/**
	 * Register a path mapping
	 * 
	 * @param pathMapping
	 *            the path mapping to register
	 * @throws IOException
	 *             if an IOException occurs
	 */
	private void register(PathMapping pathMapping) throws IOException {

		GeneratorRegistry generatorRegistry = bundlesHandler.getConfig().getGeneratorRegistry();
		List<PathMapping> mappings = new ArrayList<>();
		if (generatorRegistry.isPathGenerated(pathMapping.getPath())) {
			List<PathMapping> genPathMappings = generatorRegistry.getGeneratedPathMappings(pathMapping.getBundle(),
					pathMapping.getPath(), rsReader);
			if (genPathMappings != null) {
				mappings.addAll(genPathMappings);
			} else {
				mappings.add(pathMapping);
			}
		} else {
			mappings.add(pathMapping);
		}

		for (PathMapping pMapping : mappings) {
			String filePath = rsReader.getFilePath(pMapping.getPath());
			registerPathMapping(pMapping, filePath);
		}
	}

	/**
	 * Register a path mapping
	 * 
	 * @param pathMapping
	 *            the path mapping to register
	 * @throws IOException
	 *             if an IOException occurs
	 */
	private void register(FilePathMapping pathMapping) throws IOException {
		registerPathMapping(pathMapping, pathMapping.getPath());
	}

	/**
	 * Register the path mapping
	 * 
	 * @param pathMapping
	 *            the path mapping
	 * @param filePath
	 *            the file path
	 * @throws IOException
	 *             if an IOException occurs
	 */
	private void registerPathMapping(PathMapping pathMapping, String filePath) throws IOException {
		if (filePath != null) {

			Path p = Paths.get(filePath);
			boolean isDir = Files.isDirectory(p);
			if (!isDir) {
				p = p.getParent();
			}

			if (pathMapping.isRecursive()) {
				registerAll(p, Arrays.asList(pathMapping));
			} else {
				register(p, Arrays.asList(pathMapping));
			}
		}
	}

	/**
	 * Removes the path mapping of the bundle given in parameter from map which
	 * links Path to resource bundle
	 * 
	 * @param bundle
	 *            the bundle whose the path mapping should be removed
	 */
	private void removePathMappingFromPathMap(JoinableResourceBundle bundle) {
		for (List<PathMapping> pathMappings : pathToResourceBundle.values()) {
			for (Iterator<PathMapping> iterator = pathMappings.iterator(); iterator.hasNext();) {
				PathMapping pathMapping = (PathMapping) iterator.next();
				if (pathMapping.getBundle().getName().equals(bundle.getName())) {
					iterator.remove();
				}
			}
		}
	}

	/**
	 * Register the given directory with the WatchService
	 * 
	 * @param dir
	 *            the directory to register
	 * @param pathMapping,
	 *            the list of path mapping associated
	 */
	private void register(Path dir, List<PathMapping> pathMapping) throws IOException {

		WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		keys.put(key, dir);

		List<PathMapping> m = pathToResourceBundle.get(dir);
		if (m == null) {
			m = new ArrayList<>();
			pathToResourceBundle.put(dir, m);
		}
		m.addAll(pathMapping);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 * 
	 * @param pathMapping
	 */
	void registerAll(final Path start, final List<PathMapping> pathMappings) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				register(dir, pathMappings);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Close the watch service
	 */
	private void close() {
		try {
			watchService.close();
		} catch (IOException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (!stopWatching.get()) {
			WatchKey key;
			try {
				key = watchService.take();
			} catch (InterruptedException | ClosedWatchServiceException e) {
				close();
				return;
			}
			if (key != null) {
				Path dir = keys.get(key);
				if (dir == null) {
					LOGGER.warn("WatchKey not recognized!!");
					continue;
				}

				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();

					if (kind == OVERFLOW) {
						continue;
					}

					// Context for directory entry event is the file name of
					// entry
					Path path = (Path) event.context();
					Path resolvedPath = ((Path) key.watchable()).resolve(path);

					boolean isDir = Files.isDirectory(resolvedPath, NOFOLLOW_LINKS);
					Path dirPath = null;
					if (isDir) {
						dirPath = ((Path) key.watchable());
					} else {
						dirPath = resolvedPath.getParent();
					}

					JawrWatchEvent evt = new JawrWatchEvent(kind, resolvedPath, dirPath);
					watchEvents.add(evt);
				}

				// reset key and remove from set if directory no longer
				// accessible
				boolean valid = key.reset();
				if (!valid) {
					keys.remove(key);

					// all directories are inaccessible
					if (keys.isEmpty()) {
						break;
					}
				}
			}
		}
		close();
	}

	/**
	 * Returns true if there is no more event to process
	 * 
	 * @return true if there is no more event to process
	 */
	public boolean hasNoEventToProcess() {
		return jawrEvtProcessor.hasNoEventToProcess();
	}
}
