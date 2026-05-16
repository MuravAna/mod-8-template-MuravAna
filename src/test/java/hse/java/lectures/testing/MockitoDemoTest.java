package hse.java.lectures.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Mockito: моки, стабы, verify, ArgumentCaptor")
@ExtendWith(MockitoExtension.class)
class MockitoDemoTest {

    @Mock
    UserRepository repo;

    @Mock
    EmailGateway email;

    @InjectMocks
    UserService service;

    @Test
    @DisplayName("register: сохраняет пользователя и отправляет письмо")
    void registerHappyPath() {
        when(repo.existsByEmail("a@x.com")).thenReturn(false);
        when(repo.save(any(User.class)))
                .thenReturn(new User(42L, "Alice", "a@x.com", true));

        User result = service.register("Alice", "a@x.com");

        assertThat(result.id()).isEqualTo(42L);
        assertThat(result.name()).isEqualTo("Alice");

        verify(email).send(eq("a@x.com"), eq("Welcome"), contains("Alice"));
        verify(repo, times(1)).save(any());
        verifyNoMoreInteractions(email);
    }

    @Test
    @DisplayName("register: email уже занят — исключение и письмо НЕ отправлено")
    void registerDuplicateEmail() {
        when(repo.existsByEmail("a@x.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register("Alice", "a@x.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already used");

        verify(repo, never()).save(any());
        verifyNoInteractions(email);
    }

    @Test
    @DisplayName("ArgumentCaptor: какой объект ушёл в save()")
    void argumentCaptorDemo() {
        when(repo.existsByEmail(anyString())).thenReturn(false);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.register("Bob", "b@x.com");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repo).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.name()).isEqualTo("Bob");
        assertThat(saved.email()).isEqualTo("b@x.com");
        assertThat(saved.active()).isTrue();
    }

    @Test
    @DisplayName("getOrThrow: репозиторий пуст — IllegalArgumentException")
    void getOrThrowNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrThrow(99L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
