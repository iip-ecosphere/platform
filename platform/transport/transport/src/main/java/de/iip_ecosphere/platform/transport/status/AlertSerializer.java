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

package de.iip_ecosphere.platform.transport.status;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.transport.serialization.Serializer;

/**
 * A simple, generic alert event serializer. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class AlertSerializer implements Serializer<Alert> {

    @Override
    public Alert from(byte[] data) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(data, Alert.class);
    }

    @Override
    public byte[] to(Alert source) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsBytes(source);
    }

    @Override
    public Alert clone(Alert origin) throws IOException {
        Alert e = new Alert();
        e.setUid(origin.getUid());
        e.setCorrelationId(origin.getCorrelationId());
        e.setTimestamp(origin.getTimestamp());
        e.setFirstTimestamp(origin.getFirstTimestamp());
        e.setLastTimestamp(origin.getLastTimestamp());
        e.setClearTimestamp(origin.getClearTimestamp());
        e.setAlertname(origin.getAlertname());
        e.setSource(origin.getSource());
        e.setInstance(origin.getInstance());
        e.setInfo(origin.getInfo());
        e.setTags(origin.getTags());
        e.setSeverity(origin.getSeverity());
        e.setPriority(origin.getPriority());
        e.setEventType(origin.getEventType());
        e.setProbableCause(origin.getProbableCause());
        e.setCurrentValue(origin.getCurrentValue());
        e.setUrl(origin.getUrl());
        e.setDescription(origin.getDescription());
        e.setStatus(origin.getStatus());
        e.setRuleExpression(origin.getRuleExpression());
        e.setRuleTimeLimit(origin.getRuleTimeLimit());
        return e;
    }

    @Override
    public Class<Alert> getType() {
        return Alert.class;
    }
    
}
