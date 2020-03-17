# -*- coding: utf-8 -*-
"""
Created on Mon May 27 18:07:01 2019

@author: ialle
"""



import SNetSPy as sp
import pandas as pd

s = ['A0-00', 'A0-25', 'A0-50', 'A0-75', 'A1-00','A1-25','A1-50']
sm = ['IACF_A_0-75', 'IACF_B_0-75', 'IACF_C_0-75']
sc = ['BAS', 'BAS-GF', 'MPH', 'MPH-GF', 'MS', 'MS-GF', 'MSI', 'MSI-GF', 'MVH', 'MVH-GF', 'SWAF', 'TMRF', 'WASA']
sc2 = ['MPH', 'MS', 'MVH', 'WAOR', 'WASA']
ln = ['728','910','1092','1274','1456']
#lp = ['326','490','653','816','979']
lp = ['326','571','816','1061','1306']
le = ['1512','1814','2117','2419','2722']
lu = ['1766','2153','2539','2926','3312']
lc = ['4620']

path1 = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/Experimentos MTGSR otimiziado/NSFNet/BVTs ajustados'
path2 = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/Experimentos MTGSR otimiziado/Pacific/BVTs ajustados 2'
#sp.plotLines(path,ln,sc)
sp.plotLines2(path1,path2,ln,lp,sc2,'NSFNet','Pacific Bell',0.05)

      

def compGains(pathG,lProp,lims):
    for i in range(len(lProp)):
        path = pathG + lProp[i]
        ims = lims[i]
        arq = 'gains'
        sp.computeGains(path,ims,save=arq)

pathG = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/Experimentos MTGSR otimiziado/Pacific/Gains3/'
lProp = ['WAOR', 'WASA']
lims = [3,3]
#gs = compGains(pathG,lProp,lims)
#sp.auxPlotBars(gs,lp,['WAOR','WASA'],"","")

#sp.computeGains(path, 4, save = "gains-a")

#dfs = sp.extractDFS(path1,'_BandwidthBlockingProbability.csv','Bandwidth blocking probability',0.05)