import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class TomasuloGUI extends JFrame {
    private TomasuloProcessor processor;
    private JTable reservationStationTable;
    private JTable reorderBufferTable;
    private JTextArea instructionArea;
    private JTextArea statusArea;
    private DefaultTableModel reservationStationModel;
    private DefaultTableModel reorderBufferModel;
    private List<Instruction> instructionQueue;
    private JButton stepButton;
    private JButton resetButton;
    private JButton addInstructionButton;
    private JComboBox<String> opcodeCombo;
    private JSpinner rdSpinner, rs1Spinner, rs2Spinner, immSpinner;
    private int currentCycle = 0;
    private JLabel ipcLabel;
    private JLabel stallsLabel;

    public TomasuloGUI() {
        processor = new TomasuloProcessor(3, 6);
        instructionQueue = new ArrayList<>();
        setupGUI();
    }

    private void setupGUI() {
        setTitle("Simulador do Algoritmo de Tomasulo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1200, 800);

        // Painel principal dividido em duas partes
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setDividerLocation(400);

        // Painel superior (Instruções e Controles)
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Painel de entrada de instruções
        JPanel inputPanel = createInputPanel();
        topPanel.add(inputPanel, BorderLayout.NORTH);

        // Área de instruções
        instructionArea = new JTextArea(5, 40);
        instructionArea.setEditable(false);
        JScrollPane instructionScroll = new JScrollPane(instructionArea);
        topPanel.add(instructionScroll, BorderLayout.CENTER);

        // Painel inferior (Tabelas e Status)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Tabelas em um painel com abas
        JTabbedPane tablePane = new JTabbedPane();
        
        // Tabela de Estações de Reserva
        String[] rsColumns = {"ID", "Opcode", "Operando 1", "Operando 2", "Destino", "Ocupado", "Resultado"};
        reservationStationModel = new DefaultTableModel(rsColumns, 0);
        reservationStationTable = new JTable(reservationStationModel);
        JScrollPane rsScroll = new JScrollPane(reservationStationTable);
        tablePane.addTab("Estações de Reserva", rsScroll);

        // Tabela do Buffer de Reordenação
        String[] robColumns = {"ID", "Ocupado", "Resultado"};
        reorderBufferModel = new DefaultTableModel(robColumns, 0);
        reorderBufferTable = new JTable(reorderBufferModel);
        JScrollPane robScroll = new JScrollPane(reorderBufferTable);
        tablePane.addTab("Buffer de Reordenação", robScroll);

        bottomPanel.add(tablePane, BorderLayout.CENTER);

        // Área de status
        statusArea = new JTextArea(3, 40);
        statusArea.setEditable(false);
        JScrollPane statusScroll = new JScrollPane(statusArea);

        // Painel de métricas
        JPanel metricsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        metricsPanel.setBorder(BorderFactory.createTitledBorder("Métricas"));
        
        ipcLabel = new JLabel("ICP: 0.0");
        stallsLabel = new JLabel("Bolhas: 0");
        
        metricsPanel.add(ipcLabel);
        metricsPanel.add(stallsLabel);
        
        // Adiciona o painel de métricas ao painel de status
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(metricsPanel, BorderLayout.NORTH);
        statusPanel.add(statusScroll, BorderLayout.CENTER);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);

        // Adiciona os painéis ao split pane
        mainSplitPane.setTopComponent(topPanel);
        mainSplitPane.setBottomComponent(bottomPanel);

        // Adiciona o split pane à janela
        add(mainSplitPane, BorderLayout.CENTER);

        // Painel de controles
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);

        // Inicializa as tabelas
        updateTables();
        
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Nova Instrução"));

        // ComboBox para opcode
        String[] opcodes = {
            "ADD", "SUB", "MUL", "DIV", "BEQ", "BNE",
            "LW", "SW", "ADDI", "SUBI", "AND", "OR",
            "XOR", "SLT", "SLTI", "JAL", "JALR"
        };
        opcodeCombo = new JComboBox<>(opcodes);
        panel.add(new JLabel("Opcode:"));
        panel.add(opcodeCombo);

        // Spinners para registradores e imediato
        SpinnerNumberModel rdModel = new SpinnerNumberModel(0, 0, 31, 1);
        SpinnerNumberModel rs1Model = new SpinnerNumberModel(0, 0, 31, 1);
        SpinnerNumberModel rs2Model = new SpinnerNumberModel(0, 0, 31, 1);
        SpinnerNumberModel immModel = new SpinnerNumberModel(0, -8, 7, 1);

        rdSpinner = new JSpinner(rdModel);
        rs1Spinner = new JSpinner(rs1Model);
        rs2Spinner = new JSpinner(rs2Model);
        immSpinner = new JSpinner(immModel);

        panel.add(new JLabel("RD:"));
        panel.add(rdSpinner);
        panel.add(new JLabel("RS1:"));
        panel.add(rs1Spinner);
        panel.add(new JLabel("RS2:"));
        panel.add(rs2Spinner);
        panel.add(new JLabel("IMM:"));
        panel.add(immSpinner);

        addInstructionButton = new JButton("Adicionar Instrução");
        addInstructionButton.addActionListener(e -> addInstruction());
        panel.add(addInstructionButton);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        stepButton = new JButton("Executar Ciclo");
        stepButton.addActionListener(e -> executeCycle());

        resetButton = new JButton("Reiniciar");
        resetButton.addActionListener(e -> resetSimulation());

        panel.add(stepButton);
        panel.add(resetButton);

        return panel;
    }

    private void addInstruction() {
        String opcode = (String) opcodeCombo.getSelectedItem();
        int rd = (Integer) rdSpinner.getValue();
        int rs1 = (Integer) rs1Spinner.getValue();
        int rs2 = (Integer) rs2Spinner.getValue();
        int imm = (Integer) immSpinner.getValue();

        Instruction instruction = new Instruction(opcode, rd, rs1, rs2, imm);
        instructionQueue.add(instruction);
        processor.issueInstruction(opcode, rd, rs1, rs2, imm);

        updateInstructionArea();
        updateTables();
        updateStatus("Nova instrução adicionada: " + instructionToString(instruction));
    }

    private void executeCycle() {
        processor.executeInstructions();
        currentCycle++;
        updateTables();
        updateMetrics();
        updateStatus("Ciclo " + currentCycle + " executado");
    }

    private void resetSimulation() {
        processor = new TomasuloProcessor(3, 6);
        instructionQueue.clear();
        currentCycle = 0;
        ReservationStation.resetMetrics();
        updateTables();
        updateInstructionArea();
        updateMetrics();
        updateStatus("Simulação reiniciada");
    }

    private void updateTables() {
        // Atualiza tabela de Estações de Reserva
        reservationStationModel.setRowCount(0);
        for (int i = 0; i < processor.reservationStations.length; i++) {
            ReservationStation rs = processor.reservationStations[i];
            reservationStationModel.addRow(new Object[]{
                i,
                rs.opcode,
                rs.operand1,
                rs.operand2,
                rs.destination,
                rs.busy,
                rs.result
            });
        }

        // Atualiza tabela do Buffer de Reordenação
        reorderBufferModel.setRowCount(0);
        for (int i = 0; i < processor.reorderBuffer.busy.length; i++) {
            reorderBufferModel.addRow(new Object[]{
                i,
                processor.reorderBuffer.busy[i],
                processor.reorderBuffer.result[i]
            });
        }
    }

    private void updateInstructionArea() {
        StringBuilder sb = new StringBuilder();
        sb.append("Instruções na fila:\n");
        for (int i = 0; i < instructionQueue.size(); i++) {
            sb.append(i + 1).append(": ").append(instructionToString(instructionQueue.get(i))).append("\n");
        }
        instructionArea.setText(sb.toString());
    }

    private void updateStatus(String message) {
        statusArea.append(message + "\n");
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }

    private String instructionToString(Instruction instruction) {
        switch (instruction.opcode) {
            case "LW":
            case "SW":
                return String.format("%s R%d %d(R%d)",
                    instruction.opcode,
                    instruction.rd,
                    instruction.imm,
                    instruction.rs1);
            case "ADDI":
            case "SUBI":
            case "SLTI":
                return String.format("%s R%d R%d %d",
                    instruction.opcode,
                    instruction.rd,
                    instruction.rs1,
                    instruction.imm);
            case "BEQ":
            case "BNE":
                return String.format("%s R%d R%d %d",
                    instruction.opcode,
                    instruction.rs1,
                    instruction.rs2,
                    instruction.imm);
            case "JAL":
            case "JALR":
                return String.format("%s R%d %d",
                    instruction.opcode,
                    instruction.rd,
                    instruction.imm);
            default:
                return String.format("%s R%d R%d R%d",
                    instruction.opcode,
                    instruction.rd,
                    instruction.rs1,
                    instruction.rs2);
        }
    }

    private void updateMetrics() {
        ipcLabel.setText(String.format("ICP: %.2f", ReservationStation.getIPC()));
        stallsLabel.setText(String.format("Bolhas: %d", ReservationStation.getTotalStalls()));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TomasuloGUI());
    }
} 