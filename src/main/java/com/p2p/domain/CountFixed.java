package com.p2p.domain;

import java.util.ArrayList;

public class CountFixed implements InterestCount {

    @Override
    public ArrayList<Double> count(double principal, double InterestRate, int tenor) {
        ArrayList<Double> Installments = new ArrayList<>();

        double TotalInterest = principal * InterestRate * tenor;    //ini hitung total bunganya
        double TotalDebt = principal + TotalInterest;               //ini hitung total cicilannya
        double MonthlyInstallments = TotalDebt / tenor;             //ini total cicilan per bulannya

        for(int i = 0; i<tenor; i++) {
            Installments.add(MonthlyInstallments);                  //nambahin cicilan per bulan ke list
        }
        return Installments;
    }
}
