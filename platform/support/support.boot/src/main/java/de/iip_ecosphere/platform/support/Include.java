package de.iip_ecosphere.platform.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to indicate when value of the annotated property is to be serialized.
 * Without annotation property values are always included, but by using
 * this annotation one can specify simple exclusion rules to reduce
 * amount of properties to write out. Similar to {@code JsonInclude} in 
 * Jackson, but simplified/more generic here.
 * 
 * @author Holger Eichelberger, SSE
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Include {
    
    /**
     * Inclusion rule to use for instances (values) of types (Classes) or
     * properties annotated; defaults to {@link Type#ALWAYS}.
     */
    public Type value() default Type.ALWAYS;

    /**
     * Enumeration defining classes of properties
     * of Java Beans are to be included in serialization.
     */
    public enum Type {
        
        /**
         * Value that indicates that property is to be always included,
         * independent of value of the property.
         */
        ALWAYS,

        /**
         * Value that indicates that only properties with non-null
         * values are to be included.
         */
        NON_NULL
        
    }
}
