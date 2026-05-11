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

    public void tambahPendanaan(String lenderId, Money investasiDiberikan) throws Exception {
        java.math.BigDecimal totalBaru = this.totalTerkumpul.getAmount().add(investasiDiberikan.getAmount());
        
        if (totalBaru.compareTo(this.targetNominal.getAmount()) > 0) {
            throw new Exception("Nominal investasi melebihi target pendanaan");
        }      
          
        this.totalTerkumpul = new Money(totalBaru, this.totalTerkumpul.getCurrency());
      
    public void generateMonthlyBill() {

    }

    public void payInstallment(Money paymentAmount) {

        }

    public void ubahStatus(String status) {

    }

    public String getId() {
        return id;
    }

    public Money getTotalTerkumpul() {
        return totalTerkumpul;
    }
      
    public Money getCurrentMonthBill() { return currentMonthBill; }
    
}