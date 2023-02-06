package de.iip_ecosphere.platform.configuration.opcua.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.iip_ecosphere.platform.configuration.opcua.data.BaseType;
import de.iip_ecosphere.platform.configuration.opcua.data.RootMethodType;
import de.iip_ecosphere.platform.configuration.opcua.data.RootObjectType;
import de.iip_ecosphere.platform.configuration.opcua.data.RootVariableType;

/**
 * Information collector for OPC UA companion spec files and IVML files.
 * 
 * @author Jan-Hendrik Cepok, SSE
 */
public class Collector {

    private static Map<String, Object[]> collection = new TreeMap<String, Object[]>();
    private static int modelCounter = 1;

    /**
     * Returns the next node.
     * 
     * @param nodes the nodes to take the node from
     * @param iterator the position/iterator
     * @return the node, may be <b>null</b>
     */
    private static Element getNextNodeElement(NodeList nodes, int iterator) {
        Node n = nodes.item(iterator);
        Element node = null;
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            node = (Element) n;
        }
        return node;
    }
    
    // checkstyle: stop exception type check
    // checkstyle: stop parameter number check

    /**
     * Collects the information parameters.
     * 
     * @param fileName         the nodeset file name
     * @param objectTypeList   the already parsed object type list
     * @param objectList       the already parsed object list
     * @param variableList     the already parsed variable list
     * @param methodList       the already parsed method list
     * @param dataTypeList     the already parsed data type list
     * @param variableTypeList the already parsed variable type list
     * @param hierarchy        the base type hierarchy
     * @param reqModels        the number of required models
     */
    public static void collectInformation(String fileName, NodeList objectTypeList, NodeList objectList,
            NodeList variableList, NodeList methodList, NodeList dataTypeList, NodeList variableTypeList,
            ArrayList<BaseType> hierarchy, int reqModels) {
        try {
            BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("target/tmp/CollectedInformation.txt", true)));
            StringBuilder builder = new StringBuilder();
            long countLines = 0;
            try {
                Path file = Paths.get("src/main/resources/NodeSets/" + fileName);
                countLines = Files.lines(file).count();
            } catch (Exception e) {
                e.getStackTrace();
            }
            builder.append("Companion Spec: " + fileName + "\n\n");
            builder.append("Anzahl der Zeilen der Comp Spec: " + countLines + "\n");
            builder.append("Anzahl der importierten Modelle: " + reqModels + "\n");
            builder.append("Anzahl der verschiedenen UAElemente:\n\n");
            int counterOT = 0;
            for (int i = 0; i < objectTypeList.getLength(); i++) {
                Element object = getNextNodeElement(objectTypeList, i);
                if (object != null) {
                    counterOT += 1;
                }
            }
            builder.append("Anzahl der ObjectTypes: " + counterOT + "\n");
            int counterO = 0;
            for (int i = 0; i < objectList.getLength(); i++) {
                Element object = getNextNodeElement(objectList, i);
                if (object != null) {
                    counterO += 1;
                }
            }
            builder.append("Anzahl der Objects: " + counterO + "\n");
            int counterVT = 0;
            for (int i = 0; i < variableTypeList.getLength(); i++) {
                Element object = getNextNodeElement(variableTypeList, i);
                if (object != null) {
                    counterVT += 1;
                }
            }
            builder.append("Anzahl der VariableTypes: " + counterVT + "\n");
            int counterV = 0;
            for (int i = 0; i < variableList.getLength(); i++) {
                Element object = getNextNodeElement(variableList, i);
                if (object != null) {
                    counterV += 1;
                }
            }
            builder.append("Anzahl der Variables: " + counterV + "\n");
            int counterM = 0;
            for (int i = 0; i < methodList.getLength(); i++) {
                Element object = getNextNodeElement(methodList, i);
                if (object != null) {
                    counterM += 1;
                }
            }
            builder.append("Anzahl der Methoden: " + counterM + "\n");
            int counterD = 0;
            for (int i = 0; i < dataTypeList.getLength(); i++) {
                Element object = getNextNodeElement(dataTypeList, i);
                if (object != null) {
                    counterD += 1;
                }
            }
            builder.append("Anzahl der DataTypes: " + counterD + "\n");
            int countConnectorFields = 0;
            for (BaseType b : hierarchy) {
                if (b instanceof RootObjectType || b instanceof RootVariableType || b instanceof RootMethodType) {
                    countConnectorFields += 1;
                }
            }
            builder.append("Anzahl der ConnectorFelder: " + countConnectorFields + "\n\n\n");
            writer.write(builder.toString());
            writer.close();
            modelCounter += 1;
            collection.put(Integer.toString(modelCounter), new Object[] {fileName, countLines, reqModels, counterOT,
                counterO, counterVT, counterV, counterM, counterD, countConnectorFields});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // checkstyle: resume parameter number check
    // checkstyle: resume exception type check

    /**
     * Transfers collected information to excel.
     */
    @SuppressWarnings("resource")
    public static void informationToExcel() {
        collection.put("1",
            new Object[] {"Comp Spec", "Comp Spec Zeilen", "Importierte Modelle", "ObjectTypes", "Objects",
                "VariableTypes", "Variables", "Methods", "DataTypes", "Connector Felder",
                "Generierte Code Zeilen"});
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet = workbook.createSheet("Collection");
        XSSFRow row;
        Set<String> keyid = collection.keySet();
        int rowid = 0;

        for (String key : keyid) {
            row = spreadsheet.createRow(rowid++);
            Object[] objectArr = collection.get(key);
            int cellid = 0;

            for (Object obj : objectArr) {
                Cell cell = row.createCell(cellid++);
                if (obj instanceof Long || obj instanceof Integer) {
                    cell.setCellValue(String.valueOf(obj));
                } else {
                    cell.setCellValue((String) obj);
                }
            }
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(new File("target/tmp/Collection.xlsx"));
            try {
                workbook.write(out);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
