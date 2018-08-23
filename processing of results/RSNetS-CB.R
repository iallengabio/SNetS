library("ggplot2")
auxPlotLine <- function(dataframe, legLoads, legSol, legX, legY){
    ggplot(df, aes(x=carga, y=value, group=solucoes)) +
    geom_line(aes(linetype=solucoes, color=solucoes), size=1)+
    geom_point(aes(shape=solucoes, color=solucoes), size=3)+
    geom_errorbar(aes(ymin=lower, ymax=upper), width=.1) +
    theme_minimal()+
    labs(x = legX, y = legY)+
    theme(legend.position="bottom",legend.text = element_text(size=20), legend.title = element_blank(), legend.key.width = unit(2,"cm"))+
    scale_x_discrete(limits=legLoads)+
    theme(axis.text=element_text(size=20),axis.title=element_text(size=20,face="bold"))
}

auxPlotBar <- function(dataframe, legLoads, legSol, legX, legY){
    p <- ggplot(df, aes(carga, value, fill = solucoes))
    dodge <- position_dodge(width=0.9)
    p + geom_col(position = dodge)+
    geom_errorbar(aes(ymin = lower, ymax = upper), position = dodge, width = 0.25)+
    scale_x_discrete(limits=legLoads)+
    labs(x = legX, y = legY)+
    theme(legend.position="bottom",legend.text = element_text(size=20), legend.title = element_blank())+
    theme(axis.text=element_text(size=20),axis.title=element_text(size=20,face="bold"))+
    guides(fill=guide_legend(nrow=2,keywidth = 1,byrow=TRUE))
}

auxExtractDF <- function(arq,metric,directory,replicacoes,alpha,bs, legSol){
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
    value = medias,
    solucoes = factor(rep(legSol,length(loads))),
    upper = medias+erros,
    lower = medias-erros
  )
  return(df)
}

auxExtractDFCB <- function(directory,replicacoes,alpha,legSol){
  #read bandwidth blocking
  arq = "_BandwidthBlockingProbability.csv"
  setwd(directory)
  arquivos = c(list.files(pattern=paste("*",arq,sep=""),recursive=TRUE))
  algoritmos = c(list.files(pattern=paste("*",arq,sep=""),recursive = TRUE))
  plansBB = list();
  for(i in 1:length(arquivos)){
    plansBB[[i]] = read.csv(arquivos[i], header = TRUE, sep = ",", dec = ".", fill = TRUE, na.strings=" ")
  }
  
  #read consumed energy
  arq="ConsumedEnergy.csv"
  arquivos = c(list.files(pattern=paste("*",arq,sep=""),recursive=TRUE))
  algoritmos = c(list.files(pattern=paste("*",arq,sep=""),recursive = TRUE))
  plansCE = list();
  for(i in 1:length(arquivos)){
    plansCE[[i]] = read.csv(arquivos[i], header = TRUE, sep = ",", dec = ".", fill = TRUE, na.strings=" ")
  }
  
  #criar o dataframe extruturado para plotar os resultados usando ggplot2
  #bitCost = 0.000000006
  #energyCost = 0.0002
  bitCost = 0.0000000000003 #de acordo com http://www2.telegeography.com/hubfs/2017/presentations/telegeography-ptc17-pricing.pdf
  energyCost = 0.00000003 #de acordo com https://www.ovoenergy.com/guides/energy-guides/average-electricity-prices-kwh.html
  bsBB = 6
  bsCE = 3
  metricBB = "Bandwidth blocking probability"
  metricRB = "General requested bandwidth"
  metricCE = "Total consumed energy"
  loads = c(plansBB[[1]][which(plansBB[[1]]$Metrics==metricBB),]$LoadPoint)
  medias = c()
  for(c in 1 : length(loads)){#para cada carga
    for(p in 1 : length(plansBB)){#para cada algoritmo
      auxBB = as.numeric(plansBB[[p]][which(plansBB[[p]]$Metrics==metricBB&plansBB[[p]]$LoadPoint==loads[c]),c((bsBB+1):(bsBB+replicacoes))])
      auxRB = as.numeric(plansBB[[p]][which(plansBB[[p]]$Metrics==metricRB&plansBB[[p]]$LoadPoint==loads[c]),c((bsBB+1):(bsBB+replicacoes))])
      auxCE = as.numeric(plansCE[[p]][which(plansCE[[p]]$Metrics==metricCE&plansCE[[p]]$LoadPoint==loads[c]),c((bsCE+1):(bsCE+replicacoes))])
      dataTransfered = (1-auxBB) * auxRB
      costBenefit = (dataTransfered*bitCost)/(auxCE * energyCost)
      medias = c(medias,mean(costBenefit))
    }
  }
  
  erros = c()
  for(c in 1 : length(loads)){#para cada carga
    for(p in 1 : length(plansBB)){#para cada algoritmo
      auxBB = as.numeric(plansBB[[p]][which(plansBB[[p]]$Metrics==metricBB&plansBB[[p]]$LoadPoint==loads[c]),c((bsBB+1):(bsBB+replicacoes))])
      auxRB = as.numeric(plansBB[[p]][which(plansBB[[p]]$Metrics==metricRB&plansBB[[p]]$LoadPoint==loads[c]),c((bsBB+1):(bsBB+replicacoes))])
      auxCE = as.numeric(plansCE[[p]][which(plansCE[[p]]$Metrics==metricCE&plansCE[[p]]$LoadPoint==loads[c]),c((bsCE+1):(bsCE+replicacoes))])
      dataTransfered = auxBB * auxRB
      costBenefit = (dataTransfered*bitCost)/(auxCE * energyCost)
      sd = sd(costBenefit);
      err = qt(1-(alpha/2), replicacoes-1) * sd / sqrt(replicacoes)
      erros = c(erros,err)
    }
  }
  
  legSol = paste0(legSol, "\t")
  
  df <- data.frame(
    carga = factor(rep(legLoads,each=length(plansBB))), #aqui devem ficar os pontos de carga (repetidos pelo nÃºmero de soluÃ§Ãµes RSA diferentes)
    value = medias,
    solucoes = factor(rep(legSol,length(loads))),
    upper = medias+erros,
    lower = medias-erros
  )
  return(df)
  
  
}

directory = "C:/Users/ialle/Documents/pen driver/Academico/Doutorado/Pesquisa/Grooming/Nova Modelagem Normalizado_Line"
replicacoes = 10;
alpha = 0.1
legLoads = c("1500","2000","2500")
legSol = c("MSM","MUE","MSV","MSF","FCCF")

df = auxExtractDFCB(directory,replicacoes,alpha,legSol)
auxPlotLine(df,legLoads,legSol,"Carga na rede","RCB")
