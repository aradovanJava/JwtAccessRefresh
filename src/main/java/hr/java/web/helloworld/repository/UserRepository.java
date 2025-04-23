package hr.java.web.helloworld.repository;

import hr.java.web.helloworld.domain.ApplicationUser;
import hr.java.web.helloworld.domain.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserInfo, Long> {
    UserInfo findByUsername(String username);
}
