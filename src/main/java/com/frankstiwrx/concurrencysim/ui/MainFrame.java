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
        setSize(950, 600);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddTask = new JButton("┼ Adicionar Tarefa");
        JButton btnStep = new JButton("▶ Próximo Ciclo (Tick)");
        JButton btnReset = new JButton("🔄 Reiniciar");

        topPanel.add(btnAddTask);
        topPanel.add(btnStep);
        topPanel.add(btnReset);
        topPanel.add(new JLabel("Mecanismo de Bloqueio:"));
        topPanel.add(comboLock);

        add(topPanel, BorderLayout.NORTH);

        JList<String> taskList = new JList<>(listModel);
        taskList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(taskList), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(lblStatus);
        add(bottomPanel, BorderLayout.SOUTH);

        btnAddTask.addActionListener(e -> adicionarTarefaExemplo());
        btnStep.addActionListener(e -> executarCiclo());
        btnReset.addActionListener(e -> reiniciarSimulacao());
    }

    private void adicionarTarefaExemplo() {
        int id = tasks.size() + 1;
        int duracao = 4 + (int) (Math.random() * 5);
        int deadline = currentTime + duracao + 5 + (int) (Math.random() * 6);
        Task t = new Task(id, "Tarefa-" + id, duracao, deadline, 1);
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
            listModel.addElement(String.format(
                "[%s] %-10s | Execução Restante: %2d ms | Deadline: %2d ms | Laxity: %3d | Ciclos CPU: %2d | BusyWait: %2d",
                t.getState(), t.getName(), t.getRemainingTime(), t.getDeadline(), laxity, t.getCpuCyclesConsumed(), t.getBusyWaitingCycles()
            ));
        }
    }
}