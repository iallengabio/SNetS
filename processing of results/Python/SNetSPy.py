# -*- coding: utf-8 -*-

import pandas as pd #for csv manipulation
import glob #for filtering
import scipy.stats as st #for statistics
import matplotlib.pyplot as plt
import numpy as np

#arquivos
ABBP = '_BandwidthBlockingProbability.csv'
ABP = '_BlockingProbability.csv'
AGS = '_GroomingStatistics.csv'
ACE = '_ConsumedEnergy.csv'
ATRRU = '_TransmittersReceiversRegeneratorsUtilization.csv'
AGS = '_GroomingStatistics.csv'

#métricas
MBP = 'Blocking probability'
MBBP = 'Bandwidth blocking probability'
MRRC = 'Rate of requests by circuit'
MTCE = 'Total consumed energy (Joule)'
MAPC = 'Average power consumption (Watt)'
MTXU = 'Tx Utilization'
MRRC = 'Rate of requests by circuit'

#aux
markers = ["o","v","^","s","x","P","D","_","*","1","2","3","4"]
linestyles = ['--', '-.', ':','-', '-', '-.', ':','-','--','-.', ':','-', '--']
#markers = ["2","3","4","s","P","x","D","_","*","1","o","s","x"]
#linestyles = ['--', ':', '--', ':','--', ':', '--', ':','--',':','-', '-','-']


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
    #vbt = 5.6e-12 * timeAjust
    #cws = 3.3e-8 * timeAjust
    vbt = 1
    cws = 1
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

def extractDFSWEE(path,alpha): #weighted energy efficiency
    p = path
    arq1 = '_BandwidthBlockingProbability.csv'
    arq2 = '_ConsumedEnergy.csv'
    m1 = 'Bandwidth blocking probability'
    m2 = 'General requested bandwidth'
    m3 = 'Total consumed energy (Joule)'
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
    bcr = [(((1-bbp[i])*grb[i])*(1-bbp[i]))/(ce[i]) for i in list(range(0,len(solutions)))]
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

def extractDFSProfit(path,alpha, cws=3.3e-7, vbt=5.6e-11):
    p = path
    timeAjust = 31536000
    arq1 = '_BandwidthBlockingProbability.csv'
    arq2 = '_ConsumedEnergy.csv'
    m1 = 'Bandwidth blocking probability'
    m2 = 'General requested bandwidth'
    m3 = 'Total consumed energy (Joule)'
    vbt = vbt * timeAjust
    cws = cws * timeAjust
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

