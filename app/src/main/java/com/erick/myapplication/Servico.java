package com.erick.myapplication;

public class Servico {
    private String id;
    private String nome;
    private double preco;

    public Servico() {}

    public Servico(String nome, double preco) {
        this.nome = nome;
        this.preco = preco;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }
}
