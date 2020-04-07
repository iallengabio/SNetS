# -*- coding: utf-8 -*-
"""
Created on Thu Oct  3 13:37:59 2019

@author: iallen
"""

import pandas as pd #for csv manipulation
import SNetSPy as sp
import scipy.stats as st
import numpy as np

import glob


#path = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/New EsPAT OPH/Pacific Bell/SRNP/MSU'
#l = []
#sol = ['Pacific-MSU-SRNP-0', 'Pacific-MSU-SRNP-0100', 'Pacific-MSU-SRNP-0200', 'Pacific-MSU-SRNP-0300', 'Pacific-MSU-SRNP-0400', 'Pacific-MSU-SRNP-0500', 'Pacific-MSU-SRNP-0600', 'Pacific-MSU-SRNP-0700', 'Pacific-MSU-SRNP-0800']
#sp.plotBars(path,l,sol,al=0.05)


def extractDFSMod(path,arq,metric,alpha):#eixo x são políticas
    sol = glob.glob(path + '/*')
    xv = sol
    sol = glob.glob(sol[0] + '/*')
    sol = [s.replace('\\','/') for s in sol]
    sol = [s.split('/') for s in sol]
    sol = [s[len(s)-1] for s in sol]
    print(sol)
    
    xv = [s.replace('\\','/') for s in xv]
    xv = [s.split('/') for s in xv]
    xv = [s[len(s)-1] for s in xv]
    print(xv)
    
    path = path.replace('\\','/')
    dfs = []
    for s in sol: #one dataframe per solution
        files = [glob.glob(path + '/' + x + '/' + s + '/*' + arq)[0] for x in xv]
        csvs = [pd.read_csv(f) for f in files]
        csvs = [d[d.Metrics==metric] for d in csvs]
        csvs = [d.filter(like='rep',axis=1) for d in csvs]
        avg = [d.mean(axis=1).tolist() for d in csvs]
        numRep = len(csvs[0].columns)
        sd = [st.sem(d,axis=1,ddof=numRep-1).tolist() for d in csvs]
        errorIntervals = [st.t.interval(1-alpha,numRep-1,loc=avg[i],scale=sd[i]) for i in range(len(avg))]
        errors = [[errorIntervals[i][1].tolist()[j] - avg[i][j] for j in range(len(avg[i]))] for i in range(len(avg))]
        errors = sd
        avg = [d[0] for d in avg]
        errors = [d[0] for d in errors]
        df = pd.DataFrame()
        df['loads'] = xv
        df['mean'] = avg
        df['errors'] = errors
        dfs.append(df)
    return dfs

# Energy Efficiency
def extractDFSEEMod(path,alpha):
    MGRB = 'General requested bandwidth'
    timeAjust = 31536000
    #vbt = 5.6e-12 * timeAjust
    #cws = 3.3e-8 * timeAjust
    vbt = 1
    cws = 1
    
    sol = glob.glob(path + '/*')
    xv = sol
    sol = glob.glob(sol[0] + '/*')
    sol = [s.replace('\\','/') for s in sol]
    sol = [s.split('/') for s in sol]
    sol = [s[len(s)-1] for s in sol]
    print(sol)
    
    xv = [s.replace('\\','/') for s in xv]
    xv = [s.split('/') for s in xv]
    xv = [s[len(s)-1] for s in xv]
    print(xv)
    
    path = path.replace('\\','/')
    dfs = []
    for s in sol: #one dataframe per mecanismo
        filesBBP = [glob.glob(path + '/' + x + '/' + s + '/*' + sp.ABBP)[0] for x in xv] #3 arquivos, um para cada política
        filesCE = [glob.glob(path + '/' + x + '/' + s + '/*' + sp.ACE)[0] for x in xv]
        
        csvsABBP = [pd.read_csv(f) for f in filesBBP]
        csvsBBP = [d[d.Metrics==sp.MBBP] for d in csvsABBP]
        csvsGRB = [d[d.Metrics==MGRB] for d in csvsABBP]
        
        csvsCE = [pd.read_csv(f) for f in filesCE]
        csvsCE = [d[d.Metrics==sp.MTCE] for d in csvsCE]
        
        bbp = [d.filter(like='rep',axis=1) for d in csvsBBP]
        grb = [d.filter(like='rep',axis=1).reset_index(drop=True) for d in csvsGRB]
        ce = [d.filter(like='rep',axis=1) for d in csvsCE]
        
        
        bcr = [(((1-bbp[i])*grb[i])*vbt)/(ce[i]*cws) for i in list(range(0,len(xv)))]
        
        avg = [d.mean(axis=1).tolist() for d in bcr]
        numRep = len(bcr[0].columns)
        sd = [st.sem(d,axis=1,ddof=numRep-1).tolist() for d in bcr]
        errorIntervals = [st.t.interval(1-alpha,numRep-1,loc=avg[i],scale=sd[i]) for i in range(len(avg))]
        errors = [[errorIntervals[i][1].tolist()[j] - avg[i][j] for j in range(len(avg[i]))] for i in range(len(avg))]
        errors = sd
        avg = [d[0] for d in avg]
        errors = [d[0] for d in errors]
        df = pd.DataFrame()
        df['loads'] = xv
        df['mean'] = avg
        df['errors'] = errors
        dfs.append(df)
    return dfs

