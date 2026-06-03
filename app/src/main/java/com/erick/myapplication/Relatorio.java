package com.erick.myapplication;

import com.google.firebase.Timestamp;

public class Relatorio {
    private String data;
    private double faturamento;
    private int totalServicos;
    private Timestamp timestamp;

    public Relatorio() {}

    public Relatorio(String data, double faturamento, int totalServicos, Timestamp timestamp) {
        this.data = data;
        this.faturamento = faturamento;
        this.totalServicos = totalServicos;
        this.timestamp = timestamp;
    }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public double getFaturamento() { return faturamento; }
    public void setFaturamento(double faturamento) { this.faturamento = faturamento; }

    public int getTotalServicos() { return totalServicos; }
    public void setTotalServicos(int totalServicos) { this.totalServicos = totalServicos; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
