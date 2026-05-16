# Лекция 4: Тестирование

## Файлы

- `slides.md` — слайды в формате Marp
- Примеры кода:
  - SUT: `src/main/java/hse/java/lectures/testing/`
  - Тесты: `src/test/java/hse/java/lectures/testing/`

## Сборка слайдов

Установка Marp CLI (один раз):

```bash
npm install -g @marp-team/marp-cli
```

Сгенерировать HTML / PDF / PPTX:

```bash
cd lectures/testing
marp slides.md -o slides.html      # HTML (можно открыть в браузере)
marp slides.md -o slides.pdf       # PDF
marp slides.md -o slides.pptx      # PowerPoint
marp -s .                          # live-preview на localhost:8080
```

## Запуск примеров

```bash
mvn -B -Dtest='hse.java.lectures.testing.*' test
```

## Содержание

1. Зачем тестировать, пирамида тестов
2. JUnit 5: жизненный цикл, assert'ы, `@ParameterizedTest`, `@Tag`
3. AssertJ
4. Mockito: `@Mock`, `@InjectMocks`, `verify`, `ArgumentCaptor`
5. Spring Boot Test: `MockMvc`, `@WebMvcTest`, `@MockBean`
6. Тесты HTTP-клиентов: `MockRestServiceServer`, WireMock
7. JaCoCo, F.I.R.S.T., запахи плохих тестов
8. TDD-цикл