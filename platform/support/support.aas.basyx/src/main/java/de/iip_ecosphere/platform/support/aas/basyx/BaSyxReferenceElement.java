package de.iip_ecosphere.platform.support.aas.basyx;

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IReferenceElement;

import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.SubModel.SubModelBuilder;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxSubModel.BaSyxSubModelBuilder;

/**
 * Implements the reference element wrapper.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxReferenceElement implements ReferenceElement {
    
    private IReferenceElement reference;
    
    /**
     * Implements the reference element builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxReferenceElementBuilder implements ReferenceElementBuilder {
        
        private BaSyxSubModelBuilder parentBuilder;
        private BaSyxReferenceElement instance;
        private org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.ReferenceElement reference;
        
        /**
         * Creates a builder instance.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the short id of the reference element
         */
        BaSyxReferenceElementBuilder(BaSyxSubModelBuilder parentBuilder, String idShort) {
            if (null == idShort || 0 == idShort.length()) {
                throw new IllegalArgumentException("idShort must be given");
            }
            this.parentBuilder = parentBuilder;
            instance = new BaSyxReferenceElement();
            reference = new org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.ReferenceElement();
            reference.setIdShort(idShort);
        }
        
        @Override
        public SubModelBuilder getParentBuilder() {
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
     * Returns the BaSyx reference element.
     * 
     * @return the BaSyx reference element
     */
    IReferenceElement getReferenceElement() {
        return reference;
    }

    @Override
    public String getIdShort() {
        return reference.getIdShort();
    }

    @Override
    public Reference getValue() {
        return new BaSyxReference(reference.getValue());
    }

}
