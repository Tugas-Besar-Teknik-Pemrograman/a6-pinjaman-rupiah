package com.p2p.domain;

import java.util.ArrayList;

public interface InterestCount {
    ArrayList<Double> count(double principal, double InterestRate, int tenor); 
}
