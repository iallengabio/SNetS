library("ggplot2")

plotAuxBar <- function(arq, metric, directory, replicacoes, alpha, legLoads, legSol, legX, legY, bs){
  setwd(directory)
  arquivos = c(list.files(pattern=paste("*",arq,sep=""),recursive=TRUE))
  algoritmos = c(list.files(pattern=paste("*",arq,sep=""),recursive = TRUE))
  # primeiramente ler os arquivos
  plans = list();
  for(i in 1:length(arquivos)){
    plans[[i]] = read.csv(arquivos[i], header = TRUE, sep = ",", dec = ".", fill = TRUE, na.strings=" ")
  }
  
  
  #criar o dataframe extruturado para plotar os resultados usando ggplot2
  loads = c(plans[[1]][which(plans[[1]]$Metrics==metric),]$LoadPoint)
  medias = c()
  for(c in 1 : length(loads)){#para cada carga
    for(p in 1 : length(plans)){#para cada algoritmo
      medias = c(medias,mean(as.numeric(plans[[p]][which(plans[[p]]$Metrics==metric&plans[[p]]$LoadPoint==loads[c]),c((bs+1):(bs+replicacoes))])))
    }
  }
  erros = c()
  for(c in 1 : length(loads)){#para cada carga
    for(p in 1 : length(plans)){#para cada algoritmo
      sd = sd(as.numeric(plans[[p]][which(plans[[p]]$Metrics==metric&plans[[p]]$LoadPoint==loads[c]),c((bs+1):(bs+replicacoes))]));
      err = qt(1-(alpha/2), replicacoes-1) * sd / sqrt(replicacoes)
      erros = c(erros,err)
    }
  }
  
  legSol = paste0(legSol, "\t")
  
  df <- data.frame(
    carga = factor(rep(legLoads,each=length(plans))), #aqui devem ficar os pontos de carga (repetidos pelo nÃºmero de soluÃ§Ãµes RSA diferentes)
    bloqueio = medias,
    solucoes = factor(rep(legSol,length(loads))),
    upper = medias+erros,
    lower = medias-erros
  )
  
  ggplot(df, aes(x=carga, y=bloqueio, group=solucoes)) +
    geom_line(aes(linetype=solucoes, color=solucoes), size=1)+
    geom_point(aes(shape=solucoes, color=solucoes), size=3)+
    geom_errorbar(aes(ymin=lower, ymax=upper), width=.1) +
    theme_minimal()+
    labs(x = legX, y = legY)+
    theme(legend.position="bottom",legend.text = element_text(size=16), legend.title = element_blank(), legend.key.width = unit(2,"cm"))+
    scale_x_discrete(limits=legLoads)+
    theme(axis.text=element_text(size=16),axis.title=element_text(size=18,face="bold"))
  
  # p <- ggplot(df, aes(carga, bloqueio, fill = solucoes))
  # dodge <- position_dodge(width=0.9)
  # p + geom_col(position = dodge)+
  #   geom_errorbar(aes(ymin = lower, ymax = upper), position = dodge, width = 0.25)+
  #   scale_x_discrete(limits=legLoads)+
  #   labs(x = legX, y = legY)+
  #   theme(legend.position="bottom",legend.text = element_text(size=20), legend.title = element_blank())+
  #   theme(axis.text=element_text(size=20),axis.title=element_text(size=20,face="bold"))+
  #   guides(fill=guide_legend(nrow=2,keywidth = 1,byrow=TRUE))
}


blockingProbabilityBar <- function(directory, replicacoes, alpha, legLoads, legSol){
  arq="_BlockingProbability.csv"
  metric="Blocking probability"
  legX = "Carga na rede (Erlangs)"
  legY = "Bloqueio de requisições"
  bs = 6 #quantidade de colunas antes de come?arem os valores de cada replica??o
  
  plotAuxBar(arq, metric, directory, replicacoes, alpha, legLoads, legSol, legX, legY, bs)
}

bandwidthBlockingProbabilityBar <- function(directory, replicacoes, alpha, legLoads, legSol){
  arq="_BandwidthBlockingProbability.csv"
  metric="Bandwidth blocking probability"
  legX = "Carga na rede (Erlangs)"
  legY = "Bloqueio de banda"
  bs = 6 #quantidade de colunas antes de come?arem os valores de cada replica??o
  
  plotAuxBar(arq, metric, directory, replicacoes, alpha, legLoads, legSol, legX, legY, bs)
}

txUtilizationBar <- function(directory, replicacoes, alpha, legLoads, legSol){
  arq="TransmittersReceiversRegeneratorsUtilization.csv"
  metric="Tx Utilization"
  legX = "Carga na rede (Erlangs)"
  legY = "Utilização média de transeivers"
  bs = 4 #quantidade de colunas antes de come?arem os valores de cada replica??o
  plotAuxBar(arq, metric, directory, replicacoes, alpha, legLoads, legSol, legX, legY, bs)
}

spectrumUtilizationBar <- function(directory, replicacoes, alpha, legLoads, legSol){
  arq="SpectrumUtilization.csv"
  metric="Utilization"
  legX = "Carga na rede (Erlangs)"
  legY = "Utilização de espectro"
  bs = 6 #quantidade de colunas antes de come?arem os valores de cada replica??o
  plotAuxBar(arq, metric, directory, replicacoes, alpha, legLoads, legSol, legX, legY, bs)
}

energyConsumptionBar <- function(directory, replicacoes, alpha, legLoads, legSol){
  arq="EnergyConsumption.csv"
  metric="General power consumption"
  legX = "Carga na rede (Erlangs)"
  legY = "Consumo energético"
  bs = 6 #quantidade de colunas antes de come?arem os valores de cada replica??o
  plotAuxBar(arq, metric, directory, replicacoes, alpha, legLoads, legSol, legX, legY, bs)
}

#Gere seus gr?ficos a partir daqui utilizando as fun??es auxiliares
directory = "C:/Users/ialle/Documents/pen driver/Academico/Doutorado/Pesquisa/Grooming/Nova Modelagem Normalizado_Line"
replicacoes = 10;
alpha = 0.1
legLoads = c("1500","2000","2500")
legSol = c("MSM","MUE","MSV","MSF","FCCF")

blockingProbabilityBar(directory, replicacoes, alpha, legLoads, legSol)
bandwidthBlockingProbabilityBar(directory, replicacoes, alpha, legLoads, legSol)
spectrumUtilizationBar(directory, replicacoes, alpha, legLoads, legSol)
energyConsumptionBar(directory, replicacoes, alpha, legLoads, legSol)
txUtilizationBar(directory, replicacoes, alpha, legLoads, legSol)


