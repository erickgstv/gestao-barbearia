package com.erick.myapplication;

public class Agendamento {
    private String id;
    private String nomeCliente;
    private String telefone;
    private String email;
    private String servico;
    private Long data;
    private String nomeBarbeiro;
    private String horario;
    private String status; 

    public Agendamento() {
    }

    public Agendamento(String nomeCliente, String telefone, String email, String servico, String horario, String status, String nomeBarbeiro, Long data) {
        this.nomeCliente = nomeCliente;
        this.telefone = telefone;
        this.email = email;
        this.servico = servico;
        this.horario = horario;
        this.status = status;
        this.nomeBarbeiro = nomeBarbeiro;
        this.data = data;
    }

    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getServico() { return servico; }
    public void setServico(String servico) { this.servico = servico; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getData() { return data; }
    public void setData(Long data) { this.data = data; }
    public String getNomeBarbeiro() { return nomeBarbeiro; }
    public void setNomeBarbeiro(String nomeBarbeiro) { this.nomeBarbeiro = nomeBarbeiro; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
