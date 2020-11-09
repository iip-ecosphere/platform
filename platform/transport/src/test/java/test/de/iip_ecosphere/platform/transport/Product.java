package test.de.iip_ecosphere.platform.transport;

/**
 * A test data class.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Product {

    private String description;
    private double price;

    /**
     * Creates a product instance.
     * 
     * @param description the description
     * @param price the price
     */
    public Product(String description, double price) {
        this.description = description;
        this.price = price;
    }

    /**
     * Returns the description of the product.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the price of the product.
     * 
     * @return the price
     */
    public double getPrice() {
        return price;
    }

}
