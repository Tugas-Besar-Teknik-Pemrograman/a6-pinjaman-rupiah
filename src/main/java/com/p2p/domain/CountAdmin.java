package com.p2p.domain;

public class CountAdmin {
    public static double calculateFee(double principal, String loanType) {
        if (loanType.equalsIgnoreCase("SYARIAH")) {
            //Fee pasti untuk Syariah
            return 150000;
        } else {
            // 5% fee untuk Fixed dan Float
            return principal * 0.05;
        }
    }
}
