package com.p2p.domain;

import java.util.ArrayList;

public class CountFloat implements InterestCount {
    
    @Override
    public ArrayList<Double> count(double principal, double InterestRate, int tenor) {
        ArrayList<Double> Installments = new ArrayList<>();

        double PrincipalInstallments = principal / tenor; //menghitung cicilan pokok
        double RemainingPrincipal = principal; //sisa pokok di awal sama dengan principal

        for (int i = 0; i < tenor; i++) {
            double MonthlyInterest = RemainingPrincipal * InterestRate;
            double MonthlyInstallments = PrincipalInstallments + MonthlyInterest;

            Installments.add(MonthlyInstallments);
            RemainingPrincipal = RemainingPrincipal - PrincipalInstallments;
        }
        return Installments;
    }
}
