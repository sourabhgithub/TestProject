import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SanitizationProcessorTest {

    @Test
    void testSanitization_removesDangerousHtml() {
        // Arrange
        SafePojo pojo = new SafePojo();
        pojo.setSafeField("Hello<script>alert('XSS');</script>World!");

        // Act
        SanitizationProcessor.sanitize(pojo);

        // Assert
        assertEquals("HelloWorld", pojo.getSafeField(), "Sanitization should remove HTML tags and script content.");
    }

    @Test
    void testSanitization_allowsValidInput() {
        // Arrange
        SafePojo pojo = new SafePojo();
        pojo.setSafeField("Hello_World-123");

        // Act
        SanitizationProcessor.sanitize(pojo);

        // Assert
        assertEquals("Hello_World-123", pojo.getSafeField(), "Valid input should remain unchanged.");
    }

    @Test
    void testSanitization_handlesNullInput() {
        // Arrange
        SafePojo pojo = new SafePojo();
        pojo.setSafeField(null);

        // Act
        SanitizationProcessor.sanitize(pojo);

        // Assert
        assertNull(pojo.getSafeField(), "Sanitization should not throw errors or alter null values.");
    }

    @Test
    void testSanitization_removesInvalidCharacters() {
        // Arrange
        SafePojo pojo = new SafePojo();
        pojo.setSafeField("Hello@#$%^&*()<>World!");

        // Act
        SanitizationProcessor.sanitize(pojo);

        // Assert
        assertEquals("HelloWorld", pojo.getSafeField(), "Sanitization should remove disallowed special characters.");
    }
}
