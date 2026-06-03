package com.erick.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class ServicoRapidoAdapter extends RecyclerView.Adapter<ServicoRapidoAdapter.ViewHolder> {

    private List<Servico> servicos;

    public ServicoRapidoAdapter(List<Servico> servicos) {
        this.servicos = servicos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_servico_rapido, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Servico servico = servicos.get(position);
        holder.tvNome.setText(servico.getNome());
        holder.tvPreco.setText(String.format(Locale.getDefault(), "R$ %.2f", servico.getPreco()));
    }

    @Override
    public int getItemCount() {
        return servicos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvPreco;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNomeRapido);
            tvPreco = itemView.findViewById(R.id.tvPrecoRapido);
        }
    }
}
