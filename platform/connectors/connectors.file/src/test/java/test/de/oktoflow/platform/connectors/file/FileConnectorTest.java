/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.oktoflow.platform.connectors.file;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.types.ChannelTranslatingProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.ConnectorInputTypeAdapter;
import de.iip_ecosphere.platform.connectors.types.ConnectorOutputTypeAdapter;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import de.oktoflow.platform.connectors.file.FileConnector;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

import test.de.iip_ecosphere.platform.connectors.AbstractSerializingConnectorTest;
import test.de.iip_ecosphere.platform.connectors.ConnectorTest;
import test.de.iip_ecosphere.platform.transport.Command;
import test.de.iip_ecosphere.platform.transport.CommandJsonSerializer;
import test.de.iip_ecosphere.platform.transport.Product;
import test.de.iip_ecosphere.platform.transport.ProductJsonSerializer;

/**
 * Implements a test for {@link FileConnector}. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class FileConnectorTest {

    /**
     * Creates a connector.
     * 
     * @param outSer the output serializer to use
     * @param inSer the input serializer to use
     * @param determineTimeDiff shall a data dependent time difference be applied/simulated
     * @return the connector instance
     */
    private Connector<byte[], byte[], Product, Command> createConnector(Serializer<Product> outSer, 
        Serializer<Command> inSer, boolean determineTimeDiff) {
        Connector<byte[], byte[], Product, Command> result = new FileConnector<>(
            new ChannelTranslatingProtocolAdapter<byte[], byte[], Product, Command>(
                AbstractSerializingConnectorTest.PROD_CHANNEL, new ConnectorOutputTypeAdapter<Product>(outSer), 
                AbstractSerializingConnectorTest.CMD_CHANNEL, new ConnectorInputTypeAdapter<Command>(inSer)));
        if (determineTimeDiff) {
            result.setDataTimeDifferenceProvider(p -> 900); // for testing, constant
        }
        return result;
    }
    
    /**
     * Composes a temporary path.
     * 
     * @param postfix the path postfix after the temporary folder, e.g., a file name, may be empty
     * @return the composed path
     */
    private static String composeTmpPath(String postfix) {
        String tmp = FileUtils.getTempDirectoryPath();
        if (!tmp.endsWith(File.separator)) {
            tmp += File.separator;
        }
        return tmp + postfix;
    }
    
    /**
     * Tests a single file.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testSingleFile() throws IOException {
        testConnector("src/test/resources/singleFile/dataFile.json", composeTmpPath(""), 2, false);
    }

    /**
     * Tests a single file as resource.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testSingleFileResource() throws IOException {
        testConnector("singleFile/dataFile.json", null, 2, false);
    }

    /**
     * Tests a multiple files in a directory.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testMultipleFiles() throws IOException {
        testConnector("src/test/resources/multiFiles/", composeTmpPath("fileConnTest.txt"), 4, false);
    }

    /**
     * Tests a multiple files in a directory selected by regEx.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testRegExFiles() throws IOException {
        testConnector("src/test/resources/patternFiles/dataFile\\d.json", null, 4, true);
    }

    /**
     * Lists all temporary connector files (in temp).
     * 
     * @param considerThread shall the current thread id be considered when finding the files or shall all be returned
     * @return the files found, may be <b>null</b> for none
     */
    private static File[] listTmpConnectorFiles(boolean considerThread) {
        return FileUtils.getTempDirectory().listFiles(FileConnector.getWriteFileNameFilter(considerThread));
    }

    /**
     * Deletes temporary connector files from {@link #listTmpConnectorFiles(boolean)} without considering
     * the current thread id. If {@code writeFiles} is given and an existing file, also try to delete that file.
     * 
     * @param writeFiles the test file/folder to be written by the connector, may be empty/<b>null</b>
     * @return the temp files deleted and identified to exist before, except for {@code writeFiles}
     */
    private static File[] deleteTmpConnectorFiles(String writeFiles) {
        File[] files = listTmpConnectorFiles(false);
        if (null != files) {
            for (File f : files) {
                FileUtils.deleteQuietly(f);
            }
        }
        if (null != writeFiles) {
            File f = new File(writeFiles);
            if (f.isFile() && f.exists()) {
                FileUtils.deleteQuietly(f);
            }
        }
        return files;
    }

    /**
     * Finds the new connector file written by the connector.
     * 
     * @param before the temporary files existing before the test, {@link #deleteTmpConnectorFiles(String)}.
     * @param writeFiles the test file/folder to be written by the connector, may be empty/<b>null</b>; 
     *     takes precedence if given
     * @return the file written by the connector, may be <b>null</b> for none
     */
    private static File findNewConnectorFile(File[] before, String writeFiles) {
        File result = null;
        boolean checkTmp = true;
        if (null != writeFiles) {
            File tmp = new File(writeFiles);
            if (tmp.isFile() && tmp.exists()) {
                result = tmp;
                checkTmp = false;
            }
        } 
        if (checkTmp) {
            File[] files = listTmpConnectorFiles(true);
            if (null != files && null != before) {
                Set<File> tmp = new HashSet<>();
                CollectionUtils.addAll(tmp, files);
                for (File b : before) {
                    tmp.remove(b);
                }
                Optional<File> o = tmp.stream().findFirst();
                if (o.isPresent()) {
                    result = o.get();
                }
            }
        }
        return result;
    }

    /**
     * Tests a file connector by reading and if set up writing.
     * 
     * @param readFiles the files to read as file name, folder name or file/folder-regex
     * @param writeFiles the files to write as file name or folder name, may be <b>null</b>
     * @param receivedPredicate a predicate testing the expected number of data points
     * @param sleep the time to sleep
     * @param determineTimeDiff shall a data dependent time difference be applied/simulated
     * @throws IOException
     */
    private void testConnector(String readFiles, String writeFiles, int expectedReceived, boolean determineTimeDiff) 
        throws IOException {
        System.out.println("Testing with read (" + readFiles + ") write (" + writeFiles + ")");
        File[] tmpFiles = deleteTmpConnectorFiles(writeFiles);
        ConnectorTest.assertDescriptorRegistration(FileConnector.Descriptor.class);
        ConnectorParameter cParams = ConnectorParameterBuilder.newBuilder("localhost", 10)
            .setNotificationInterval(1000) // stabilize test
            .setSpecificSetting(FileConnector.SETTING_READ_FILES, readFiles)
            .setSpecificSetting(FileConnector.SETTING_WRITE_FILES, writeFiles)
            .setSpecificSetting(FileConnector.SETTING_DATA_TIMEDIFF, 100)
            .build();

        Serializer<Product> outSer = new ProductJsonSerializer();
        SerializerRegistry.registerSerializer(outSer);
        Serializer<Command> inSer = new CommandJsonSerializer();
        SerializerRegistry.registerSerializer(inSer);

        Connector<byte[], byte[], Product, Command> c = createConnector(outSer, inSer, determineTimeDiff);
        ConnectorTest.assertInstance(c, false);
        ConnectorTest.assertConnectorProperties(c);
        c.connect(cParams);
        ConnectorTest.assertInstance(c, true);
        List<Product> receivedProducts = new ArrayList<>();
        c.setReceptionCallback(new ReceptionCallback<Product>() {
            
            @Override
            public void received(Product data) {
                receivedProducts.add(data);
                System.out.println("RECEIVED " + data.getDescription() + " " + data.getPrice());
            }
            
            @Override
            public Class<Product> getType() {
                return Product.class;
            }
        });
        c.enableNotifications(false); // for polling
        TimeUtils.sleep(expectedReceived * 1500);
        Command cmd = new Command("run");
        c.write(cmd);

        ConnectorTest.assertInstance(c, true);
        c.disconnect();
        ConnectorTest.assertInstance(c, false);
        c.dispose();

        SerializerRegistry.unregisterSerializer(outSer);
        SerializerRegistry.unregisterSerializer(inSer);

        Assert.assertEquals(expectedReceived, receivedProducts.size());
        List<Product> receivedProductsSorted = new ArrayList<>(receivedProducts);
        Collections.sort(receivedProductsSorted, (p1, p2) -> p1.getDescription().compareTo(p2.getDescription()));
        Assert.assertEquals(receivedProductsSorted, receivedProducts); // default object id sufficient
        
        if (writeFiles != null) {
            File written = findNewConnectorFile(tmpFiles, writeFiles);
            Assert.assertNotNull("No file written", written);
            String writtenContents = FileUtils.readFileToString(written, Charset.defaultCharset()).trim();
            String expectedContents = new String(inSer.to(cmd)).trim();
            Assert.assertEquals(expectedContents, writtenContents);
            FileUtils.deleteQuietly(written);
        }
    }

}
