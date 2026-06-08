package com.erick.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvAgendamentos, rvHorariosRapidos, rvServicosRapidos;
    private AgendamentoAdapter adapter;
    private HorarioRapidoAdapter horarioRapidoAdapter;
    private ServicoRapidoAdapter servicoRapidoAdapter;
    private List<Agendamento> listaAgendamentos;
    private List<Horario> listaHorariosRapidos;
    private List<Servico> listaServicosRapidos;
    private FirebaseFirestore db;

    private TextView tvFaturamentoHoje, tvServicosHoje;
    private double faturamentoTotal = 0.0;
    private int servicosConcluidos = 0;
    private boolean primeiraCarga = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        criarCanalNotificacao();

        tvFaturamentoHoje = findViewById(R.id.tvFaturamentoHoje);
        tvServicosHoje = findViewById(R.id.tvServicosHoje);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        listaAgendamentos = new ArrayList<>();

        rvAgendamentos = findViewById(R.id.rvAgendamentos);
        rvAgendamentos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AgendamentoAdapter(listaAgendamentos);
        rvAgendamentos.setAdapter(adapter);

        rvHorariosRapidos = findViewById(R.id.rvHorariosRapidos);
        rvHorariosRapidos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        listaHorariosRapidos = new ArrayList<>();
        horarioRapidoAdapter = new HorarioRapidoAdapter(listaHorariosRapidos);
        rvHorariosRapidos.setAdapter(horarioRapidoAdapter);

        rvServicosRapidos = findViewById(R.id.rvServicosRapidos);
        rvServicosRapidos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        listaServicosRapidos = new ArrayList<>();
        servicoRapidoAdapter = new ServicoRapidoAdapter(listaServicosRapidos);
        rvServicosRapidos.setAdapter(servicoRapidoAdapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_servicos) {
                startActivity(new Intent(this, GestaoServicosActivity.class));
                return true;
            } else if (id == R.id.nav_horarios) {
                startActivity(new Intent(this, GestaoHorariosActivity.class));
                return true;
            } else if (id == R.id.nav_relatorios) {
                startActivity(new Intent(this, RelatoriosActivity.class));
                return true;
            }
            return true;
        });

        ouvirDadosRapidos();
        ouvirAgendamentos();
        verificarPermissaoNotificacao();

        FloatingActionButton fab = findViewById(R.id.fabAddAgendamento);
        fab.setOnClickListener(v -> mostrarDialogoNovoAgendamento());
    }

    private void mostrarDialogoNovoAgendamento() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Novo Agendamento Manual");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etNome = new EditText(this);
        etNome.setHint("Nome do Cliente");
        layout.addView(etNome);

        final EditText etTelefone = new EditText(this);
        etTelefone.setHint("WhatsApp (00) 90000-0000");
        layout.addView(etTelefone);

        final Spinner spServico = new Spinner(this);
        List<String> nomesServicos = listaServicosRapidos.stream()
                .map(s -> s.getNome() + " - R$ " + String.format("%.2f", s.getPreco()))
                .collect(Collectors.toList());
        ArrayAdapter<String> adapterServico = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nomesServicos);
        spServico.setAdapter(adapterServico);
        layout.addView(spServico);

        final Spinner spHorario = new Spinner(this);
        List<String> horasDisponiveis = listaHorariosRapidos.stream()
                .filter(Horario::isDisponivel)
                .map(Horario::getHora)
                .collect(Collectors.toList());
        ArrayAdapter<String> adapterHorario = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, horasDisponiveis);
        spHorario.setAdapter(adapterHorario);
        layout.addView(spHorario);

        builder.setView(layout);

        builder.setPositiveButton("Agendar", (dialog, which) -> {
            String nome = etNome.getText().toString().trim();
            String tel = etTelefone.getText().toString().trim();
            String servicoSel = spServico.getSelectedItem() != null ? spServico.getSelectedItem().toString() : "";
            String horaSel = spHorario.getSelectedItem() != null ? spHorario.getSelectedItem().toString() : "";

            if (nome.isEmpty() || tel.isEmpty() || servicoSel.isEmpty() || horaSel.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            salvarAgendamentoManual(nome, tel, servicoSel, horaSel);
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void salvarAgendamentoManual(String nome, String tel, String servico, String hora) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("nomeCliente", nome);
        data.put("telefone", tel);
        data.put("servico", servico);
        data.put("horario", hora);
        data.put("data", System.currentTimeMillis());
        data.put("status", "Pendente");

        db.collection("agendamentos").add(data)
            .addOnSuccessListener(doc -> {
                db.collection("config_horarios")
                    .whereEqualTo("hora", hora)
                    .get()
                    .addOnSuccessListener(snap -> {
                        if (!snap.isEmpty()) {
                            snap.getDocuments().get(0).getReference().update("disponivel", false);
                        }
                    });
                Toast.makeText(this, "Agendado!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar", Toast.LENGTH_SHORT).show());
    }

    private void verificarPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void ouvirAgendamentos() {
        db.collection("agendamentos")
            .orderBy("data", Query.Direction.DESCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) return;
                
                if (value != null) {
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED && !primeiraCarga) {
                            try {
                                Agendamento novo = dc.getDocument().toObject(Agendamento.class);
                                notificarNovoAgendamento(novo);
                            } catch (Exception ignored) {}
                        }
                    }
                    primeiraCarga = false;

                    listaAgendamentos.clear();
                    faturamentoTotal = 0;
                    servicosConcluidos = 0;

                    for (QueryDocumentSnapshot doc : value) {
                        try {
                            Agendamento agendamento = doc.toObject(Agendamento.class);
                            agendamento.setId(doc.getId());
                            listaAgendamentos.add(agendamento);

                            if ("Concluído".equals(agendamento.getStatus())) {
                                servicosConcluidos++;
                                faturamentoTotal += extrairPreco(agendamento.getServico());
                            }
                        } catch (Exception ignored) {}
                    }
                    adapter.notifyDataSetChanged();
                    atualizarResumoCapa();
                    salvarRelatorioNoFirestore();
                }
            });
    }

    private void criarCanalNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("agendamentos", "Agendamentos", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notificações de novos agendamentos");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void notificarNovoAgendamento(Agendamento agendamento) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "agendamentos")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Novo Agendamento!")
                .setContentText(agendamento.getNomeCliente() + " agendou às " + agendamento.getHorario())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private double extrairPreco(String servicoText) {
        try {
            if (servicoText.contains("R$")) {
                String precoStr = servicoText.split("R\\$")[1].trim().replace(",", ".");
                return Double.parseDouble(precoStr);
            }
        } catch (Exception e) {
            return 0.0;
        }
        return 0.0;
    }

    private void atualizarResumoCapa() {
        if (tvFaturamentoHoje != null) {
            tvFaturamentoHoje.setText(String.format(java.util.Locale.getDefault(), "R$ %.2f", faturamentoTotal));
        }
        if (tvServicosHoje != null) {
            tvServicosHoje.setText(String.valueOf(servicosConcluidos));
        }
    }

    private void salvarRelatorioNoFirestore() {
        String dataHoje = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        java.util.Map<String, Object> relatorio = new java.util.HashMap<>();
        relatorio.put("data", dataHoje);
        relatorio.put("faturamento", faturamentoTotal);
        relatorio.put("totalServicos", servicosConcluidos);
        relatorio.put("timestamp", com.google.firebase.Timestamp.now());

        db.collection("relatorios_diarios").document(dataHoje).set(relatorio);
    }

    private void ouvirDadosRapidos() {
        db.collection("config_horarios").orderBy("hora")
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    android.util.Log.e("FirestoreError", "Erro ao ouvir horários rápidos", error);
                    return;
                }
                if (value != null) {
                    listaHorariosRapidos.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Horario h = doc.toObject(Horario.class);
                        h.setId(doc.getId());
                        listaHorariosRapidos.add(h);
                    }
                    horarioRapidoAdapter.notifyDataSetChanged();
                }
            });

        db.collection("servicos").orderBy("nome")
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    android.util.Log.e("FirestoreError", "Erro ao ouvir serviços rápidos", error);
                    return;
                }
                if (value != null) {
                    listaServicosRapidos.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Servico s = doc.toObject(Servico.class);
                        s.setId(doc.getId());
                        listaServicosRapidos.add(s);
                    }
                    servicoRapidoAdapter.notifyDataSetChanged();
                }
            });
    }
}
