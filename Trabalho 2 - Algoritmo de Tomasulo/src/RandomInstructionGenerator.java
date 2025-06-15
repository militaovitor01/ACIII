/*
* Integrantes:
* 
* */

import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ReservationStation {
    String opcode;
    int operand1;
    int operand2;
    boolean busy;
    int result;
    int destination;
    int latency;
    int startCycle;
    static int totalInstructions = 0;
    static int totalCycles = 0;
    static int totalStalls = 0;
    private int currentCycle = 0;

    public ReservationStation() {
        this.opcode = "";
        this.operand1 = 0;
        this.operand2 = 0;
        this.busy = false;
        this.result = 0;
        this.destination = -1;
        this.latency = 0;
        this.startCycle = 0;
        this.currentCycle = 0;
    }

    public void setCurrentCycle(int cycle) {
        this.currentCycle = cycle;
    }

    public void execute() {
        switch (opcode) {
            case "ADD":
                executeAdd();
                break;
            case "SUB":
                executeSub();
                break;
            case "MUL":
                executeMul();
                break;
            case "DIV":
                executeDiv();
                break;
            case "BEQ":
                executeBeq();
                break;
            case "BNE":
                executeBne();
                break;
            case "LW":
                executeLw();
                break;
            case "SW":
                executeSw();
                break;
            case "ADDI":
                executeAddi();
                break;
            case "SUBI":
                executeSubi();
                break;
            case "AND":
                executeAnd();
                break;
            case "OR":
                executeOr();
                break;
            case "XOR":
                executeXor();
                break;
            case "SLT":
                executeSlt();
                break;
            case "SLTI":
                executeSlti();
                break;
            case "JAL":
                executeJal();
                break;
            case "JALR":
                executeJalr();
                break;
            // Adicione outros casos conforme necessário
        }
        retireInstruction();
    }

    private void executeAdd() {
        result = operand1 + operand2;
        latency = 1;
        retireInstruction();
    }
    private void executeSub() {
        result = operand1 - operand2;
        latency = 1;
        retireInstruction();
    }
    private void executeMul() {
        result = operand1 * operand2;
        latency = 3;
        retireInstruction();
    }
    private void executeDiv() {
        if (operand2 != 0) {
            result = operand1 / operand2;
            latency = 5;
            retireInstruction();
        } else {
            // Trate a divisão por zero aqui (lançando uma exceção, por exemplo)
            throw new ArithmeticException("Tentativa de divisão por zero");
        }
    }
    private void executeLw() {
        // Simula acesso à memória
        result = operand1 + operand2; // endereço base + offset
        latency = 2;
        retireInstruction();
    }
    private void executeSw() {
        // Simula escrita na memória
        result = operand1 + operand2; // endereço base + offset
        latency = 2;
        retireInstruction();
    }
    private void executeAddi() {
        result = operand1 + operand2; // operand2 é o valor imediato
        latency = 1;
        retireInstruction();
    }
    private void executeSubi() {
        result = operand1 - operand2; // operand2 é o valor imediato
        latency = 1;
        retireInstruction();
    }
    private void executeAnd() {
        result = operand1 & operand2;
        latency = 1;
        retireInstruction();
    }
    private void executeOr() {
        result = operand1 | operand2;
        latency = 1;
        retireInstruction();
    }
    private void executeXor() {
        result = operand1 ^ operand2;
        latency = 1;
        retireInstruction();
    }
    private void executeSlt() {
        result = (operand1 < operand2) ? 1 : 0;
        latency = 1;
        retireInstruction();
    }
    private void executeSlti() {
        result = (operand1 < operand2) ? 1 : 0; // operand2 é o valor imediato
        latency = 1;
        retireInstruction();
    }
    private void executeJal() {
        result = operand1; // endereço de retorno
        latency = 1;
        retireInstruction();
    }
    private void executeJalr() {
        result = operand1; // endereço de retorno
        latency = 1;
        retireInstruction();
    }
    public void executeBne() {
        shouldBranch = operand1 != operand2;
        latency = 1;
        retireInstruction();
    }
    private void executeBeq() {
        shouldBranch = operand1 == operand2;
        latency = 1;
        retireInstruction();
    }

    private boolean isReadyToRetire() {
        if (!busy) return false;
        
        // Verifica se os operandos estão disponíveis
        if (reorderBuffer.busy[operand1] || reorderBuffer.busy[operand2]) {
            return false;
        }

        // Verifica se a instrução já executou por tempo suficiente
        return (currentCycle - startCycle) >= latency;
    }

    private void retireInstruction() {
        if (isReadyToRetire()) {
            totalInstructions++;
            totalCycles += (currentCycle - startCycle);
            if (currentCycle - startCycle > latency) {
                totalStalls += (currentCycle - startCycle - latency);
            }
            
            if (destination >= 0 && destination < reorderBuffer.busy.length) {
                reorderBuffer.busy[destination] = false;
                reorderBuffer.result[destination] = result;
                reset();
            }
        }
    }

    public boolean isBranchInstruction() {
        return "BNE".equals(opcode);
    }

    private boolean shouldBranch;

    ReorderBuffer reorderBuffer;

    public ReservationStation(ReorderBuffer reorderBuffer) {
        this.reorderBuffer = reorderBuffer;
    }

    void reset() {
        opcode = "";
        operand1 = 0;
        operand2 = 0;
        busy = false;
        result = 0;
        destination = -1;
        shouldBranch=false;
        latency = 0;
        startCycle = 0;
        currentCycle = 0;
    }

    // Método que retorna se a instrução de desvio condicional deve ser tomada
    public boolean shouldTakeBranch() {
        return shouldBranch;
    }

    public static double getIPC() {
        return totalInstructions > 0 ? (double) totalInstructions / totalCycles : 0;
    }

    public static int getTotalStalls() {
        return totalStalls;
    }

    public static void resetMetrics() {
        totalInstructions = 0;
        totalCycles = 0;
        totalStalls = 0;
    }
}
class ReorderBuffer {
    boolean[] busy;
    int[] result;

