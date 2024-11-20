import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SanitizeInput {
    String message() default "Sanitizing input for security purposes.";
}


public class InputSanitizer {

    // Example: Basic sanitization by removing dangerous characters or patterns
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        // Remove potentially dangerous characters or tags
        return input.replaceAll("<.*?>", "") // Removes HTML tags
                    .replaceAll("[^a-zA-Z0-9 _-]", ""); // Removes non-allowed characters
    }
}

import java.lang.reflect.Field;

public class SanitizationProcessor {

    public static void sanitize(Object object) {
        if (object == null) {
            return;
        }

        Field[] fields = object.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(SanitizeInput.class)) {
                field.setAccessible(true); // Allow access to private fields
                try {
                    Object value = field.get(object);
                    if (value instanceof String) {
                        String sanitizedValue = InputSanitizer.sanitize((String) value);
                        field.set(object, sanitizedValue);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
