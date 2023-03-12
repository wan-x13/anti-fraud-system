package antifraud.repository;

import antifraud.entity.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@SuppressWarnings("unused")
public interface TransactionRepository extends CrudRepository<Transaction ,Long> {
    List<Transaction> findAll();
    List<Transaction> findAllByDateBetweenAndNumber(LocalDateTime start, LocalDateTime end, String number);
    List<Transaction> findByNumber(String number);
}
