package hse.java.lectures.testing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("JUnit 5: жизненный цикл и базовые проверки")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JUnitBasicsDemoTest {

    private Calculator calc;

    @BeforeAll
    static void beforeAll() {
        // выполняется один раз перед всеми тестами класса
    }

    @BeforeEach
    void setUp() {
        calc = new Calculator();
    }

    @AfterEach
    void tearDown() {
        // здесь закрываем ресурсы: сокеты, файлы, БД-сессии
    }

    @Test
    @Order(1)
    @DisplayName("сложение положительных чисел")
    void addPositive() {
        assertEquals(5, calc.add(2, 3));
    }

    @Test
    @Order(2)
    @DisplayName("деление на ноль кидает ArithmeticException")
    void divideByZeroThrows() {
        ArithmeticException ex = assertThrows(
                ArithmeticException.class,
                () -> calc.divide(10, 0));
        assertTrue(ex.getMessage().contains("zero"));
    }

    @Test
    @Order(3)
    @DisplayName("assertAll выполняет все проверки, даже если первая упала")
    void assertAllRunsAll() {
        assertAll("calculator",
                () -> assertEquals(4, calc.add(2, 2)),
                () -> assertEquals(0, calc.add(-1, 1)),
                () -> assertTrue(calc.isPrime(7))
        );
    }

    @Test
    @Disabled("демонстрация: тест отключён, но виден в отчёте")
    void disabledExample() {
        fail("не должен выполняться");
    }
}