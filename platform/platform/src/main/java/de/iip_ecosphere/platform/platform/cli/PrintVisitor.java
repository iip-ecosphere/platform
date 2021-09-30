package de.iip_ecosphere.platform.platform.cli;

import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

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
    
    /**
     * Information about nested collection levels.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class CollectionInfo {
        private boolean skip;
        private boolean emitted;

        /**
         * Creates an instance.
         * 
         * @param skip skip the collection transitively ({@code false}) or emit it ({@code true})
         */
        private CollectionInfo(boolean skip) {
            this.skip = skip;
        }
        
        @Override
        public String toString() {
            return skip + " " + emitted;
        }
        
    }
    
    private String collPrefix;
    private String indent = "";
    private PrintType[] skipLevel;
    private Predicate<SubmodelElementCollection> filter;
    private Stack<CollectionInfo> collectionLevel = new Stack<CollectionInfo>();

    /**
     * Creates a visitor instance.
     * 
     * @param collPrefix a prefix string to be printed before the name of collection, may be <b>null</b> for not 
     *     printing the collection name
     * @param filter a predicate to exclude certain submodel elements collection from output 
     * @param skipLevel how to handle printout per level, if not given {@link PrintType#ID_SHORT} is used
     */
    public PrintVisitor(String collPrefix, Predicate<SubmodelElementCollection> filter, PrintType... skipLevel) {
        this.collPrefix = collPrefix;
        this.skipLevel = skipLevel;
        this.filter = filter;
    }
    
    /**
     * Returns the print/skip type for the specified level.
     *
     * @param collection to collection to check for
     * @return the print/skip type
     */
    private PrintType getSkipLevel(SubmodelElementCollection collection) {
        PrintType result;
        int level = collectionLevel.size() - 1;
        if (level < 0 || level >= skipLevel.length) {
            result = PrintType.ID_SHORT;
        } else {
            result = skipLevel[level];
        }
        if (skipEntireCollection(collection)) {
            result = PrintType.NO;
        }
        return result;
    }
    
    /**
     * Returns whether an entire submodel element collection shall be skipped.
     * 
     * @param collection the collection to check
     * @return {@code true} for skip transitively, {@code false} for print
     */
    private boolean skipEntireCollection(SubmodelElementCollection collection) {
        return (null != filter && !filter.test(collection));
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
        CollectionInfo info;
        if (collectionLevel.isEmpty()) {
            info = null;
        } else {
            info = collectionLevel.peek();
        }
        if (null == info || !info.skip) {
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
            if (null != info) {
                info.emitted = true; // assuming that collections at least have a property
            }
        }
    }

    @Override
    public void visitOperation(Operation operation) {
    }

    @Override
    public void visitReferenceElement(ReferenceElement referenceElement) {
    }

    @Override
    public void visitSubmodelElementCollection(SubmodelElementCollection collection) {
        CollectionInfo info = new CollectionInfo(skipEntireCollection(collection));
        collectionLevel.push(info);
        PrintType type = getSkipLevel(collection);
        if (type != PrintType.NO) {
            if (PrintType.PREFIX == type) {
                if (null != collPrefix) {
                    System.out.println(indent + collPrefix + collection.getIdShort());
                }
            } else {
                System.out.println(indent + "-" + collection.getIdShort());
            }
            indent += "  ";
            info.emitted = true; // assuming that collection elements in the first place determine the output
        }
    }

    @Override
    public void endSubmodelElementCollection(SubmodelElementCollection collection) {
        CollectionInfo info = collectionLevel.peek();
        PrintType type = getSkipLevel(collection);
        if (type != PrintType.NO) {
            indent = indent.substring(0, indent.length() - 2);
            if (!info.emitted) {
                System.out.println(indent + " None.");
            }
        }
        collectionLevel.pop();
    }
    
}