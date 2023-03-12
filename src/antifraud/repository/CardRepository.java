package antifraud.repository;

import antifraud.entity.Card;
import antifraud.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CardRepository extends CrudRepository<Card, Long > {
    List<Card> findAll();
    List<Card> findAllByIsLockedTrue();
    boolean existsByNumberAndIsLockedTrue(String number);
    Optional<Card> findByNumber(String number);
}
