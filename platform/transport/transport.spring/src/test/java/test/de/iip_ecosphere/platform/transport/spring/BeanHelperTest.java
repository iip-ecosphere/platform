package test.de.iip_ecosphere.platform.transport.spring;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.spring.BeanHelper;

/**
 * Brings up a spring application doing nothing rather than testing the {@link BeanHelper].
 * 
 * @author Holger Eichelberger, SSE
 */
@SpringBootTest(classes = TransportFactoryConfigurationTest.class)
@RunWith(SpringRunner.class)
@ComponentScan(basePackages = "de.iip_ecosphere.platform.transport.spring")
public class BeanHelperTest {
    
    @Autowired
    private ApplicationContext ctx;
    
    /**
     * Tests the transport parameter bean helper.
     */
    @Test
    public void testBeanHelper() {
        final String beanName = "myTpBean";
        TransportParameter bean = new TransportParameter("h", 1111, "c");
        // test context does not have a child context as a binder has
        AnnotationConfigApplicationContext child = new AnnotationConfigApplicationContext();
        child.setParent(ctx);
        BeanHelper.registerInParentContext(child, bean, beanName);
        // if we ask the child, we get the results from the registration in the parent
        Assert.assertTrue(child.containsBean(beanName));
        Assert.assertTrue(child.getParent().getBean(TransportParameter.class) == bean);
        Assert.assertTrue(child.getParent().getBean(beanName) == bean);

        BeanHelper.registerInParentContext(child, bean);
        Assert.assertTrue(child.containsBean(beanName));
    }

}
