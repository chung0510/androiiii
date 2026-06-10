package com.smartbox.backend.service;

public class PriceUtils {

    public static long getUnitPrice(String rentType) {
        return switch (rentType) {
            case "HOUR" -> 30000;
            case "DAY" -> 150000;
            case "MONTH" -> 1000000;
            case "ONE_TIME" -> 15000;
            default -> 0;
        };
    }
}