package hse.java.lectures.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Spring Test: MockMvc через standaloneSetup")
class MockMvcDemoTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new GreetingController())
            .build();

    @Test
    void helloDefaultNameReturnsOk() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, world!"));
    }

    @Test
    void helloWithNameReturnsOk() throws Exception {
        mockMvc.perform(get("/hello").param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, Alice!"));
    }

    @Test
    void helloBlankNameReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/hello").param("name", "   "))
                .andExpect(status().is4xxClientError());
    }
}
