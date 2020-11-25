package io.sharpink.persistence.dao.user;

import io.sharpink.persistence.entity.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserDaoTest {

  @Autowired UserDao userDao;
  @Autowired TestEntityManager entityManager;

  @BeforeEach
  void init() {
    //@formatter:off
    asList(
      User.builder().nickname("TheBitchWhoWantedToBeAQueen").email("cersei.lannister@westeros.org").password("POW3R_15_POW3R").build(),
      User.builder().nickname("PlzDontCutMyHeaaaaadAaargh").email("ned_stark@westeros.org").password("Winteriscoming123").build(),
      User.builder().nickname("Dany T.").email("mother_of_dragons@westeros.org").password("dracarys").build()
    ).forEach(entityManager::persist);
    entityManager.flush();
    //@formatter:on
  }

  @Test
  @DisplayName("Should return user if login exists and password matches")
  void findByCredentials_LoginExistsAndCorrectPassword() {
    // when
    Optional<User> userOptional = userDao.findByCredentials("TheBitchWhoWantedToBeAQueen", "POW3R_15_POW3R");

    // then
    assertThat(userOptional).isPresent();
    User cersei = userOptional.get();
    assertThat(cersei.getNickname()).isEqualTo("TheBitchWhoWantedToBeAQueen");
    assertThat(cersei.getEmail()).isEqualTo("cersei.lannister@westeros.org");
    assertThat(cersei.getPassword()).isEqualTo("POW3R_15_POW3R");
  }

  @Test
  @DisplayName("Should return user if email exists and password matches")
  void findByCredentials_EmailExistsAndCorrectPassword() {
    // when
    Optional<User> userOptional = userDao.findByCredentials("ned_stark@westeros.org", "Winteriscoming123");

    // then
    assertThat(userOptional).isPresent();
    User cersei = userOptional.get();
    assertThat(cersei.getNickname()).isEqualTo("PlzDontCutMyHeaaaaadAaargh");
    assertThat(cersei.getEmail()).isEqualTo("ned_stark@westeros.org");
    assertThat(cersei.getPassword()).isEqualTo("Winteriscoming123");
  }

  @Test
  @DisplayName("Should return an empty Optional if user does not exist")
  void findByCredentials_UserDoesNotExist() {
    // when
    Optional<User> userOptional = userDao.findByCredentials("Arya", "Password123");

    // then
    assertThat(userOptional).isEmpty();
  }

  @Test
  @DisplayName("Should return an empty Optional if user exists but wrong password is passed")
  void findByCredentials_UserExistsButWrongPasswordIsPassed() {
    // when
    Optional<User> userOptional = userDao.findByCredentials("TheBitchWhoWantedToBeAQueen", "$H4M3");

    // then
    assertThat(userOptional).isEmpty();
  }
}
