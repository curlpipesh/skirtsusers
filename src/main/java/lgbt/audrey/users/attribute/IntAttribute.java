package lgbt.audrey.users.attribute;

/**
 * @author audrey
 * @since 2/3/16.
 */
public class IntAttribute implements Attribute<Long> {
    private long value;
    
    public IntAttribute(final long value) {
        this.value = value;
    }
    
    @Override
    public Long get() {
        return value;
    }
    
    @Override
    public Attribute<Long> set(final Long value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    @Override
    public String getType() {
        return "int";
    }
}