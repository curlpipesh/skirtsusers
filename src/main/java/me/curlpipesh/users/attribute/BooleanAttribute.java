package me.curlpipesh.users.attribute;

/**
 * @author audrey
 * @since 2/3/16.
 */
public class BooleanAttribute implements Attribute<Boolean> {
    private boolean value;

    public BooleanAttribute(final boolean value) {
        this.value = value;
    }

    @Override
    public Boolean get() {
        return value;
    }

    @Override
    public Attribute<Boolean> set(final Boolean value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

    @Override
    public String getType() {
        return "boolean";
    }
}
