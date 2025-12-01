package library0.repository;



import library0.Loan;



import java.util.List;

import java.util.Optional;



public interface LoanRepository {

    Loan save(Loan loan);

    List<Loan> findAll();

    Optional<Loan> findById(String id);

    List<Loan> findByUserId(String userId);

}