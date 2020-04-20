# -*- coding: utf-8 -*-
"""
Created on Fri Apr 10 15:18:06 2020

@author: ialle
"""

import pandas as pd #for csv manipulation
import glob #for filtering
import scipy.stats as st #for statistics
import numpy as np

#arquivos
RF_BBP = '_BandwidthBlockingProbability.csv'
RF_BP = '_BlockingProbability.csv'
RF_GS = '_GroomingStatistics.csv'
RF_CE = '_ConsumedEnergy.csv'
RF_TRRU = '_TransmittersReceiversRegeneratorsUtilization.csv'
RF_EF = '_ExternalFragmentation.csv'

#métricas mapeadas
M_BP = ('Blocking probability',RF_BP)
M_BBP = ('Bandwidth blocking probability',RF_BBP)
M_RRC = ('Rate of requests by circuit',RF_GS)
M_TCE = ('Total consumed energy (Joule)',RF_CE)
M_APC = ('Average power consumption (Watt)',RF_CE)
M_TXU = ('Tx Utilization',RF_TRRU)
M_EFV = ('External Fragmentation (Vertical)',RF_EF)
M_GRB = ('General requested bandwidth',RF_BBP)
#BBP components
M_BBP_F = ('Blocking probability by fragmentation',RF_BBP)
M_BBP_LT = ('Blocking probability by lack of transmitters',RF_BBP)
M_BBP_LR = ('Blocking probability by lack of receivers',RF_BBP)
M_BBP_QOTN = ('Blocking probability by QoTN',RF_BBP)
M_BBP_QOTO = ('Blocking probability by QoTO',RF_BBP)
M_BBP_O = ('Blocking probability by other',RF_BBP)



#Aux functions -------------------------------------------------------------------------

def readFiles(path,resultFile):
    rfiles = glob.glob(path + '/*/*'+resultFile)
    rfiles = [s.replace('\\','/') for s in rfiles]
    solutions = [s.split('/') for s in rfiles]
    solutions = [s[len(s)-2] for s in solutions]#serach name of solutions (name of directory)
    rcsvs = [pd.read_csv(s) for s in rfiles] #read files
    return rcsvs, solutions

#return the interest values, load points and solutions
def getInterestValues(rcsvs,metric):
    frcsvs = [d[d.Metrics==metric] for d in rcsvs] #filter metric
    loads = frcsvs[0]['LoadPoint'].tolist() #get load points
    frcsvs = [d.filter(like='rep',axis=1).reset_index(drop=True) for d in frcsvs] #filter interest values
    return frcsvs, loads

def computeAverageSDErrors(rcsvs,alpha):
    avg = [d.mean(axis=1).tolist() for d in rcsvs] #compute mean
    numRep = len(rcsvs[0].columns) #number of replications
    sd = [st.sem(d,axis=1,ddof=numRep-1).tolist() for d in rcsvs] #standart deviation
    errorIntervals = [st.t.interval(1-alpha,numRep-1,loc=avg[i],scale=sd[i]) for i in range(len(avg))] #error intervals
    errors = [[errorIntervals[i][1].tolist()[j] - avg[i][j] for j in range(len(avg[i]))] for i in range(len(avg))] #error = limSup - mean
    return avg,sd,errors

def makeDFs(avg,loads,errors):
    dfs = [pd.DataFrame() for i in range(len(avg))] #create dataframes
    for i in range(len(avg)):
        dfs[i]['x'] = loads
        dfs[i]['mean'] = avg[i]
        dfs[i]['errors'] = errors[i]
    return dfs

#Aux functions end ---------------------------------------------------------------------

#extract generic Dataframes
def extractDFs(path,metric,alpha):
    rcsvs, solutions = readFiles(path,metric[1])
    rcsvs, loads = getInterestValues(rcsvs,metric[0])#filter interest values
    #rcsvs = [1/i for i in rcsvs]
    avg,sd,errors = computeAverageSDErrors(rcsvs,alpha)
    #errors = sd
    dfs = makeDFs(avg,loads,errors)
    return dfs, solutions

