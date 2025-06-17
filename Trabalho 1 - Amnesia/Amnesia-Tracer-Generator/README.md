# Amnesia Tracer Generator - v0.4
* Autor: Mateus Leal Sobreira

## Introdução

A partir da construção de um Algoritmo na pseudo-linguagem *KTT*, gera um arquivo de Tracer de execução para ser utilizado no Simulador de Hierarquia de Memória Amnesia.

## Uso

### Compilação
Para compilar a aplicação é utilizado o GNU GCC. Abaixo é demonstrado um exemplo de compilação em **Linux** para o arquivo `TracerGenerator`:

```
gcc -o TracerGenerator TracerGenerator.c
```
 
### Execução
Com o programa compilado, sua execução no **Linux** é dada por:

```
./TracerGenerator <arquivo.KTT>
```

O arquivo que deve ser referenciado para execução é um arquivo escrito na pseudo-linguagem *KTT*.

## Katta's Tracer Pseudo-Language (*KTT*)
### Descrição
A *Katta's Tracer Pseudo-Language* ou *KTT* é uma pseudo-linguagem baseada na liguagem assembly que tem o propósito de descrever a execução de um programa para que possa ser gerado um tracer de execução que será utilizado no Simulador Amnesia.

### Características do interpretador
#### Comentários
Os *comentários* não são lidos pelo interpretador e auxiliam o programador a realizar anotações sobre a execução da aplicação. 
```
//Exemplo de um comentário
```

#### Registradores
O interpretador da pseudo-lingua KTT utiliza do conceito de registradores da Arquitetura `CISC` (Complex Instruction Set Computer) e contém um total de 4 registradores:
* EAX - `A`
* EBX - `B`
* ECX - `C`
* EDX - `D`

#### Acesso á Memória
O acesso á memória de dados inicia a partir do endereço `0x00F00000` e é realizada a cada 4 Bytes (32 bits). Todos os endereço a serem lidos seguem o padrão de `Base + Offset`.

O endereço da Base pode ser representado a partir do inicial, demarcado pelo valor *0*  ou a partir de um registrador. 
```
MOV A 0[0] //Elemento que está no Endereço inicial e Offset 0
MOV A B[2] //Elemento que está no Endereço de Base demarcado pelo valor no registrador B somado ao Ofsset 2.
```

Como os dados estão dispostos em conjuntos de 4 Bytes, é necessário levar em consideração este fato para realizar cálculos para acesso à memória. Assim os dois métodos abaixo são similares.
```
//Método 1:
MOV A 0[2]
```
```
//Método 2:
MOV B 0x00F00000 //Primeiro endereço da memória
ADD B 8 //Offset 2 (2*4Bytes)
MOV A B[0]
```

