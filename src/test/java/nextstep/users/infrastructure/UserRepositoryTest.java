package nextstep.users.infrastructure;

import nextstep.users.domain.NsUser;
import nextstep.users.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
public class UserRepositoryTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepositoryTest.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new JdbcUserRepository(jdbcTemplate);
    }

    @Test
    void findByUserId() {
        Optional<NsUser> nsUser = userRepository.findByUserId("javajigi");
        assertThat(nsUser.isEmpty()).isFalse();
        LOGGER.debug("NsUser: {}", nsUser.get());
    }

    @Test
    @DisplayName("선생님이 저장되었는지 확인")
    void checkTeacher() {
        Optional<NsUser> nsUser = userRepository.findByUserId("javamaster");
        assertThat(nsUser.isEmpty()).isFalse();
        assertThat(nsUser.get().isTeacher()).isTrue();
    }

}
