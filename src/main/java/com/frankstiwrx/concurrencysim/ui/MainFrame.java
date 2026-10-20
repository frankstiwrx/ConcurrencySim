package com.frankstiwrx.concurrencysim.ui;

import com.frankstiwrx.concurrencysim.model.Task;
import com.frankstiwrx.concurrencysim.scheduler.EDZLScheduler;
import com.frankstiwrx.concurrencysim.sync.LockType;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private final List<Task> tasks = new ArrayList<>();
    private final EDZLScheduler scheduler = new EDZLScheduler();
    private int currentTime = 0;

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JLabel lblStatus = new JLabel("Tempo de Simulação: 0 ms");
    private final JComboBox<LockType> comboLock = new JComboBox<>(LockType.values());

    public MainFrame() {
        super("Simulador Visual de Concorrência & Escalonamento EDZL");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 650);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // Painel Superior de Controles
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton btnAddCustomTask = new JButton("➕ Tarefa Personalizada");
        JButton btnAddRandomTask = new JButton("🎲 Tarefa Aleatória");
        JButton btnStep = new JButton("▶ Próximo Ciclo (Tick)");
        JButton btnReset = new JButton("🔄 Reiniciar");

        topPanel.add(btnAddCustomTask);
        topPanel.add(btnAddRandomTask);
        topPanel.add(btnStep);
        topPanel.add(btnReset);
        topPanel.add(new JLabel("Sincronização:"));
        topPanel.add(comboLock);

        add(topPanel, BorderLayout.NORTH);

        // Lista Principal de Tarefas
        JList<String> taskList = new JList<>(listModel);
        taskList.setFont(new Font("Monospaced", Font.PLAIN, 13));
        add(new JScrollPane(taskList), BorderLayout.CENTER);

        // Rodapé de Status
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(lblStatus);
        add(bottomPanel, BorderLayout.SOUTH);

        // Eventos
        btnAddCustomTask.addActionListener(e -> abrirDialogoNovaTarefa());
        btnAddRandomTask.addActionListener(e -> adicionarTarefaExemplo());
        btnStep.addActionListener(e -> executarCiclo());
        btnReset.addActionListener(e -> reiniciarSimulacao());
    }

    /**
     * Modal para inclusão de tarefas personalizadas com validação de parâmetros
     */
    private void abrirDialogoNovaTarefa() {
        JTextField txtNome = new JTextField("T" + (tasks.size() + 1) + "_Critica");
        JTextField txtExecucao = new JTextField("5");
        JTextField txtDeadline = new JTextField("8");
        JTextField txtPrioridade = new JTextField("1");

        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.add(new JLabel("Nome da Tarefa:"));
        panel.add(txtNome);
        panel.add(new JLabel("Tempo de Execução / C (ms):"));
        panel.add(txtExecucao);
        panel.add(new JLabel("Deadline Relativo / D (ms):"));
        panel.add(txtDeadline);
        panel.add(new JLabel("Prioridade Base:"));
        panel.add(txtPrioridade);

        int result = JOptionPane.showConfirmDialog(
            this, panel, "Configurar Tarefa Personalizada",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                String nome = txtNome.getText().trim();
                if (nome.isEmpty()) nome = "Tarefa-" + (tasks.size() + 1);

                int execucao = Integer.parseInt(txtExecucao.getText().trim());
                int deadlineRelativo = Integer.parseInt(txtDeadline.getText().trim());
                int prioridade = Integer.parseInt(txtPrioridade.getText().trim());

                if (execucao <= 0 || deadlineRelativo <= 0) {
                    JOptionPane.showMessageDialog(this, "Tempo de execução e deadline devem ser números positivos!", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int deadlineAbsoluto = currentTime + deadlineRelativo;
                int id = tasks.size() + 1;

                Task t = new Task(id, nome, execucao, deadlineAbsoluto, prioridade);
                tasks.add(t);
                atualizarUI();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Insira valores numéricos inteiros válidos para os campos numéricos.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void adicionarTarefaExemplo() {
        int id = tasks.size() + 1;
        int duracao = 4 + (int) (Math.random() * 5);
        int deadline = currentTime + duracao + 5 + (int) (Math.random() * 6);
        Task t = new Task(id, "T" + id + "_Aleatoria", duracao, deadline, 1);
        tasks.add(t);
        atualizarUI();
    }

    private void executarCiclo() {
        currentTime++;
        scheduler.schedule(tasks, 2, currentTime);

        for (Task t : tasks) {
            if (t.getState() == Task.State.READY || t.getState() == Task.State.RUNNING) {
                t.tickExecution();
                break;
            }
        }

        lblStatus.setText("Tempo de Simulação: " + currentTime + " ms");
        atualizarUI();
    }

    private void reiniciarSimulacao() {
        currentTime = 0;
        for (Task t : tasks) {
            t.reset(currentTime);
        }
        lblStatus.setText("Tempo de Simulação: 0 ms");
        atualizarUI();
    }

    private void atualizarUI() {
        listModel.clear();
        for (Task t : tasks) {
            int laxity = t.getLaxity(currentTime);
            String alertaZeroLaxity = (laxity <= 0 && t.getRemainingTime() > 0) ? " ⚠️ [ZERO LAXITY - PRIORIDADE MÁXIMA]" : "";
            
            listModel.addElement(String.format(
                "[%-8s] %-18s | Restante: %2d ms | Deadline Abs: %2d ms | Laxity: %3d | CPU: %2d ms%s",
                t.getState(), t.getName(), t.getRemainingTime(), t.getDeadline(), laxity, t.getCpuCyclesConsumed(), alertaZeroLaxity
            ));
        }
    }
}