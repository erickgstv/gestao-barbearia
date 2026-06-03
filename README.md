# 💈 Sistema de Gestão para Barbearia

Sistema completo e integrado para gestão de barbearia, composto por um **Aplicativo Android** para o barbeiro e um **Site Dinâmico** para agendamento dos clientes.

## 🚀 Link do Site (Produção)
Acesse para agendamentos: [https://barbearia-gest.netlify.app/](https://barbearia-gest.netlify.app/)

## 📱 Funcionalidades do Aplicativo (Barbeiro)
*   **Notificações em Tempo Real:** Receba um aviso no celular assim que um cliente agendar pelo site.
*   **Gestão de Agendamentos:** Visualize a lista de serviços do dia com nome, telefone (WhatsApp) e horário.
*   **Controle Financeiro:** Relatório de ganhos diários e histórico de faturamento.
*   **Personalização:**
    *   Adicione, edite ou remova serviços e preços.
    *   Gerencie seus horários de atendimento (abrir/fechar slots).
*   **Acesso Rápido:** Interface otimizada sem necessidade de login repetitivo para o dono.

## 🌐 Funcionalidades do Site (Cliente)
*   **Agendamento Simplificado:** Escolha de serviço e horário disponível em segundos.
*   **Interface Moderna:** Design limpo, rápido e responsivo (estilo Verde Spotify).
*   **Confirmação Instantânea:** Sem burocracia, o horário é reservado no banco de dados no momento do clique.

## 🛠️ Tecnologias Utilizadas
*   **Android Studio:** Desenvolvimento do aplicativo nativo (Java).
*   **Firebase Firestore:** Banco de dados NoSQL em tempo real para sincronização entre site e app.
*   **Firebase Cloud Messaging (Local):** Sistema de detecção de novos agendamentos.
*   **Netlify:** Hospedagem de alta performance para o site.
*   **Git/GitHub:** Controle de versão e deploy.

## 🔒 Segurança
O projeto utiliza um arquivo `.gitignore` rigoroso para garantir que chaves de API sensíveis e arquivos de configuração do Firebase (`google-services.json`) não sejam expostos publicamente.

---
Desenvolvido por Erick para facilitar a gestão e a experiência do cliente.