def extractDFSProfitR(path,alpha,loadP,cws,vbtMult, n):
    p = path
    timeAjust = 31536000
    #timeAjust = 1
    arq1 = '_BandwidthBlockingProbability.csv'
    arq2 = '_ConsumedEnergy.csv'
    m1 = 'Bandwidth blocking probability'
    m2 = 'General requested bandwidth'
    m3 = 'Total consumed energy (Joule)'
    cws = cws * timeAjust
    aux = glob.glob(p + '/*/*'+arq1)
    aux = [s.replace('\\','/') for s in aux]
    solutions = [s.split('/') for s in aux]
    solutions = [s[len(s)-2] for s in solutions]#serach name of solutions
    print(solutions)
    aux = [pd.read_csv(s) for s in aux] #read files
    bbp = [d[d.Metrics==m1] for d in aux] #filter metric
    bbp = [d.filter(like='rep',axis=1) for d in bbp]
    grb = [d[d.Metrics==m2] for d in aux] #filter metric
    grb = [d.filter(like='rep',axis=1).reset_index(drop=True) for d in grb]
    aux = glob.glob(p + '/*/*'+arq2)
    aux = [s.replace('\\','/') for s in aux]
    aux = [pd.read_csv(s) for s in aux] #read files
    ce = [d[d.Metrics==m3] for d in aux] #filter metric
    ce = [d.filter(like='rep',axis=1).reset_index(drop=True) for d in ce]
    bbpl = [d.iloc[loadP].tolist() for d in bbp]
    grbl = [d.iloc[loadP].tolist() for d in grb]
    cel = [d.iloc[loadP].tolist() for d in ce]
    sb = [[(1-bbpl[i][j]) * grbl[i][j] for j in range(len(bbpl[i]))] for i in range(len(bbpl))]
    vbts = [cws / (vbtMult**i) for i in list(range(2,n+2))]
    vbts = list(reversed(vbts))
    print(vbts)
    dfps = []
    for j in range(len(sb)):
        dfp = pd.DataFrame()
        for i in range(len(sb[j])):
            dfp['rep'+str(i)] = [vbt*sb[j][i] - cws*cel[j][i] for vbt in vbts]
        dfps = dfps + [dfp]
    avg = [d.mean(axis=1).tolist() for d in dfps]
    numRep = len(dfps[0].columns)
    sd = [st.sem(d,axis=1,ddof=numRep-1).tolist() for d in dfps]
    errorIntervals = [st.t.interval(1-alpha,numRep-1,loc=avg[i],scale=sd[i]) for i in range(len(avg))]
    dfs = [pd.DataFrame() for i in range(len(avg))]
    errors = [[errorIntervals[i][1].tolist()[j] - avg[i][j] for j in range(len(avg[i]))] for i in range(len(avg))] #error = limSup - mean
    errors = sd
    for i in range(len(avg)):
        dfs[i]['loads'] = list(range(len(vbts)))
        dfs[i]['mean'] = avg[i]
        dfs[i]['errors'] = errors[i]
    return dfs

#------------------------------------------------------------------------------//-------------------------------------------------------------------------------


YLegends = ['Circuit Blocking','Bandwidth Blocking','Consumed Energy (J)','Power Consumption (W)','Energy efficiency (b/J)','Weighted Energy efficiency (b/J)','Simplified Profit ($)','Average Transponder Utilization','Request-Circuit Ratio','Deployed BVTs']
ArqNames = ['CB.png','BB.png','CE.png','PCE.png','EE.png','WEE.png','PROFIT.png','TXU.png','RRC.png','MBVTU.png']
XLegend = 'Network load (Erlangs)'

def plotLines(path,loads=[],sol=[], al=0.05):
    n = path.split('/')
    n = n[len(n)-1]
    n = n + '_Line_'
    lDfs = [extractDFS(path,ABP,MBP,al),extractDFS(path,ABBP,MBBP,al), extractDFS(path,ACE,MTCE,al), extractDFS(path,ACE,MAPC,al), extractDFSBCR(path,al), extractDFSWEE(path,al), extractDFSProfit(path,al), extractDFS(path,ATRRU,MTXU,al), extractDFS(path,AGS,MRRC,al), extractDFSMBVTU(path,al)]
    lYl = YLegends
    lAn = ArqNames
    xl = XLegend
    if loads==[]:
        loads = lDfs[0][0]['loads'].tolist()
    if sol==[]:
        sol = range(len(lDfs[0]))

    for i in range(len(lDfs)):
        dfs = lDfs[i]
        yl = lYl[i]
        a = path + '/' + n + lAn[i]
        auxPlotLine(dfs,loads,sol,xl,yl,show=True,arq=a)

