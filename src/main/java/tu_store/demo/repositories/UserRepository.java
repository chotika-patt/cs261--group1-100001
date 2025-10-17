package tu_store.demo.repositories;
import tu_store.demo.models.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Long>  {
    User findFirstByUsername(String username);
    User findFirstByUsernameIgnoreCase(String username);
    User findByUsername(String username);
}
