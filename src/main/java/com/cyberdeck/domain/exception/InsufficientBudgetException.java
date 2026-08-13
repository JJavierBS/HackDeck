package com.cyberdeck.domain.exception;

public class InsufficientBudgetException extends RuntimeException {

    public InsufficientBudgetException(int available, int cost) {
        super("Presupuesto insuficiente: quedan " + available + " y la accion cuesta " + cost);
    }
}
