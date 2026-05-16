package hse.java.lectures.testing;

public class UserService {
    private final UserRepository repo;
    private final EmailGateway email;

    public UserService(UserRepository repo, EmailGateway email) {
        this.repo = repo;
        this.email = email;
    }

    public User register(String name, String emailAddress) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is blank");
        }
        if (repo.existsByEmail(emailAddress)) {
            throw new IllegalStateException("email already used: " + emailAddress);
        }
        User saved = repo.save(new User(0L, name, emailAddress, true));
        email.send(emailAddress, "Welcome", "Hi, " + name + "!");
        return saved;
    }

    public User getOrThrow(long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("no user: " + id));
    }
}