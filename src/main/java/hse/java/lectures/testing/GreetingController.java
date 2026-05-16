package hse.java.lectures.testing;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class GreetingController {

    @GetMapping("/hello")
    public Greeting hello(@RequestParam(name = "name", defaultValue = "world") String name) {
        if (name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is blank");
        }
        return new Greeting("Hello, " + name + "!");
    }
}
