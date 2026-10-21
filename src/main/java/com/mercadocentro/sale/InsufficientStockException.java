package com.mercadocentro.sale;

public class InsufficientStockException extends RuntimeException {
  public InsufficientStockException(String msg) { super(msg); }
}