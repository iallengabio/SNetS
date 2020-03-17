# -*- coding: utf-8 -*-

import pandas as pd #for csv manipulation
import glob #for filtering
import scipy.stats as st #for statistics
import matplotlib
import matplotlib.pyplot as plt
import matplotlib.ticker as mtick
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

#aux
markers = ["o","v","^","s","x","P","D","_","*","1","2","3","4"]
linestyles = ['--', '-.', ':','-', '-', '-.', ':','-','--','-.', ':','-', '--']
#markers = ["2","3","4","s","P","x","D","_","*","1","o","s","x"]
#linestyles = ['--', ':', '--', ':','--', ':', '--', ':','--',':','-', '-','-']


def minGains(dfs,ims,inv=False):
    dfs = [d.filter(like='mean',axis=1) for d in dfs]
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
    return df

def computeMaxBVTs(path):
    bbp = glob.glob(path + '/*_TransmittersReceiversRegeneratorsUtilization.csv')
    bbp = pd.read_csv(bbp[0])
    mtx = bbp[bbp.Metrics=='Max Tx Utilization Per Node']
    mrx = bbp[bbp.Metrics=='Max Rx Utilization Per Node']
    nodes = mtx['Node'].tolist()
    mtx = mtx.filter(like='rep',axis=1)
    mrx = mrx.filter(like='rep',axis=1)
    mtx = mtx.max(axis=1) + 2
    mrx = mrx.max(axis=1) + 2
    mtx = mtx.tolist()
    mrx = mrx.tolist()
    mtx = [str(int(d)) for d in mtx]
    mrx = [str(int(d)) for d in mrx]    
    for i in range(len(nodes)):
        print('{"name":"'+nodes[i]+'","transmitters":'+mtx[i]+',"receivers":'+mrx[i]+',"regenerators":0},')

#------------------------------------------------------------------------------//-------------------------------------------------------------------------------

YLegends = ['Circuit Blocking','Bandwidth Blocking','Consumed Energy (J)','Power Consumption (W)','Energy efficiency (b/J)','Weighted Energy efficiency (b/J)','Simplified Profit ($)','Average Transponder Utilization','Request-Circuit Ratio','Deployed BVTs']
ArqNames = ['CB.png','BB.png','CE.png','PCE.png','EE.png','WEE.png','PROFIT.png','TXU.png','RRC.png','MBVTU.png']
XLegend = 'Network load (Erlangs)'

def plotLineAx(ax,dfs, sol):
    for i in range(len(dfs)):
        x = np.arange(len(dfs[i]['loads']))
        y = dfs[i]['mean']
        e = dfs[i]['errors']
        ax.errorbar(x, y, xerr=0, yerr=e, linestyle=linestyles[i], marker=markers[i], label=sol[i])

def decorateLineAx(ax,dfs,loads,xLegend,yLegend,title):
    x = np.arange(len(dfs[0]['loads']))
    ax.set_xticks(x, minor=False)
    ax.set_xticklabels(loads, fontdict=None, minor=False)
    ax.ticklabel_format(axis='y', style='sci', scilimits=(-10, 10))
    ax.set_xlabel(xLegend)
    ax.set_ylabel(yLegend)
    ax.grid(axis='y')
    ax.set_title(title)
    
    
def plotBarAx(ax, dfs, sol, barWidth):
    qs = len(dfs)
    qsaux = (qs-1)/2
    for i in range(len(dfs)):
        x = np.arange(len(dfs[i]['loads']))
        y = dfs[i]['mean']
        e = dfs[i]['errors']
        ax.bar(x+(i-qsaux)*barWidth, y, barWidth, yerr=e, label=sol[i])
        
def decorateBarAx(ax, dfs, loads, xLegend, yLegend, title):
    x = np.arange(len(dfs[0]['loads']))
    ax.set_xticks(x, minor=False)
    ax.set_xticklabels(loads, fontdict=None, minor=False)    
    ax.ticklabel_format(axis='y', style='sci', scilimits=(-10, 10))
    ax.set_xlabel(xLegend)
    ax.set_ylabel(yLegend)
    ax.grid(axis='y')
    ax.set_title(title)
    
def SPlotLine2h(dfs1,dfs2,loads1,loads2,sol,xl1,xl2,yl1,yl2,t1="",t2="",show=True,arq="",nc = 5):
    fs = 14
    font = {'family':'serif','size':fs}    
    matplotlib.rc('font', **font)
    matplotlib.rcParams['figure.figsize'] = (12, 6.5)
    
    fig, axs = plt.subplots(nrows=1, ncols=2)
    
    ax = axs.flat[0]    
    plotLineAx(ax, dfs1,sol)
    decorateLineAx(ax, dfs1, loads1, xl1, yl1, t1)
    
    ax = axs.flat[1]
    plotLineAx(ax, dfs2,sol)
    decorateLineAx(ax, dfs2, loads2, xl2, yl2, t2)
    

    if len(sol) < nc:
        nc = len(sol)
        
    fig.subplots_adjust(bottom=0.16)
    fig.legend(sol, loc='lower center', ncol = nc, fontsize=fs)
    
    if arq != "":
        fig.savefig(arq, dpi=150)
    if show:
        fig.show()
    plt.close()

