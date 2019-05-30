library("ggplot2")
library("ggpubr")
source("C:/Users/ialle/Documents/workspace/IdeaProjects/SNetS/processing of results/RSNetS-LinePlot.R",encoding = "utf-8")

metricPlots = c(blockingProbability,bandwidthBlockingProbability,consumedEnergy,costBenefit,energyConsumption,maxBVTUtilization,spectrumUtilization,profit)
#metricPlots = c(costBenefit)

directory1 = "C:/Users/ialle/Documents/pen driver/Academico/Doutorado/Pesquisa/Grooming/Nova Modelagem Normalizado_Line_NSFNet_New"
directory2 = "C:/Users/ialle/Documents/pen driver/Academico/Doutorado/Pesquisa/Grooming/Nova Modelagem Normalizado_Line_NSFNet"
replicacoes = 10
alpha = 0.1
legLoads1 = c("491","655","819")
legLoads2 = c("1500","2000","2500")
legSol = c("MSM","MUE","MSV","MSF","FCCF")
#legSol = c("20","25","30","35","40")

title1 = "NSFNet"
title2 = "USA"
nc = 2
nr = 1
plots = c();
for (metricPlot in metricPlots) {
  legLoads = legLoads1
  p1 <- metricPlot(directory1, replicacoes, alpha, legLoads, legSol) +
        ggtitle(title1) + theme(plot.title = element_text(face="bold", size=28, hjust=0.5))
  legLoads = legLoads2
  p2 <- metricPlot(directory2, replicacoes, alpha, legLoads, legSol) +
        ggtitle(title2) + theme(plot.title = element_text(face="bold", size=28, hjust=0.5))
  show(ggarrange(p1, p2, ncol=nc, nrow=nr, common.legend = TRUE, legend="bottom"))
}

