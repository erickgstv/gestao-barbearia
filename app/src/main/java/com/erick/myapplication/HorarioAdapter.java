package com.erick.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class HorarioAdapter extends RecyclerView.Adapter<HorarioAdapter.HorarioViewHolder> {

    public interface OnHorarioClickListener {
        void onHorarioClick(Horario horario);
    }

    private List<Horario> horarios;
    private OnHorarioClickListener listener;

    public HorarioAdapter(List<Horario> horarios, OnHorarioClickListener listener) {
        this.horarios = horarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HorarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_horario, parent, false);
        return new HorarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HorarioViewHolder holder, int position) {
        Horario horario = horarios.get(position);
        holder.tvHora.setText(horario.getHora());
        holder.switchDisponivel.setChecked(horario.isDisponivel());

        holder.switchDisponivel.setOnCheckedChangeListener(null); // Evita triggers indesejados ao reciclar
        holder.switchDisponivel.setChecked(horario.isDisponivel());
        holder.switchDisponivel.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (horario.getId() != null) {
                FirebaseFirestore.getInstance()
                        .collection("config_horarios")
                        .document(horario.getId())
                        .update("disponivel", isChecked);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onHorarioClick(horario);
        });

        // Clique longo também pode servir de atalho para exclusão
        holder.itemView.setOnLongClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                .setTitle("Excluir Horário")
                .setMessage("Deseja remover este horário permanentemente?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    FirebaseFirestore.getInstance()
                        .collection("config_horarios")
                        .document(horario.getId())
                        .delete();
                })
                .setNegativeButton("Cancelar", null)
                .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return horarios.size();
    }

    public static class HorarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvHora;
        SwitchCompat switchDisponivel;

        public HorarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHora = itemView.findViewById(R.id.tvHora);
            switchDisponivel = itemView.findViewById(R.id.switchDisponivel);
        }
    }
}
