

package org.example.ecomapp.util;

import java.util.function.Function;

public final class FieldUtils {

    private FieldUtils() {
        // Prevent creating instances of this utility class
    }

    // Check whether a String field has text
    public static <T> boolean hasText(
            T model,
            Function<T, String> fieldGetter) {

        String value = fieldGetter.apply(model);

        return value != null && !value.isBlank();
    }

    // Check whether a non-String field has a value
    public static <T, V> boolean hasValue(
            T model,
            Function<T, V> fieldGetter) {

        return fieldGetter.apply(model) != null;
    }
}
