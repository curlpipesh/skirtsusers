package me.curlpipesh.users.attribute;

/**
 * @author audrey
 * @since 2/3/16.
 */
public class DoubleAttribute implements Attribute<Double> {
    private double value;
    
    public DoubleAttribute(final double value) {
        this.value = value;
    }
    
    @Override
    public Double get() {
        return value;
    }
    
    @Override
    public Attribute<Double> set(final Double value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    @Override
    public String getType() {
        return "double";
    }
}
