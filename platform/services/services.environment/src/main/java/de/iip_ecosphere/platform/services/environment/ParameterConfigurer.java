package de.iip_ecosphere.platform.services.environment;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * A parameter configurer for a parameter, including a type translator from JSON, a {@link ValueConfigurer} for
 * setting the value and an {@link Supplier} to obtain the parameter value for failure recovery.
 * 
 * @param <T> the type of the parameter
 * @author Holger Eichelberger, SSE
 */
public class ParameterConfigurer<T> implements ValueConfigurer<T> {

    private String name;
    private TypeTranslator<String, T> translator;
    private ValueConfigurer<T> cfg;
    private Supplier<T> getter;
    private Class<T> cls;
    private String sysProperty;
    
    /**
     * Creates a parameter configurer without getter, i.e., implicitly recovery is disabled.
     * 
     * @param name the name of the parameter
     * @param cls the class representing the type
     * @param translator the type translator
     * @param cfg the parameter value configurer
     */
    public ParameterConfigurer(String name, Class<T> cls, TypeTranslator<String, T> translator, 
        ValueConfigurer<T> cfg) {
        this(name, cls, translator, cfg, null);
    }

    /**
     * Creates a parameter configurer without getter, i.e., implicitly recovery is disabled.
     * 
     * @param name the name of the parameter
     * @param cls the class representing the type
     * @param translator the type translator
     * @param cfg the parameter value configurer
     * @param getter a function returning the actual value of the parameter, used for recovery, may be <b>null</b> 
     *   for disabling recovery on this parameter
     */
    public ParameterConfigurer(String name, Class<T> cls, TypeTranslator<String, T> translator, 
        ValueConfigurer<T> cfg, Supplier<T> getter) {
        this.name = name;
        this.cls = cls;
        this.translator = translator;
        this.cfg = cfg;
        this.getter = getter;
    }

    /**
     * Optional system property that shall be considered during initialization.
     * 
     * @param sysProperty system property name that shall be used via {@code translator} to initialize the parameter, 
     *   takes precedence
     * @return <b>this</b> (builder style)
     */
    public ParameterConfigurer<T> withSystemProperty(String sysProperty) {
        this.sysProperty = sysProperty;
        if (null != cfg) {
            Object value = getValueFromSysProperty(null);
            if (cls.isInstance(value)) {
                try {
                    configure(cls.cast(value));
                    LoggerFactory.getLogger(ParameterConfigurer.class).info("Initialized parameter {} from system "
                        + "property {} with value {}", name, sysProperty, value);
                } catch (ExecutionException e) {
                    LoggerFactory.getLogger(ParameterConfigurer.class).warn("Cannot initialize value from system "
                        + "property {}: {}", sysProperty, e.getMessage());
                }
            }
        }
        return this;
    }
    
    @Override
    public void configure(T value) throws ExecutionException {
        cfg.configure(value);
    }
    
    /**
     * Returns the type translator to be used for this parameter.
     * 
     * @return the type translator
     */
    public TypeTranslator<String, T> getTranslator() {
        return translator;
    }
    
    /**
     * Returns the value getter.
     * 
     * @return the getter, may be <b>null</b>
     */
    public Supplier<T> getGetter() {
        return getter;
    }
    
    /**
     * Returns the name of the parameter.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the type of the parameter.
     * 
     * @return the type
     */
    public Class<T> getType() {
        return cls;
    }

    /**
     * Adds the string representation of {@code value} to {@code values} in terms of the parameter represented by this 
     * configurer. Any problem/error will be logged rather than thrown.
     * 
     * @param values the values to be modified as a side effect
     * @param value the value to be added
     */
    public void addValue(Map<String, String> values, Object value) {
        value = getValueFromSysProperty(value);
        if (cls.isInstance(value)) {
            try {
                values.put(name, translator.from(cls.cast(value)));
            } catch (IOException e) {
                LoggerFactory.getLogger(ParameterConfigurer.class).warn(
                    "Cannot add value for service parameter {}: {}", name, e.getMessage());
            }
        } else {
            LoggerFactory.getLogger(ParameterConfigurer.class).warn("Cannot add value for " 
                + name + " as value is not instance of " + cls.getName());
        }
    }
    
    /**
     * Returns the object value determined from the attached system property.
     * 
     * @param value initial value, may be <b>null</b>
     * @return the value if there is a system property and it can be converted/translated, {@code value} else
     */
    private Object getValueFromSysProperty(Object value) {
        if (sysProperty != null) {
            String propValue = System.getProperty(sysProperty, null);
            if (null != propValue) {
                try {
                    value = translator.to(propValue);
                } catch (IOException e) {
                    LoggerFactory.getLogger(ParameterConfigurer.class).warn("Cannot convert system property {} to "
                        + "value of service parameter {}: {}. Ignoring system property.", 
                        sysProperty, name, e.getMessage());
                }
            }
        }
        return value;
    }
    
}