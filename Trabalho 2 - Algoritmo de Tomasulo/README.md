# Implementação do Algoritmo de Tomasulo

Este projeto implementa uma simulação do Algoritmo de Tomasulo, um método de execução dinâmica de instruções que permite execução fora de ordem (out-of-order execution) em processadores modernos.

## Visão Geral

O Algoritmo de Tomasulo é uma técnica fundamental em arquitetura de computadores que permite:
- Execução fora de ordem de instruções
- Renomeação dinâmica de registradores
- Eliminação de hazards de dados
- Execução paralela de instruções independentes

## Componentes Principais

### 1. Estações de Reserva (Reservation Stations)
- Implementadas na classe `ReservationStation`
- Responsáveis por armazenar e executar instruções
- Suportam as seguintes operações:
  - ADD (Adição)
  - SUB (Subtração)
  - MUL (Multiplicação)
  - DIV (Divisão)
  - BEQ (Branch if Equal)
  - BNE (Branch if Not Equal)

### 2. Buffer de Reordenação (Reorder Buffer)
- Implementado na classe `ReorderBuffer`
- Mantém o estado das instruções em execução
- Garante a ordem correta de conclusão das instruções
- Gerencia a renomeação de registradores

### 3. Processador Tomasulo
- Implementado na classe `TomasuloProcessor`
- Coordena a execução das instruções
- Gerencia as estações de reserva e o buffer de reordenação
- Suporta execução paralela através de múltiplas threads

### 4. Gerador de Instruções Aleatórias
- Implementado na classe `RandomInstructionGenerator`
- Gera instruções RISC-V aleatórias para teste
- Suporta diferentes tipos de instruções (aritméticas e de desvio)

## Como Usar

### Execução Manual
1. Execute o programa principal
2. Digite as instruções no formato: `opcode rd rs1 rs2 imm`
   - Exemplo: `ADD 1 2 3 0`
3. O sistema processará as instruções e mostrará o estado após cada ciclo

### Execução com Instruções Aleatórias
O sistema também suporta geração automática de instruções para testes.

## Estrutura do Projeto

```
src/
├── RandomInstructionGenerator.java  # Gerador de instruções e implementação principal
└── [Outros arquivos fonte]
```

## Características Implementadas

- Execução fora de ordem de instruções
- Suporte a múltiplas estações de reserva
- Buffer de reordenação para garantir ordem de conclusão
- Execução paralela através de múltiplas threads
- Suporte a instruções de desvio condicional
- Detecção e tratamento de hazards de dados
- Visualização do estado do processador em cada ciclo

## Limitações Atuais

- Número fixo de estações de reserva (configurável na inicialização)
- Tamanho fixo do buffer de reordenação
- Conjunto limitado de instruções suportadas
- Tratamento básico de exceções (ex: divisão por zero)

## Requisitos

- Java 8 ou superior
- Ambiente de desenvolvimento Java configurado

## Como Compilar e Executar

1. Compile o projeto:
```bash
javac src/*.java
```

2. Execute o programa:
```bash
java -cp src RandomInstructionGenerator
```

## Contribuições

Contribuições são bem-vindas! Sinta-se à vontade para:
- Reportar bugs
- Sugerir melhorias
- Adicionar novas funcionalidades
- Melhorar a documentação

## Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para mais detalhes. 