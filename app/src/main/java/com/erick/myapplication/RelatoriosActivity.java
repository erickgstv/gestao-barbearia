package com.erick.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class RelatoriosActivity extends AppCompatActivity {

    private RecyclerView rvRelatorios;
    private RelatorioAdapter adapter;
    private List<Relatorio> listaRelatorios;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorios);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarRelatorios);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Histórico de Ganhos");
        }

        db = FirebaseFirestore.getInstance();
        listaRelatorios = new ArrayList<>();
        rvRelatorios = findViewById(R.id.rvRelatorios);
        rvRelatorios.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RelatorioAdapter(listaRelatorios);
        rvRelatorios.setAdapter(adapter);

        carregarRelatorios();
    }

    private void carregarRelatorios() {
        db.collection("relatorios_diarios")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    android.util.Log.e("FirestoreError", "Erro ao carregar relatórios", error);
                    return;
                }
                if (value != null) {
                    listaRelatorios.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Relatorio r = doc.toObject(Relatorio.class);
                        listaRelatorios.add(r);
                    }
                    adapter.notifyDataSetChanged();
                }
            });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
