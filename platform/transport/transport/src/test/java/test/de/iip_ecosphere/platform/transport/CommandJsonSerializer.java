/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/
package test.de.iip_ecosphere.platform.transport;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.iip_ecosphere.platform.transport.serialization.Serializer;

/**
 * A test serializer for {@link Command}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CommandJsonSerializer implements Serializer<Command> {

    @SuppressWarnings("unchecked")
    @Override
    public byte[] to(Command value) throws IOException {
        JSONObject json = new JSONObject();
        json.put("command", value.getCommand());
        return json.toJSONString().getBytes();
    }

    @Override
    public Command from(byte[] data) throws IOException {
        Command result;
        try {
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(new String(data));
            result = new Command(JsonUtils.readString(obj, "command"));
        } catch (ParseException e) {
            throw new IOException(e.getMessage(), e);
        } catch (ClassCastException e) {
            throw new IOException(e.getMessage(), e);
        }
        return result;
    }
    
    @Override
    public Command clone(Command origin) throws IOException {
        return new Command(origin.getCommand());
    }

    @Override
    public Class<Command> getType() {
        return Command.class;
    }

}