#### Label
As *labels* tem a finalidade de referênciar um ponteiro para uma posição na memória. Normalmente as label são utilizadas juntamente com as instruções de [*GOF*](#gof---goto-if-flag), [*JMP*](#jmp) e [*JMR*](#jmr---jump-and-return) para alterar a sequência de execução da aplicação.
```
Label:
```

### Instruções Disponíveis
#### MOV
A instrução *MOV* tem a finalidade de inserir um valor em um registrador. Este valor pode ser um Imediato, estar contido em outro registrador ou em um endereço na memória.
```
MOV A 0xA //A = 0xA (Imediato)
MOV A B //A = B (Valor do Registrador)
MOV A 0[0] //A = MEM[0] (Valor na posição de memória 0)
```

#### ADD
A instrução *ADD* tem a finalidade de somar o valor de um registrador a outro. Este outro valor pode ser um Imediato, estar contido em outro registrador ou em um endereço na memória.
```
ADD A 0xA //A = A + 0xA (Imediato)
ADD A B //A = A + B (Valor do Registrador)
ADD A 0[0] //A = A + MEM[0] (Valor na posição de memória 0)
```

#### SUB
A instrução *SUB* tem a finalidade de subtrair o valor de um registrador a outro. Este outro valor pode ser um Imediato, estar contido em outro registrador ou em um endereço na memória.
```
SUB A 0xA //A = A - 0xA (Imediato)
SUB A B //A = A - B (Valor do Registrador)
SUB A 0[0] //A = A - MEM[0] (Valor na posição de memória 0)
```

#### STR
A instrução *STR* tem a finalidade de armazenar um valor na memória. Este valor pode ser um Imediato, estar contido em outro registrador ou em outro endereço na memória.
```
STR 0[0] 0xA //MEM[0] = 0xA (Imediato)
STR 0[0] A //MEM[0] = A (Valor do Registrador)
STR 0[0] 0[1] //MEM[0] = MEM[1] (Valor na posição de memória 1)
```

#### SIT - (Set if True)
A instrução *SIT* tem a finalidade alterar o valor da `Flag de Compração` de acordo com uma verificação lógica. Existem 4 tipos de verificações, sendo elas:
* Igualdade `A = B`
* Maior que `A > B`
* Menor que `A < B`
* Diferença `A : B`

Os valores de A e B podem ser Imediatos, estarem contidos em outros registradors ou em endereços na memória.

O valor da flag, que é alterada por esta instrução, é consumida por todas as outras, porém somente as intruções [*GOF*](#gof---goto-if-flag) e [*LIF*](#lif---linha-if-true) utilizam realmente  este valor.
```
SIT A < 0xA //Flag = A < AxA ? True : False
SIT A : B //Flag = A != B ? True : False
SIT 0[0] = A //Flag = MEM[0] == A ? True : False
```

#### GOF - (GOto if Flag)
A instrução *GOF* tem a finalidade de alterar o fluxo de execução de um programa de acordo com a `Flag de Comparação`. Se esta for **Verdadeira** a execução é retomada a partir da instrução demarcada pela [*label*](#label). Se **Falsa** a execução continua a partir da próxima instrução.
```
GOF Label
```

#### JMP
A instrução *JMP* tem a finalidade de alterar o fluxo de execução de um programa. A execução é retomada a partir da instrução demarcada pela [*label*](#label).
```
JMP Label
```

#### JMR - (JuMp and Return)
A instrução *JMR* tem a finalidade de alterar o fluxo de execução de um programa. A execução é retomada a partir da instrução demarcada pela [*label*](#label). Quando chamada, esta instrução armazena o endereço da instrução abaixo dela. Este endereço será utilizado pela instrução [*RET*](#ret).
```
JMR Label
```

#### RET
A instrução *RET* tem a finalidade de alterar o fluxo de execução de um programa a partir do endereço armazenado pela instrução [*JMR*](#jmr---jump-and-return).
```
RET
```

#### LIF - (Linha If True)
A instrução *LIF* tem a finalidade de alterar o fluxo de execução de um programa de acordo com a `Flag de Comparação`. Se esta for **Verdadeira** a execução continua a partir da próxima instrução. Se **Falsa** a execução continua a partir da segunda instrução.
```
LIF
```

#### HLT
A instrução *HLT* tem a finalidade de terminar a execução do programa antes da decodificação de todas as instruções.
```
HLT
```

#### PSH
A instrução *PSH* tem a finalidade de inserir o valor de um registrador na Pilha (Stack). Este valor é armazenado entre as as instruções de [*GOF*](#gof---goto-if-flag), [*JMP*](#jmp), [*JMR*](#jmr---jump-and-return) e [*RET*](#ret).

Os valores inseridos por esta instrução seguem ordem `FILO` (First In, Last Out).
```
PSH A //Adiciona o valor de A na Pilha
```

#### POP
A instrução *POP* tem a finalidade de remover um valor inserido anterioirmente do topo da Pilha (Stack) e insere em um registrador.

Os valores removidos por esta instrução seguem ordem `FILO` (First In, Last Out).
```
POP A //Remove o valor na Pilha e insere em A
```

## Amnesia Tracer
Além de executar as pseudo-instruções KTT, o interpretador gera um arquivo das operações realizadas no formato do Tracer para ser utilizado pelo Simulador de Hierarquia de Memória Amnesia.

De acordo com a documentação encontrada no [site do Amnesia](http://amnesia.lasdpc.icmc.usp.br/), o arquivo de Tracer é consistido de linhas, onde cada linha denota respectivamente o `tipo` do acesso e o `endereço de memória` deste acesso.

Cada elemento da linha é separada por virgula, sendo que qualquer informação após os dois valores acima citados, são considerados como comentários e não são utilizados pelo Simulador Amnesia.

Os tipos de acesso que são utilizados pelo interpretador ao gerar o arquivo de Tracer são:
* `0` - Leitura da Memória
* `1` - Escrita na Memória
* `2` - Leitura de Instrução

## Exemplos
Visando melhorar o entendimento da formatação dos arquivos de entrada e saída da aplicação, estão dispostos nas pastas `input` e `output`, respectivamente, algoritmos básicos implementados na linguagem KTT e o resultado do Tracer de execução gerado.

## Problemas Encontrados
Nesta versão da aplicação, os seguintes problemas foram encontrados:
* Arquivo Tracer é salvo sempre com nome de `Tracer.txt`
* É necessário deixar uma linha em branco antes e depois do algoritmo em KTT.
* Não há limite definido de Stack e Memória de Dados, sendo possível estas se sobreporem.

## Próximas Atualizações
- [ ] Adicionar mais instruções aritméticas
- [ ] Corrigir os [*Problemas Encontrados*](#problemas-encontrados)
- [ ] Otimizar a leitura das instruções
- [ ] Adicionar mais notações do interpretador
