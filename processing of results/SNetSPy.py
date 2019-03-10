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

#arquivos
ABBP = '_BandwidthBlockingProbability.csv'
AGS = '_GroomingStatistics.csv'
ACE = '_ConsumedEnergy.csv'
ATRRU = '_TransmittersReceiversRegeneratorsUtilization.csv'
AGS = '_GroomingStatistics.csv'

#métricas
MBBP = 'Bandwidth blocking probability'
MRRC = 'Rate of requests by circuit'
MTCE = 'Total consumed energy (Joule)'
MAPC = 'Average power consumption (Watt)'
MTXU = 'Tx Utilization'
MRRC = 'Rate of requests by circuit'

#aux
markers = ["o","v","^","s","P","x","D","_","*","2"]
linestyles = ['-', '--', '-.', ':','-', '--', '-.', ':','-', '--']



def minGains(dfs,ims,inv=False):
    mySol = dfs[ims]
    del dfs[ims]
    if inv:
        dfs = [(mySol/d)-1 for d in dfs]
    else:
        dfs = [1-(mySol/d) for d in dfs]
    dfs = [d['mean'].tolist() for d in dfs]
    t = [[d[i] for d in dfs] for i in range(len(dfs[0]))]
    minGains = [min(d) for d in t]
    return minGains

def extractDFS(path,arq,metric,alpha):
    bbp = glob.glob(path + '/*/*'+arq)
    bbp = [s.replace('\\','/') for s in bbp]
    solutions = [s.split('/') for s in bbp]
    solutions = [s[len(s)-2] for s in solutions]#serach name of solutions
    print(solutions)
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

def extractDFSMBVTU(path,alpha):
    MTUPN = 'Max Tx Utilization Per Node'
    MRUPN = 'Max Rx Utilization Per Node'
    arq = ATRRU
    trr = glob.glob(path + '/*/*'+arq)
    trr = [s.replace('\\','/') for s in trr]
    solutions = [s.split('/') for s in trr]
    solutions = [s[len(s)-2] for s in solutions]#search name of solutions
    print(solutions)
    trr = [pd.read_csv(s) for s in trr] #read files
    trrA = [d[d.Metrics==MTXU] for d in trr] #filter metric
    loads = trrA[0]['LoadPoint'].tolist() #get load points
    trr1 = [d[d.Metrics==MTUPN] for d in trr] #filter metric
    trr1 = [d.groupby(['LoadPoint']).sum()[list(d.filter(like='rep',axis=1))] for d in trr1]
    trr2 = [d[d.Metrics==MRUPN] for d in trr] #filter metric
    trr2 = [d.groupby(['LoadPoint']).sum()[list(d.filter(like='rep',axis=1))] for d in trr2]
    trr = [trr1[i]+trr2[i] for i in list(range(0,len(solutions)))]
    # bcr = [(((1-bbp[i])*grb[i])*vbt)/(ce[i]*cws) for i in list(range(0,len(solutions)))]
    avg = [d.mean(axis=1).tolist() for d in trr]
    numRep = len(trr[0].columns)
    sd = [st.sem(d,axis=1,ddof=numRep-1).tolist() for d in trr]
    errorIntervals = [st.t.interval(1-alpha,numRep-1,loc=avg[i],scale=sd[i]) for i in range(len(avg))]
    dfs = [pd.DataFrame() for i in range(len(avg))]
    errors = [[errorIntervals[i][1].tolist()[j] - avg[i][j] for j in range(len(avg[i]))] for i in range(len(avg))] #error = limSup - mean
    errors = sd
    for i in range(len(avg)):
        dfs[i]['loads'] = loads
        dfs[i]['mean'] = avg[i]
        dfs[i]['errors'] = errors[i]
    return dfs

