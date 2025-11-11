package test.de.oktoflow.platform.support.bytecode.bytebuddy;

import org.junit.Test;

import de.iip_ecosphere.platform.support.ConfiguredName;
import de.iip_ecosphere.platform.support.IgnoreProperties;
import de.iip_ecosphere.platform.support.bytecode.Bytecode;
import de.iip_ecosphere.platform.support.bytecode.Bytecode.ClassBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Assert;

/**
 * Tests {@link BytecodeTest}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BytecodeTest {

    /**
     * A test data class in oktoflow style.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Data {
        
        private int intField;

        /**
         * Returns the int field value.
         * 
         * @return the int field value
         */
        public int getIntField() {
            return intField;
        }

        /**
         * Changes the int field value.
         * 
         * @param intField the new int field value
         */
        public void setIntField(int intField) {
            this.intField = intField;
        }
        
    }
    
    /**
     * An annotation for testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    public @interface MyAnnotation {

        /**
         * Some value.
         */
        public String value() default "";
        
    }
    
    /**
    * Interface to represent all potential inputs to the service and the JSON input format.
    * Defines the meta attributes (thus $ prefixes), needs to be refined with actual attributes 
    * by using class. Can be used for dynamic class proxying.
    *
    * @author Holger Eichelberger, SSE
    */
    public interface BaseDataUnitFunctions {

        // checkstyle: stop names check

        /**
         * Returns the delay period between this and the next data unit.
         *
         * @return the period in ms, use default/last value if zero or negative
         */
        public int get$period();

        /**
         * Returns the number of repeats of this data unit.
         *
         * @return the number of repeats, negative for infinite
         */
        public int get$repeats();

        /**
        * Changes the delay period between this and the next data unit. [snakeyaml]
        *
        * @param $period the period in ms, default/last value if zero or negative
        */
        public void set$period(int $period);
        
        /**
         * Changes the number of repeats of this data unit. [snakeyaml]
         *
         * @param $repeats the number of repeats, negative for infinite
         */
        public void set$repeats(int $repeats);

        // checkstyle: resume names check

    }

    /**
     * Tests extending a data class.
     * 
     * @throws NoSuchFieldException shall not occur
     * @throws IllegalAccessException shall not occur
     * @throws InstantiationException shall not occur
     * @throws NoSuchMethodException shall not occur
     * @throws ClassNotFoundException shall not occur
     * @throws InvocationTargetException shall not occur
     * @throws IllegalArgumentException shall not occur
     */
    @Test
    public void testCreateDataClass() throws NoSuchFieldException, NoSuchMethodException, InstantiationException, 
        IllegalAccessException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException {
        ClassBuilder<Data> builder = Bytecode.getInstance().createClassBuilder("iip.mock.Mock", 
            Data.class, BytecodeTest.class.getClassLoader());
        builder.implement(BaseDataUnitFunctions.class);
        builder.annotate(IgnoreProperties.class)
            .define("ignoreUnknown", true)
            .build();
        builder.defineProperty("$period", Integer.TYPE)
            .annotate(ConfiguredName.class)
                .define("value", "abc")
                .build()
            .build();
        builder.defineProperty("$repeats", Integer.TYPE)
            .build();
        Class<? extends Data> cls = builder.build();

        Assert.assertNotNull(cls);
        Assert.assertEquals("iip.mock.Mock", cls.getName());
        IgnoreProperties a1 = cls.getAnnotation(IgnoreProperties.class);
        Assert.assertNotNull(a1);
        Assert.assertEquals(true, a1.ignoreUnknown());
        Data d = cls.getConstructor().newInstance();
        Assert.assertNotNull(d);
        
        Field f = cls.getDeclaredField("$period");
        Assert.assertEquals(Integer.TYPE, f.getType());
        Assert.assertTrue(Modifier.isPrivate(f.getModifiers()));
        ConfiguredName p = f.getAnnotation(ConfiguredName.class);
        Assert.assertEquals("abc", p.value());

        Method m = cls.getDeclaredMethod("set$repeats", Integer.TYPE);
        Assert.assertTrue(Modifier.isPublic(m.getModifiers()));
        Assert.assertEquals(Void.TYPE, m.getReturnType());
        m.invoke(d, 2);
        m = cls.getDeclaredMethod("get$repeats");
        Assert.assertEquals(Integer.TYPE, m.getReturnType());
        Assert.assertTrue(Modifier.isPublic(m.getModifiers()));
        Assert.assertEquals(2, m.invoke(d));

        f = cls.getDeclaredField("$period");
        Assert.assertEquals(Integer.TYPE, f.getType());
        Assert.assertTrue(Modifier.isPrivate(f.getModifiers()));
        m = cls.getDeclaredMethod("set$period", Integer.TYPE);
        Assert.assertTrue(Modifier.isPublic(m.getModifiers()));
        Assert.assertEquals(Void.TYPE, m.getReturnType());
        m = cls.getDeclaredMethod("get$period");
        Assert.assertEquals(Integer.TYPE, m.getReturnType());
        Assert.assertTrue(Modifier.isPublic(m.getModifiers()));
    }

    /**
     * Tests creating a dynamic class with public fields.
     * 
     * @throws NoSuchFieldException shall not occur
     * @throws ClassNotFoundException shall not occur
     */
    @Test
    public void testCreateBuilderClass() throws NoSuchFieldException, ClassNotFoundException {
        Class<? extends Object> cls = Bytecode.getInstance().createClassBuilder("MyType", 
            Object.class, BytecodeTest.class.getClassLoader())
            .definePublicField("intField", Integer.TYPE)
                .annotate(ConfiguredName.class)
                    .define("value", "bce")
                    .build()
                .build()
            .definePublicField("stringField", String.class).build()
            .build();
        
        Assert.assertNotNull(cls);
        Assert.assertEquals("MyType", cls.getName());
        
        Field f = cls.getDeclaredField("intField");
        Assert.assertEquals(Integer.TYPE, f.getType());
        Assert.assertTrue(Modifier.isPublic(f.getModifiers()));
        ConfiguredName p = f.getAnnotation(ConfiguredName.class);
        Assert.assertNotNull(p);
        Assert.assertEquals("bce", p.value());

        f = cls.getDeclaredField("stringField");
        Assert.assertEquals(String.class, f.getType());
        Assert.assertTrue(Modifier.isPublic(f.getModifiers()));
        p = f.getAnnotation(ConfiguredName.class);
        Assert.assertNull(p);
    }

}
