package com.lasgemelas.model;

import java.math.BigDecimal;

import java.io.Serializable;

public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private Producto producto;
    private int cantidad;
    private String tipo; // "venta" or "alquiler"

    private int dias = 1; // Default to 1 day

    public CartItem(Producto producto, int cantidad, String tipo) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.tipo = tipo;
        this.dias = 1;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getDias() {
        return dias;
    }

    public void setDias(int dias) {
        this.dias = dias;
    }

    public BigDecimal getSubtotal() {
        BigDecimal precio = "alquiler".equals(tipo) ? producto.getPrecioAlquiler() : producto.getPrecioVenta();
        if (precio == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = precio.multiply(BigDecimal.valueOf(cantidad));
        if ("alquiler".equals(tipo)) {
            total = total.multiply(BigDecimal.valueOf(dias));
        }
        return total;
    }
}
