package com.truebalance.creditiq.enrichment.integration;

public interface CibilClient {

    CibilResult check(String phone);

    record CibilResult(int score, String band, String dbMatchFlag) {}
}
