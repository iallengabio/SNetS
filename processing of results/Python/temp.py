# -*- coding: utf-8 -*-
"""
Created on Thu Oct  3 13:37:59 2019

@author: ialle
"""
import SNetSDataFrameFactory as sdff
import pandas as pd
import numpy as np
import matplotlib
import matplotlib.pyplot as plt

import SNetSPlot as snp


font = {'family':'serif','size':14}
matplotlib.rc('font', **font)

def auxPlotAllMetrics(path, loads):
    ADFs, labels = sdff.getAllDFs(path,0.05)
    _, sol = sdff.readFiles(path,sdff.RF_BBP)
    arqNames = ['bb','rb','rlr','tce','apc','bu','ef','ee','db']
    for i in range(len(ADFs)):
        fig, _ = snp.single_plot_lines(ADFs[i],sol,loads,'network load',labels[i])
        fig.savefig(path+'/'+arqNames[i]+'.png',dpi=150,bbox_inches='tight')

def auxPlotAllPolicies(path, loads):
    pathMPH = path + '/MPH'
    pathMS = path + '/MS'
    pathMVH = path + '/MVH'
    
    adMPH, labels = sdff.getAllDFs(pathMPH,0.05)
    adMS, _ = sdff.getAllDFs(pathMS,0.05)
    adMVH, _ = sdff.getAllDFs(pathMVH,0.05)
    
    _, sol = sdff.readFiles(pathMPH,sdff.RF_BBP)
    
    lTitles = ['MPH','MS','MVH']
    lXlabels = ['Network load (erlangs)']*3
    l_lYlabels = [[l]*3 for l in labels]
    lLoads = [loads]*3
    
    l_l_dfs = [[adMPH[i],adMS[i],adMVH[i]] for i in range(len(adMPH))] #para cada métrica um elemento desta lista que é outra lista de dataframes para cada política
    arqNames = ['bb','rb','rlr','tce','apc','bu','ef','ee','db']
    for i in range(len(l_l_dfs)):
        l_dfs = l_l_dfs[i]
        lYlabels = l_lYlabels[i]
        fig, _ = snp.multi_plot_lines(1,3,l_dfs,sol,lLoads,lXlabels,lYlabels,lTitles)
        fig.savefig(path+'/'+arqNames[i]+'.png',dpi=150,bbox_inches='tight')
    
#path = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/AGG/Mecanismos/Pacific Bell/CompVC2'
#path = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/AGG/SSTG/NSFNet/CompVC2'
#path = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/New EsPAT OPH/NSFNet/CompVC'
#path = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/New EsPAT OPH/Pacific Bell/CompVC'
path = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/AGG/Novos 2/USA/Comp/MPH'
loadsN = ['1092','1274','1456','1638','1820']
#loadsP = ['218','435','653','870','1088']
loadsP = ['163','326','490','653','816']
loadsE = ['1210','1814','2419','3024','3629']
loadsU = ['1766','2208','2650','3091','3533']
#auxPlotAllPolicies(path,loadsE)

auxPlotAllMetrics(path,loadsU)




def auxPlotBBRComp(path,loads,alpha):
    dfs, sol = sdff.extractDFs(path,sdff.M_BBP_F,alpha)
    frag, tsol = sdff.transposeDFs(dfs,sol)
    dfs,sol = sdff.extractDFsBBPComp(path,[sdff.M_BBP_LR,sdff.M_BBP_LT,sdff.M_BBP_O,sdff.M_BBP_QOTN,sdff.M_BBP_QOTO],alpha)
    other, tsol = sdff.transposeDFs(dfs,sol)
    fig,_ = snp.single_plot_lines(frag+other,['BBR due to fragmentation','BBR due to other reasons'],loads,sizeMultX=3,xLabel="σ",yLabel="Bandwidth blocking ratio",correctX=0,correctY=0.15)
    fig.savefig(path+'/bbr_frag.png',dpi=150,bbox_inches='tight')
    
def auxPlotBBRComp2(path,loads,alpha):
    fig,ax = plt.subplots(1, 1)
    snp.fig_resize(fig,3,1)
    dfs,sol,_ = auxExtractBBRComp(path,0.05)
    snp.stacked_bars_plotter_ax(ax,dfs,0.5)
    snp.decorate_ax(ax,loads,'σ','Bandwidth blocking ratio')
    fig.tight_layout()
    snp.fig_legend(fig,sol,0.2,3)
    fig.savefig(path+'/bbr_frag_comp.png',dpi=150,bbox_inches='tight')
    
def auxExtractBBRComp(path,alpha):
    dfs, sol = sdff.extractDFs(path,sdff.M_BBP_F,0.05)   
    frag, tsol = sdff.transposeDFs(dfs,sol)
    dfs,sol = sdff.extractDFsBBPComp(path,[sdff.M_BBP_QOTN,sdff.M_BBP_QOTO],0.05)
    pli, tsol = sdff.transposeDFs(dfs,sol)
    dfs,sol = sdff.extractDFsBBPComp(path,[sdff.M_BBP_LR,sdff.M_BBP_LT,sdff.M_BBP_O],0.05)
    other, tsol = sdff.transposeDFs(dfs,sol)
    return frag+pli+other, ['BBR due to fragmentation','BBR due to physicall layer impairments','BBR due to other reasons'], sol
    
path = 'C:/Users/ialle/Google Drive/Doutorado/Simulacoes/AGG/Novos 2/USA/SSTGTuning/MPH/evaluations'
loads = range(0,40)
#auxPlotBBRComp(path,loads,0.05)
#auxPlotBBRComp2(path,loads,0.05)

#fig, ax = plt.subplots(1, 1)
#snp.stacked_bars_plotter_ax(ax,frag+other,0.35) 

#allDFs, mLabels = sdff.getAllDFs(path,0.05)
#allDFsT = [sdff.transposeDFs(d,loads)[0] for d in allDFs]
#snp.single_plot_lines(allDFsT[6],['1'],loads)
    


    

    
    