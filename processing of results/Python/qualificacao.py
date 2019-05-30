# -*- coding: utf-8 -*-
"""
Created on Wed Feb 27 05:59:22 2019
scripts gráficos qualificação
@author: Iallen
"""
import SNetSPy as sp
import pandas as pd

#s = ['BAS','MSU','MVH','MPH','IACF']
#s = [' SSTG\n(σ=40)','   SRNP\n(100Gbps)','     Sem\nmecanismo']
s = ['σ=0','σ=10','σ=20','σ=30','σ=40']
#s = [' SSTG\n(σ=40)','     SRNP\n(MRLB=100)','No mechanism']
#s = [' SSTG\n(σ=40)','     SRNP\n(MRLB=100)','     SRNP\n(MRLB=200)','     SRNP\n(MRLB=400)','No mechanism']
ln = ['800','1200','1600']
lp = ['326','653','979']
#l = ['267','400','533','667']
#sp.plotBars('C:/Users/ialle/Dropbox/Simulacoes/Doutorado/Experimentos qualificacao/NSFNet/EsPAT_TP/IACF',ln,s)
#sp.plotLines('C:/Users/ialle/Dropbox/Simulacoes/Doutorado/Experimentos qualificacao/NSFNet/EsPAT_TP/IACF',ln,s)

#dfs = sp.extractDFS('C:/Users/ialle/Dropbox/Simulacoes/Doutorado/Experimentos 2/Pacific/Politicas_TP',sp.ABBP,sp.MBBP,0.05)

sp.computeGains('C:/Users/ialle/Dropbox/Simulacoes/Doutorado/Experimentos qualificacao/Pacific/ZComp/IACF',0,save='gains')