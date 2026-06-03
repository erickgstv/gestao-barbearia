# Barbearia Gestão - App & Site

Este é o sistema que desenvolvi para automatizar os agendamentos da minha barbearia. Ele resolve o problema de ficar combinando horários pelo WhatsApp, centralizando tudo em um app no meu celular e um site para os clientes.

## 🔗 Link para Agendamento
Os clientes agendam por aqui: [https://barbearia-gest.netlify.app/](https://barbearia-gest.netlify.app/)

## 🛠 Como o sistema funciona

### O Site (Cliente)
O cliente entra, escolhe o que quer fazer (Corte, Barba, etc) e vê os horários que eu deixei disponíveis. Assim que ele confirma, o horário sai da lista automaticamente para ninguém mais pegar o mesmo momento.

### O Aplicativo (Meu controle)
É por onde eu gerencio tudo no dia a dia:
*   **Aviso de novo cliente:** O celular apita na hora que alguém agenda no site.
*   **Agenda do dia:** Consigo ver quem vai vir, o que vai fazer e o contato (WhatsApp) da pessoa.
*   **Controle de dinheiro:** O app soma o que eu ganhei no dia e mostra o histórico de faturamento.
*   **Flexibilidade:** Posso mudar preços, cadastrar novos serviços ou fechar a barbearia em horários específicos direto pelo app.

## 💻 Parte Técnica (resumida)
*   **App:** Desenvolvido nativo para Android (Java).
*   **Site:** HTML/JS dinâmico hospedado no Netlify.
*   **Banco de Dados:** Firebase Firestore (sincroniza o site e o app em tempo real).
*   **Segurança:** Configurado para não subir chaves sensíveis para o GitHub.

---
Criado para ser simples, rápido e funcional.
