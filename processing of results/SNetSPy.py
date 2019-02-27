# -*- coding: utf-8 -*-
"""
Spyder Editor

Este é um arquivo de script temporário.
"""

import pandas as pd #for csv manipulation
import glob #for filtering
import scipy.stats as st #for statistics
import matplotlib.pyplot as plt
import numpy as np


def extractDFS(path,arq,metric,alpha):
    bbp = glob.glob(path + '/*/*'+arq)
    bbp = [s.replace('\\','/') for s in bbp]
    solutions = [s.split('/') for s in bbp]
    solutions = [s[len(s)-2] for s in solutions]#serach name of solutions
    bbp = [pd.read_csv(s) for s in bbp] #read files
    bbp = [d[d.Metrics==metric] for d in bbp] #filter metric
    loads = bbp[0]['LoadPoint'].tolist() #get load points
    bbp = [d.filter(like='rep',axis=1) for d in bbp]
    avg = [d.mean(axis=1).tolist() for d in bbp]
    numRep = len(bbp[0].columns)
    sd = [st.sem(d,axis=1,ddof=numRep-1).tolist() for d in bbp]
    errorIntervals = [st.t.interval(1-alpha,numRep-1,loc=avg[i],scale=sd[i]) for i in range(len(avg))]
    dfs = [pd.DataFrame() for i in range(len(avg))]
    errors = [[errorIntervals[i][1].tolist()[j] - avg[i][j] for j in range(len(avg[i]))] for i in range(len(avg))] #error = limSup - mean
    errors = sd
    for i in range(len(avg)):
        dfs[i]['loads'] = loads
        dfs[i]['mean'] = avg[i]
        dfs[i]['errors'] = errors[i]
    return dfs

def auxPlotLine(dfs, loads, sol, xl="", yl="", lp = 'upper center'):
    fs = 18
    markers = ["o","v","^","s","P","x","D","_","*","2"]
    linestyles = ['-', '--', '-.', ':','-', '--', '-.', ':','-', '--']
    #plt.figure(figsize=(10,7))
    for i in range(len(dfs)):
        x = dfs[i]['loads'];
        y = dfs[i]['mean']
        e = dfs[i]['errors']
        plt.errorbar(x, y, xerr=0, yerr=e, linestyle=linestyles[i], marker=markers[i], label=sol[i])
        plt.xticks(x, loads)
        
    plt.xlabel(xl,fontsize=fs)
    plt.ylabel(yl,fontsize=fs)   
    plt.xticks(fontsize=fs)
    plt.yticks(fontsize=fs)
    plt.grid(axis='y')
    if len(sol) > 5:
        nc = 5
    else:
        nc = len(sol)
    plt.legend(loc=lp, ncol = 1, fontsize=fs)
    plt.savefig("S_EC_NSFNet_MPH.pdf", dpi=150,bbox_inches='tight')
    plt.show()
    
def auxPlotBars(dfs, loads, sol, xl="", yl="", lp = 'upper center'):
    fs = 30
    width = 0.09
    ind = dfs[0]['loads']
    plt.figure(figsize=(30,9))
    for i in range(len(dfs)):
        plt.bar(ind+i*width, dfs[i]['mean'], width, yerr=dfs[i]['errors'], label=sol[i])
    
    plt.ylabel(yl, fontsize=fs)
    plt.xlabel(xl, fontsize=fs)
    plt.xticks(ind+width*4, loads, fontsize=fs)
    plt.yticks(fontsize=fs)
    plt.grid(axis='y')
    if len(sol) > 5:
        nc = 5
    else:
        nc = len(sol)
    plt.legend(loc=lp, ncol = nc, fontsize=fs)
    plt.savefig("TBB_NFSNet_MVH_SRNP.pdf", dpi=150,bbox_inches='tight')
    plt.show()

p = 'C:/Users/ialle/Dropbox/Simulacoes/Simp/NSFNet/MPH'
#a = '_BandwidthBlockingProbability.csv'
#a = '_GroomingStatistics.csv'
a = '_ConsumedEnergy.csv'
#a = '_TransmittersReceiversRegeneratorsUtilization.csv'
#m = 'Bandwidth blocking probability'
#m = 'Rate of requests by circuit'
m = 'Total consumed energy (Joule)'
#m = 'Tx Utilization'
al = 0.05

dfs = extractDFS(p,a,m,al)
loads = ['267','400','533','667']
#sol = ['σ=0','σ=10','σ=20','σ=30','σ=40','σ=50','σ=60','σ=70','σ=80','σ=90']
#sol = ['0Gbps','50Gbps','100Gbps','150Gbps','200Gbps','250Gbps','300Gbps','350Gbps','400Gbps']
sol = ['SRNP (200Gbps)','Sem mecanismo', 'EsPEC (σ=40)']
xl = 'Carga na rede (erlangs)'
yl = 'EC (Joules)'

#srnpGain = np.asarray(dfs[0]['mean'])/np.asarray(dfs[1]['mean']) - 1
#especGain = np.asarray(dfs[2]['mean'])/np.asarray(dfs[1]['mean']) - 1

#auxPlotBars(dfs,loads,sol,xl,yl)
auxPlotLine(dfs,loads,sol,xl,yl,'upper right')



