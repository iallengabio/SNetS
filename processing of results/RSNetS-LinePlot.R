  library("ggplot2")
  # ------------------------ PARAMETRIZAÃÃO -------------------------------
  arq="_BlockingProbability.csv"
  metric="Blocking probability"
  directory = "D:/Worksspaces/InteliJ/SNetS/simulations/Grooming"
  replicacoes = 10;
  alpha = 0.01
  
  legLoads = c("baixa","media","alta")
  legSol = c("MGMPH","MGMVH","NTG","STG")
  legX = "Carga na rede"
  legY = "Probabilidade de bloqueio"
  #--------------------------------------------------------------------------
  
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
      medias = c(medias,mean(as.numeric(plans[[p]][which(plans[[p]]$Metrics==metric&plans[[p]]$LoadPoint==loads[c]),c((6+1):(6+replicacoes))])))
    }
  }
  erros = c()
  for(c in 1 : length(loads)){#para cada carga
    for(p in 1 : length(plans)){#para cada algoritmo
      sd = sd(as.numeric(plans[[p]][which(plans[[p]]$Metrics==metric&plans[[p]]$LoadPoint==loads[c]),c((6+1):(6+replicacoes))]));
      err = qt(1-(alpha/2), replicacoes-1) * sd / sqrt(replicacoes)
      erros = c(erros,err)
    }
  }
  
  legSol = paste0(legSol, "\t")
  
  df <- data.frame(
    carga = factor(rep(legLoads,each=length(plans))), #aqui devem ficar os pontos de carga (repetidos pelo número de soluções RSA diferentes)
    bloqueio = medias,
    solucoes = factor(rep(legSol,length(loads))),
    upper = medias+erros,
    lower = medias-erros
  )
  
  ggplot(df, aes(x=carga, y=bloqueio, group=solucoes)) +
    geom_line(aes(linetype=solucoes))+
    geom_point(aes(shape=solucoes))+
    theme_minimal()+
    labs(x = legX, y = legY)+
    theme(legend.position="bottom",legend.text = element_text(size=16), legend.title = element_blank(), legend.key.width = unit(2,"cm"))+
    geom_errorbar(aes(ymin=lower, ymax=upper), width=.1) +
    scale_x_discrete(limits=legLoads)+
    theme(axis.text=element_text(size=16),axis.title=element_text(size=18,face="bold"))
    
    
  
