# -*- coding: utf-8 -*-
"""
Created on Wed Apr  8 17:34:41 2020

@author: ialle
"""

import SNetSPy as sp
import pandas as pd

sol = ['MPH', 'MS', 'MVH']
loads = ['1092','1274','1456','1638','1820']

path1 = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/AGG/First/AGG'
path2 = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/AGG/First/MTGSR'
#sp.plotLines(path,ln,sc)
sp.plotLines2(path1,path2,loads,loads,sol,'AGG','MTGSR',0.05)