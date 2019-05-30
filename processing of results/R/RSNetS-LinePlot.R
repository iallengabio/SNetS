library("ggplot2")
source("C:/Users/ialle/Dropbox/workspaces/ideaProjects/SNetS/processing of results/RSNetS.R",encoding = "utf-8")

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
  arq="_ConsumedEnergy.csv"
  metric="Average power consumption (Watt)"
  legX = "Load (Erlangs)"
  legY = "Average power consumption (Watt)"
  bs = 3 #quantidade de colunas antes de come?arem os valores de cada replica??o
  res = auxPlotLine(auxExtractDF(arq,metric,directory,replicacoes,alpha,bs,legSol),legLoads,legSol,legX,legY)+scale_y_continuous(labels = scales::scientific)
  return(res)
}

consumedEnergy <- function(directory, replicacoes, alpha, legLoads, legSol){
  arq="_ConsumedEnergy.csv"
  metric="Total consumed energy (Joule)"
  legX = "Load (Erlangs)"
  legY = "Total energy consumption (Joule)"
  bs = 3 #quantidade de colunas antes de come?arem os valores de cada replica??o
  df = auxExtractDF(arq,metric,directory,replicacoes,alpha,bs,legSol)
  return(auxPlotLine(df,legLoads,legSol,legX,legY)+scale_y_continuous(labels = scales::scientific))
}

consumedEnergyOld <- function(directory, replicacoes, alpha, legLoads, legSol){
  arq="ConsumedEnergy.csv"
  metric="Total consumed energy"
  legX = "Load (Erlangs)"
  legY = "Total energy consumption (Joule) Iallen"
  bs = 3 #quantidade de colunas antes de come?arem os valores de cada replica??o
  df = auxExtractDF(arq,metric,directory,replicacoes,alpha,bs,legSol)
  return(auxPlotLine(df,legLoads,legSol,legX,legY)+scale_y_continuous(labels = scales::scientific))
}

consumedEnergyBVT <- function(directory, replicacoes, alpha, legLoads, legSol){
  arq="_ConsumedEnergy.csv"
  metric="Total energy consumption by transponders (Joule)"
  legX = "Load (Erlangs)"
  legY = "Total energy consumption by transponders (Joule)"
  bs = 3 #quantidade de colunas antes de come?arem os valores de cada replica??o
  df = auxExtractDF(arq,metric,directory,replicacoes,alpha,bs,legSol)
  return(auxPlotLine(df,legLoads,legSol,legX,legY)+scale_y_continuous(labels = scales::scientific))
}

consumedEnergyOXC <- function(directory, replicacoes, alpha, legLoads, legSol){
  arq="_ConsumedEnergy.csv"
  metric="Total energy consumption by OXCs (Joule)"
  legX = "Load (Erlangs)"
  legY = "Total energy consumption by OXCs (Joule)"
  bs = 3 #quantidade de colunas antes de come?arem os valores de cada replica??o
  df = auxExtractDF(arq,metric,directory,replicacoes,alpha,bs,legSol)
  return(auxPlotLine(df,legLoads,legSol,legX,legY)+scale_y_continuous(labels = scales::scientific))
}

consumedEnergyAmp <- function(directory, replicacoes, alpha, legLoads, legSol){
  arq="_ConsumedEnergy.csv"
  metric="Total energy consumption by Amplifiers (Joule)"
  legX = "Load (Erlangs)"
  legY = "Total energy consumption by Amplifiers (Joule)"
  bs = 3 #quantidade de colunas antes de come?arem os valores de cada replica??o
  df = auxExtractDF(arq,metric,directory,replicacoes,alpha,bs,legSol)
  return(auxPlotLine(df,legLoads,legSol,legX,legY)+scale_y_continuous(labels = scales::scientific))
}

EnergyEfficiency<- function(directory, replicacoes, alpha, legLoads, legSol){
  arq="_ConsumedEnergy.csv"
  metric="Energy efficiency (bits/Joule)"
  legX = "Carga na rede (Erlangs)"
  legY = "Energy efficiency (bits/Joule)"
  bs = 3 #quantidade de colunas antes de come?arem os valores de cada replica??o
  df = auxExtractDF(arq,metric,directory,replicacoes,alpha,bs,legSol)
  return(auxPlotLine(df,legLoads,legSol,legX,legY)+scale_y_continuous(labels = scales::scientific))
}

costBenefit <- function(directory, replicacoes, alpha, legLoads, legSol){
  df = auxExtractDFCB(directory,replicacoes,alpha,legSol)
  return(auxPlotLine(df,legLoads,legSol,"Carga na rede","RCB"))
}

profit <- function(directory, replicacoes, alpha, legLoads, legSol){
  df = auxExtractDFProfit(directory,replicacoes,alpha,legSol)
  show(df)
  return(auxPlotLine(df,legLoads,legSol,"Carga na rede","Lucro ($)"))
}

maxBVTUtilization <- function(directory, replicacoes, alpha, legLoads, legSol){
  df = auxExtractDFMaxBVTUt(directory,replicacoes,alpha,legSol)
  return(auxPlotLine(df,legLoads,legSol,"Carga na rede","Utilização máxima de BVTs"))
}

#Gere seus gr?ficos a partir daqui utilizando as fun??es auxiliares
directory = "C:/Users/ialle/Dropbox/Simulações/6 pontos de carga/Sigma"
replicacoes = 10;
alpha = 0.05
legLoads = c("400","533","667","800","933","1067")
legSol = c("s0","s10","s20","s30","s40","s50","s60","s70","s80","s90")

# 
# blockingProbability(directory, replicacoes, alpha, legLoads, legSol)
# bandwidthBlockingProbability(directory, replicacoes, alpha, legLoads, legSol)
# spectrumUtilization(directory, replicacoes, alpha, legLoads, legSol)
# txUtilization(directory, replicacoes, alpha, legLoads, legSol)
# costBenefit(directory, replicacoes, alpha, legLoads, legSol)
# maxBVTUtilization(directory, replicacoes, alpha, legLoads, legSol)
# profit(directory, replicacoes, alpha, legLoads, legSol)
# energyConsumption(directory, replicacoes, alpha, legLoads, legSol)
# consumedEnergy(directory, replicacoes, alpha, legLoads, legSol)
# consumedEnergyBVT(directory, replicacoes, alpha, legLoads, legSol)
# consumedEnergyOXC(directory, replicacoes, alpha, legLoads, legSol)
# consumedEnergyAmp(directory, replicacoes, alpha, legLoads, legSol)
# EnergyEfficiency(directory, replicacoes, alpha, legLoads, legSol)
