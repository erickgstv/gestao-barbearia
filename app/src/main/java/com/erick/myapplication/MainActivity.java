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

    private RecyclerView rvAgendamentos, rvHorarios, rvServicos;
    private AgendamentoAdapter adapter;
    private HorarioRapidoAdapter horarioAdapter;
    private ServicoRapidoAdapter servicoAdapter;
    private List<Agendamento> agendamentos;
    private List<Horario> horarios;
    private List<Servico> servicos;
    private FirebaseFirestore db;

    private TextView tvFaturamento, tvTotalServicos;
    private double faturamento = 0.0;
    private int concluidos = 0;
    private boolean carregando = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        configurarNotificacoes();

        tvFaturamento = findViewById(R.id.tvFaturamentoHoje);
        tvTotalServicos = findViewById(R.id.tvServicosHoje);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        agendamentos = new ArrayList<>();

        rvAgendamentos = findViewById(R.id.rvAgendamentos);
        rvAgendamentos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AgendamentoAdapter(agendamentos);
        rvAgendamentos.setAdapter(adapter);

        rvHorarios = findViewById(R.id.rvHorariosRapidos);
        rvHorarios.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        horarios = new ArrayList<>();
        horarioAdapter = new HorarioRapidoAdapter(horarios);
        rvHorarios.setAdapter(horarioAdapter);

        rvServicos = findViewById(R.id.rvServicosRapidos);
        rvServicos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        servicos = new ArrayList<>();
        servicoAdapter = new ServicoRapidoAdapter(servicos);
        rvServicos.setAdapter(servicoAdapter);

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_servicos) {
                startActivity(new Intent(this, GestaoServicosActivity.class));
            } else if (id == R.id.nav_horarios) {
                startActivity(new Intent(this, GestaoHorariosActivity.class));
            } else if (id == R.id.nav_relatorios) {
                startActivity(new Intent(this, RelatoriosActivity.class));
            }
            return true;
        });

        fetchConfig();
        observeAgendamentos();
        checkPermissions();

        findViewById(R.id.fabAddAgendamento).setOnClickListener(v -> showManualAddDialog());
    }

    private void showManualAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Novo Agendamento");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etNome = new EditText(this);
        etNome.setHint("Nome do Cliente");
        layout.addView(etNome);

        final EditText etTel = new EditText(this);
        etTel.setHint("Telefone");
        layout.addView(etTel);

        final Spinner spServico = new Spinner(this);
        List<String> listServicos = servicos.stream()
                .map(s -> s.getNome() + " - R$ " + String.format("%.2f", s.getPreco()))
                .collect(Collectors.toList());
        spServico.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listServicos));
        layout.addView(spServico);

        final Spinner spHora = new Spinner(this);
        List<String> listHoras = horarios.stream()
                .filter(Horario::isDisponivel)
                .map(Horario::getHora)
                .collect(Collectors.toList());
        spHora.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listHoras));
        layout.addView(spHora);

        builder.setView(layout);
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String nome = etNome.getText().toString().trim();
            String tel = etTel.getText().toString().trim();
            String serv = spServico.getSelectedItem() != null ? spServico.getSelectedItem().toString() : "";
            String hora = spHora.getSelectedItem() != null ? spHora.getSelectedItem().toString() : "";

            if (!nome.isEmpty() && !tel.isEmpty() && !serv.isEmpty() && !hora.isEmpty()) {
                saveManual(nome, tel, serv, hora);
            } else {
                Toast.makeText(this, "Preencha tudo!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Sair", null);
        builder.show();
    }

    private void saveManual(String nome, String tel, String serv, String hora) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("nomeCliente", nome);
        map.put("telefone", tel);
        map.put("servico", serv);
        map.put("horario", hora);
        map.put("data", System.currentTimeMillis());
        map.put("status", "Pendente");

        db.collection("agendamentos").add(map)
            .addOnSuccessListener(doc -> {
                db.collection("config_horarios").whereEqualTo("hora", hora).get()
                    .addOnSuccessListener(snap -> {
                        if (!snap.isEmpty()) snap.getDocuments().get(0).getReference().update("disponivel", false);
                    });
                Toast.makeText(this, "Salvo!", Toast.LENGTH_SHORT).show();
            });
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void observeAgendamentos() {
        db.collection("agendamentos")
            .orderBy("data", Query.Direction.DESCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null || value == null) return;
                
                for (DocumentChange dc : value.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED && !carregando) {
                        try {
                            notificarNovoAgendamento(dc.getDocument().toObject(Agendamento.class));
                        } catch (Exception ignored) {}
                    }
                }
                carregando = false;

                agendamentos.clear();
                faturamento = 0;
                concluidos = 0;

                for (QueryDocumentSnapshot doc : value) {
                    try {
                        Agendamento a = doc.toObject(Agendamento.class);
                        a.setId(doc.getId());
                        agendamentos.add(a);

                        if ("Concluído".equals(a.getStatus())) {
                            concluidos++;
                            faturamento += extrairPreco(a.getServico());
                        }
                    } catch (Exception ignored) {}
                }
                adapter.notifyDataSetChanged();
                updateUI();
                saveDailyReport();
            });
    }

    private void configurarNotificacoes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("agendamentos", "Agendamentos", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void notificarNovoAgendamento(Agendamento a) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "agendamentos")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Novo Agendamento")
                .setContentText(a.getNomeCliente() + " às " + a.getHorario())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private double extrairPreco(String text) {
        try {
            if (text.contains("R$")) {
                return Double.parseDouble(text.split("R\\$")[1].trim().replace(",", "."));
            }
        } catch (Exception ignored) {}
        return 0.0;
    }

    private void updateUI() {
        if (tvFaturamento != null) tvFaturamento.setText(String.format(java.util.Locale.getDefault(), "R$ %.2f", faturamento));
        if (tvTotalServicos != null) tvTotalServicos.setText(String.valueOf(concluidos));
    }

    private void saveDailyReport() {
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        java.util.Map<String, Object> report = new java.util.HashMap<>();
        report.put("data", today);
        report.put("faturamento", faturamento);
        report.put("totalServicos", concluidos);
        report.put("timestamp", com.google.firebase.Timestamp.now());
        db.collection("relatorios_diarios").document(today).set(report);
    }

    private void fetchConfig() {
        db.collection("config_horarios").orderBy("hora")
            .addSnapshotListener((v, e) -> {
                if (v == null) return;
                horarios.clear();
                for (QueryDocumentSnapshot doc : v) {
                    Horario h = doc.toObject(Horario.class);
                    h.setId(doc.getId());
                    horarios.add(h);
                }
                horarioAdapter.notifyDataSetChanged();
            });

        db.collection("servicos").orderBy("nome")
            .addSnapshotListener((v, e) -> {
                if (v == null) return;
                servicos.clear();
                for (QueryDocumentSnapshot doc : v) {
                    Servico s = doc.toObject(Servico.class);
                    s.setId(doc.getId());
                    servicos.add(s);
                }
                servicoAdapter.notifyDataSetChanged();
            });
    }
}
