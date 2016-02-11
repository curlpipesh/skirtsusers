package me.curlpipesh.users.attribute;

import me.curlpipesh.users.user.SkirtsUser;

/**
 * An attribute is a persistent value attached to a {@link SkirtsUser}
 * instance. An example use-case for this is something like tracking whether
 * an online user is muted.
 *
 * @author audrey
 * @since 2/3/16.
 */
@SuppressWarnings("unused")
public interface Attribute<T> {
    T get();

    Attribute<T> set(T value);

    @Override
    String toString();

    String getType();

    @SuppressWarnings("unchecked")
    static Attribute<?> fromString(final String type, final String value) {
        switch(type) {
            case "boolean":
                return new BooleanAttribute(Boolean.valueOf(value));
            case "int":
            case "integer":
            case "long":
                return new IntAttribute(Long.valueOf(value));
            case "double":
            case "float":
                return new DoubleAttribute(Double.valueOf(value));
            case "string":
            case "text":
                return new StringAttribute(value);
            default:
                throw new IllegalArgumentException(String.format("Unknown attribute type: '%s'.", type));
        }
    }
}
