# -*- coding: utf-8 -*-
"""
Created on Thu Oct  3 13:37:59 2019

@author: ialle
"""

import pandas as pd #for csv manipulation
import SNetSPy as sp

#p1 = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/Experimentos MTGSR otimiziado/NSFNet/NSFNet EE.csv'
#p2 = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/Experimentos MTGSR otimiziado/Pacific/Pacific EE.csv'
#c1 = pd.read_csv(p1)
#c2 = pd.read_csv(p2)
#dfs1 = [c1]
#dfs2 = [c2]
#loads = ['MPH', 'MS', 'MVH', 'WAOR', 'WASA']
#sol = ['0']
#xl = 'Traffic Grooming Policies'
#yl = 'Maximum energy efficiency (b/J)'
#sp.SPlotBar2(dfs1,dfs2,loads,loads,sol,xl,xl,yl,yl,t1="NSFNet",t2="Pacific Bell",show=True,arq="",nc = 5)


path = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/New EsPAT OPH/NSFNet/WASA'
l = []
sol = ['SE-0', 'SE-08', 'SE-16', 'SE-20', 'SE-22', 'SE-23', 'SE-24', 'SE-25', 'SE-26', 'SE-28', 'SE-32', 'SE-48', 'SE-64']
sp.plotBars(path,l,sol,al=0.05)