def plotLines2(path1,path2,loads1=[],loads2=[],sol=[],t1="",t2="", al=0.05):
    n = path1.split('/')
    n = n[len(n)-1]
    n = n + '_Line_'
    lDfs1 = [extractDFS(path1,ABP,MBP,al),extractDFS(path1,ABBP,MBBP,al), extractDFS(path1,ACE,MTCE,al), extractDFS(path1,ACE,MAPC,al), extractDFSBCR(path1,al), extractDFSWEE(path1,al), extractDFSProfit(path1,al), extractDFS(path1,ATRRU,MTXU,al), extractDFS(path1,AGS,MRRC,al), extractDFSMBVTU(path1,al)]
    lDfs2 = [extractDFS(path2,ABP,MBP,al),extractDFS(path2,ABBP,MBBP,al), extractDFS(path2,ACE,MTCE,al), extractDFS(path2,ACE,MAPC,al), extractDFSBCR(path2,al), extractDFSWEE(path2,al), extractDFSProfit(path2,al), extractDFS(path2,ATRRU,MTXU,al), extractDFS(path2,AGS,MRRC,al), extractDFSMBVTU(path2,al)]
    lYl = YLegends
    lAn = ArqNames
    xl = XLegend
    if loads1==[]:
        loads1 = lDfs1[0][0]['loads'].tolist()
    if loads2==[]:
        loads2 = lDfs2[0][0]['loads'].tolist()
    if sol==[]:
        sol = range(len(lDfs1[0]))

    for i in range(len(lDfs1)):
        dfs1 = lDfs1[i]
        dfs2 = lDfs2[i]
        yl = lYl[i]
        a = path1 + '/' + n + lAn[i]
        SPlotLine2(dfs1,dfs2,loads1,loads2,sol,xl,xl,yl,yl,t1,t2,show=False,arq=a)


def plotBars(path,loads=[],sol=[], al=0.05):
    n = path.split('/')
    n = n[len(n)-1]
    n = n + '_Bar_'
    lDfs = [extractDFS(path,ABP,MBP,al),extractDFS(path,ABBP,MBBP,al), extractDFS(path,ACE,MTCE,al), extractDFS(path,ACE,MAPC,al), extractDFSBCR(path,al), extractDFSWEE(path,al), extractDFSProfit(path,al), extractDFS(path,ATRRU,MTXU,al), extractDFS(path,AGS,MRRC,al), extractDFSMBVTU(path,al)]
    lYl = YLegends
    lAn = ArqNames
    xl = XLegend
    lLp = ['upper left','upper right','lower right','upper left','upper left','upper left','upper left','upper left','upper left','upper left','upper left']

    if loads==[]:
        loads = lDfs[0][0]['loads'].tolist()
    if sol==[]:
        sol = range(len(lDfs[0]))

    for i in range(len(lDfs)):
        dfs = lDfs[i]
        yl = lYl[i]
        lp = lLp[i]
        a = path + '/' + n + lAn[i]
        auxPlotBars(dfs,loads,sol,xl,yl,True,a,lp)

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

def auxPlotBars(dfs, loads, sol, xl="", yl="",show=True,arq="", lp = 'lower center', nc = 5):
    fs = 30
    width = 0.7
    indmult = 2
    ind = np.arange(len(dfs[0]['loads']))
    plt.figure(figsize=(12,6))
    for i in range(len(dfs)):
        plt.bar(ind+width*i, dfs[i]['mean'], width*1, yerr=dfs[i]['errors'], label=sol[i])        

    plt.ylabel(yl, fontsize=fs)
    plt.xlabel(xl, fontsize=fs)
    plt.xticks(ind*indmult+width*2, loads, fontsize=fs)
    plt.yticks(fontsize=fs)
    plt.grid(axis='y')
    if len(sol) < nc:
        nc = len(sol)
    plt.legend(loc=lp, ncol = nc, fontsize=fs)
    plt.legend(loc=lp, ncol = nc, fontsize=fs,bbox_to_anchor=(0,-0.18))
    if arq != "":
        plt.savefig(arq, dpi=150,bbox_inches='tight')
    if show:
        plt.show()
    plt.close()

def auxPlotLine(dfs, loads, sol, xl="", yl="",show=True,arq="", lp = 'lower center', nc = 5):
    fs = 27
    plt.figure(figsize=(10,7))
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
    #plt.yscale('log')
    plt.legend(loc=lp, ncol = nc, fontsize=fs,bbox_to_anchor=(0.5,-0.43))
    if arq != "":
        plt.savefig(arq, dpi=150,bbox_inches='tight')
    if show:
        plt.show()
    plt.close()

