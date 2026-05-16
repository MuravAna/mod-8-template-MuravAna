---
marp: true
theme: default
paginate: true
size: 16:9
header: 'Java Extended · Модуль 8 · Лекция 4'
footer: 'Тестирование на JVM'
style: |
  section { font-size: 24px; }
  pre { font-size: 18px; }
  code { font-size: 18px; }
  h1 { color: #1a5490; }
  h2 { color: #1a5490; }
---

# Тестирование на JVM
### JUnit 5 · AssertJ · Mockito · Spring Boot Test

Лекция 4 · Модуль 8 · Java Extended

---

## План лекции

1. **Зачем** тестировать и какие бывают тесты
2. **JUnit 5** — жизненный цикл, параметризация, теги
3. **AssertJ** — fluent-assertions
4. **Mockito** — моки, stub'ы, verify
5. **Spring Boot Test** — MockMvc, срезы контекста
6. **HTTP-клиенты** — MockRestServiceServer, WireMock, Retrofit, REST Assured
7. **Покрытие** и принципы хорошего теста
8. **TDD**-цикл
9. **Практическое занятие** — тесты для реального API

> Примеры кода: `src/main/java/hse/java/lectures/testing/` и `src/test/java/hse/java/lectures/testing/`

---

# 1. Зачем тестировать

---

## Зачем

- **Регрессии** — изменение в одном месте не ломает другое
- **Документация поведения** — тесты показывают, как код должен работать
- **Безопасный рефакторинг** — можно перекраивать внутренности
- **Дизайн-фидбек** — если код тяжело тестировать, он плохо спроектирован

Стоимость бага растёт по фазам: **dev → review → CI → staging → prod**.
Тесты ловят его на самой дешёвой стадии.

---

## Пирамида тестов

```
                   
                  / \
                 /e2e\         мало, медленные, хрупкие
                /-----\
               /integr.\      средне, поднимают контекст
              /---------\
             /   unit    \    много, быстрые, изолированные
            /-------------\
```

- **Unit** — один класс, без I/O, миллисекунды
- **Integration** — несколько компонентов или Spring-контекст
- **E2E** — весь стек, реальные клиенты, реальные БД

В этом курсе у вас в основном **integration black-box** тесты (через REST/STOMP),
а юнит-уровень через Mockito — там, где нужно изолировать сервисный слой.

---

# 2. JUnit 5

---

## Жизненный цикл

```java
class CalculatorTest {
    private Calculator calc;

    @BeforeAll  static void beforeAll() { /* 1 раз на класс */ }
    @BeforeEach void setUp()           { calc = new Calculator(); }
    @AfterEach  void tearDown()        { /* закрываем ресурсы */ }
    @AfterAll   static void afterAll() { /* 1 раз на класс */ }

    @Test
    void addPositive() {
        assertEquals(5, calc.add(2, 3));
    }
}
```

→ см. `JUnitBasicsDemoTest.java`

---

## Базовые assert'ы

```java
assertEquals(expected, actual);
assertTrue(condition);
assertNull(value);
assertSame(obj1, obj2);          // ссылочное равенство

// Проверка исключения
ArithmeticException ex = assertThrows(
    ArithmeticException.class,
    () -> calc.divide(10, 0));
assertTrue(ex.getMessage().contains("zero"));

// Группировка — все assert'ы выполняются, даже если первый упал
assertAll("calculator",
    () -> assertEquals(4, calc.add(2, 2)),
    () -> assertTrue(calc.isPrime(7))
);
```

---

## Удобные аннотации

| Аннотация | Зачем |
|-----------|-------|
| `@DisplayName("...")` | Человеческое имя в отчёте |
| `@Nested` | Сгруппировать тесты внутри класса |
| `@TestMethodOrder` + `@Order(n)` | Зафиксировать порядок (когда нужно) |
| `@Disabled("причина")` | Временно отключить |
| `@Timeout(value=1, unit=SECONDS)` | Падать, если тест медленный |
| `@RepeatedTest(10)` | Прогнать N раз (для flaky-проверки) |

---

## Параметризованные тесты

```java
// Один параметр
@ParameterizedTest(name = "{0} — простое число")
@ValueSource(ints = {2, 3, 5, 7, 11, 13})
void primes(int n) { assertTrue(calc.isPrime(n)); }

// Несколько параметров
@ParameterizedTest(name = "{0} + {1} = {2}")
@CsvSource({"1, 1, 2", "2, 3, 5", "-1, 1, 0"})
void addCsv(int a, int b, int expected) {
    assertEquals(expected, calc.add(a, b));
}

// Произвольные объекты — через метод-провайдер
@ParameterizedTest
@MethodSource("divisionCases")
void divideMethodSource(int a, int b, int expected) { ... }

static Stream<Arguments> divisionCases() {
    return Stream.of(Arguments.of(10, 2, 5), Arguments.of(9, 3, 3));
}
```

→ см. `ParameterizedDemoTest.java`

---

## `@Tag` — фильтрация тестов

```java
@Tag("chat-stomp-1")
class ChatStompRestTest { ... }
```

```bash
mvn -B -Dgroups=chat-stomp-1 test
```

В **этом проекте** CI grading устроен именно так:
- Каждое задание = несколько групп (`chat-stomp-1`, `chat-stomp-2`, …)
- `.github/tag-weights.json` задаёт баллы за группу
- Surefire считает passed/ran → `(passed/ran) * weight`

**Без `@Tag` тест не попадёт в grading.**

---

# 3. AssertJ

---

## Зачем AssertJ

Цепочка проверок читается как предложение и даёт детальные сообщения об ошибках.

```java
import static org.assertj.core.api.Assertions.*;

assertThat(greeting)
    .isNotEmpty()
    .startsWith("Hello")
    .endsWith("!")
    .contains("world");

assertThat(names)
    .hasSize(3)
    .contains("Bob")
    .doesNotContain("Eve")
    .containsExactly("Alice", "Bob", "Charlie");

assertThatThrownBy(() -> service.register("", "x"))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("blank");
```

→ см. `AssertJDemoTest.java`

---

## AssertJ vs стандарт

```java
// JUnit
assertEquals(3, list.size());
assertTrue(list.contains("Bob"));
assertFalse(list.contains("Eve"));

// AssertJ — то же самое
assertThat(list)
    .hasSize(3)
    .contains("Bob")
    .doesNotContain("Eve");
```

При падении первого варианта вы получите `expected: <3> but was: <2>`.
AssertJ скажет: `Expected size: 3 but was: 2 in: ["Alice", "Charlie"]`.

> AssertJ уже подтянут через `spring-boot-starter-test`.

---

# 4. Mockito

---

## Зачем моки

Юнит-тест должен быть **изолирован**. Если ваш `UserService` зависит от:
- `UserRepository` (БД)
- `EmailGateway` (внешний сервис)

— реальные подставлять нельзя: медленно, нестабильно, требует инфраструктуры.

**Мок** = объект, который притворяется зависимостью:
- На вызов `repo.findById(42)` возвращает то, что мы скажем
- Запоминает все вызовы — можно потом проверить

---

## Базовый паттерн: arrange / act / assert

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository repo;
    @Mock EmailGateway email;
    @InjectMocks UserService service;   // получит моки в конструктор

    @Test
    void registerHappyPath() {
        // arrange — настраиваем поведение моков
        when(repo.existsByEmail("a@x.com")).thenReturn(false);
        when(repo.save(any(User.class)))
            .thenReturn(new User(42L, "Alice", "a@x.com", true));

        // act
        User result = service.register("Alice", "a@x.com");

        // assert — возвращаемое значение
        assertThat(result.id()).isEqualTo(42L);

        // assert — побочные эффекты
        verify(email).send(eq("a@x.com"), eq("Welcome"), contains("Alice"));
        verify(repo, times(1)).save(any());
    }
}
```

---

## `verify` — проверка взаимодействий

```java
verify(mock).method(args);              // ровно 1 раз
verify(mock, times(3)).method(args);    // ровно 3 раза
verify(mock, never()).method(any());    // ни разу
verify(mock, atLeast(2)).method(any());

verifyNoInteractions(mock);             // мок вообще не дёргали
verifyNoMoreInteractions(mock);         // ничего сверх уже проверенного
```

Матчеры: `any()`, `eq("x")`, `anyString()`, `argThat(p -> p.length() > 3)`.

**Правило:** если используешь матчер хоть для одного аргумента, используй для всех.

---

## `ArgumentCaptor` — поймать аргумент

Когда нужно проверить **что именно** ушло в зависимость:

```java
service.register("Bob", "b@x.com");

ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
verify(repo).save(captor.capture());

User saved = captor.getValue();
assertThat(saved.name()).isEqualTo("Bob");
assertThat(saved.email()).isEqualTo("b@x.com");
assertThat(saved.active()).isTrue();
```

→ см. `MockitoDemoTest.java`

---

## Stub / Mock / Spy

- **Stub** — отвечает заранее заданными значениями. `when(...).thenReturn(...)`
- **Mock** — stub + запоминает вызовы для `verify`
- **Spy** — обёртка над **реальным** объектом; по умолчанию вызывает реальные методы, можно переопределить отдельные.

```java
List<String> spy = spy(new ArrayList<>());
spy.add("x");                        // реальный add
when(spy.size()).thenReturn(100);    // переопределили
```

> Spy — последнее средство. Если без него никак — обычно дизайн не очень.

---

## Когда **не** мокать

- **Value-объекты** (record'ы, DTO) — мокать `User` нет смысла, создайте `new User(...)`
- **Стандартную библиотеку** (`String`, `List`, `Map`)
- **Простую логику без I/O** — пусть исполняется реально
- **Финальные классы и статические методы** — технически возможно, но обычно указывает на проблемы дизайна

Если ваш SUT нужно обвесить пятью моками — он делает слишком много.

---

# 5. Spring Boot Test

---

## Варианты загрузки контекста

| Аннотация | Что грузит | Скорость |
|-----------|------------|----------|
| `@SpringBootTest` | Весь контекст | медленно |
| `@WebMvcTest` | Только web-слой | быстро |
| `@DataJpaTest` | JPA + H2 | средне |
| `@JsonTest` | Сериализация Jackson | быстро |
| без Spring-аннотаций | Ничего | мгновенно |

**Правило:** бери минимально необходимое.
- Тест сервиса с моками — без Spring вообще
- Тест контроллера с моком сервиса — `@WebMvcTest`
- E2E проверка через реальный HTTP — `@SpringBootTest(webEnvironment = RANDOM_PORT)`

---

## MockMvc — тестирование REST без реального сервера

```java
@SpringBootTest(classes = GreetingApplication.class)
@AutoConfigureMockMvc
class GreetingMockMvcTest {

    @Autowired MockMvc mockMvc;

    @Test
    void helloReturnsJson() throws Exception {
        mockMvc.perform(get("/hello").param("name", "Alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Hello, Alice!"));
    }

    @Test
    void helloBlankNameReturns400() throws Exception {
        mockMvc.perform(get("/hello").param("name", "   "))
            .andExpect(status().is4xxClientError());
    }
}
```

→ см. `MockMvcDemoTest.java`

---

## Важные нюансы в **этом** проекте

```java
@SpringBootTest(classes = ChatStompApplication.class,    // ← обязательно явно
                webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class ChatStompRestTest { ... }
```

- `classes = ...` — иначе сканируется весь classpath, ловите `BeanDefinitionStore`
- `@DirtiesContext` — контекст пересоздаётся между тестами (state-leak в чатах)
- Для STOMP: `connectAsync()` вместо deprecated `connect()`, `MappingJackson2MessageConverter` обязателен для JSON
- WebSocket: `setAllowedOriginPatterns("*")` вместо `setAllowedOrigins("*")` (CORS + SockJS)

---

## `@MockBean` — подменить бин в контексте

```java
@WebMvcTest(GreetingApplication.GreetingController.class)
class GreetingControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean SomeService service;   // настоящий бин заменён моком

    @Test
    void getsMockedValue() throws Exception {
        when(service.compute()).thenReturn(42);
        mockMvc.perform(get("/x"))
            .andExpect(jsonPath("$.value").value(42));
    }
}
```

> `@MockBean` в Spring Boot 3.4+ помечен deprecated в пользу `@MockitoBean` — но в 3.4.5 оба работают.

---

# 6. HTTP-клиенты

---

## Проблема

Ваш код ходит во внешний API (REST Countries). Как тестировать?

- **Реальные запросы** — медленно, flaky, может не работать в CI
- **Мок всего HTTP-клиента** — не проверяет сборку URL, парсинг ответа, обработку ошибок

**Решение:** поднять фейковый HTTP-сервер на момент теста.

---

## MockRestServiceServer — для `RestTemplate`

```java
RestTemplate rest = new RestTemplate();
MockRestServiceServer server = MockRestServiceServer.createServer(rest);

server.expect(requestTo("/name/japan"))
      .andRespond(withSuccess(
          "[{\"name\":{\"common\":\"Japan\"},\"capital\":[\"Tokyo\"]}]",
          MediaType.APPLICATION_JSON));

Country result = new CountriesClient(rest).findByName("japan");

assertThat(result.capital()).isEqualTo("Tokyo");
server.verify();
```

Подходит для практики `rest-countries-tests`.

---

## WireMock — независимо от клиента

```java
@RegisterExtension
static WireMockExtension wm = WireMockExtension.newInstance()
    .options(wireMockConfig().dynamicPort()).build();

@Test
void parsesCountry() {
    wm.stubFor(get("/name/japan").willReturn(okJson("""
        [{"name":{"common":"Japan"},"capital":["Tokyo"]}]
    """)));

    Country result = new CountriesClient(wm.baseUrl()).findByName("japan");

    assertThat(result.capital()).isEqualTo("Tokyo");
    wm.verify(getRequestedFor(urlEqualTo("/name/japan")));
}
```

WireMock работает с **любым** HTTP-клиентом (RestTemplate, WebClient, OkHttp, HttpClient).

---

## Retrofit — типизированный HTTP-клиент

Описываем API как Java-интерфейс с аннотациями — Retrofit генерирует реализацию.

```java
public interface CountriesApi {
    @GET("name/{name}")
    Call<List<Country>> findByName(@Path("name") String name);

    @GET("alpha")
    Call<List<Country>> findByCodes(@Query("codes") String codes);
}

Retrofit retrofit = new Retrofit.Builder()
    .baseUrl("https://restcountries.com/v3.1/")
    .addConverterFactory(JacksonConverterFactory.create())
    .build();

CountriesApi api = retrofit.create(CountriesApi.class);
Response<List<Country>> response = api.findByName("japan").execute();
assertThat(response.code()).isEqualTo(200);
assertThat(response.body()).extracting(Country::name).contains("Japan");
```

Преимущество: компилятор ловит ошибки в сигнатуре, JSON разбирается автоматически.

---

## Retrofit + MockWebServer

Для тестов Retrofit использует `MockWebServer` от OkHttp — поднимает локальный HTTP-сервер.

```java
MockWebServer server = new MockWebServer();
server.start();

server.enqueue(new MockResponse()
    .setResponseCode(200)
    .setBody("""
        [{"name":{"common":"Japan"},"capital":["Tokyo"]}]
    """));

Retrofit retrofit = new Retrofit.Builder()
    .baseUrl(server.url("/"))
    .addConverterFactory(JacksonConverterFactory.create())
    .build();

CountriesApi api = retrofit.create(CountriesApi.class);
Country result = api.findByName("japan").execute().body().get(0);

assertThat(result.capital()).contains("Tokyo");

RecordedRequest request = server.takeRequest();
assertThat(request.getPath()).isEqualTo("/name/japan");

server.shutdown();
```

---

## REST Assured — DSL для HTTP-тестов

Декларативная запись запроса и проверок в одном выражении — удобно для end-to-end тестов REST API.

```java
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Test
void japanHasTokyo() {
    given()
        .baseUri("https://restcountries.com/v3.1")
        .accept(ContentType.JSON)
    .when()
        .get("/name/japan")
    .then()
        .statusCode(200)
        .body("[0].name.common", equalTo("Japan"))
        .body("[0].capital[0]", equalTo("Tokyo"))
        .body("[0].population", greaterThan(100_000_000));
}
```

Чтение: **given** (что подготовили) → **when** (что выполнили) → **then** (что ожидаем).

---

## REST Assured: дополнительные возможности

```java
// Параметры, заголовки, тело
given()
    .baseUri(BASE)
    .header("X-Api-Key", "secret")
    .queryParam("fields", "name,capital")
    .contentType(ContentType.JSON)
    .body(new SearchRequest("japan"))
.when()
    .post("/search")
.then()
    .statusCode(201)
    .header("Location", containsString("/countries/"))
    .time(lessThan(2000L), TimeUnit.MILLISECONDS);

// Извлечение значения для дальнейших проверок
String capital = given().baseUri(BASE)
    .when().get("/name/japan")
    .then().statusCode(200)
    .extract().jsonPath().getString("[0].capital[0]");

assertThat(capital).isEqualTo("Tokyo");
```

JSON-path синтаксис: `name.common`, `[0].capital[0]`, `findAll { it.population > 1e8 }`.

---

## Когда что выбирать

| Инструмент | Подходит для |
|------------|--------------|
| `RestTemplate` + `MockRestServiceServer` | Spring-проект, тестируем свой клиент |
| WireMock | Любой HTTP-клиент; нужны сложные stub'ы и сценарии |
| Retrofit + MockWebServer | Декларативный клиент по интерфейсу; типобезопасность |
| REST Assured | E2E-тесты публичного REST API, читаемый DSL |

Для практики выберите **один** инструмент и пройдите путь от запроса до проверок целиком.

---

# 7. Качество

---

## JaCoCo — покрытие

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution><goals><goal>prepare-agent</goal></goals></execution>
        <execution>
            <id>report</id><phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
    </executions>
</plugin>
```

`mvn test` → отчёт в `target/site/jacoco/index.html`.

**Покрытие — это диагностический инструмент, а не цель.**
100% покрытие на бессмысленных тестах хуже, чем 60% на содержательных.

---

## F.I.R.S.T. — признаки хорошего теста

- **Fast** — миллисекунды; иначе их перестают запускать
- **Independent** — порядок и пропуски не ломают остальные
- **Repeatable** — без зависимостей от времени, сети, файловой системы
- **Self-validating** — pass/fail, без чтения логов глазами
- **Timely** — пишутся вместе с кодом, не «когда-нибудь потом»

---

## Типичные антипаттерны

- **Тестирует реализацию** — ломается при любом рефакторинге без изменения поведения
- **Один тест — много assert'ов про разные сценарии** — сложно понять, что упало
- **Магические `Thread.sleep(1000)`** — flaky, замените на ожидание условия
- **Дублирование setup** — извлеките в `@BeforeEach` или `@Nested`
- **Имя `test1`, `test2`** — нечитаемо, используйте `@DisplayName`
- **Закомментированные тесты** — либо чините, либо удаляйте

---

# 8. TDD: red → green → refactor

---

## Цикл

1. **Red** — пишем тест на ещё не существующее поведение. Он падает.
2. **Green** — минимальный код, чтобы тест прошёл. Можно «грязно».
3. **Refactor** — чистим реализацию, тесты держат сетку безопасности.

**Зачем:**
- Сразу понятно, чего код должен достигать (тест = спецификация)
- Не пишется лишний код «на будущее»
- Получаете тестовое покрытие бесплатно

Не догма. Хорошо работает на чистой логике, хуже — на UI/glue-коде.

---

# Практическое занятие

## Тесты для реального публичного API

---

## Задание

Цель — пройти полный путь тестирования: от изучения чужого REST API
до набора тестов, который ловит регрессии и описывает поведение сервиса.

**Целевой API:** REST Countries — https://restcountries.com/v3.1

Альтернативы (на выбор группы): PokeAPI, Open-Meteo, JSONPlaceholder, Star Wars API.

### Этапы

1. **Изучить API** — endpoints, форматы ответов, коды ошибок
2. **Спроектировать сценарии** — happy path, граничные случаи, ошибки
3. **Реализовать тесты** — на одном из изученных инструментов
4. **Оформить отчёт** — список покрытых сценариев, найденные особенности API

---

## Этап 1 — изучение API

Перед написанием тестов **прочитайте документацию** и поэкспериментируйте через `curl` или Postman.

Что нужно зафиксировать:
- Доступные эндпоинты, методы и параметры
- Структура успешного ответа (поля, типы, обязательность)
- Коды ошибок и их формат (например, что отдаёт API на несуществующую страну)
- Лимиты, кеширование, специфика поведения (регистр, локали, кодировка)

Результат этапа — короткий конспект API в `tasks/classwork/api-tests/api-notes.md`.

---

## Этап 2 — сценарии тестирования

Разделите проверки на категории и подумайте, **что должен ловить каждый тест**.

| Категория | Примеры сценариев |
|-----------|-------------------|
| Happy path | Запрос существующего ресурса возвращает корректные поля |
| Границы данных | Очень длинный/короткий ввод, спецсимволы, юникод |
| Параметры запроса | Фильтры, сортировки, выбор полей (`fields=...`) |
| Ошибки клиента | 400, 404, неподдерживаемый метод |
| Контракт ответа | Обязательные поля присутствуют, типы соответствуют |
| Перформанс (опц.) | Время ответа меньше порога |

Запишите сценарии до кода — это и есть **спецификация**.

---

## Этап 3 — реализация

Стек на выбор (один — основной, остальные опционально для сравнения):

- **REST Assured** — короткий DSL, идеален для публичного API
- **Retrofit + Jackson** — типизированный клиент, дисциплинирует структуру
- **RestTemplate / WebClient** + AssertJ — если ближе Spring-стек

Обязательные требования:
- JUnit 5 + AssertJ (или Hamcrest для REST Assured)
- Минимум **одна** параметризованная серия (`@ParameterizedTest`)
- Отдельный тест на негативный сценарий (404 / невалидный ввод)
- Тесты независимы: порядок и пропуски не ломают остальные

---

## Этап 4 — отчёт

В корне задания — файл `REPORT.md`:

1. **Какой API** выбран и почему
2. **Покрытые сценарии** — таблица из этапа 2 с пометкой «реализовано / пропущено»
3. **Что узнали об API** — неожиданные поведения, неоднозначности документации
4. **Инструмент** — что использовали, что понравилось / не понравилось
5. **Запуск** — команда для прогона тестов

Отчёт — не формальность: показывает, что вы **поняли** тестируемую систему,
а не написали проверки наугад.

---

## Критерии приёмки

- [ ] Не меньше **8 тестовых методов**, осмысленных, не дублирующих друг друга
- [ ] Покрыты как минимум **3 разных эндпоинта** или **3 категории** сценариев
- [ ] Есть параметризованный тест и тест на ошибку
- [ ] Имена тестов читаемы (camelCase + при необходимости `@DisplayName`)
- [ ] `mvn test` проходит локально из чистого clone'а
- [ ] `REPORT.md` заполнен

---

## Полезные ссылки

**Документация:**
- JUnit 5: junit.org/junit5/docs/current/user-guide
- AssertJ: assertj.github.io/doc
- Mockito: site.mockito.org
- Spring Test: docs.spring.io/spring-framework/reference/testing.html
- REST Assured: rest-assured.io
- Retrofit: square.github.io/retrofit
- WireMock: wiremock.org

**Примеры из лекции:**
- `src/main/java/hse/java/lectures/testing/`
- `src/test/java/hse/java/lectures/testing/`

```bash
mvn -B -Dtest='*DemoTest' test
```

---

# Вопросы?