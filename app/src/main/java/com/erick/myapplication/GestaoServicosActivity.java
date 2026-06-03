package com.erick.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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

public class GestaoServicosActivity extends AppCompatActivity {

    private RecyclerView rvServicos;
    private ServicoAdapter adapter;
    private List<Servico> listaServicos;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestao_servicos);

        db = FirebaseFirestore.getInstance();
        listaServicos = new ArrayList<>();
        rvServicos = findViewById(R.id.rvServicos);
        rvServicos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ServicoAdapter(listaServicos, servico -> mostrarDialogoEditar(servico));
        rvServicos.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddServico);
        fab.setOnClickListener(v -> mostrarDialogoAdicionar());

        ouvirServicos();
    }

    private void mostrarDialogoEditar(Servico servico) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_servico, null);
        EditText etNome = view.findViewById(R.id.etNomeServico);
        EditText etPreco = view.findViewById(R.id.etPrecoServico);

        etNome.setText(servico.getNome());
        etPreco.setText(String.valueOf(servico.getPreco()));

        new AlertDialog.Builder(this)
                .setTitle("Editar Serviço")
                .setView(view)
                .setPositiveButton("Atualizar", (dialog, which) -> {
                    String nome = etNome.getText().toString();
                    String precoStr = etPreco.getText().toString();
                    if (!nome.isEmpty() && !precoStr.isEmpty()) {
                        try {
                            double preco = Double.parseDouble(precoStr);
                            db.collection("servicos").document(servico.getId())
                                    .update("nome", nome, "preco", preco)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Serviço atualizado", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(this, "Erro ao atualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Preço inválido", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Excluir", (dialog, which) -> {
                    db.collection("servicos").document(servico.getId()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Serviço excluído", Toast.LENGTH_SHORT).show());
                })
                .setNeutralButton("Cancelar", null)
                .show();
    }

    private void ouvirServicos() {
        db.collection("servicos").addSnapshotListener((value, error) -> {
            if (error != null) {
                android.util.Log.e("FirestoreError", "Erro ao ouvir serviços", error);
                return;
            }
            if (value != null) {
                listaServicos.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Servico s = doc.toObject(Servico.class);
                    s.setId(doc.getId());
                    listaServicos.add(s);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void mostrarDialogoAdicionar() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_servico, null);
        EditText etNome = view.findViewById(R.id.etNomeServico);
        EditText etPreco = view.findViewById(R.id.etPrecoServico);

        new AlertDialog.Builder(this)
                .setTitle("Novo Serviço")
                .setView(view)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String nome = etNome.getText().toString();
                    String precoStr = etPreco.getText().toString();
                    if (!nome.isEmpty() && !precoStr.isEmpty()) {
                        try {
                            double preco = Double.parseDouble(precoStr);
                            Servico s = new Servico(nome, preco);
                            db.collection("servicos").add(s)
                                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Preço inválido", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