    public ReorderBuffer(int size) {
        this.busy = new boolean[size];
        this.result = new int[size];
    }
}

class TomasuloProcessor {
    ReservationStation[] reservationStations;
    ReorderBuffer reorderBuffer;
    int cycle;
    private int programCounter = 0;
    private int instructionIndex = 0;

    private void updateState() {
        ReservationStation station = reservationStations[instructionIndex];
        station.setCurrentCycle(cycle);
        
        if (station.busy && !reorderBuffer.busy[station.operand1] && !reorderBuffer.busy[station.operand2]) {
            station.execute();
            if (station.isBranchInstruction() && station.shouldTakeBranch()) {
                instructionIndex = station.destination;
            } else {
                instructionIndex = (instructionIndex + 1) % reservationStations.length;
            }
            station.reset();
        } else {
            instructionIndex = (instructionIndex + 1) % reservationStations.length;
        }
        cycle++;
    }

    // Adicionar ao método execute
    public void execute() {
        updateState();
    }

    public TomasuloProcessor(int numReservations, int reorderBufferSize) {
        this.reservationStations = new ReservationStation[numReservations];
        this.reorderBuffer = new ReorderBuffer(reorderBufferSize);

        for (int i = 0; i < numReservations; i++) {
            this.reservationStations[i] = new ReservationStation(reorderBuffer);
        }
        this.cycle = 0;
    }

    public void issueRiscVInstruction(RiscVInstruction instruction, int rd, int rs1, int rs2) {
        String opcode = instruction.toString();
        issueInstruction(opcode, rd, rs1, rs2,0);
    }

    public void issueInstruction(String opcode, int rd, int rs1, int rs2, int imm) {
        ReservationStation station = findFreeReservationStation();
        if (station != null) {
            station.opcode = opcode;
            station.operand1 = rs1;
            station.operand2 = rs2;
            station.busy = true;
            station.result = 0;
            station.destination = rd;
            station.startCycle = cycle;

            if (!reorderBuffer.busy[rs1] && !reorderBuffer.busy[rs2]) {
                reorderBuffer.busy[rd] = true;
                reorderBuffer.result[rd] = 0;
            } else {
                station.busy = false;
            }
        }
    }

    public void executeInstructions() {
        updateState();
        // Se desejar fazer algo adicional após a execução de cada instrução, pode ser adicionado aqui
    }

    public void executeParallelInstructions(int numThreads) {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.execute(this::executeInstructions);
        }

        executor.shutdown();
        // Esperar até que todas as threads terminem
        while (!executor.isTerminated()) {
            // Aguardar
        }
    }


    public void printStatus() {
        System.out.println("Ciclo: " + cycle);
        for (int i = 0; i < reservationStations.length; i++) {
            ReservationStation station = reservationStations[i];
            System.out.println("INST" + i + ": " + station.opcode + " R" + station.operand1 + " R" + station.operand2 + " - " + "Destino: R" + station.destination);
        }
        for (int i = 0; i < reorderBuffer.busy.length; i++) {
            System.out.println("ROB" + i + ": " + (reorderBuffer.busy[i] ? "Ocupado" : "Livre  |" + " Resultado: " + reorderBuffer.result[i]));
        }
        System.out.println();
    }

    private ReservationStation findFreeReservationStation() {
        for (ReservationStation station : reservationStations) {
            if (!station.busy) {
                return station;
            }
        }
        return null; // No free reservation stations
    }
}

