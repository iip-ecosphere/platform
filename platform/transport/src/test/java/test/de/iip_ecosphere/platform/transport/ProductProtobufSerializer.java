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
    public byte[] serialize(Product value) throws IOException {
        ProductOuterClass.Product prod = ProductOuterClass.Product.newBuilder()
            .setDescription(value.getDescription())
            .setPrice(value.getPrice())
            .build();
        return prod.toByteArray();
    }

    @Override
    public Product deserialize(byte[] data) throws IOException {
        ProductOuterClass.Product prod = ProductOuterClass.Product.parseFrom(data);
        return new Product(prod.getDescription(), prod.getPrice());
    }

    @Override
    public Class<Product> getType() {
        return Product.class;
    }

}
