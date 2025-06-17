/*
    Katta K-uantum Processor v0.4
    Mateus Leal Sobreira - 2025
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define WORDSIZE 4 //Processador de 32 bits
#define DATAADDR 0x00F00000 //Primeiro Endereço de Dados
#define INSTADDR 0x00000000 //Primeiro Endereço de Instruções
#define STCKADDR 0xFFFFFFFF //Primeiro Endereço do Stack (Stack -> <- Dados) 

int BancoRegistradores[4]; //Processador Baseado no 8086 - EAX[0], EBX[1], ECX[2], EDX[3]

typedef struct BancoIns{

    int hexAddrs;
    char instrucao[100];
    struct BancoIns *prox;

} Instrucao;

typedef struct BancoLabels { //Armazena as Label para a repetição
    char ID[20];
    int hexaddr;
    Instrucao *aponta;
    struct BancoLabels *prox;
} Label;

typedef struct BancoMem {
    int ID;
    int value;
    struct BancoMem *prox;
} Memoria;

Memoria *inicioPos = NULL;
Label *inicioLabel = NULL;
Instrucao *inicioInstrucao = NULL; //Inicio da Pilha de Instrucoes
Instrucao *PP = NULL; //Program Pointer
Memoria *SP = NULL; //Stack Pointer
Instrucao *FPointer = NULL; //Ponteiro de Função (RET)

int PC = INSTADDR; //Inicializa o PC
int flagCods = 0; //Flag para o SIT
FILE *arq;
FILE *tracer;


void concatCharString(char* string, char letra){
    int tam = strlen(string);
    string[tam] = letra;
    string[tam + 1] = '\0';
}

Instrucao* addInstruction(char *instrucao){
    Instrucao *a = inicioInstrucao;

    if(a == NULL){
        inicioInstrucao = (Instrucao *)malloc(sizeof(Instrucao));
        strcpy(inicioInstrucao->instrucao,instrucao);
        inicioInstrucao->hexAddrs = PC;
        return inicioInstrucao;
    }

    while (a->prox != NULL){a = a->prox;}
    a->prox = (Instrucao *)malloc(sizeof(Instrucao));
    strcpy(a->prox->instrucao,instrucao);
    a->prox->hexAddrs = PC;
    return a->prox;
}

void pushStack(int dados){

    //TODO: Verificação sobre o tamanho da memória em relação ao STACK
    Memoria *a = (Memoria *)malloc(sizeof(Memoria));
    a->value = dados;

    if(SP == NULL){
        a->ID = STCKADDR;
        SP = a;
        fprintf(tracer,"1 %.8x %s\n",STCKADDR,"Escrever na Memória");
        return;
    } else {
        a->ID = SP->ID - 4; //Palavra
        a->prox = SP;
        SP = a;
        fprintf(tracer,"1 %.8x %s\n",SP->ID,"Escrever na Memória");
    }
}

int popStack(){
    if(SP == NULL){
        return -1; //Paia
    }
    fprintf(tracer,"0 %.8x %s\n",SP->ID,"Ler na Memória");
    int aux = SP->value;
    Memoria *auxP = SP;
    SP = SP->prox;
    free(auxP);
    return aux;
}

void instrucaoFree(Instrucao *i){
    if(i == NULL)return;
    instrucaoFree(i->prox);
    free(i);
}

void instrucaoLog(){
    Instrucao *i = inicioInstrucao;
    while (i != NULL)
    {
        printf("ADDR: 0x%X - Instrucao: %s\n",i->hexAddrs,i->instrucao);
        i = i->prox;
    }
    
}

Label* addLabel(char* label){
    Label *l = inicioLabel;

    if(l == NULL){
        inicioLabel = (Label *)malloc(sizeof(Label));
        strcpy(inicioLabel->ID,label);
        inicioLabel->hexaddr = PC;
        return inicioLabel;
    }

    while (l->prox != NULL){l = l->prox;}
    l->prox = (Label *)malloc(sizeof(Label));
    strcpy(l->prox->ID,label);
    l->prox->hexaddr = PC;
    return l->prox;   
}

void jumpLabel(char * label){
    Label *l = inicioLabel;

    while (l != NULL)
    {
        if(!strcmp(l->ID,label)){
            PP = l->aponta;
        }
        l = l->prox;
    }
    
}

void labelFree(Label *l){
    if(l == NULL)return;
    labelFree(l->prox);
    free(l);
}

void labelLog(){
    Label *l = inicioLabel;
    while (l != NULL)
    {
        printf("Nome: %s - Valor: 0x%X\n",l->ID,l->hexaddr);
        l = l->prox;
    }
    
}

void memFree(Memoria *m){
    if(m == NULL)return;
    memFree(m->prox);
    free(m);
}

void memLog(Memoria *mem){
    Memoria *m = mem;
    while (m != NULL)
    {
        printf("Endereço: 0x%X - Valor: 0x%X\n",m->ID,m->value);
        m = m->prox;
    }
    
}

int memLeitura(int ID){
    Memoria *m = inicioPos;
    fprintf(tracer,"0 %.8x %s\n",ID,"Ler Memória");

    while (m != NULL)
    {
        if(m->ID == ID){
            return m->value; //Retorna o valor na memória
        }
        m = m->prox;
    }
    
    return -1; //Não encontrado na Memória

}

void memStore(int ID, int value){
    Memoria *m = inicioPos;
    Memoria *aux;

    fprintf(tracer,"1 %.8x %s\n",ID,"Escrever na Memória");

    while (m != NULL)
    {
        if(m->ID == ID){
            m->value = value;
            return; //Atualizou o valor;
        }
        aux = m;
        m = m->prox;
    }
    
    if(inicioPos == NULL){
        inicioPos = (Memoria *)malloc(sizeof(Memoria));
        inicioPos->ID = ID;
        inicioPos->value = value;
        return;
    }

    //Não encontrado, Criar Novo Endereço
    aux->prox = (Memoria *)malloc(sizeof(Memoria));
    aux->prox->ID = ID;
    aux->prox->value = value;

}

void setFlag(int a, int b, char *op){
    if(!strcmp(op,"=")){

        flagCods = (a == b ? 1 : 0);

    } else if(!strcmp(op,"<")){

        flagCods = (a < b ? 1 : 0);

    } else if(!strcmp(op,">")){
        
        flagCods = (a > b ? 1 : 0);
    }
}

int decodeMemRegImed(char *valueRaw){

    if(strchr(valueRaw,'x') != NULL){ //Imediato em Base 16 (Hexadecimal)
        return (int)strtol(valueRaw,NULL,16);
    }else if(strchr(valueRaw,'[') != NULL){ //Carrega da Memória
        valueRaw[strlen(valueRaw)-1] = '\0';
        char reg = valueRaw[0];
        int memaddr = reg >= 65 ? BancoRegistradores[toupper(reg) - 65] >= DATAADDR ? BancoRegistradores[toupper(reg) - 65] + atoi(valueRaw+2)*4 : DATAADDR + atoi(valueRaw+2)*4 : DATAADDR + atoi(valueRaw+2)*4;
        return memLeitura(memaddr);
    } else if(!isdigit(valueRaw[0])){ //Registrador
        return BancoRegistradores[toupper(valueRaw[0])-65];
    } else { //Valor Decimal
        return atoi(valueRaw);
    }

}

Label *ultimaLabel = NULL;

void readInstructions(char *instructions){

    //printf("%s\n",instructions);
    if(strchr(instructions,':') != NULL){ //Definição de Label
        strtok(instructions," ");
        instructions[strlen(instructions)-1] = '\0';
        if(ultimaLabel == NULL) ultimaLabel = addLabel(instructions); //Adciona Label e aguarda próxima instrução
        else strcpy(ultimaLabel->ID, instructions); //Label após Label
        return; //Não Atualiza PC
    } else {
        if(ultimaLabel != NULL){
            ultimaLabel->aponta = addInstruction(instructions);
            ultimaLabel = NULL;
            PC += 4;
            return;
        }
        addInstruction(instructions);
        PC += 4;
    }
}

void decodeInstrucao(Instrucao *ponteiroInstrucao){

    //printf("%s\n",ponteiroInstrucao->instrucao);
    char aux[100];
    strcpy(aux,ponteiroInstrucao->instrucao); //Mantem a integridade da instrução na memória
    char *opp = strtok(aux," "); //Decodifica o Opcode da Instrução
    if(!strcmp(opp,"MOV")){
        fprintf(tracer,"2 %.8x %s\n",ponteiroInstrucao->hexAddrs,"Move para Registrador");
        //MOV reg1 reg2/Mem/Ime
        int reg1 = toupper((strtok(NULL," ")[0]))-65; //Registrador de Destino [0,1,2 ou 3]
        int value = decodeMemRegImed(strtok(NULL," "));
        BancoRegistradores[reg1] = value;

    } else if(!strcmp(opp,"ADD")){
        fprintf(tracer,"2 %.8x %s\n",ponteiroInstrucao->hexAddrs,"Adição");
        //ADD reg1 reg2/Mem/Ime
        int reg1 = toupper((strtok(NULL," ")[0]))-65; //Registrador de Destino [0,1,2 ou 3]
        int value = decodeMemRegImed(strtok(NULL," "));
        BancoRegistradores[reg1] += value;

    } else if(!strcmp(opp,"SUB")){
        fprintf(tracer,"2 %.8x %s\n",ponteiroInstrucao->hexAddrs,"Subtração");
        //SUB reg1 reg2/Mem/Ime
        int reg1 = toupper((strtok(NULL," ")[0]))-65; //Registrador de Destino [0,1,2 ou 3]
        int value = decodeMemRegImed(strtok(NULL," "));
        BancoRegistradores[reg1] -= value;

    } else if(!strcmp(opp,"STR")){
        fprintf(tracer,"2 %.8x %s\n",ponteiroInstrucao->hexAddrs,"Guarda na Memória");
        //STR Mem Reg
        char *aux = strtok(NULL," ");
        aux[strlen(aux)-1] = '\0';
        char reg = aux[0];
        int memaddr = reg >= 65 ? BancoRegistradores[toupper(reg) - 65] >= DATAADDR ? BancoRegistradores[toupper(reg) - 65] + atoi(aux+2)*4 : DATAADDR + atoi(aux+2)*4 : DATAADDR + atoi(aux+2)*4;
        memStore(memaddr,decodeMemRegImed(strtok(NULL," ")));

        return;

    } else if(!strcmp(opp,"SIT")){
        fprintf(tracer,"2 %.8x %s\n",ponteiroInstrucao->hexAddrs,"Set if Equal");
        int a = decodeMemRegImed(strtok(NULL," "));
        char *op = strtok(NULL," ");
        int b = decodeMemRegImed(strtok(NULL," "));
        setFlag(a,b,op);
        return;

    } else if(!strcmp(opp,"GOF")){
        fprintf(tracer,"2 %.8x %s\n",ponteiroInstrucao->hexAddrs,"Jump Condicional");
        if(flagCods){
            jumpLabel(strtok(NULL," "));
        }

    } else if(!strcmp(opp,"JMP")){
        fprintf(tracer,"2 %.8x %s\n",ponteiroInstrucao->hexAddrs,"Jump");
        jumpLabel(strtok(NULL," ")); //Pula para a label, não atualiza o Ponteiro de Retorno

    } else if(!strcmp(opp,"JMR")){
        fprintf(tracer,"2 %.8x %s\n",ponteiroInstrucao->hexAddrs,"Finaliza Applicação");
        FPointer = ponteiroInstrucao->prox;
        jumpLabel(strtok(NULL," ")); //Pula para a Label, atualizando o Ponteiro de Retorno
    
    } else if(!strcmp(opp,"RET")){
        fprintf(tracer,"2 %.8x %s\n",ponteiroInstrucao->hexAddrs,"Retorno");
        PP = FPointer; //Retorna para a posição da chamada do JMR
    
    } else if(!strcmp(opp,"LIF")){
        fprintf(tracer,"2 %.8x %s\n",ponteiroInstrucao->hexAddrs,"Executa linha se verdadeiro");
        if(!flagCods){
            PP = ponteiroInstrucao->prox->prox; //Proxima instrução
        }

    } else if(!strcmp(opp,"HLT")){ //Halt
        fprintf(tracer,"2 %.8x %s\n",ponteiroInstrucao->hexAddrs,"Finaliza Código (HALT)");
        PP = NULL;

    } else if(!strcmp(opp,"PSH")){
        fprintf(tracer,"2 %.8x %s\n",ponteiroInstrucao->hexAddrs,"Inserir no Stack");
        //PSH reg
        int reg = toupper((strtok(NULL," ")[0]))-65; //Registrador [0,1,2 ou 3]
        pushStack(BancoRegistradores[reg]); //Empurra no Stack

    } else if(!strcmp(opp,"POP")){
        fprintf(tracer,"2 %.8x %s\n",ponteiroInstrucao->hexAddrs,"Retirar do Stack");
        //PSH reg
        int reg = toupper((strtok(NULL," ")[0]))-65; //Registrador [0,1,2 ou 3]
        BancoRegistradores[reg] = popStack(); //Retira do Stack

    }

    flagCods = 0;
}

int main(int argc, char const *argv[])
{
    if(argc < 2){
        printf("Uso Incorreto !!! Uso: %s <NomeArquivo.ktt>\n",argv[0]);
        return 0;
    }

    int flagComentario = 0; //É alto quando entra em um comentário e Baixo quando sai

    char linha[100]; //Buffer do Arquivo
    int letra; //Lê letra a letra
    arq = fopen(argv[1],"r"); //Abre o arquivo

    if(arq == NULL){ //Não foi possivel abrir / encontrar o arquivo
        printf("Não foi possível encontrar o arquivo!!!\n");
        return 0;
    }
    tracer = fopen("Tracer.txt","w");

    while ((letra = fgetc(arq)) != EOF && PC <= DATAADDR){
        if(letra == '/')flagComentario = 1; //Verdadeiro
        if(letra != '\n'){
            if(!flagComentario){
                concatCharString(linha,letra);
            }
            continue;
        }
        flagComentario = 0; //Falso
        if(strlen(linha) > 1) readInstructions(linha);
        memset(linha,0,100);
    }

    PP = inicioInstrucao; //Primrira instrução é carregada em PP

    //Execução do Código Carregado
    while (PP != NULL) {
        Instrucao *PPaux = PP;
        char aux[100];
        strcpy(aux,PP->instrucao);
        decodeInstrucao(PP);
        if(PP == PPaux)PP = PP->prox; //Ocorreu um Salto, não pegar próxima instrução
    }

    printf("Registradores:\n");
    for(int i = 0; i < 4; i++){
        printf("E%cX - 0x%X\n",(65 + i),BancoRegistradores[i]);
    }

    printf("Memoria:\n");
    memLog(inicioPos);
    memFree(inicioPos);
    printf("STACK:\n");
    memLog(SP);
    memFree(SP);
    printf("Labels:\n");
    labelLog();
    labelFree(inicioLabel);
    //printf("Instrucoes:\n");
    //instrucaoLog();
    instrucaoFree(inicioInstrucao);
    fclose(arq);
    fclose(tracer);
    
}
