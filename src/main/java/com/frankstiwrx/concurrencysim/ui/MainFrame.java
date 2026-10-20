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
    private final JList<String> taskList = new JList<>(listModel);
    private final JLabel lblStatus = new JLabel("Tempo de Simulação: 0 ms | Cores Ativos: 2");
    private final JComboBox<LockType> comboLock = new JComboBox<>(LockType.values());
    private final JComboBox<Integer> comboCores = new JComboBox<>(new Integer[]{1, 2, 4});

    private final GanttPanel ganttPanel;
    private final JScrollPane ganttScrollPane;
    private final Timer autoSimulationTimer;
    private final JButton btnPlayPause = new JButton("▶ Play");
    private final JSlider speedSlider = new JSlider(100, 1000, 500);

    public MainFrame() {
        super("Simulador Visual de Concorrência & Escalonamento EDZL");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 750);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
        JButton btnAddCustomTask = new JButton("┼ Nova Tarefa");
        JButton btnAddRandomTask = new JButton("🎲 Aleatória");
        JButton btnRemoveTask = new JButton("❌ Remover Selecionada");
        JButton btnStep = new JButton("⏭ Tick");
        JButton btnReset = new JButton("🔄 Reiniciar");

        topPanel.add(btnAddCustomTask);
        topPanel.add(btnAddRandomTask);
        topPanel.add(btnRemoveTask);
        topPanel.add(btnStep);
        topPanel.add(btnPlayPause);
        topPanel.add(btnReset);
        topPanel.add(new JLabel("CPUs:"));
        topPanel.add(comboCores);
        topPanel.add(new JLabel("Sincronização:"));
        topPanel.add(comboLock);

        add(topPanel, BorderLayout.NORTH);

        taskList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        comboCores.setSelectedItem(2);

        ganttPanel = new GanttPanel(2);
        ganttScrollPane = new JScrollPane(
            ganttPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        );

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(taskList), ganttScrollPane);
        splitPane.setResizeWeight(0.60);
        add(splitPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(lblStatus);

        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        speedPanel.add(new JLabel("Velocidade (ms):"));
        speedPanel.add(speedSlider);

        bottomPanel.add(statusPanel, BorderLayout.WEST);
        bottomPanel.add(speedPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        autoSimulationTimer = new Timer(speedSlider.getValue(), e -> executarCiclo());

        btnAddCustomTask.addActionListener(e -> abrirDialogoNovaTarefa());
        btnAddRandomTask.addActionListener(e -> adicionarTarefaExemplo());
        btnRemoveTask.addActionListener(e -> removerTarefaSelecionada());
        btnStep.addActionListener(e -> executarCiclo());
        btnPlayPause.addActionListener(e -> alternarSimulacao());
        btnReset.addActionListener(e -> reiniciarSimulacao());

        comboCores.addActionListener(e -> reiniciarSimulacao());

        speedSlider.addChangeListener(e -> autoSimulationTimer.setDelay(speedSlider.getValue()));
    }

    private void removerTarefaSelecionada() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1 && selectedIndex < tasks.size()) {
            tasks.remove(selectedIndex);
            atualizarUI();
        } else {
            JOptionPane.showMessageDialog(this, "Selecione uma tarefa na lista para remover.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void alternarSimulacao() {
        if (autoSimulationTimer.isRunning()) {
            autoSimulationTimer.stop();
            btnPlayPause.setText("▶ Play");
        } else {
            autoSimulationTimer.start();
            btnPlayPause.setText("⏸ Pause");
        }
    }

    private void abrirDialogoNovaTarefa() {
        JTextField txtNome = new JTextField("T" + (tasks.size() + 1));
        JTextField txtExecucao = new JTextField("5");
        JTextField txtDeadline = new JTextField("8");
        JTextField txtPrioridade = new JTextField("1");

        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.add(new JLabel("Nome da Tarefa:"));
        panel.add(txtNome);
        panel.add(new JLabel("Execução / C (ms):"));
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
                if (nome.isEmpty()) nome = "T" + (tasks.size() + 1);

                int execucao = Integer.parseInt(txtExecucao.getText().trim());
                int deadlineRelativo = Integer.parseInt(txtDeadline.getText().trim());
                int prioridade = Integer.parseInt(txtPrioridade.getText().trim());

                if (execucao <= 0 || deadlineRelativo <= 0) {
                    JOptionPane.showMessageDialog(this, "Valores de execução e deadline devem ser positivos.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Task t = new Task(tasks.size() + 1, nome, execucao, deadlineRelativo, prioridade);
                t.setAbsoluteDeadline(currentTime + deadlineRelativo);
                tasks.add(t);
                atualizarUI();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Valores numéricos inválidos.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void adicionarTarefaExemplo() {
        int id = tasks.size() + 1;
        int duracao = 3 + (int) (Math.random() * 5);
        int deadlineRelativo = duracao + 4 + (int) (Math.random() * 6);
        Task t = new Task(id, "T" + id + "_Rand", duracao, deadlineRelativo, 1);
        t.setAbsoluteDeadline(currentTime + deadlineRelativo);
        tasks.add(t);
        atualizarUI();
    }

    private void executarCiclo() {
        currentTime++;
        int numCores = (Integer) comboCores.getSelectedItem();

        scheduler.schedule(tasks, numCores, currentTime);

        boolean temTarefaAtiva = false;
        int coresUsados = 0;

        for (Task t : tasks) {
            if (t.getState() == Task.State.READY || t.getState() == Task.State.RUNNING) {
                temTarefaAtiva = true;
                if (coresUsados < numCores) {
                    t.setState(Task.State.RUNNING);
                    t.tickExecution();
                    ganttPanel.registrarTick(currentTime, coresUsados, t.getName());
                    coresUsados++;
                } else {
                    t.setState(Task.State.READY);
                }
            }
        }

        while (coresUsados < numCores) {
            ganttPanel.registrarTick(currentTime, coresUsados, "IDLE");
            coresUsados++;
        }

        // Interrompe o Timer automaticamente quando todas as tarefas terminam
        if (!temTarefaAtiva && !tasks.isEmpty()) {
            if (autoSimulationTimer.isRunning()) {
                autoSimulationTimer.stop();
                btnPlayPause.setText("▶ Play");
            }
            lblStatus.setText(String.format("✓ Simulação Concluída (%d ms) | Todos os processos finalizados | CPUs: %d", currentTime, numCores));
        } else {
            lblStatus.setText(String.format("Tempo de Simulação: %d ms | Cores Ativos: %d", currentTime, numCores));
        }

        atualizarUI();

        SwingUtilities.invokeLater(() -> {
            ganttScrollPane.validate();
            JScrollBar horizontal = ganttScrollPane.getHorizontalScrollBar();
            horizontal.setValue(horizontal.getMaximum());
        });
    }

    private void reiniciarSimulacao() {
        if (autoSimulationTimer.isRunning()) {
            autoSimulationTimer.stop();
            btnPlayPause.setText("▶ Play");
        }

        currentTime = 0;
        int numCores = (Integer) comboCores.getSelectedItem();
        for (Task t : tasks) {
            t.reset(currentTime);
        }
        ganttPanel.resetCores(numCores);
        lblStatus.setText(String.format("Tempo de Simulação: 0 ms | Cores Ativos: %d", numCores));
        atualizarUI();
    }

    private void atualizarUI() {
        listModel.clear();
        for (Task t : tasks) {
            int laxity = t.getLaxity(currentTime);
            String alertaZeroLaxity = (laxity <= 0 && t.getRemainingTime() > 0 && t.getState() != Task.State.FINISHED) ? " ⚠️ [ZERO LAXITY - PRIORIDADE MÁXIMA]" : "";

            listModel.addElement(String.format(
                "[%-8s] %-16s | Restante: %2d ms | Deadline Abs: %2d ms | Laxity: %3d | CPU: %2d ms%s",
                t.getState(), t.getName(), t.getRemainingTime(), t.getAbsoluteDeadline(), laxity, t.getCpuCyclesConsumed(), alertaZeroLaxity
            ));
        }
    }
}