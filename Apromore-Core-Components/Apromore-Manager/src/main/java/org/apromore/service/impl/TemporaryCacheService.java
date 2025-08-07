/*-
 * #%L
 * This file is part of "Apromore Core".
 * %%
 * Copyright (C) 2018 - 2022 Apromore Pty Ltd.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package org.apromore.service.impl;

import org.apromore.apmlog.APMLog;
import org.apromore.apmlog.APMLogService;
import org.apromore.cache.ehcache.CacheRepository;
import org.apromore.commons.config.ConfigBean;
import org.apromore.dao.jpa.LogRepositoryCustomImpl;
import org.apromore.dao.model.Log;
import org.apromore.dao.model.Storage;
import org.apromore.storage.StorageClient;
import org.apromore.storage.StorageType;
import org.apromore.storage.factory.StorageManagementFactory;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.in.XMxmlGZIPParser;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XParserRegistry;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XMxmlGZIPSerializer;
import org.deckfour.xes.out.XMxmlSerializer;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

public class TemporaryCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogRepositoryCustomImpl.class);

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    @Resource
    private CacheRepository cacheRepo;

    @Resource
    private APMLogService apmLogService;

    @Resource
    private ConfigBean config;

    @Resource
    private StorageManagementFactory<StorageClient> storageFactory;

    @Value("${storage.logPrefix}")
    private String logPrefix;

    public CacheRepository getCacheRepo() {
	return cacheRepo;
    }

    public void setCacheRepo(CacheRepository cacheRepo) {
	this.cacheRepo = cacheRepo;
    }

    public APMLogService getApmLogService() {
	return apmLogService;
    }

    public void setApmLogService(APMLogService apmLogService) {
	this.apmLogService = apmLogService;
    }

    public ConfigBean getConfig() {
	return config;
    }

    public void setConfig(ConfigBean config) {
	this.config = config;
    }

    public StorageManagementFactory<StorageClient> getStorageFactory() {
	return storageFactory;
    }

    public void setStorageFactory(StorageManagementFactory<StorageClient> storageFactory) {
	this.storageFactory = storageFactory;
    }

    private static final String APMLOG_CACHE_KEY_SUFFIX = "APMLog";

    public Storage storeProcessLog(final Integer folderId, final String logName, XLog log, final Integer userID,
            final String domain, final String created) {

	Storage storage = new Storage();
	LOGGER.debug("Storing Log {} {}", log.size(), logName);
	if (log != null && logName != null) {
	    String logNameId = simpleDateFormat.format(new Date());

	    try {
		final String name = logNameId + "_" + logName + ".xes.gz";
		exportToStorage(name, log);
		storage.setKey(name);
		storage.setPrefix(logPrefix);
		storage.setStoragePath(config.getStoragePath());

		LOGGER.debug("Memory Used: {} MB", getMemoryUsage().getUsed() / 1024 / 1024);

		if (shouldCache(log)) {
		    // Store corresponding object into cache
		    cacheRepo.put(name, log);
		    LOGGER.debug("Put XLog [hash: {}] into Cache [{}] using Key [{}].", log.hashCode(),
		            cacheRepo.getCacheName(), logNameId);

		    APMLog apmLog = apmLogService.findAPMLogForXLog(log);
		    if (apmLog != null) {
			cacheRepo.put(name + APMLOG_CACHE_KEY_SUFFIX, apmLog);
			LOGGER.debug("Put APMLog [hash: {}] into Cache [{}] using Key [{}].", log.hashCode(),
			        cacheRepo.getCacheName(), logNameId + APMLOG_CACHE_KEY_SUFFIX);
		    } else {
			LOGGER.debug("The APMLog of XLog [hash: {}] is null, skip caching.", log.hashCode());
		    }

		    LOGGER.debug("Memory Used: {} MB", getMemoryUsage().getUsed() / 1024 / 1024);
		    LOGGER.debug("Memory Available: {} MB",
		            (getMemoryUsage().getMax() - getMemoryUsage().getUsed()) / 1024 / 1024);
		    LOGGER.debug("The number of elements in the memory store = {}", cacheRepo.getMemoryStoreSize());
		} else {
		    LOGGER.debug("The total number of events in this log exceed cache threshold");
		}

		return storage;
	    } catch (Exception e) {
		LOGGER.error("Unable to store process log", e);
	    }

	}
	return null;
    }

    public void deleteProcessLog(Log log) {
	if (log != null) {
	    try {
		String storagePath = "FILE" + StorageType.STORAGE_PATH_SEPARATOR + config.getLogsDir();
		String prefix = null;
		String key = log.getFilePath() + "_" + log.getName() + ".xes.gz";

		if (log.getStorage() != null) {
		    storagePath = log.getStorage().getStoragePath();
		    prefix = log.getStorage().getPrefix();
		    key = log.getStorage().getKey();
		}
		StorageClient storageClient = storageFactory.getStorageClient(storagePath);
		String name = key;

		storageClient.delete(prefix, name);
		// Remove corresponding object from cache
		cacheRepo.evict(key);
		cacheRepo.evict(key + APMLOG_CACHE_KEY_SUFFIX);
		System.gc(); // Force GC after cache eviction
		LOGGER.debug("Delete XLog [ KEY: {}] from cache [{}]", key, cacheRepo.getCacheName());
		LOGGER.debug("The number of elements in the memory store = {}", cacheRepo.getMemoryStoreSize());

	    } catch (Exception e) {
		LOGGER.error("Unable to delete process log", e);
	    }
	}
    }

    /**
     * Load XES log file from cache, if not found then load from Event Logs
     * Repository
     *
     * @param log
     * @return
     */
    public XLog getProcessLog(Log log, String factoryName) {

	if (log != null) {

	    // ******* profiling code start here ********
	    long startTime = System.nanoTime();
	    long elapsedNanos;
	    // ******* profiling code end here ********

	    String key = log.getFilePath() + "_" + log.getName() + ".xes.gz";
	    String prefix = null;
	    String storagePath = "FILE" + StorageType.STORAGE_PATH_SEPARATOR + config.getLogsDir();

	    if (log.getStorage() != null) { 
            key = log.getStorage().getKey();
            prefix = log.getStorage().getPrefix();
            storagePath = log.getStorage().getStoragePath();
            LOGGER.debug("Read log file {} at storage path {} on environment prefix {}", key, storagePath, prefix);
	    }

	    XLog element = (XLog) cacheRepo.get(key);

	    if (element == null) {
		// If doesn't hit cache
		LOGGER.debug("Cache for [KEY: {}] is null.", key);

		try {
		    StorageClient storageClient = storageFactory.getStorageClient(storagePath);

		    XFactory factory = getXFactory(factoryName);
		    InputStream inputStream = storageClient.getInputStream(prefix, key);
		    XLog xlog = importFromFile(factory, inputStream, key);

		    inputStream.close();
		    // ******* profiling code start here ********
		    elapsedNanos = System.nanoTime() - startTime;
		    LOGGER.debug("Retrieved XES log {} [{}]. Elapsed time: {} ms", key, xlog.hashCode(),
		            elapsedNanos / 1000000);
		    LOGGER.debug("Memory Used: {} MB", getMemoryUsage().getUsed() / 1024 / 1024);
		    LOGGER.debug("Memory Available: {} MB",
		            (getMemoryUsage().getMax() - getMemoryUsage().getUsed()) / 1024 / 1024);
		    startTime = System.nanoTime();
		    // ******* profiling code end here ********

		    if (shouldCache(xlog)) {
			cacheRepo.put(key, xlog);
			elapsedNanos = System.nanoTime() - startTime;
			LOGGER.debug("Cache XLog [KEY:{}" + key + "]. " + "Elapsed time: {} ms.", key,
			        elapsedNanos / 1000000);

			startTime = System.nanoTime();
			APMLog apmLog = apmLogService.findAPMLogForXLog(xlog);
			if (apmLog != null) {
			    cacheRepo.put(key + APMLOG_CACHE_KEY_SUFFIX, apmLog);
			    elapsedNanos = System.nanoTime() - startTime;
			    LOGGER.debug("Construct and cache APMLog [KEY:{}]. Elapsed time: {} ms.",
			            key + APMLOG_CACHE_KEY_SUFFIX, elapsedNanos / 1000000);
			} else {
			    LOGGER.debug("The APMLog of XLog [hash: {}] is null, skip caching.", xlog.hashCode());
			}

			LOGGER.debug("Memory Used: {} MB", getMemoryUsage().getUsed() / 1024 / 1024);
			LOGGER.debug("Memory Available: {} MB",
			        (getMemoryUsage().getMax() - getMemoryUsage().getUsed()) / 1024 / 1024);
			LOGGER.debug("The number of elements in the memory store = {}", cacheRepo.getMemoryStoreSize());
		    } else {
			LOGGER.debug("The total number of events in this log exceed cache threshold");
		    }

		    return xlog;
		} catch (Exception e) {
			LOGGER.error("Read log file {} at storage path {} on environment prefix {}", key, storagePath, prefix);
		    LOGGER.error("Unable to get process log", e);
		}

	    } else {
		// If cache hit
		LOGGER.debug("Get XLog [HASH:{} KEY:{}] from cache [{}]", element.hashCode(), key,
		        cacheRepo.getCacheName());
		LOGGER.debug("Memory Used: {} MB", getMemoryUsage().getUsed() / 1024 / 1024);
		return element;
	    }
	}
	return null;
    }

    protected MemoryUsage getMemoryUsage() {
	MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
	return memoryMXBean.getHeapMemoryUsage();
    }

    /**
     * Load aggregated log
     *
     * @param log
     * @return
     */
    public APMLog getAggregatedLog(Log log) {
	if (log != null) {

	    // ******* profiling code start here ********
	    long startTime = System.nanoTime();
	    long elapsedNanos;
	    // ******* profiling code end here ********

	    String key = log.getFilePath() + APMLOG_CACHE_KEY_SUFFIX;
	    if (log.getStorage() != null) {
		key = log.getStorage().getKey() + APMLOG_CACHE_KEY_SUFFIX;
	    }

	    APMLog element = (APMLog) cacheRepo.get(key);

	    if (element == null) {
		// If doesn't hit cache
		LOGGER.debug("Cache for [KEY: {}] is null.", key);

		try {
		    XLog xLog = getProcessLog(log, null);
		    APMLog apmLog = apmLogService.findAPMLogForXLog(xLog);

		    if (shouldCache(xLog)) {
			cacheRepo.put(key, apmLog);
			elapsedNanos = System.nanoTime() - startTime;
			LOGGER.debug("Put APMLog [KEY:{}] into Cache. Elapsed time: {} ms.", key,
			        elapsedNanos / 1000000);
			LOGGER.debug("The number of elements in the memory store = {}", cacheRepo.getMemoryStoreSize());
		    }

		    return apmLog;

		} catch (Exception e) {
		    LOGGER.error("Unable to get aggregate log", e);
		}

	    } else {
		// If cache hit
		LOGGER.debug("Get APMLog [HASH:{} KEY:{}] from cache [{}]", element.hashCode(), key,
		        cacheRepo.getCacheName());
		return element;
	    }
	}
	return null;
    }

    private boolean shouldCache(XLog xLog) {

	/**
	 * The total number of events in this log.
	 */
	int numberOfEvents = 0;
	/**
	 * The number of traces in this log.
	 */
	int numberOfTraces = 0;

	int numOfEventsLimit = 0;
	int numOfTracesLimit = 0;

	for (XTrace trace : xLog) {
	    numberOfTraces++;
	    for (XEvent event : trace) {
		numberOfEvents++;
	    }
	}

	try {
	    numOfEventsLimit = Integer.parseInt(config.getCache().getNumOfEvent().replaceAll(",", ""));
	    numOfTracesLimit = Integer.parseInt(config.getCache().getNumOfTrace().replaceAll(",", ""));

	} catch (NumberFormatException e) {
	    LOGGER.error("Cache threshold value is wrong, please check the setting in config file", e);
	}

	if ((numOfEventsLimit != 0 && numberOfEvents > numOfEventsLimit)
	        || (numOfTracesLimit != 0 && numberOfTraces > numOfTracesLimit)) {
	    return false;
	}

	return true;
    }

    private XFactory getXFactory(String factoryName) {

	if (factoryName != null) {
	    // Look for a registered XFactory with the specified name
	    for (XFactory factory : XFactoryRegistry.instance().getAvailable()) {
		if (Objects.equals(factory.getName(), factoryName)) {
		    return factory;
		}
	    }
	}

	// If the named factory couldn't be found, fall back to the default
	return XFactoryRegistry.instance().currentDefault();
    }

    /* ************************** Util Methods ******************************* */

    public XLog importFromFile(XFactory factory, InputStream inputStream, String location) throws Exception {
	if (location.endsWith("mxml.gz")) {
	    return importFromInputStream(inputStream, new XMxmlGZIPParser(factory));
	} else if (location.endsWith("mxml")) {
	    return importFromInputStream(inputStream, new XMxmlParser(factory));
	} else if (location.endsWith("xes.gz")) {
	    return importFromInputStream(inputStream, new XesXmlGZIPParser(factory));
	} else if (location.endsWith("xes")) {
	    return importFromInputStream(inputStream, new XesXmlParser(factory));
	}
	return null;
    }

    public void exportToStorage(String name, XLog log) throws Exception {
	if (name.endsWith("mxml.gz")) {
	    exportToInputStream(log, name, new XMxmlGZIPSerializer());
	} else if (name.endsWith("mxml")) {
	    exportToInputStream(log, name, new XMxmlSerializer());
	} else if (name.endsWith("xes.gz")) {
	    exportToInputStream(log, name, new XesXmlGZIPSerializer());
	} else if (name.endsWith("xes")) {
	    exportToInputStream(log, name, new XesXmlSerializer());
	}
    }

    public XLog importFromInputStream(InputStream inputStream, XParser parser) throws Exception {
	Collection<XLog> logs;
	try {
	    logs = parser.parse(inputStream);
	} catch (Exception e) {
	    LOGGER.error("Unable to parse logs from stream", e);
	    logs = null;
	}
	if (logs == null) {
	    // try any other parser
	    for (XParser p : XParserRegistry.instance().getAvailable()) {
		if (p == parser) {
		    continue;
		}
		try {
		    logs = p.parse(inputStream);
		    if (logs.size() > 0) {
			break;
		    }
		} catch (Exception e1) {
		    // ignore and move on.
		    logs = null;
		}
	    }
	}

	// log sanity checks;
	// notify user if the log is awkward / does miss crucial information
	if (logs == null || logs.size() == 0) {
	    throw new Exception("No logs contained in log!");
	}

	XLog log = logs.iterator().next();
	if (XConceptExtension.instance().extractName(log) == null) {
	    XConceptExtension.instance().assignName(log, "Anonymous log imported from ");
	}

	if (log.isEmpty()) {
	    throw new Exception("No process instances contained in log!");
	}

	return log;
    }

    public void exportToInputStream(XLog log, String name, XSerializer serializer) {

		try {
			OutputStream outputStream =
					storageFactory.getStorageClient(config.getStoragePath()).getOutputStream(logPrefix, name);
			serializer.serialize(log, outputStream);
		} catch (Exception e) {
			LOGGER.error("Unable to export log {} to input stream", name, e);
		}
	}

}
