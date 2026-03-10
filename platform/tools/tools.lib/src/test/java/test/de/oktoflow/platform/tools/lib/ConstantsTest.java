package test.de.oktoflow.platform.tools.lib;

import org.junit.Assert;
import org.junit.Test;

import de.oktoflow.platform.tools.lib.loader.Constants;
import de.oktoflow.platform.tools.lib.loader.Constants.UnpackMode;

public class ConstantsTest {
    
    /**
     * Tests {@link Constants#toUnpackMode(String, de.oktoflow.platform.tools.lib.loader.Constants.UnpackMode)}.
     */
    @Test
    public void testToUnpackMode() {
        Assert.assertEquals(UnpackMode.JARS, Constants.toUnpackMode(null, UnpackMode.JARS));
        Assert.assertEquals(UnpackMode.JARS, Constants.toUnpackMode("abc", UnpackMode.JARS));
        Assert.assertEquals(UnpackMode.JARS, Constants.toUnpackMode("JARS", UnpackMode.JARS));
        Assert.assertEquals(UnpackMode.RESOLVE, Constants.toUnpackMode("RESOLVE", UnpackMode.JARS));
    }

}
