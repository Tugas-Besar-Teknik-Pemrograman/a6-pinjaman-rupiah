package com.p2p.domain.borrower;

public interface BorrowerRepository {
<<<<<<< dev/faqih
    Borrower findById(String id);

=======
    Optional<Borrower> findById(String id);
>>>>>>> main
    void save(Borrower borrower);
}