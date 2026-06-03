package com.erick.myapplication;

public class Horario {
    private String id;
    private String hora;
    private boolean disponivel;

    public Horario() {}

    public Horario(String hora, boolean disponivel) {
        this.hora = hora;
        this.disponivel = disponivel;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }
}