def SPlotLine2(dfs1,dfs2,loads1,loads2,sol,xl1,xl2,yl1,yl2,t1="",t2="",show=True,arq="",nc = 5):
    fs = 27
    plt.figure(figsize=(20,7))
    lp = 'lower center'

    plt.subplot(1,2,1)
    for i in range(len(dfs1)):
        x = dfs1[i]['loads'];
        y = dfs1[i]['mean']
        e = dfs1[i]['errors']
        plt.errorbar(x, y, xerr=0, yerr=e, linestyle=linestyles[i], marker=markers[i], label=sol[i])
        plt.xticks(x, loads1)

    plt.ticklabel_format(axis='y', style='sci', scilimits=(-2, 3))
    plt.xlabel(xl1,fontsize=fs)
    plt.ylabel(yl1,fontsize=fs)
    plt.xticks(fontsize=fs)
    plt.yticks(fontsize=fs)
    plt.grid(axis='y')
    plt.title(t1, fontsize=fs)

    plt.subplot(1,2,2)
    for i in range(len(dfs2)):
        x = dfs2[i]['loads'];
        y = dfs2[i]['mean']
        e = dfs2[i]['errors']
        plt.errorbar(x, y, xerr=0, yerr=e, linestyle=linestyles[i], marker=markers[i], label=sol[i])
        plt.xticks(x, loads2)

    plt.ticklabel_format(axis='y', style='sci', scilimits=(-2, 3),fontsize=fs)
    plt.xlabel(xl2,fontsize=fs)
    plt.ylabel(yl2,fontsize=fs)
    plt.xticks(fontsize=fs)
    plt.yticks(fontsize=fs)
    plt.grid(axis='y')
    plt.title(t2, fontsize=fs)


    if len(sol) < nc:
        nc = len(sol)
    #plt.yscale('log')
    plt.legend(loc=lp, ncol = nc, fontsize=fs,bbox_to_anchor=(-0.19,-0.40))
    if arq != "":
        plt.savefig(arq, dpi=150,bbox_inches='tight')
    if show:
        plt.show()
    plt.close()

def SPlotBar2(dfs1,dfs2,loads1,loads2,sol,xl1,xl2,yl1,yl2,t1="",t2="",show=True,arq="",nc = 5):
    fs = 27
    plt.figure(figsize=(20,7))
    lp = 'lower center'

    plt.subplot(1,2,1)
    for i in range(len(dfs1)):
        x = dfs1[i]['loads'];
        y = dfs1[i]['mean']
        e = dfs1[i]['errors']
        plt.bar(x, y, 0.7, yerr=e, label=sol[i])

    plt.ticklabel_format(axis='y', style='sci', scilimits=(-2, 3),fontsize=fs)
    plt.ylabel(yl1, fontsize=fs)
    plt.xlabel(xl1, fontsize=fs)
    plt.xticks(dfs1[0]['loads'], loads1, fontsize=fs)
    plt.yticks(fontsize=fs)
    plt.grid(axis='y')
    plt.title(t1, fontsize=fs)

    plt.subplot(1,2,2)
    for i in range(len(dfs2)):
        x = dfs2[i]['loads'];
        y = dfs2[i]['mean']
        e = dfs2[i]['errors']
        plt.bar(x, y, 0.7, yerr=e, label=sol[i])

    plt.ticklabel_format(axis='y', style='sci', scilimits=(-2, 3),fontsize=fs)
    plt.ylabel(yl2, fontsize=fs)
    plt.xlabel(xl2, fontsize=fs)
    plt.xticks(dfs2[0]['loads'], loads2, fontsize=fs)
    plt.yticks(fontsize=fs)
    plt.grid(axis='y')
    plt.title(t2, fontsize=fs)


    if len(sol) < nc:
        nc = len(sol)
    #plt.yscale('log')
    #plt.legend(loc=lp, ncol = nc, fontsize=fs,bbox_to_anchor=(-0.19,-0.40))
    if arq != "":
        plt.savefig(arq, dpi=150,bbox_inches='tight')
    if show:
        plt.show()
    plt.close()

#------------------------------------------------------------------------------------------------------------------------