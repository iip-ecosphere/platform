package de.iip_ecosphere.platform.configuration;

/**
 * Basically, the amount of EASy logging is defined via the Log4J logging configuration. However, we can log
 * more and even more, in particular during startup. These levels refer to the specific startup logging that
 * we have under control here.
 * 
 * @author Holger Eichelberger, SSE
 */
public enum EasyLogLevel {
    NORMAL,
    VERBOSE,
    EXTRA_VERBOSE
}