package com.p2p.domain;

import java.util.ArrayList;

public class CountSyariah implements InterestCount {
    
    @Override
    public ArrayList<Double> count(double principal, double InterestRate, int tenor) {
        ArrayList<Double> Installments = new ArrayList<>();

        double TotalPayment = principal + InterestRate;
        double MonthlyInstallments = TotalPayment / tenor;

        for(int i = 0; i < tenor; i++) {
           Installments.add(MonthlyInstallments); 
        }

        return Installments;
    }
}
