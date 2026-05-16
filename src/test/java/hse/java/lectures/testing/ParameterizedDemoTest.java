package hse.java.lectures.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JUnit 5: параметризованные тесты")
class ParameterizedDemoTest {

    private final Calculator calc = new Calculator();

    @ParameterizedTest(name = "{0} — простое число")
    @ValueSource(ints = {2, 3, 5, 7, 11, 13})
    void primes(int n) {
        assertTrue(calc.isPrime(n));
    }

    @ParameterizedTest(name = "{0} + {1} = {2}")
    @CsvSource({
            "1, 1, 2",
            "2, 3, 5",
            "-1, 1, 0",
            "0, 0, 0"
    })
    void addCsv(int a, int b, int expected) {
        assertEquals(expected, calc.add(a, b));
    }

    @ParameterizedTest
    @MethodSource("divisionCases")
    void divideMethodSource(int a, int b, int expected) {
        assertEquals(expected, calc.divide(a, b));
    }

    static Stream<Arguments> divisionCases() {
        return Stream.of(
                Arguments.of(10, 2, 5),
                Arguments.of(9, 3, 3),
                Arguments.of(7, 2, 3) // целочисленное деление
        );
    }
}