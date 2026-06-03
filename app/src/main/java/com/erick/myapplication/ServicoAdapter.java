package com.erick.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ServicoAdapter extends RecyclerView.Adapter<ServicoAdapter.ServicoViewHolder> {

    public interface OnServicoClickListener {
        void onServicoClick(Servico servico);
    }

    private List<Servico> servicos;
    private OnServicoClickListener listener;

    public ServicoAdapter(List<Servico> servicos, OnServicoClickListener listener) {
        this.servicos = servicos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServicoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_servico, parent, false);
        return new ServicoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicoViewHolder holder, int position) {
        Servico servico = servicos.get(position);
        holder.tvNomeServico.setText(servico.getNome());
        holder.tvPrecoServico.setText(String.format("R$ %.2f", servico.getPreco()));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onServicoClick(servico);
        });
    }

    @Override
    public int getItemCount() {
        return servicos.size();
    }

    public static class ServicoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomeServico, tvPrecoServico;

        public ServicoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeServico = itemView.findViewById(R.id.tvNomeServico);
            tvPrecoServico = itemView.findViewById(R.id.tvPrecoServico);
        }
    }
}
