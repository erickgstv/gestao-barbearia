package com.erick.myapplication;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class GestaoHorariosActivity extends AppCompatActivity {

    private RecyclerView rvHorarios;
    private HorarioAdapter adapter;
    private List<Horario> listaHorarios;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestao_horarios);

        db = FirebaseFirestore.getInstance();
        listaHorarios = new ArrayList<>();
        rvHorarios = findViewById(R.id.rvHorarios);
        rvHorarios.setLayoutManager(new LinearLayoutManager(this));
        
        // Agora passando o listener para permitir edição ao clicar
        adapter = new HorarioAdapter(listaHorarios, this::mostrarDialogoEditar);
        rvHorarios.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddHorario);
        fab.setOnClickListener(v -> mostrarDialogoAdicionar());

        ouvirHorarios();
    }

    private String formatarHora(String hora) {
        try {
            hora = hora.replace(" ", "");
            if (!hora.contains(":")) {
                if (hora.length() <= 2) {
                    int h = Integer.parseInt(hora);
                    if (h >= 0 && h <= 23) return String.format(java.util.Locale.getDefault(), "%02d:00", h);
                } else if (hora.length() == 3) {
                    return "0" + hora.charAt(0) + ":" + hora.substring(1);
                } else if (hora.length() == 4) {
                    return hora.substring(0, 2) + ":" + hora.substring(2);
                }
            } else {
                String[] partes = hora.split(":");
                if (partes.length == 2) {
                    int h = Integer.parseInt(partes[0]);
                    int m = Integer.parseInt(partes[1]);
                    return String.format(java.util.Locale.getDefault(), "%02d:%02d", h, m);
                }
            }
        } catch (Exception ignored) {}
        return hora;
    }

    private void mostrarDialogoEditar(Horario horario) {
        EditText etHora = new EditText(this);
        etHora.setText(horario.getHora());
        etHora.setHint("Ex: 09:00");
        etHora.setPadding(50, 40, 50, 40);

        new AlertDialog.Builder(this)
                .setTitle("Editar Horário")
                .setView(etHora)
                .setPositiveButton("Atualizar", (dialog, which) -> {
                    String novaHora = formatarHora(etHora.getText().toString().trim());
                    if (!novaHora.isEmpty()) {
                        db.collection("config_horarios").document(horario.getId())
                                .update("hora", novaHora)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Horário atualizado", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao atualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .setNeutralButton("Excluir", (dialog, which) -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Confirmar Exclusão")
                            .setMessage("Deseja realmente remover o horário " + horario.getHora() + "?")
                            .setPositiveButton("Sim, excluir", (d, w) -> {
                                db.collection("config_horarios").document(horario.getId()).delete()
                                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Horário removido", Toast.LENGTH_SHORT).show());
                            })
                            .setNegativeButton("Não", null)
                            .show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoAdicionar() {
        EditText etHora = new EditText(this);
        etHora.setHint("Ex: 09:00");
        etHora.setPadding(50, 40, 50, 40);

        new AlertDialog.Builder(this)
                .setTitle("Novo Horário")
                .setView(etHora)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String hora = formatarHora(etHora.getText().toString().trim());
                    if (!hora.isEmpty()) {
                        Horario h = new Horario(hora, true);
                        db.collection("config_horarios").add(h)
                            .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Informe o horário", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ouvirHorarios() {
        db.collection("config_horarios")
            .orderBy("hora")
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    android.util.Log.e("FirestoreError", "Erro ao ouvir horários", error);
                    return;
                }
                if (value != null) {
                    listaHorarios.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Horario h = doc.toObject(Horario.class);
                        h.setId(doc.getId());
                        listaHorarios.add(h);
                    }
                    adapter.notifyDataSetChanged();
                }
            });
    }
}