def extractDFSBCR(path,alpha):
    p = path
    timeAjust = 31536000
    arq1 = '_BandwidthBlockingProbability.csv'
    arq2 = '_ConsumedEnergy.csv'
    m1 = 'Bandwidth blocking probability'
    m2 = 'General requested bandwidth'
    m3 = 'Total consumed energy (Joule)'
    vbt = 5.6e-12 * timeAjust
    cws = 3.3e-8 * timeAjust
    aux = glob.glob(p + '/*/*'+arq1)
    aux = [s.replace('\\','/') for s in aux]
    solutions = [s.split('/') for s in aux]
    solutions = [s[len(s)-2] for s in solutions]#serach name of solutions
    print(solutions)
    aux = [pd.read_csv(s) for s in aux] #read files
    bbp = [d[d.Metrics==m1] for d in aux] #filter metric
    loads = bbp[0]['LoadPoint'].tolist() #get load points
    bbp = [d.filter(like='rep',axis=1) for d in bbp]
    grb = [d[d.Metrics==m2] for d in aux] #filter metric
    grb = [d.filter(like='rep',axis=1).reset_index(drop=True) for d in grb]
    aux = glob.glob(p + '/*/*'+arq2)
    aux = [s.replace('\\','/') for s in aux]
    aux = [pd.read_csv(s) for s in aux] #read files
    ce = [d[d.Metrics==m3] for d in aux] #filter metric
    ce = [d.filter(like='rep',axis=1).reset_index(drop=True) for d in ce]
    bcr = [(((1-bbp[i])*grb[i])*vbt)/(ce[i]*cws) for i in list(range(0,len(solutions)))]
    avg = [d.mean(axis=1).tolist() for d in bcr]
    numRep = len(bcr[0].columns)
    sd = [st.sem(d,axis=1,ddof=numRep-1).tolist() for d in bcr]
    errorIntervals = [st.t.interval(1-alpha,numRep-1,loc=avg[i],scale=sd[i]) for i in range(len(avg))]
    dfs = [pd.DataFrame() for i in range(len(avg))]
    errors = [[errorIntervals[i][1].tolist()[j] - avg[i][j] for j in range(len(avg[i]))] for i in range(len(avg))] #error = limSup - mean
    errors = sd
    for i in range(len(avg)):
            dfs[i]['loads'] = loads
            dfs[i]['mean'] = avg[i]
            dfs[i]['errors'] = errors[i]
    return dfs

def extractDFSProfit(path,alpha):
    p = path
    timeAjust = 31536000
    arq1 = '_BandwidthBlockingProbability.csv'
    arq2 = '_ConsumedEnergy.csv'
    m1 = 'Bandwidth blocking probability'
    m2 = 'General requested bandwidth'
    m3 = 'Total consumed energy (Joule)'
    vbt = 5.6e-12 * timeAjust
    cws = 3.3e-8 * timeAjust
    aux = glob.glob(p + '/*/*'+arq1)
    aux = [s.replace('\\','/') for s in aux]
    solutions = [s.split('/') for s in aux]
    solutions = [s[len(s)-2] for s in solutions]#serach name of solutions
    aux = [pd.read_csv(s) for s in aux] #read files
    bbp = [d[d.Metrics==m1] for d in aux] #filter metric
    loads = bbp[0]['LoadPoint'].tolist() #get load points
    bbp = [d.filter(like='rep',axis=1) for d in bbp]
    grb = [d[d.Metrics==m2] for d in aux] #filter metric
    grb = [d.filter(like='rep',axis=1).reset_index(drop=True) for d in grb]
    aux = glob.glob(p + '/*/*'+arq2)
    aux = [s.replace('\\','/') for s in aux]
    aux = [pd.read_csv(s) for s in aux] #read files
    ce = [d[d.Metrics==m3] for d in aux] #filter metric
    ce = [d.filter(like='rep',axis=1).reset_index(drop=True) for d in ce]
    bcr = [(((1-bbp[i])*grb[i])*vbt)-(ce[i]*cws) for i in list(range(0,len(solutions)))]
    avg = [d.mean(axis=1).tolist() for d in bcr]
    numRep = len(bcr[0].columns)
    sd = [st.sem(d,axis=1,ddof=numRep-1).tolist() for d in bcr]
    errorIntervals = [st.t.interval(1-alpha,numRep-1,loc=avg[i],scale=sd[i]) for i in range(len(avg))]
    dfs = [pd.DataFrame() for i in range(len(avg))]
    errors = [[errorIntervals[i][1].tolist()[j] - avg[i][j] for j in range(len(avg[i]))] for i in range(len(avg))] #error = limSup - mean
    errors = sd
    for i in range(len(avg)):
            dfs[i]['loads'] = loads
            dfs[i]['mean'] = avg[i]
            dfs[i]['errors'] = errors[i]
    return dfs



    
