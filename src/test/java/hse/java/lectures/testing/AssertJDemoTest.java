package hse.java.lectures.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AssertJ: fluent-проверки")
class AssertJDemoTest {

    @Test
    void strings() {
        String greeting = "Hello, world!";
        assertThat(greeting)
                .isNotEmpty()
                .startsWith("Hello")
                .endsWith("!")
                .contains("world");
    }

    @Test
    void collections() {
        List<String> names = List.of("Alice", "Bob", "Charlie");
        assertThat(names)
                .hasSize(3)
                .contains("Bob")
                .doesNotContain("Eve")
                .containsExactly("Alice", "Bob", "Charlie");
    }

    @Test
    void objects() {
        User u = new User(1, "Alice", "a@x.com", true);
        assertThat(u)
                .extracting(User::name, User::email, User::active)
                .containsExactly("Alice", "a@x.com", true);
    }

    @Test
    void exceptions() {
        assertThatThrownBy(() -> { throw new IllegalStateException("boom: 42"); })
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("boom")
                .hasMessageMatching(".*\\d+.*");
    }

    @Test
    void maps() {
        Map<String, Integer> scores = Map.of("Alice", 90, "Bob", 85);
        assertThat(scores)
                .hasSize(2)
                .containsEntry("Alice", 90)
                .containsKey("Bob");
    }
}