#extract DFs for one or more(sum) BBP Components
def extractDFsBBPComp(path,metrics,alpha):
    rcsvs, solutions = readFiles(path,metrics[0][1])#same files for all metrics (RF_BBP)
    _, loads = getInterestValues(rcsvs,metrics[0][0])#get loads
    components = [getInterestValues(rcsvs,m[0])[0] for m in metrics]
    sumComp = []
    for i in range(0,len(components[0])):
        comps = [components[j][i] for j in range(0,len(components))]
        sumComps = sum(comps)
        sumComp += [sumComps]
    avg,sd,errors = computeAverageSDErrors(sumComp,alpha)
    #errors = sd
    dfs = makeDFs(avg,loads,errors)
    return dfs, solutions
    

#extract energy efficiency dataframes
def extractDFsEE(path,alpha):
    rbbps, solutions = readFiles(path,M_BBP[1])
    bbp, loads = getInterestValues(rbbps,M_BBP[0])
    grb, _ = getInterestValues(rbbps,M_GRB[0])
    rces, _ = readFiles(path,M_TCE[1])
    ce, _ = getInterestValues(rces,M_TCE[0])
    bcr = [(1-bbp[i])*grb[i]/ce[i] for i in list(range(0,len(solutions)))]
    avg,sd,errors = computeAverageSDErrors(bcr,alpha)
    #errors = sd
    dfs = makeDFs(avg,loads,errors)
    return dfs, solutions

#extract deployed BVTs dataframes (based on max utilization)
def extractDFsDBVTs(path,alpha):
    MTUPN = 'Max Tx Utilization Per Node'
    MRUPN = 'Max Rx Utilization Per Node'
    
    trr, solutions = readFiles(path,M_TXU[1])
    _, loads = getInterestValues(trr,M_TXU[0]) #get load points (interest values not used)
    
    trr1 = [d[d.Metrics==MTUPN] for d in trr] #filter metric
    trr1 = [d.groupby(['LoadPoint']).sum()[list(d.filter(like='rep',axis=1))] for d in trr1]
    trr2 = [d[d.Metrics==MRUPN] for d in trr] #filter metric
    trr2 = [d.groupby(['LoadPoint']).sum()[list(d.filter(like='rep',axis=1))] for d in trr2]
    trr = [trr1[i]+trr2[i] for i in list(range(0,len(solutions)))]
    
    avg,sd,errors = computeAverageSDErrors(trr,alpha)
    #errors = sd
    dfs = makeDFs(avg,loads,errors)
    return dfs, solutions

#language -> en|pt
def getAllDFs(path,alpha,language='en'):
    #generic metrics
    gms = [M_BBP,M_BP,M_RRC,M_TCE,M_APC,M_TXU,M_EFV]
    dfs = [extractDFs(path,gms[i],alpha)[0] for i in range(len(gms))]
    dfs = dfs + [extractDFsEE(path,alpha)[0],extractDFsDBVTs(path,alpha)[0]]
    metric_labels_pt = ['Bloqueio de banda','Bloqueio de requisições','Requisições atendidas por circuito','Energia consumida (J)','Potência de consumo (W)','Utilização de BVTs','Fragmentação externa (vertical)','Eficiência energética (b/J)','BVTs implantados'] #respective metric labels
    metric_labels_en = ['Bandwidth blocking ratio','Requests blocking ratio','Request-lightpath ratio','Total consumed energy (J)','Average power consumption (W)','BVTs utilization','External fragmentation','Energy Efficiency (b/J)','Deployed BVTs']
    
    if(language=='en'):
        return dfs, metric_labels_en
    else:
        return dfs, metric_labels_pt
    
def transposeDFs(dfs,sol):
    newDfs = [pd.DataFrame() for i in dfs[0]['x'].values]
    newSol = dfs[0]['x'].values
    for i in range(len(newDfs)):
        newDfs[i]['x'] = sol
        newDfs[i]['mean'] = [d['mean'][i] for d in dfs]
        newDfs[i]['errors'] = [d['errors'][i] for d in dfs]
    return newDfs, newSol
    


