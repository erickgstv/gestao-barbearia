package com.erick.myapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class HorarioRapidoAdapter extends RecyclerView.Adapter<HorarioRapidoAdapter.ViewHolder> {

    private List<Horario> horarios;

    public HorarioRapidoAdapter(List<Horario> horarios) {
        this.horarios = horarios;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_horario_rapido, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Horario horario = horarios.get(position);
        holder.tvHora.setText(horario.getHora());
        
        // Estilo Claro: Verde se estiver aberto, Cinza Claro se estiver fechado
        if (horario.isDisponivel()) {
            holder.card.setCardBackgroundColor(Color.parseColor("#1DB954"));
            holder.tvHora.setTextColor(Color.WHITE);
        } else {
            holder.card.setCardBackgroundColor(Color.parseColor("#E9ECEF"));
            holder.tvHora.setTextColor(Color.parseColor("#212121"));
        }

        // Clique rápido para abrir/fechar o horário
        holder.itemView.setOnClickListener(v -> {
            if (horario.getId() != null) {
                FirebaseFirestore.getInstance()
                        .collection("config_horarios")
                        .document(horario.getId())
                        .update("disponivel", !horario.isDisponivel())
                        .addOnFailureListener(e -> {
                            android.widget.Toast.makeText(holder.itemView.getContext(), "Erro ao atualizar: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return horarios.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHora;
        CardView card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHora = itemView.findViewById(R.id.tvHoraRapida);
            card = (CardView) itemView;
        }
    }
}