def SPlotLine2v(dfs1,dfs2,loads1,loads2,sol,xl1,xl2,yl1,yl2,t1="",t2="",show=True,arq="",nc = 5):
    fs = 14
    font = {'family':'serif','size':fs}    
    matplotlib.rc('font', **font)
    matplotlib.rcParams['figure.figsize'] = (6.5, 8)
    
    fig, axs = plt.subplots(nrows=2, ncols=1, constrained_layout=True)    
       
    ax = axs.flat[0]    
    plotLineAx(ax, dfs1,sol)
    decorateLineAx(ax, dfs1, loads1, xl1, yl1, t1)
    
    ax = axs.flat[1]
    plotLineAx(ax, dfs2,sol)
    decorateLineAx(ax, dfs2, loads2, xl2, yl2, t2)
    
    if len(sol) < nc:
        nc = len(sol)
        
    ax.legend(loc='lower center', ncol = nc, fontsize=fs,bbox_to_anchor=(0.45,-0.38))    
    
    if arq != "":
        fig.savefig(arq, dpi=150,bbox_inches='tight')
    if show:
        fig.show()
    plt.close()
    
def SPlotBar2v(dfs1,dfs2,loads1,loads2,sol,xl1,xl2,yl1,yl2,t1="",t2="",show=True,arq="",nc = 5):
    fs = 14
    font = {'family':'serif',
            'size':fs}
    
    matplotlib.rc('font', **font)
    matplotlib.rcParams['figure.figsize'] = (6.5, 8)
    
    fig, axs = plt.subplots(nrows=2, ncols=1, constrained_layout=True)    
    
    barWidth = 0.3
    
    ax = axs.flat[0]    
    plotBarAx(ax, dfs1, sol,barWidth)
    decorateBarAx(ax, dfs1, loads1, xl1, yl1, t1)
    
    ax = axs.flat[1]
    plotBarAx(ax, dfs2,sol, barWidth)
    decorateBarAx(ax, dfs2, loads2, xl2, yl2, t2)
    
    if len(sol) < nc:
        nc = len(sol)
        
    ax.legend(loc='lower center', ncol = nc, fontsize=fs,bbox_to_anchor=(0.45,-0.38))    
    
    if arq != "":
        fig.savefig(arq, dpi=150,bbox_inches='tight')
    if show:
        fig.show()
    plt.close()

def SPlotBar2h(dfs1,dfs2,loads1,loads2,sol,xl1,xl2,yl1,yl2,t1="",t2="",show=True,arq="",nc = 5):
    fs = 14
    font = {'family':'serif','size':fs}
    matplotlib.rc('font', **font)
    matplotlib.rcParams['figure.figsize'] = (12, 6.5)
    barWidth = 0.3
    
    fig, axs = plt.subplots(nrows=1, ncols=2)
    
    ax = axs.flat[0]    
    plotBarAx(ax, dfs1,sol, barWidth)
    decorateBarAx(ax, dfs1, loads1, xl1, yl1, t1)
    
    ax = axs.flat[1]
    plotBarAx(ax, dfs2,sol, barWidth)
    decorateBarAx(ax, dfs2, loads2, xl2, yl2, t2)
    
    if len(sol) < nc:
        nc = len(sol)
        
    fig.subplots_adjust(bottom=0.16)
    fig.legend(sol, loc='lower center', ncol = nc, fontsize=fs)
    
    if arq != "":
        fig.savefig(arq, dpi=150)
    if show:
        fig.show()
    plt.close()

def SPlotBars(dfs, loads, sol, xl="", yl="",show=True,arq="", lp = 'lower center', nc = 5):
    fs = 14
    font = {'family':'serif','size':fs}    
    matplotlib.rc('font', **font)
    matplotlib.rcParams['figure.figsize'] = (7, 6.5)
    barWidth = 0.3
    
    fig, axs = plt.subplots(nrows=1, ncols=1)
    
    ax = axs.flat[0]    
    plotBarAx(ax, dfs,sol,barWidth)
    decorateBarAx(ax, dfs, loads, xl, yl, '')
    
    if len(sol) < nc:
        nc = len(sol)
    fig.subplots_adjust(bottom=0.16)
    fig.legend(sol, loc='lower center', ncol = nc, fontsize=fs)
    
    if arq != "":
        fig.savefig(arq, dpi=150,bbox_inches='tight')
    if show:
        plt.show()
    plt.close()

def SPlotLine(dfs, loads, sol, xl="", yl="",show=True,arq="", lp = 'lower center', nc = 5):
    fs = 14
    font = {'family':'serif','size':fs}    
    matplotlib.rc('font', **font)
    matplotlib.rcParams['figure.figsize'] = (7, 6.5)
    
    fig, axs = plt.subplots(nrows=1, ncols=1)
    
    ax = axs.flat[0]    
    plotLineAx(ax, dfs,sol)
    decorateLineAx(ax, dfs, loads, xl, yl, '')
    
    if len(sol) < nc:
        nc = len(sol)
    fig.subplots_adjust(bottom=0.16)
    fig.legend(sol, loc='lower center', ncol = nc, fontsize=fs)
    
    if arq != "":
        fig.savefig(arq, dpi=150,bbox_inches='tight')
    if show:
        plt.show()
    plt.close()

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
        SPlotLine(dfs,loads,sol,xl,yl,show=True,arq=a)

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
        if(i==1):
            show = True
        else:
            show = False
        SPlotLine2v(dfs1,dfs2,loads1,loads2,sol,xl,xl,yl,yl,t1,t2,show,arq=a)


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
        SPlotBars(dfs,loads,sol,xl,yl,True,a,lp)


#------------------------------------------ // -----------------------------------------------------------------------------------

#------------------------------------------------------------------------------------------------------------------------
   





