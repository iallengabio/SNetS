# -*- coding: utf-8 -*-
"""
Created on Wed Feb 27 05:59:22 2019
scripts gráficos qualificação
@author: Iallen
"""
import SNetSPy as sp
#gráficos da PBB em função do sigma e carga
def pbb(policy):
    p = 'C:/Users/ialle/Dropbox/Simulacoes/Doutorado/Experimentos/EON/EON_CompleteSharing_EsPAT\EON_CompleteSharing_EsPAT_' + policy
    a = '_BandwidthBlockingProbability.csv'
    m = 'Bandwidth blocking probability'
    al = 0.05
    dfs = sp.extractDFS(p,a,m,al)
    loads = ['267','400','533','667']
    sol = ['σ=0','σ=30','σ=60','σ=90']
    xl = 'Carga na rede (erlangs)'
    yl = 'BBR'
    sp.auxPlotBars(dfs,loads,sol,xl,yl)
    
def ce(policy):
    p = 'C:/Users/ialle/Dropbox/Simulacoes/Doutorado/Experimentos/EON/EON_CompleteSharing_EsPAT\EON_CompleteSharing_EsPAT_' + policy
    a = '_ConsumedEnergy.csv'
    m = 'Total consumed energy (Joule)'
    al = 0.05
    dfs = sp.extractDFS(p,a,m,al)
    loads = ['267','400','533','667']
    sol = ['σ=0','σ=30','σ=60','σ=90']
    xl = 'Carga na rede (erlangs)'
    yl = 'CE'
    sp.auxPlotBars(dfs,loads,sol,xl,yl)
    
def bcr(policy):
    p = 'C:/Users/ialle/Dropbox/Simulacoes/Doutorado/Experimentos/EON/EON_CompleteSharing_EsPAT\EON_CompleteSharing_EsPAT_' + policy
    al = 0.05
    dfs = sp.extractDFSBCR(p,al)
    loads = ['267','400','533','667']
    sol = ['σ=0','σ=30','σ=60','σ=90']
    xl = 'Carga na rede (erlangs)'
    yl = 'BCR'
    sp.auxPlotBars(dfs,loads,sol,xl,yl)
    
bcr('BAS')
pbb('BAS')
ce('BAS')