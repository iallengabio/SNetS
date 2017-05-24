# SNetS
Slice Network Simulator

O SNetS é um simulador de redes ópticas elásticas voltado para a avaliação de desempenho de algoritmos de agregação de tráfego, roteamento, seleção de modulação e alocação de espectro. Você pode utilizar uma vasta gama de algoritmos GRMLSA já implementados ou implementar novas soluções de alocação de recursos em diversos cenários. O SNetS simula o comportamento da rede incluindo efeitos de camada física e permite a simulação de algoritmos em ambiente sujeito a falhas (sobrevivência de rede).

### Dependencies

- Java 8
- Maven
- GSON
- Firebase SDK

### Build

    $ mvn package

### Run
- Local Simulation


    $ java -jar target/snets-1.0-SNAPSHOT-jar-with-dependencies.jar DIR
      
Where `DIR` is the **absolute** path to directory holding simulation config
files.

- Simulation Server


    $ java -jar target/snets-1.0-SNAPSHOT-jar-with-dependencies.jar

In this way the program works as a simulation server.,