def extractDFSMBVTUMod(path,alpha):
    MTUPN = 'Max Tx Utilization Per Node'
    MRUPN = 'Max Rx Utilization Per Node'
    
    sol = glob.glob(path + '/*')
    xv = sol
    sol = glob.glob(sol[0] + '/*')
    sol = [s.replace('\\','/') for s in sol]
    sol = [s.split('/') for s in sol]
    sol = [s[len(s)-1] for s in sol]
    print(sol)
    
    xv = [s.replace('\\','/') for s in xv]
    xv = [s.split('/') for s in xv]
    xv = [s[len(s)-1] for s in xv]
    print(xv)
    
    path = path.replace('\\','/')
    dfs = []
    for s in sol: #one dataframe per solution
        files = [glob.glob(path + '/' + x + '/' + s + '/*' + sp.ATRRU)[0] for x in xv]
        csvs = [pd.read_csv(f) for f in files]
        csvsMT = [d[d.Metrics==MTUPN] for d in csvs]
        csvsMR = [d[d.Metrics==MRUPN] for d in csvs]
        
        csvsMT = [d.groupby(['LoadPoint']).sum()[list(d.filter(like='rep',axis=1))] for d in csvsMT]
        csvsMR = [d.groupby(['LoadPoint']).sum()[list(d.filter(like='rep',axis=1))] for d in csvsMR]
        
        mbvtu = [csvsMT[i]+csvsMR[i] for i in list(range(0,len(xv)))]
        
        avg = [d.mean(axis=1).tolist() for d in mbvtu]
        numRep = len(mbvtu[0].columns)
        sd = [st.sem(d,axis=1,ddof=numRep-1).tolist() for d in mbvtu]
        errorIntervals = [st.t.interval(1-alpha,numRep-1,loc=avg[i],scale=sd[i]) for i in range(len(avg))]
        errors = [[errorIntervals[i][1].tolist()[j] - avg[i][j] for j in range(len(avg[i]))] for i in range(len(avg))]
        errors = sd
        avg = [d[0] for d in avg]
        errors = [d[0] for d in errors]
        df = pd.DataFrame()
        df['loads'] = xv
        df['mean'] = avg
        df['errors'] = errors
        dfs.append(df)
    return dfs


