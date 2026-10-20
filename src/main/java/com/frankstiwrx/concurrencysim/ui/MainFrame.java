package com.frankstiwrx.concurrencysim.ui;

import com.frankstiwrx.concurrencysim.model.Task;
import com.frankstiwrx.concurrencysim.scheduler.EDZLScheduler;
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

    public MainFrame() {
        super("Simulador Visual de Concorrência & Escalonamento EDZL");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // Painel de Controle
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddTask = new JButton("┼ Adicionar Tarefa");
        JButton btnStep = new JButton("▶ Próximo Ciclo (Tick)");
        JComboBox<String> comboSync = new JComboBox<>(new String[]{"Mutex (Passivo)", "Spinlock (Busy Wait)"});

        topPanel.add(btnAddTask);
        topPanel.add(btnStep);
        topPanel.add(new JLabel("Mecanismo de Bloqueio:"));
        topPanel.add(comboSync);

        add(topPanel, BorderLayout.NORTH);

        // Lista de Tarefas / Status
        JList<String> taskList = new JList<>(listModel);
        add(new JScrollPane(taskList), BorderLayout.CENTER);

        // Rodapé
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(lblStatus);
        add(bottomPanel, BorderLayout.SOUTH);

        // Eventos
        btnAddTask.addActionListener(e -> adicionarTarefaExemplo());
        btnStep.addActionListener(e -> executarCiclo());
    }

    private void adicionarTarefaExemplo() {
        int id = tasks.size() + 1;
        Task t = new Task(id, "Task-" + id, 5, currentTime + 10, 1);
        tasks.add(t);
        atualizarUI();
    }

    private void executarCiclo() {
        currentTime++;
        scheduler.schedule(tasks, 2, currentTime);

        // Executa 1 ciclo da tarefa de maior prioridade
        for (Task t : tasks) {
            if (t.getState() == Task.State.READY || t.getState() == Task.State.RUNNING) {
                t.tickExecution();
                break;
            }
        }

        lblStatus.setText("Tempo de Simulação: " + currentTime + " ms");
        atualizarUI();
    }

    private void atualizarUI() {
        listModel.clear();
        for (Task t : tasks) {
            int laxity = t.getLaxity(currentTime);
            listModel.addElement(String.format(
                "%s | Estado: %s | Execucao Restante: %d ms | Deadline: %d | Laxity: %d | Ciclos CPU: %d",
                t.getName(), t.getState(), t.getRemainingTime(), t.getDeadline(), laxity, t.getCpuCyclesConsumed()
            ));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new MainFrame().setVisible(true);
        });
    }
}