package com.erick.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class RelatorioAdapter extends RecyclerView.Adapter<RelatorioAdapter.RelatorioViewHolder> {

    private List<Relatorio> relatorios;

    public RelatorioAdapter(List<Relatorio> relatorios) {
        this.relatorios = relatorios;
    }

    @NonNull
    @Override
    public RelatorioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_relatorio, parent, false);
        return new RelatorioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatorioViewHolder holder, int position) {
        Relatorio r = relatorios.get(position);
        holder.tvData.setText(r.getData());
        holder.tvFaturamento.setText(String.format(Locale.getDefault(), "R$ %.2f", r.getFaturamento()));
        holder.tvQtdServicos.setText(r.getTotalServicos() + " serviços");
    }

    @Override
    public int getItemCount() {
        return relatorios.size();
    }

    public static class RelatorioViewHolder extends RecyclerView.ViewHolder {
        TextView tvData, tvFaturamento, tvQtdServicos;

        public RelatorioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvData = itemView.findViewById(R.id.tvDataRelatorio);
            tvFaturamento = itemView.findViewById(R.id.tvFaturamentoRelatorio);
            tvQtdServicos = itemView.findViewById(R.id.tvQtdServicosRelatorio);
        }
    }
}
