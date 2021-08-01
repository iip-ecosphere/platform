package de.iip_ecosphere.platform.platform.cli;

import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.environment.metricsProvider.meterRepresentation.MeterRepresentation;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.Asset;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;

/**
 * A visitor for printing a structured list of sub-model elements collections and their properties.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PrintVisitor implements AasVisitor {
    
    /**
     * Print types for controlling nested collection output.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum PrintType {
        
        /**
         * Don't print, skip.
         */
        NO,
        
        /**
         * Print out the short id (default).
         */
        ID_SHORT,
        
        /**
         * Print the given prefix.
         */
        PREFIX
    }
    
    private String collPrefix;
    private String indent = "";
    private boolean emitted;
    private PrintType[] skipLevel;
    private int collectionLevel = 0;
    
    /**
     * Creates a visitor instance.
     * 
     * @param collPrefix a prefix string to be printed before the name of collection, may be <b>null</b> for not 
     *     printing the collection name
     * @param skipLevel how to handle printout per level, if not given {@link PrintType#ID_SHORT} is used
     */
    public PrintVisitor(String collPrefix, PrintType... skipLevel) {
        this.collPrefix = collPrefix;
        this.skipLevel = skipLevel;
    }
    
    /**
     * Returns the print/skip type for the specified level.
     * 
     * @param level the level to look for
     * @return the print/skip type
     */
    private PrintType getSkipLevel(int level) {
        PrintType result;
        if (level < 0 || level >= skipLevel.length) {
            result = PrintType.ID_SHORT;
        } else {
            result = skipLevel[level];
        }
        return result;
    }

    @Override
    public void visitAas(Aas aas) {
    }

    @Override
    public void endAas(Aas aas) {
    }

    @Override
    public void visitAsset(Asset asset) {
    }

    @Override
    public void visitSubmodel(Submodel submodel) {
    }

    @Override
    public void endSubmodel(Submodel submodel) {
    }

    @Override
    public void visitProperty(Property property) {
        Object val;
        try {
            val = property.getValue();
            if (null != val) {
                try {
                    Meter m = MeterRepresentation.parseMeter(val.toString());
                    if (m instanceof Gauge) {
                        double value = ((Gauge) m).value();
                        if (value > 1000) { // heuristic, assumption
                            val = String.format("%.0f", value);
                        } else {
                            val = String.format("%f", value);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
        } catch (ExecutionException e) {
            val = "?";
        }
        System.out.println(indent + property.getIdShort() + ": " + val);
        emitted = true; // assuming that collections at least have a property
    }

    @Override
    public void visitOperation(Operation operation) {
    }

    @Override
    public void visitReferenceElement(ReferenceElement referenceElement) {
    }

    @Override
    public void visitSubmodelElementCollection(SubmodelElementCollection collection) {
        PrintType type = getSkipLevel(collectionLevel);
        if (type != PrintType.NO) {
            if (PrintType.PREFIX == type) {
                if (null != collPrefix) {
                    System.out.println(indent + collPrefix + collection.getIdShort());
                }
            } else {
                System.out.println(indent + "-" + collection.getIdShort());
            }
            indent += "  ";
            emitted = true; // assuming that collection elements in the first place determine the output
        }
        collectionLevel++;
    }

    @Override
    public void endSubmodelElementCollection(SubmodelElementCollection collection) {
        collectionLevel--;
        PrintType type = getSkipLevel(collectionLevel);
        if (type != PrintType.NO) {
            indent = indent.substring(0, indent.length() - 2);
            if (!emitted) {
                System.out.println(indent + " None.");
            }
        }
    }
    
}