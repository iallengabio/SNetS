# -*- coding: utf-8 -*-
"""
Created on Wed Apr  8 17:34:41 2020

@author: ialle
"""

import SNetSPy as sp
import SSTG as sstg

sol = ['MPH', 'MS', 'MVH']
loads = ['1092','1274','1456','1638','1820']

path1 = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/AGG/First/AGG'
path2 = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/AGG/First/MTGSR'
#sp.plotLines(path,ln,sc)
#sp.plotLines2(path1,path2,loads,loads,sol,'AGG','MTGSR',0.05)



#SSTG tunning
path = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/AGG/SSTG/NSFNet/SSTGTuning/MVH'
l = []
sol = ['SE-0', 'SE-1', 'SE-10', 'SE-11', 'SE-12', 'SE-13', 'SE-14', 'SE-15', 'SE-16', 'SE-17', 'SE-18', 'SE-19', 'SE-2', 'SE-20', 'SE-21', 'SE-22', 'SE-23', 'SE-24', 'SE-25', 'SE-26', 'SE-27', 'SE-28', 'SE-29', 'SE-3', 'SE-30', 'SE-31', 'SE-32', 'SE-33', 'SE-34', 'SE-35', 'SE-36', 'SE-37', 'SE-38', 'SE-39', 'SE-4', 'SE-40', 'SE-41', 'SE-42', 'SE-43', 'SE-44', 'SE-45', 'SE-46', 'SE-47', 'SE-48', 'SE-49', 'SE-5', 'SE-50', 'SE-51', 'SE-52', 'SE-53', 'SE-54', 'SE-55', 'SE-56', 'SE-57', 'SE-58', 'SE-59', 'SE-6', 'SE-60', 'SE-61', 'SE-62', 'SE-63', 'SE-64', 'SE-7', 'SE-8', 'SE-9']
#sp.plotBars2(path,path,l,l,sol,al=0.05)




#Ajuste de BVTs
path = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/AGG/SSTG/NSFNet/ajuste de BVTs/SRNP/MVH'
sp.computeMaxBVTs(path)
