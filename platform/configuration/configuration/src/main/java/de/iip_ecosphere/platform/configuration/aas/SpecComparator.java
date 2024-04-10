package de.iip_ecosphere.platform.configuration.aas;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.iip_ecosphere.platform.configuration.FallbackLogger.LoggingLevel;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.FileUtils;

public class SpecComparator {

    private static final Map<File, SpecPair> SPECS = new TreeMap<>((f1, f2) -> -f1.toString().compareTo(f2.toString()));
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("# %"); //"#.00 %"
    
    private static class SpecPair {
        
        private File xlsx;
        private File aasx;
        
    }

    /**
     * Registers a given {@code file}, selects for AASX or XLSX.
     * 
     * @param file the file
     */
    private static void register(File file) {
        File folder = file.getParentFile();
        SpecPair pair = SPECS.get(folder);
        if (null == pair) {
            pair = new SpecPair();
            SPECS.put(folder, pair);
        }
        String name = file.getName();
        if (name.endsWith(".aasx") && !name.endsWith(".spec.aasx")) {
            pair.aasx = file;
        } else if (name.endsWith(".xlsx")) {
            if (name.endsWith(".mod.xlsx")) {
                if (pair.xlsx != null) {
                    pair.xlsx = file;
                }
            } else {
                pair.xlsx = file;
            }
        }
    }
    
    /**
     * Sorts and returns the types in {@code summary}.
     * 
     * @param summary the summary instance
     * @return the type
     */
    private static List<AasType> sortTypes(AasSpecSummary summary) {
        List<AasType> result = CollectionUtils.toList(summary.types());
        Collections.sort(result, (t1, t2) -> t1.getIdShort().compareTo(t2.getIdShort()));
        return result;
    }
    
    /**
     * Compares two spec summaries.
     * 
     * @param pdf the PDF summary (considered to be ground truth)
     * @param aas the AAS summary
     */
    private static void compare(AasSpecSummary pdf, AasSpecSummary aas) {
        List<AasType> pdfTypes = sortTypes(pdf);
        Map<String, AasType> aasTypes = new HashMap<>();
        for (AasType t : aas.types()) {
            aasTypes.put(t.getIdShort(), t);
        }
        
        int elementsInPdf = 0;
        int overlappingInAasx = 0;
        for (AasType p : pdfTypes) {
            AasType a = aasTypes.get(p.getIdShort());
            elementsInPdf += 4; // field comparisons below        
            if (a != null) {
                if (p.getDescription() != null && a.getDescription() != null) {
                    overlappingInAasx++;
                }
                if (p.getSemanticId() != null && p.getSemanticId().equals(a.getSemanticId())) {
                    overlappingInAasx++;
                }
                if (p.getSmeType() != null && p.getSmeType() == a.getSmeType()) {
                    overlappingInAasx++;
                }
                if (p.isGeneric() && a.isGeneric()) {
                    overlappingInAasx++;
                }
                
                Map<String, AasField> aasFields = new HashMap<>();
                for (AasField f: a.fields()) {
                    aasFields.put(f.getIdShort(), f);
                }
                for (AasField pf: p.fields()) {
                    AasField af = aasFields.get(pf.getIdShort());
                    elementsInPdf += 6; // field comparisons below        
                    if (af != null) {
                        if (pf.getDescription() != null && af.getDescription() != null) {
                            overlappingInAasx++;
                        }
                        if (pf.getExampleValues() != null && af.getExampleValues() != null) {
                            overlappingInAasx++;
                        }
                        if (pf.getSemanticId() != null && pf.getSemanticId().equals(af.getSemanticId())) {
                            overlappingInAasx++;
                        }
                        if (pf.getValueType() != null && pf.getValueType().equals(af.getValueType())) {
                            overlappingInAasx++;
                        }
                        if (pf.getLowerCardinality() == af.getLowerCardinality()) {
                            overlappingInAasx++;
                        }
                        if (pf.getUpperCardinality() == af.getUpperCardinality()) {
                            overlappingInAasx++;
                        }
                    }
                }
            }
        }
        double overlap = overlappingInAasx / ((double) elementsInPdf);
        System.out.println(" pdf: " + elementsInPdf + " aasx: " + overlappingInAasx 
            + ": " + PERCENT_FORMAT.format(overlap));
    }
    
    /**
     * Reads the spec files and compares them structurally.
     * 
     * @param args the command line arguments, ignored
     */
    public static void main(String[] args) {
        ParsingUtils.setLoggingLevel(LoggingLevel.ERROR);
        FileUtils.listFiles(new File("src/test/resources/idta"), 
            f -> f.isDirectory() || f.isFile(), f -> register(f));

        for (File f : SPECS.keySet()) { // sorted
            SpecPair pair = SPECS.get(f);
            System.out.println(f);
            if (pair.xlsx != null && pair.aasx != null) {
                try {
                    AasSpecSummary xlsx = ReadExcelFile.read(pair.xlsx.toString());
                    AasSpecSummary aasx = ReadAasxFile.read(pair.aasx.toString(), 
                        ReadAasxFile.getSpecNumber(pair.aasx));
                    compare(xlsx, aasx);
                } catch (IOException e) {
                    System.out.println("Cannot compare files in " + f + ": " + e.getMessage());
                }
            } else {
                System.out.println("Cannot compare files in " + f + ": One not found; xlsx: " 
                    + pair.xlsx + " aasx: " + pair.aasx);
            }
        }
    }

}