class Instruction {
    String opcode;
    int rd, rs1, rs2;
    int imm;
    int latency;

    public Instruction(String opcode, int rd, int rs1, int rs2, int imm) {
        this.opcode = opcode;
        this.rd = rd;
        this.rs1 = rs1;
        this.rs2 = rs2;
        this.imm = imm;
        this.latency = getLatencyForOpcode(opcode);
    }

    private int getLatencyForOpcode(String opcode) {
        switch (opcode) {
            case "ADD":
            case "SUB":
            case "ADDI":
            case "SUBI":
            case "AND":
            case "OR":
            case "XOR":
            case "SLT":
            case "SLTI":
            case "BEQ":
            case "BNE":
            case "JAL":
            case "JALR":
                return 1;
            case "LW":
            case "SW":
                return 2;
            case "MUL":
                return 3;
            case "DIV":
                return 5;
            default:
                return 1;
        }
    }
}
/*
public class MultithreadedRandomInstructionGenerator {
    private static int programCounter;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TomasuloProcessor tomasuloProcessor = new TomasuloProcessor(3, 6);

        // Solicitar instruções do usuário
        System.out.println("Digite as instruções no formato 'opcode rd rs1 rs2 imm' (por exemplo, ADD 1 2 3 0):");
        for (int i = 0; i < 6; i++) {
            System.out.print("Instrução " + (i + 1) + ": ");
            String input = scanner.nextLine();
            String[] parts = input.split(" ");
            if (parts.length == 5) {
                String opcode = parts[0];
                int rd = Integer.parseInt(parts[1]);
                int rs1 = Integer.parseInt(parts[2]);
                int rs2 = Integer.parseInt(parts[3]);
                int imm = Integer.parseInt(parts[4]);
                tomasuloProcessor.issueInstruction(opcode, rd, rs1, rs2, imm);
            } else {
                System.out.println("Formato inválido. Por favor, digite novamente.");
                i--;  // Repetir a iteração
            }
        }

        // Simular a execução por alguns ciclos em múltiplas threads
        tomasuloProcessor.executeParallelInstructions(3);

        scanner.close();
    }
}

*/
public class RandomInstructionGenerator {
    private static int programCounter;

    public static Instruction generateRandomInstruction() {
        Random random = new Random();

        RiscVInstruction[] riscVInstructions = {RiscVInstruction.ADD, RiscVInstruction.SUB, RiscVInstruction.MUL, RiscVInstruction.DIV, RiscVInstruction.BEQ, RiscVInstruction.BNE};
        RiscVInstruction randomInstruction = riscVInstructions[random.nextInt(riscVInstructions.length)];

        int rd = random.nextInt(5);
        int rs1 = random.nextInt(10);
        int rs2 = random.nextInt(10);
        int imm = 0; // Inicialize com 0 por padrão

        // Adicione o imediato para instruções de branch
        if (randomInstruction == RiscVInstruction.BEQ || randomInstruction == RiscVInstruction.BNE) {
            imm = random.nextInt(16) - 8; // Valor aleatório entre - 8 e 7
            int destination = programCounter + imm; // Ajuste o destino do desvio
            return new Instruction(randomInstruction.toString(), rd, rs1, rs2, destination);
        }

        return new Instruction(randomInstruction.toString(), rd, rs1,rs2,imm);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TomasuloProcessor tomasuloProcessor = new TomasuloProcessor(3, 6);

        // Solicitar instruções do usuário
        System.out.println("Digite as instruções no formato 'opcode rd rs1 rs2 imm' (por exemplo, ADD 1 2 3 0):");
        for (int i = 0; i < 5; i++) {
            System.out.print("Instrução " + (i + 1) + ": ");
            String input = scanner.nextLine();
            String[] parts = input.split(" ");
            if (parts.length == 5) {
                String opcode = parts[0];
                int rd = Integer.parseInt(parts[1]);
                int rs1 = Integer.parseInt(parts[2]);
                int rs2 = Integer.parseInt(parts[3]);
                int imm = Integer.parseInt(parts[4]);
                tomasuloProcessor.issueInstruction(opcode, rd, rs1, rs2, imm);
            } else {
                System.out.println("Formato inválido. Por favor, digite novamente.");
                i--;  // Repetir a iteração
            }
        }

        // Simular a execução por alguns ciclos
        for (int i = 0; i < 5; i++) {
            tomasuloProcessor.executeInstructions();
            tomasuloProcessor.printStatus();
        }

        scanner.close();
    }
}