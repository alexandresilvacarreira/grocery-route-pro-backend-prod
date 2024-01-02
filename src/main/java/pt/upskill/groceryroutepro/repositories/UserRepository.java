package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    User getByEmail(String email);
    User findByConfirmation_Token(String token);

}
