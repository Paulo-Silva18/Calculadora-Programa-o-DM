# Calculadora — Atividade 05 (Programação para Dispositivos Móveis)

Calculadora funcional desenvolvida em **Kotlin** com **Jetpack Compose**, feita para a
Atividade 05 da disciplina de Programação para Dispositivos Móveis (IFTM Campus Patrocínio).

## Funcionalidades

- Dígitos de 0 a 9, ponto decimal e as quatro operações fundamentais (+, −, ×, ÷)
- Botões **C** (limpar) e **⌫** (apagar último caractere)
- Botão **=** com cálculo respeitando a precedência matemática (potência > multiplicação/divisão > soma/subtração), implementado com o algoritmo *Shunting-Yard* + avaliação em notação polonesa reversa (RPN)
- Tratamento de exceções: divisão por zero, expressões inválidas e resultados indefinidos exibem uma mensagem de erro no visor em vez de derrubar o app
- Travas de entrada:
  - Bloqueia o acionamento de operadores matemáticos consecutivos
  - Bloqueia múltiplos pontos decimais no mesmo número
  - Bloqueia início de expressão com operador
- **Desafio extra (calculadora científica):** seno, cosseno, tangente (em graus), logaritmo base 10 e potenciação (xʸ), acessíveis por um teclado expansível (botão "Funções científicas") que revela uma linha extra de botões
- UI personalizada: paleta de cores escura com destaque laranja para operadores, verde para o "=", vermelho para limpar, botões circulares e visor de alto contraste

## Estrutura do projeto

```
Calculadora/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/iftm/calculadora/
│           ├── MainActivity.kt        # UI em Jetpack Compose
│           └── CalculatorEngine.kt    # Tokenização, precedência (Shunting-Yard) e avaliação
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Como compilar e executar

### Pré-requisitos

- [Android Studio](https://developer.android.com/studio) (versão Koala/2024.1 ou mais recente recomendado)
- JDK 17 (normalmente já incluso no Android Studio)
- Um emulador Android (API 24+) ou um dispositivo físico com depuração USB habilitada

### Passo a passo

1. Clone o repositório:
   ```bash
   git clone https://github.com/Paulo-Silva18/Calculadora-Programa-o-DM.git
   cd Calculadora
   ```
2. Abra a pasta do projeto no Android Studio (**File → Open**).
3. Aguarde o **Gradle Sync** automático (o Android Studio baixa as dependências do Compose e do AGP declaradas nos arquivos `build.gradle.kts`).
   - Caso o Android Studio sugira atualizar o Android Gradle Plugin (AGP) ou o Kotlin, pode aceitar — as versões usadas aqui (AGP 8.5, Kotlin 1.9.24, Compose BOM 2024.06.00) são compatíveis com Android Studio recentes.
4. Selecione um emulador ou conecte um dispositivo físico.
5. Clique em **Run ▶** (ou `Shift+F10`) para compilar e instalar o app.

### Compilar via linha de comando (opcional)

```bash
./gradlew assembleDebug
```
O APK gerado ficará em `app/build/outputs/apk/debug/app-debug.apk`.

## Observações de design

- A lógica de expressão é construída como uma string única (ex.: `"12+7*3"`) e avaliada
  apenas quando o botão **=** é pressionado, permitindo respeitar a precedência das
  operações mesmo digitando "de corrida" (sem parênteses).
- As funções científicas (seno, cosseno, tangente, log) são aplicadas **imediatamente**
  sobre o número atualmente digitado — como em calculadoras físicas — em vez de fazerem
  parte da árvore de precedência, o que simplifica o parser sem perder a robustez do
  cálculo aritmético principal.
- Ângulos trigonométricos são interpretados em **graus**.
