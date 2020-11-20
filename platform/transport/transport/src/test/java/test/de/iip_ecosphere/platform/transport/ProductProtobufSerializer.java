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

import de.iip_ecosphere.platform.transport.serialization.Serializer;
import test.de.iip_ecosphere.platform.transport.proto.ProductOuterClass;

/**
 * Implements a test protobuf serializer for {@link Product}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ProductProtobufSerializer implements Serializer<Product> {

    @Override
    public byte[] to(Product value) throws IOException {
        ProductOuterClass.Product prod = ProductOuterClass.Product.newBuilder()
            .setDescription(value.getDescription())
            .setPrice(value.getPrice())
            .build();
        return prod.toByteArray();
    }

    @Override
    public Product from(byte[] data) throws IOException {
        ProductOuterClass.Product prod = ProductOuterClass.Product.parseFrom(data);
        return new Product(prod.getDescription(), prod.getPrice());
    }
    
    @Override
    public Product clone(Product origin) throws IOException {
        return new Product(origin.getDescription(), origin.getPrice());
    }

    @Override
    public Class<Product> getType() {
        return Product.class;
    }

}
