library("ggplot2")
source("C:/Users/ialle/Documents/workspace/IdeaProjects/SNetS/processing of results/RSNetS.R",encoding = "utf-8")

blockingProbability <- function(directory, replicacoes, alpha, legLoads, legSol){
  arq="_BlockingProbability.csv"
  metric="Blocking probability"
  legX = "Carga na rede (Erlangs)"
  legY = "Bloqueio de requisições"
  bs = 6 #quantidade de colunas antes de come?arem os valores de cada replica??o

  
  return(auxPlotLine(auxExtractDF(arq,metric,directory,replicacoes,alpha,bs,legSol),legLoads,legSol,legX,legY))
}

bandwidthBlockingProbability <- function(directory, replicacoes, alpha, legLoads, legSol){
  arq="_BandwidthBlockingProbability.csv"
  metric="Bandwidth blocking probability"
  legX = "Carga na rede (Erlangs)"
  legY = "Bloqueio de banda"
  bs = 6 #quantidade de colunas antes de come?arem os valores de cada replica??o

  return(auxPlotLine(auxExtractDF(arq,metric,directory,replicacoes,alpha,bs,legSol),legLoads,legSol,legX,legY))
}

txUtilization <- function(directory, replicacoes, alpha, legLoads, legSol){
  arq="TransmittersReceiversRegeneratorsUtilization.csv"
  metric="Tx Utilization"
  legX = "Carga na rede (Erlangs)"
  legY = "Utilização média de transeivers"
  bs = 4 #quantidade de colunas antes de come?arem os valores de cada replica??o
  return(auxPlotLine(auxExtractDF(arq,metric,directory,replicacoes,alpha,bs,legSol),legLoads,legSol,legX,legY))
}

spectrumUtilization <- function(directory, replicacoes, alpha, legLoads, legSol){
  arq="SpectrumUtilization.csv"
  metric="Utilization"
  legX = "Carga na rede (Erlangs)"
  legY = "Utilização de espectro"
  bs = 6 #quantidade de colunas antes de come?arem os valores de cada replica??o
  return(auxPlotLine(auxExtractDF(arq,metric,directory,replicacoes,alpha,bs,legSol),legLoads,legSol,legX,legY))
}

energyConsumption <- function(directory, replicacoes, alpha, legLoads, legSol){
  arq="EnergyConsumption.csv"
  metric="General power consumption"
  legX = "Carga na rede (Erlangs)"
  legY = "Consumo energético  (W)"
  bs = 6 #quantidade de colunas antes de come?arem os valores de cada replica??o
  res = auxPlotLine(auxExtractDF(arq,metric,directory,replicacoes,alpha,bs,legSol),legLoads,legSol,legX,legY)+scale_y_continuous(labels = scales::scientific)
  return(res)
}

consumedEnergy <- function(directory, replicacoes, alpha, legLoads, legSol){
  arq="ConsumedEnergy.csv"
  metric="Total consumed energy"
  legX = "Carga na rede (Erlangs)"
  legY = "Energia consumida (Wd)"
  bs = 3 #quantidade de colunas antes de come?arem os valores de cada replica??o
  return(auxPlotLine(auxExtractDF(arq,metric,directory,replicacoes,alpha,bs,legSol),legLoads,legSol,legX,legY)+scale_y_continuous(labels = scales::scientific))
}

costBenefit <- function(directory, replicacoes, alpha, legLoads, legSol){
  df = auxExtractDFCB(directory,replicacoes,alpha,legSol)
  return(auxPlotLine(df,legLoads,legSol,"Carga na rede","RCB"))
}

profit <- function(directory, replicacoes, alpha, legLoads, legSol){
  df = auxExtractDFProfit(directory,replicacoes,alpha,legSol)
  return(auxPlotLine(df,legLoads,legSol,"Carga na rede","Lucro ($)"))
}

maxBVTUtilization <- function(directory, replicacoes, alpha, legLoads, legSol){
  df = auxExtractDFMaxBVTUt(directory,replicacoes,alpha,legSol)
  return(auxPlotLine(df,legLoads,legSol,"Carga na rede","Utilização máxima de BVTs"))
}

#Gere seus gr?ficos a partir daqui utilizando as fun??es auxiliares
directory = "C:/Users/ialle/Documents/pen driver/Academico/Doutorado/Pesquisa/Grooming/Limitação de transeivers/MSF"
replicacoes = 10;
alpha = 0.05
legLoads = c("1","2","3","4")
legSol = c("20","25","30","35","40")
# legSol = c("MSM","MUE","MSV","MSF","FCCF")

blockingProbability(directory, replicacoes, alpha, legLoads, legSol)
bandwidthBlockingProbability(directory, replicacoes, alpha, legLoads, legSol)
spectrumUtilization(directory, replicacoes, alpha, legLoads, legSol)
energyConsumption(directory, replicacoes, alpha, legLoads, legSol)
txUtilization(directory, replicacoes, alpha, legLoads, legSol)
consumedEnergy(directory, replicacoes, alpha, legLoads, legSol)
costBenefit(directory, replicacoes, alpha, legLoads, legSol)
maxBVTUtilization(directory, replicacoes, alpha, legLoads, legSol)
profit(directory, replicacoes, alpha, legLoads, legSol)

#teste do git
