package de.oktoflow.platform.support.json.jackson;

import java.util.Set;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import de.iip_ecosphere.platform.support.ConfiguredName;
import de.iip_ecosphere.platform.support.Filter;
import de.iip_ecosphere.platform.support.Ignore;
import de.iip_ecosphere.platform.support.IgnoreProperties;

/**
 * Basic annotation introspector for abstracting oktoflow data annotations, in particular {@link ConfiguredName} 
 * and {@link Ignore}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class OktoAnnotationIntrospector extends JacksonAnnotationIntrospector {

    private static final long serialVersionUID = -1021095562978855964L;
    private Set<String> exclusions;
    private Set<Object> ignore;
    
    /**
     * Sets the member exclusions.
     * 
     * @param exclusions the exclusion member names
     */
    public void setExclusions(Set<String> exclusions) {
        this.exclusions = exclusions;
    }

    /**
     * Sets the ignores.
     * 
     * @param ignore the objects to be ignored
     */
    public void setIgnore(Set<Object> ignore) {
        this.ignore = ignore;
    }

    @Override
    public boolean hasIgnoreMarker(AnnotatedMember member) {
        Ignore ignoreAnn = member.getAnnotation(Ignore.class);
        if (null != ignoreAnn) {
            return ignoreAnn.value();
        }
        if (exclusions != null) {
            boolean exclude = exclusions.contains(member.getName());
            if (!exclude) {
                ConfiguredName cfgName = member.getAnnotation(ConfiguredName.class);
                if (null != cfgName && cfgName.value() != null) {
                    exclude = exclusions.contains(cfgName.value());
                }
            }
            return exclude;
        }
        if (null != ignore) {
            return ignore.contains(member.getType().getRawClass()) || ignore.contains(member.getMember());
        }
        return super.hasIgnoreMarker(member);
    }
    
    @Override
    public PropertyName findNameForDeserialization(Annotated member) {
        ConfiguredName cfgName = member.getAnnotation(ConfiguredName.class);
        if (cfgName != null) {
            return PropertyName.construct(cfgName.value());
        } else {                
            return super.findNameForDeserialization(member);
        }
    }

    @Override
    public PropertyName findNameForSerialization(Annotated member) {
        ConfiguredName cfgName = member.getAnnotation(ConfiguredName.class);
        if (cfgName != null) {
            return PropertyName.construct(cfgName.value());
        } else {                
            return super.findNameForSerialization(member);
        }
    }
    
    @Override
    public JsonIgnoreProperties.Value findPropertyIgnorals(Annotated member) {
        IgnoreProperties ignoreProp = member.getAnnotation(IgnoreProperties.class);
        if (ignoreProp == null) {
            return JsonIgnoreProperties.Value.empty();
        }
        return super.findPropertyIgnorals(member);
    }
    
    @Override
    public Object findFilterId(Annotated member) {
        Filter filter = member.getAnnotation(Filter.class);
        if (filter != null) {
            String id = filter.value();
            // Empty String is same as not having annotation, to allow overrides
            if (id.length() > 0) {
                return id;
            }
        }
        return super.findFilterId(member);
    }
    
    /**
     * Sets the annotation introspector as specified.
     * 
     * @param introspector the actual introspector, may be <b>null</b>
     * @param setter the consumer that actually sets the introspector
     * @param configurer the configurer function, may be <b>null</b> for none
     * @return the actual introspector (new if <b>null</b> before or reconfigured)
     */
    public static OktoAnnotationIntrospector set(OktoAnnotationIntrospector introspector, 
        Consumer<OktoAnnotationIntrospector> setter, Consumer<OktoAnnotationIntrospector> configurer) {
        if (null == introspector) {
            introspector = new OktoAnnotationIntrospector();
            setter.accept(introspector);
        }
        if (null != configurer) {
            configurer.accept(introspector);
        }
        return introspector;
    }
    
}