package me.curlpipesh.users.attribute;

/**
 * @author audrey
 * @since 2/3/16.
 */
public class StringAttribute implements Attribute<String> {
    private String value;

    public StringAttribute(final String value) {
        this.value = value;
    }

    @Override
    public String get() {
        return value;
    }

    @Override
    public Attribute<String> set(final String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public String getType() {
        return "string";
    }
}
