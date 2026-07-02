package de.iip_ecosphere.platform.configuration.easyProducer;

import java.util.function.Consumer;

import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.uni_hildesheim.sse.easy.loader.framework.Log.LoaderLogger;
import net.ssehub.easy.basics.logger.ILogger;
import net.ssehub.easy.varModel.confModel.AssignmentResolver;

/**
 * EASy-to-oktoflow logging adapter.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EasyLogger implements ILogger, LoaderLogger {
    
    private static final LogConsumer NO_LOG_CONSUMER = (l, m) -> { };
    private Logger logger;
    private LogConsumer consumer = NO_LOG_CONSUMER;
    
    /**
     * Optional piggyback log consumer.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface LogConsumer {
        
        /**
         * Performs logging.
         * 
         * @param level the logging level
         * @param msg the logging message
         */
        public void log(LogLevel level, String msg);
        
    }
    
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
        OFF,
        TEXT
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
        log(msg, clazz, bundleName, LogLevel.INFO, m -> logger.info(m));
    }

    @Override
    public void error(String msg, Class<?> clazz, String bundleName) {
        log(msg, clazz, bundleName, LogLevel.ERROR, m -> logger.error(m));
    }

    @Override
    public void warn(String msg, Class<?> clazz, String bundleName) {
        log(msg, clazz, bundleName, LogLevel.WARN, m -> logger.warn(m));
    }

    @Override
    public void debug(String msg, Class<?> clazz, String bundleName) {
        log(msg, clazz, bundleName, LogLevel.DEBUG, m -> logger.debug(m));
    }

    @Override
    public void exception(String msg, Class<?> clazz, String bundleName) {
        log(msg, clazz, bundleName, LogLevel.ERROR, m -> logger.error(m));
    }

    /**
     * Sets the log consumer.
     * 
     * @param consumer the consumer, may be <b>null</b> leading to no consumer
     */
    public void setLogConsumer(LogConsumer consumer) {
        if (null == consumer) {
            consumer = NO_LOG_CONSUMER;
        }
        this.consumer = consumer;
    }
    
    /**
     * Performs the logging.
     * 
     * @param msg the message
     * @param clazz the originating class
     * @param bundleName the originating bundle name
     * @param level the logging level
     * @param logConsumer the piggiback log consumer
     */
    private void log(String msg, Class<?> clazz, String bundleName, LogLevel level, Consumer<String> logConsumer) {
        if (allowLogging(msg, clazz, bundleName, level)) {
            String tmp = "[" + clazz.getName() + "] " + msg;
            logConsumer.accept(tmp);
            consumer.log(level, tmp);
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
        boolean emit = true;
        if (emit && level.ordinal() < LogLevel.WARN.ordinal()) { // emit warn/error anyway
            emit = !ConfigurationLifecycleDescriptor.isEasyLoggingDisabled(clazz.getName());
        }
        if (emit && clazz == AssignmentResolver.class) {
            emit = false;
        }
        return emit;
    }
    
}
