/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.ivml;

import java.util.LinkedList;
import java.util.List;

import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphEdge;
import de.iip_ecosphere.platform.configuration.ivml.IvmlGraphMapper.IvmlGraphNode;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Default graph node implementation. {@link #getName()} is bound against the nested variable 
 * {@link #getNameVarName()}. Shall serve for a more generic mapping, to be part of EASY-Producer, thus, customizable.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DefaultNode extends DefaultGraphElement implements IvmlGraphNode {

    private List<IvmlGraphEdge> inEdges = new LinkedList<>();
    private List<IvmlGraphEdge> outEdges = new LinkedList<>();
    private int xPos = INVALID_POSITION;
    private int yPos = INVALID_POSITION;
    private int width = INVALID_SIZE;
    private int height = INVALID_SIZE;
    private String impl;
    
    /**
     * Creates a graph node.
     * 
     * @param var the underlying variable
     */
    public DefaultNode(IDecisionVariable var) {
        super(var);
    }

    @Override
    public int getXPos() {
        return getIntValue(getXPosVarName(), xPos, INVALID_POSITION);
    }

    /**
     * Returns the IVML variable name of the horizontal position.
     * 
     * @return the variable name
     */
    protected String getXPosVarName() {
        return "xPos";
    }

    @Override
    public int getYPos() {
        return getIntValue(getYPosVarName(), yPos, INVALID_POSITION);
    }
    
    /**
     * Returns the IVML variable name of the vertical position.
     * 
     * @return the variable name
     */
    protected String getYPosVarName() {
        return "yPos";
    }

    @Override
    public int getWidth() {
        return getIntValue(getWidthVarName(), width, INVALID_SIZE);
    }

    /**
     * Returns the IVML variable name of the width.
     * 
     * @return the variable name
     */
    protected String getWidthVarName() {
        return "width";
    }

    @Override
    public int getHeight() {
        return getIntValue(getHeightVarName(), height, INVALID_SIZE);
    }

    /**
     * Returns the IVML variable name of the height.
     * 
     * @return the variable name
     */
    protected String getHeightVarName() {
        return "height";
    }

    @Override
    public String getName() {
        String result = super.getName(); 
        if (null == result || result.length() == 0) { 
            result = getImplImpl(); // prevent recursive fallback
        }
        return result;
    }
    
    /**
     * Returns the configured name of the implementing service.
     * 
     * @return the name, may be <b>null</b>
     */
    private String getImplImpl() {
        return getStringValue(getImplVarName(), impl);
    }
    
    @Override
    public String getImpl() {
        String result = getImplImpl(); 
        if (null == result || result.length() == 0) {
            result = super.getName(); // prevent recursive fallback
        }
        return result;
    }
    
    /**
     * Returns the IVML variable name of the service implementation.
     * 
     * @return the variable name
     */
    protected String getImplVarName() {
        return "impl";
    }

    @Override
    public void setXPos(int xPos) {
        this.xPos = xPos;
    }

    @Override
    public void setYPos(int yPos) {
        this.yPos = yPos;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void setImpl(String impl) {
        this.impl = impl;
    }

    @Override
    public Iterable<IvmlGraphEdge> inEdges() {
        return inEdges;
    }

    @Override
    public Iterable<IvmlGraphEdge> outEdges() {
        return outEdges;
    }

    @Override
    public int getInEdgesCount() {
        return inEdges.size();
    }

    @Override
    public int getOutEdgesCount() {
        return outEdges.size();
    }

    /**
     * Adds an outgoing edge to this node.
     * 
     * @param edge the edge
     */
    public void addEdge(IvmlGraphEdge edge) {
        if (edge.getStart() == this) {
            outEdges.add(edge);
        } else if (edge.getEnd() == this) {
            inEdges.add(edge);
        }
    }

}
