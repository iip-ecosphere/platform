package de.iip_ecosphere.platform.support.aas.basyx;

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IReferenceElement;
import org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;

/**
 * Implements the reference element wrapper.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxReferenceElement extends BaSyxSubmodelElement implements ReferenceElement {
    
    private IReferenceElement reference;
    
    /**
     * Implements the reference element builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxReferenceElementBuilder implements ReferenceElementBuilder {
        
        private BaSyxSubmodelElementContainerBuilder<?> parentBuilder;
        private BaSyxReferenceElement instance;
        private org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.ReferenceElement reference;
        
        /**
         * Creates a builder instance.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the short id of the reference element
         */
        BaSyxReferenceElementBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, String idShort) {
            if (null == idShort || 0 == idShort.length()) {
                throw new IllegalArgumentException("idShort must be given");
            }
            this.parentBuilder = parentBuilder;
            instance = new BaSyxReferenceElement();
            reference = new org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.ReferenceElement();
            reference.setIdShort(idShort);
        }
        
        @Override
        public BaSyxSubmodelElementContainerBuilder<?> getParentBuilder() {
            return parentBuilder;
        }

        @Override
        public ReferenceElement build() {
            instance.reference = reference;
            return parentBuilder.register(instance);
        }

        @Override
        public ReferenceElementBuilder setValue(Reference value) {
            if (!(value instanceof BaSyxReference)) {
                throw new IllegalArgumentException("value must be of type reference");
            }
            reference.setValue(((BaSyxReference) value).getReference()); 
            return this;
        }
        
    }
    
    /**
     * Creates an instance. Prevents external access.
     */
    private BaSyxReferenceElement() {
    }
    
    /**
     * Creates an instance and directly sets the reference.
     * 
     * @param reference the reference
     */
    BaSyxReferenceElement(IReferenceElement reference) {
        this.reference = reference;
    }
    
    /**
     * Returns the BaSyx reference element.
     * 
     * @return the BaSyx reference element
     */
    IReferenceElement getReferenceElement() {
        return reference;
    }

    // checkstyle: stop exception type check

    @Override
    public String getIdShort() {
        try {
            return reference.getIdShort();
        } catch (ResourceNotFoundException e) { // TODO check BaSyx Bug 0.1.0-SNAPSHOT for dynamic properties
            return "";
        }
    }

    @Override
    public Reference getValue() {
        try {
            return new BaSyxReference(reference.getValue());
        } catch (ResourceNotFoundException e) { // TODO check BaSyx Bug 0.1.0-SNAPSHOT for dynamic properties
            return null;
        }
    }

    // checkstyle: resume exception type check

    @Override
    IReferenceElement getSubmodelElement() {
        return reference;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitReferenceElement(this);
    }

}
