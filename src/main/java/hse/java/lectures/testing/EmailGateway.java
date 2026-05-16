package hse.java.lectures.testing;

public interface EmailGateway {
    void send(String to, String subject, String body);
}