def auxPlotBars(dfs, loads, sol, xl="", yl="",show=True,arq="", lp = 'lower center', nc = 5):
    fs = 30
    width = 0.05
    indmult = 0.35
    ind = dfs[0]['loads']
    plt.figure(figsize=(20,6))
    for i in range(len(dfs)):
        plt.bar(ind*indmult+i*width, dfs[i]['mean'], width*0.95, yerr=dfs[i]['errors'], label=sol[i])
    
    plt.ylabel(yl, fontsize=fs)
    plt.xlabel(xl, fontsize=fs)
    plt.xticks(ind*indmult+width*2, loads, fontsize=fs)
    plt.yticks(fontsize=fs)
    plt.grid(axis='y')
    #if len(sol) < nc:
    #    nc = len(sol)
    #plt.legend(loc=lp, ncol = nc, fontsize=fs)
    plt.legend(loc=lp, ncol = nc, fontsize=fs,bbox_to_anchor=(0,-0.18))
    if arq != "":
        plt.savefig(arq, dpi=150,bbox_inches='tight')
    if show:
        plt.show()
    plt.close()
    
def plotLines(path,loads=[],sol=[], al=0.05):
    n = path.split('/')
    n = n[len(n)-1]
    n = n + '_Line_'
    #PBB
    dfs = extractDFS(path,ABBP,MBBP,al)   
    if loads==[]:
        loads = dfs[0]['loads'].tolist()
    if sol==[]:
        sol = range(len(dfs))
    a = path + '/' + n + 'BB.pdf'
    auxPlotLine(dfs,loads,sol,xl='Carga na rede (Erlangs)',yl='Bloqueio de banda',show=True,arq=a)
    
    #CE
    dfs = extractDFS(path,ACE,MTCE,al)
    a = path + '/' + n + 'CE.pdf'
    auxPlotLine(dfs,loads,sol,xl='Carga na rede (Erlangs)',yl='Energia consumida (J)',show=False,arq=a)
    
    #PCE
    dfs = extractDFS(path,ACE,MAPC,al)
    a = path + '/' + n + 'PCE.pdf'
    auxPlotLine(dfs,loads,sol,xl='Carga na rede (Erlangs)',yl='Potência de consumo (W)',show=False,arq=a)
    
    #BCR
    dfs = extractDFSBCR(path,al)
    a = path + '/' + n + 'BCR.pdf'
    auxPlotLine(dfs,loads,sol,xl='Carga na rede (Erlangs)',yl='Relação custo-benefício',show=False,arq=a)
    
    #PROFIT
    dfs = extractDFSProfit(path,al)
    a = path + '/' + n + 'PROFIT.pdf'
    auxPlotLine(dfs,loads,sol,xl='Carga na rede (Erlangs)',yl='Lucro ($)',show=False,arq=a)
    
    #TXU
    dfs = extractDFS(path,ATRRU,MTXU,al)
    a = path + '/' + n + 'TXU.pdf'
    auxPlotLine(dfs,loads,sol,xl='Carga na rede (Erlangs)',yl='Utilização de Tx',show=False,arq=a)
    
    #RRC
    dfs = extractDFS(path,AGS,MRRC,al)
    a = path + '/' + n + 'RRC.pdf'
    auxPlotLine(dfs,loads,sol,xl='Carga na rede (Erlangs)',yl='Requisições por circuito',show=False,arq=a)
    
    #MBVTU
    dfs = extractDFSMBVTU(path,al)
    a = path + '/' + n + 'MBVTU.pdf'
    auxPlotLine(dfs,loads,sol,xl='Carga na rede (Erlangs)',yl='Utilização máxima de BVTs',show=False,arq=a)
    
