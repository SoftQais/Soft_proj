package library0.repository;

import library0.Fine;

import java.util.List;
import java.util.Optional;

public interface FineRepository {

    Fine save(Fine fine);

    List<Fine> findAll();

    List<Fine> findByUserId(String userId);

    Optional<Fine> findByLoanId(String loanId);
}