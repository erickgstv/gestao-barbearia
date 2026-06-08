package com.erick.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AgendamentoAdapter extends RecyclerView.Adapter<AgendamentoAdapter.AgendamentoViewHolder> {

    private final List<Agendamento> agendamentos;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public AgendamentoAdapter(List<Agendamento> agendamentos) {
        this.agendamentos = agendamentos;
    }

    @NonNull
    @Override
    public AgendamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_agendamento, parent, false);
        return new AgendamentoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AgendamentoViewHolder holder, int position) {
        Agendamento agendamento = agendamentos.get(position);
        
        holder.tvNomeCliente.setText(agendamento.getNomeCliente());
        
        String contato = "";
        if (agendamento.getTelefone() != null && !agendamento.getTelefone().isEmpty()) contato = agendamento.getTelefone();
        if (agendamento.getEmail() != null && !agendamento.getEmail().isEmpty()) {
            if (!contato.isEmpty()) contato += " | ";
            contato += agendamento.getEmail();
        }
        holder.tvContato.setText(contato);
        holder.tvContato.setVisibility(contato.isEmpty() ? View.GONE : View.VISIBLE);

        holder.tvServico.setText(agendamento.getServico());
        
        String dataFormatada = agendamento.getData() != null ? dateFormat.format(agendamento.getData()) : "";
        holder.tvHorario.setText(String.format("%s às %s", dataFormatada, agendamento.getHorario()));
        
        holder.tvStatus.setText(agendamento.getStatus());

        if ("Concluído".equals(agendamento.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_concluded);
            holder.tvStatus.setTextColor(Color.parseColor("#155724"));
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            holder.tvStatus.setTextColor(Color.parseColor("#856404"));
        }

        holder.itemView.setOnClickListener(v -> {
            String[] opcoes = {"Finalizar Serviço", "Remarcar (Data/Hora)", "Excluir Agendamento", "Cancelar"};
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Gerenciar Agendamento")
                    .setItems(opcoes, (dialog, which) -> {
                        if (which == 0) finalizarAgendamento(holder, agendamento);
                        else if (which == 1) mostrarDialogoRemarcar(holder, agendamento);
                        else if (which == 2) excluirAgendamento(holder, agendamento);
                    })
                    .show();
        });
    }

    private void mostrarDialogoRemarcar(AgendamentoViewHolder holder, Agendamento agendamento) {
        View view = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.dialog_add_servico, null);
        EditText etNome = view.findViewById(R.id.etNomeServico);
        EditText etHora = view.findViewById(R.id.etPrecoServico);
        
        etNome.setText(agendamento.getNomeCliente());
        etHora.setText(agendamento.getHorario());
        etHora.setHint("Novo Horário (ex: 10:00)");

        final Calendar calendar = Calendar.getInstance();
        if (agendamento.getData() != null) calendar.setTimeInMillis(agendamento.getData());

        new AlertDialog.Builder(holder.itemView.getContext())
                .setTitle("Remarcar")
                .setView(view)
                .setNeutralButton("Mudar Data", (dialog, which) -> {
                    new DatePickerDialog(holder.itemView.getContext(), (view1, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        agendamento.setData(calendar.getTimeInMillis());
                        mostrarDialogoRemarcar(holder, agendamento);
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                })
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String novoNome = etNome.getText().toString();
                    String novaHora = etHora.getText().toString();
                    
                    FirebaseFirestore.getInstance()
                            .collection("agendamentos")
                            .document(agendamento.getId())
                            .update("nomeCliente", novoNome, "horario", novaHora, "data", calendar.getTimeInMillis())
                            .addOnSuccessListener(v -> Toast.makeText(holder.itemView.getContext(), "Atualizado!", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Voltar", null)
                .show();
    }

    private void finalizarAgendamento(AgendamentoViewHolder holder, Agendamento agendamento) {
        if (agendamento.getId() != null) {
            FirebaseFirestore.getInstance().collection("agendamentos").document(agendamento.getId()).update("status", "Concluído");
        }
    }

    private void excluirAgendamento(AgendamentoViewHolder holder, Agendamento agendamento) {
        new AlertDialog.Builder(holder.itemView.getContext())
                .setTitle("Confirmar Exclusão")
                .setMessage("Deseja remover este agendamento?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    if (agendamento.getId() != null) {
                        FirebaseFirestore.getInstance().collection("agendamentos").document(agendamento.getId()).delete();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return agendamentos.size();
    }

    public static class AgendamentoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomeCliente, tvContato, tvServico, tvHorario, tvStatus;

        public AgendamentoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeCliente = itemView.findViewById(R.id.tvNomeCliente);
            tvContato = itemView.findViewById(R.id.tvContato);
            tvServico = itemView.findViewById(R.id.tvServico);
            tvHorario = itemView.findViewById(R.id.tvHorario);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