def plotBars(path,loads=[],sol=[], al=0.05):
    n = path.split('/')
    n = n[len(n)-1]
    n = n + '_Bar_'
    #PBB
    dfs = extractDFS(path,ABBP,MBBP,al)   
    if loads==[]:
        loads = dfs[0]['loads'].tolist()
    if sol==[]:
        sol = range(len(dfs))
    a = path + '/' + n + 'BB.pdf'
    auxPlotBars(dfs,loads,sol,xl='Carga na rede (Erlangs)',yl='Bloqueio de banda',show=False,arq=a,lp='upper left')
    
    #CE
    dfs = extractDFS(path,ACE,MTCE,al)
    a = path + '/' + n + 'CE.pdf'
    auxPlotBars(dfs,loads,sol,xl='Carga na rede (Erlangs)',yl='Energia consumida (J)',show=False,arq=a,lp='upper right')
    
    #PCE
    dfs = extractDFS(path,ACE,MAPC,al)
    a = path + '/' + n + 'PCE.pdf'
    auxPlotBars(dfs,loads,sol,xl='Carga na rede (Erlangs)',yl='Potência de consumo (W)',show=False,arq=a,lp='lower right')
    
    #BCR
    dfs = extractDFSBCR(path,al)
    a = path + '/' + n + 'BCR.pdf'
    auxPlotBars(dfs,loads,sol,xl='Carga na rede (Erlangs)',yl='Relação custo-benefício',show=False,arq=a,lp='upper left')
    
    #PROFIT
    dfs = extractDFSProfit(path,al)
    a = path + '/' + n + 'PROFIT.pdf'
    auxPlotBars(dfs,loads,sol,xl='Carga na rede (Erlangs)',yl='Lucro ($)',show=False,arq=a,lp='upper left')
    
    #TXU
    dfs = extractDFS(path,ATRRU,MTXU,al)
    a = path + '/' + n + 'TXU.pdf'
    auxPlotBars(dfs,loads,sol,xl='Carga na rede (Erlangs)',yl='Utilização de Tx',show=False,arq=a,lp='upper left')
    
    #RRC
    dfs = extractDFS(path,AGS,MRRC,al)
    a = path + '/' + n + 'RRC.pdf'
    auxPlotBars(dfs,loads,sol,xl='Carga na rede (Erlangs)',yl='Requisições por circuito',show=False,arq=a,lp='upper left')
    
    #MBVTU
    dfs = extractDFSMBVTU(path,al)
    a = path + '/' + n + 'MBVTU.pdf'
    auxPlotBars(dfs,loads,sol,xl='Carga na rede (Erlangs)',yl='Utilização máxima de BVTs',show=False,arq=a,lp='upper left')


def computeGains(path, ims, save = ""):
    #metricas comuns negativas
    arqs = [ABBP,ACE,ACE,ATRRU]
    metr = [MBBP,MTCE,MAPC,MTXU]
    gains = [minGains(extractDFS(path,arqs[i],metr[i],0.05),ims) for i in range(len(arqs))]
    df = pd.DataFrame()
    for i in range(len(metr)):
        df[metr[i]] = gains[i]
    
    #metricas comuns positivas
    arqs = [AGS]
    metr = [MRRC]
    gains = [minGains(extractDFS(path,arqs[i],metr[i],0.05),ims,inv=True) for i in range(len(arqs))]
    for i in range(len(metr)):
        df[metr[i]] = gains[i]
    
    #metricas especiais
    df['Benefit to cost ratio'] = minGains(extractDFSBCR(path,0.05),ims,inv=True)
    df['Profit'] = minGains(extractDFSProfit(path,0.05),ims,inv=True)
    df['Max BVT Utilization'] = minGains(extractDFSMBVTU(path,0.05),ims)
    if save!="":
        df.to_csv(path+'/'+save+'.csv')
    return df;
#------------------------------------------ // -----------------------------------------------------------------------------------

def auxPlotLine(dfs, loads, sol, xl="", yl="",show=True,arq="", lp = 'lower center', nc = 3):
    fs = 17
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
    if len(sol) < nc:
        nc = len(sol)
    
    plt.legend(loc=lp, ncol = nc, fontsize=fs,bbox_to_anchor=(0.5,-0.42))
    if arq != "":
        plt.savefig(arq, dpi=150,bbox_inches='tight')
    if show:
        plt.show()
    plt.close()

s = ['EsPAT (σ=40)','SRNP (MRLB=100)','SRNP (MRLB=200)','SRNP (MRLB=400)','Sem mecanismo']
ln = ['800','1200','1600']
lp = ['326','653','979']
#sp.plotBars('C:/Users/ialle/Dropbox/Simulacoes/Doutorado/Experimentos 2/Pacific/EsPAT_TP/MSU',loads=l,sol=s)
#plotLines('C:/Users/ialle/Dropbox/Simulacoes/Doutorado/Experimentos qualificacao/Pacific/ZComp/IACF',loads=lp,sol=s)

#srnpGain = np.asarray(dfs[0]['mean'])/np.asarray(dfs[1]['mean']) - 1
#especGain = np.asarray(dfs[2]['mean'])/np.asarray(dfs[1]['mean']) - 1







