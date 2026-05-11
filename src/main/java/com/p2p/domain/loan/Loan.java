package com.p2p.domain.loan;

import com.p2p.domain.valueobject.Money;
import com.p2p.domain.loan.strategy.InterestCalculationStrategy;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Loan {
    private String id;
    private String borrowerId;
    private Money targetNominal;
    private Money remainingPrincipal;
    private int tenor;
    private String status;
    
    private InterestCalculationStrategy interestStrategy;
    private Money currentMonthBill;

    public Loan(String id, String borrowerId, Money targetNominal, int tenor) {

    }

    public void setInterestStrategy(InterestCalculationStrategy strategy) {

    }

    public void generateMonthlyBill() {

    }

    public void payInstallment(Money paymentAmount) {

        }

    public void ubahStatus(String status) {

    }

    public String getId() {
        return id;
    }

    public Money getCurrentMonthBill() { return currentMonthBill; }
    
}