package com.mandal.model;

/**
 * Payment method options for contributions.
 */
public enum PaymentMethod {
    CASH,
    UPI,
    BANK_TRANSFER,
    CHEQUE;

    public static PaymentMethod fromString(String value) {
        try {
            return PaymentMethod.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
