# -*- coding: utf-8 -*-
"""
Created on Mon May 27 18:07:01 2019

@author: ialle
"""

import SNetSPy as sp
import pandas as pd

s = ['D0-00', 'D0-25', 'D0-50', 'D0-75', 'D1-00']
ln = ['728','910','1092','1274','1456']
lp = ['326','490','653','816','979']
le = ['1512','1814','2117','2419','2722']
lu = ['1766','2153','2539','2926','3312']
lc = ['1540','2310','3080','3850','4620']


sp.plotBars('C:/Users/ialle/Google Drive/Doutorado/Simulacoes/Experimentos IACF Delta/Pacific',lp,s)