def auxPlotMod(path1,path2,xlabels1,xlabels2,sol,title1,title2,arq,alpha):
    xlegend = 'policies'
    metrics = [sp.MBP,sp.MBBP,sp.MRRC,sp.MTCE,sp.MAPC,sp.MTXU]
    yLegends = ['Request blocking','Bandwidth blocking','Request-circuit ratio','Consumed Energy (J)','Power consumption (W)','BVTs utilization']
    arqs = [sp.ABP,sp.ABBP,sp.AGS,sp.ACE,sp.ACE,sp.ATRRU]
    save = ['CB.png','BB.png','RRC.png','CE.png','PCE.png','TXU.png']
    
    for i in np.arange(len(metrics)):
        dfs1 = extractDFSMod(path1,arqs[i],metrics[i],alpha)
        dfs2 = extractDFSMod(path2,arqs[i],metrics[i],alpha)
        a = arq + "\\"+save[i]
        sp.SPlotBar2v(dfs1,dfs2,xlabels1,xlabels2,sol,xlegend,xlegend,yLegends[i],yLegends[i],title1,title2,False,a,3)
        
    dfs1 = extractDFSEEMod(path1,alpha)
    dfs2 = extractDFSEEMod(path2,alpha)
    a = arq + "\\EE.png"
    yLegend = "Energy Efficiency (b/J)"
    sp.SPlotBar2v(dfs1,dfs2,xlabels1,xlabels2,sol,xlegend,xlegend,yLegend,yLegend,title1,title2,False,a,3)
    
    dfs1 = extractDFSMBVTUMod(path1,alpha)
    dfs2 = extractDFSMBVTUMod(path2,alpha)
    a = arq + "\\DBVTs.png"
    yLegend = "Deployed BVTs"
    sp.SPlotBar2v(dfs1,dfs2,xlabels1,xlabels2,sol,xlegend,xlegend,yLegend,yLegend,title1,title2,False,a,3)

def computeGainsMod(path, ims, save = ""):
    #metricas comuns negativas
    arqs = [sp.ABBP,sp.ACE,sp.ACE,sp.ATRRU]
    metr = [sp.MBBP,sp.MTCE,sp.MAPC,sp.MTXU]
    gains = [sp.minGains(extractDFSMod(path,arqs[i],metr[i],0.05),ims) for i in range(len(arqs))]
    df = pd.DataFrame()
    for i in range(len(metr)):
        df[metr[i]] = gains[i]

    #metricas comuns positivas
    arqs = [sp.AGS]
    metr = [sp.MRRC]
    gains = [sp.minGains(extractDFSMod(path,arqs[i],metr[i],0.05),ims,inv=True) for i in range(len(arqs))]
    for i in range(len(metr)):
        df[metr[i]] = gains[i]

    #metricas especiais
    df['Energy Efficiency'] = sp.minGains(extractDFSEEMod(path,0.05),ims,inv=True)
    #df['Profit'] = minGains(extractDFSProfit(path,0.05),ims,inv=True)
    df['Deployed BVTs'] = sp.minGains(extractDFSMBVTUMod(path,0.05),ims)
    if save!="":
        df.to_csv(path+'/'+save+'.csv')
    return df

#------------------------------------------------------------------------------------------------------------------------------------
    
    
#path = 'C:\\Users\\ialle\\Google Drive\\Doutorado\\Simulacoes\\New EsPAT OPH\\Pacific Bell\\Comp\\MVH\\SSTG'
#sp.computeMaxBVTs(path)
    
path1 = 'C:\\Users\\ialle\\Google Drive\\Doutorado\\Simulacoes\\New EsPAT OPH\\NSFNet\\Comp'
path2 = 'C:\\Users\\ialle\\Google Drive\\Doutorado\\Simulacoes\\New EsPAT OPH\\Pacific Bell\\Comp'
alpha = 0.05
sol = ['Traditional GRMLSA','SRNP', 'SSTG']
loads = ['MPH', 'MS', 'MVH']
arq = 'C:\\Users\\ialle\\Google Drive\\Doutorado\\Simulacoes\\New EsPAT OPH\\NSFNet'
#auxPlotMod(path1,path2,loads,loads,sol,'NSFNet','Pacific Bell',arq,alpha)
#computeGainsMod(path1,2,'nsfnet-sstg-best')
#computeGainsMod(path2,2,'pacific-sstg-best')

path1 = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/New EsPAT OPH/NSFNet/CompVC'
path2 = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/New EsPAT OPH/Pacific Bell/CompVC'
loadsLN = ['1092','1274','1456','1638','1820']
loadsLP = ['218','435','653','870','1088']
solLN = ['MPH_Sem_mecanismo', 'MPH_SRNP', 'MPH_SSTG', 'MSU_Sem_mecanismo', 'MSU_SRNP', 'MSU_SSTG', 'MVH_Sem_mecanismo', 'MVH_SRNP', 'MVH_SSTG']
sp.plotLines2(path1,path2,loadsLN,loadsLP,solLN,"NSFNet","Pacific Bell")