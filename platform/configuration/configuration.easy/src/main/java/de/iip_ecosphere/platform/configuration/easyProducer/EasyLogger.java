package de.iip_ecosphere.platform.configuration.easyProducer;

import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.uni_hildesheim.sse.easy.loader.framework.Log.LoaderLogger;
import net.ssehub.easy.basics.logger.ILogger;

/**
 * SLF4J-to-EASy logging adapter.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EasyLogger implements ILogger, LoaderLogger {
    
    private Logger logger;
    
    /**
     * Defines the basic logging levels in here. Keep sequence/ordinals according to imporance, lowest first.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum LogLevel {
        TRACE,
        DEBUG,
        INFO, 
        WARN, 
        ERROR,
        OFF
    }
    
    /**
     * Creates a logger instance that logs to the oktoflow logger of this class.
     */
    public EasyLogger() {
        this(LoggerFactory.getLogger(EasyLogger.class));
    }

    /**
     * Creates a logger instance that logs to the given oktoflow logger.
     * 
     * @param logger the logger to use
     */
    public EasyLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String msg, Class<?> clazz, String bundleName) {
        if (allowLogging(msg, clazz, bundleName, LogLevel.INFO)) {
            logger.info("[" + clazz.getName() + "] " + msg);
        }
    }

    @Override
    public void error(String msg, Class<?> clazz, String bundleName) {
        if (allowLogging(msg, clazz, bundleName, LogLevel.ERROR)) {
            logger.error("[" + clazz.getName() + "] " + msg);
        }
    }

    @Override
    public void warn(String msg, Class<?> clazz, String bundleName) {
        if (allowLogging(msg, clazz, bundleName, LogLevel.WARN)) {
            logger.warn("[" + clazz.getName() + "] " + msg);
        }
    }

    @Override
    public void debug(String msg, Class<?> clazz, String bundleName) {
        if (allowLogging(msg, clazz, bundleName, LogLevel.DEBUG)) {
            logger.debug("[" + clazz.getName() + "] " + msg);
        }
    }

    @Override
    public void exception(String msg, Class<?> clazz, String bundleName) {
        if (allowLogging(msg, clazz, bundleName, LogLevel.ERROR)) {
            logger.error("[" + clazz.getName() + "] " + msg);
        }
    }

    @Override
    public void error(String error) {
        logger.error("[Loader] " + error);
    }

    @Override
    public void error(String error, Exception exception) {
        logger.error("[Loader] " + error);
    }

    @Override
    public void warn(String warning) {
        logger.warn("[Loader] " + warning);
    }

    @Override
    public void warn(String warning, Exception exception) {
        logger.warn("[Loader] " + warning);
    }

    @Override
    public void info(String msg) {
        logger.warn("[Loader] " + msg); // warn for now
    }
    
    /**
     * Returns whether logging is allowed.
     * 
     * @param msg the message
     * @param clazz the originating class
     * @param bundleName the originating bundle
     * @param level the logging level
     * @return {@code true} for log the message, {@code false} for consume and be quiet
     */
    protected boolean allowLogging(String msg, Class<?> clazz, String bundleName, LogLevel level) {
        return true;
    }
    
}
