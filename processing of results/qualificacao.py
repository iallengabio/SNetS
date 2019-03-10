# -*- coding: utf-8 -*-
"""
Created on Wed Feb 27 05:59:22 2019
scripts gráficos qualificação
@author: Iallen
"""
import SNetSPy as sp
import pandas as pd

#s = ['BAS','MSU','MVH','MPH','IACF']
#s = ['σ=0','σ=10','σ=20','σ=30','σ=40']
s = ['EsPAT (σ=40)','SRNP100','Sem mecanismo']
l = ['800','1200','1600']
lp = ['326','653','979']
#sp.plotBars('C:/Users/ialle/Dropbox/Simulacoes/Doutorado/Experimentos qualificacao/Pacific/EsPAT_TP/MSU',loads=l,sol=s)
#sp.plotLines('C:/Users/ialle/Dropbox/Simulacoes/Doutorado/Experimentos qualificacao/Pacific/ZComp/IACF',loads=lp,sol=s)

#dfs = sp.extractDFS('C:/Users/ialle/Dropbox/Simulacoes/Doutorado/Experimentos 2/Pacific/Politicas_TP',sp.ABBP,sp.MBBP,0.05)

sp.computeGains('C:/Users/ialle/Dropbox/Simulacoes/Doutorado/Experimentos qualificacao/NSFNet/Politicas_TP',4,save='gains')