package com.lasgemelas.model;

import java.math.BigDecimal;

public class CartItem {
    private Producto producto;
    private int cantidad;
    private String tipo; // "venta" or "alquiler"

    public CartItem(Producto producto, int cantidad, String tipo) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.tipo = tipo;
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

    public BigDecimal getSubtotal() {
        BigDecimal precio = "alquiler".equals(tipo) ? producto.getPrecioAlquiler() : producto.getPrecioVenta();
        if (precio == null) {
            return BigDecimal.ZERO;
        }
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }
}
