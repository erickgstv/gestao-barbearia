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

import java.util.ArrayList;
import java.util.List;

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

        // 1. Lista Principal (Agendamentos)
        rvAgendamentos = findViewById(R.id.rvAgendamentos);
        rvAgendamentos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AgendamentoAdapter(listaAgendamentos);
        rvAgendamentos.setAdapter(adapter);

        // 2. Barra Spotify - Horários Rápidos
        rvHorariosRapidos = findViewById(R.id.rvHorariosRapidos);
        rvHorariosRapidos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        listaHorariosRapidos = new ArrayList<>();
        horarioRapidoAdapter = new HorarioRapidoAdapter(listaHorariosRapidos);
        rvHorariosRapidos.setAdapter(horarioRapidoAdapter);

        // 3. Barra Spotify - Serviços Rápidos
        rvServicosRapidos = findViewById(R.id.rvServicosRapidos);
        rvServicosRapidos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        listaServicosRapidos = new ArrayList<>();
        servicoRapidoAdapter = new ServicoRapidoAdapter(listaServicosRapidos);
        rvServicosRapidos.setAdapter(servicoRapidoAdapter);

        // 4. Navegação Inferior
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
    }

    private void verificarPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void ouvirAgendamentos() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        long inicioHoje = cal.getTimeInMillis();

        db.collection("agendamentos")
            .whereGreaterThanOrEqualTo("data", inicioHoje)
            .orderBy("data", Query.Direction.ASCENDING) 
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    android.util.Log.e("FirestoreError", "Erro ao ouvir agendamentos", error);
                    return;
                }
                if (value != null) {
                    // Detectar se há novos agendamentos para notificar
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED && !primeiraCarga) {
                            Agendamento novo = dc.getDocument().toObject(Agendamento.class);
                            notificarNovoAgendamento(novo);
                        }
                    }
                    primeiraCarga = false;

                    listaAgendamentos.clear(); 
                    faturamentoTotal = 0;
                    servicosConcluidos = 0;

                    for (QueryDocumentSnapshot doc : value) {
                        Agendamento agendamento = doc.toObject(Agendamento.class);
                        agendamento.setId(doc.getId());
                        listaAgendamentos.add(agendamento);

                        if ("Concluído".equals(agendamento.getStatus())) {
                            servicosConcluidos++;
                            faturamentoTotal += extrairPreco(agendamento.getServico());
                        }
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
