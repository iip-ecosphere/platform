package de.iip_ecosphere.platform.configuration.ivml;

import net.ssehub.easy.varModel.model.datatypes.DerivedDatatype;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.TypeQueries;

/**
 * Defines the IVML type kind.
 * 
 * @author Holger Eichelberger, SSE
 */
public enum IvmlTypeKind {
    
    PRIMITIVE(1),
    ENUM(2),
    CONTAINER(3),
    CONSTRAINT(4),
    DERIVED(9),
    COMPOUND(10);
    
    private int id;
    
    /**
     * Creates a constant. We do not rely on ordinals if something needs to be added here
     * 
     * @param id the unique id
     */
    private IvmlTypeKind(int id) {
        this.id = id;
    }
    
    /**
     * Returns the unique id.
     * 
     * @return the id
     */
    public int getId() {
        return id;
    }
    
    /**
     * Determines the type kind of {@code type}.
     * 
     * @param type the type
     * @return the type kind
     */
    public static IvmlTypeKind asTypeKind(IDatatype type) {
        IvmlTypeKind result;
        if (TypeQueries.isCompound(type)) {
            result = IvmlTypeKind.COMPOUND;
        } else if (TypeQueries.isEnum(type)) {
            result = IvmlTypeKind.ENUM;
        } else if (TypeQueries.isContainer(type)) {
            result = IvmlTypeKind.CONTAINER;
        } else if (TypeQueries.isConstraint(type)) {
            result = IvmlTypeKind.CONSTRAINT;
        } else if (type instanceof DerivedDatatype) {
            result = IvmlTypeKind.DERIVED;
        } else {
            result = IvmlTypeKind.PRIMITIVE;
        }
        return result;
    }

}