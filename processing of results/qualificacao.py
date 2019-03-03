# -*- coding: utf-8 -*-
"""
Created on Wed Feb 27 05:59:22 2019
scripts gráficos qualificação
@author: Iallen
"""
import SNetSPy as sp
#gráficos da PBB em função do sigma e carga
path = 'C:/Users/ialle/Dropbox/Simulacoes/Doutorado/Experimentos/NSFNet/NSFNet_CompleteSharing_EsPAT/NSFNet_CompleteSharing_EsPAT_'
loads = ['267','400','533']
sol = ['σ=0','σ=10','σ=20','σ=30','σ=40']
def pbb(policy):    
    p = path + policy
    al = 0.05
    dfs = sp.extractDFS(p,sp.ABBP,sp.MBBP,al)    
    xl = 'Carga na rede (erlangs)'
    yl = 'BBR'
    sp.auxPlotBars(dfs,loads,sol,xl,yl)
    
def ce(policy):
    p = path + policy    
    al = 0.05
    dfs = sp.extractDFS(p,sp.ACE,sp.MTCE,al)
    xl = 'Carga na rede (erlangs)'
    yl = 'CE'
    sp.auxPlotBars(dfs,loads,sol,xl,yl)
    
def bcr(policy):
    p = path + policy    
    al = 0.05
    dfs = sp.extractDFSBCR(p,al)
    xl = 'Carga na rede (erlangs)'
    yl = 'BCR'
    sp.auxPlotBars(dfs,loads,sol,xl,yl)
    
def pbbId():
    p = 'C:/Users/ialle/Dropbox/Simulacoes/Doutorado/Experimentos/NSFNet/politicas_espat_id'
    s = ['IACF_E2','BAS','IACF','MPH','MSU','MVH']
    al = 0.05
    dfs = sp.extractDFS(p,sp.ABBP,sp.MBBP,al) 
    xl = 'Carga na rede (erlangs)'
    yl = 'BBR'
    sp.auxPlotLine(dfs,loads,s,xl,yl)
    
def ceId():
    p = 'C:/Users/ialle/Dropbox/Simulacoes/Doutorado/Experimentos/NSFNet/politicas_espat_id'
    s = ['IACF_E2','BAS','IACF','MPH','MSU','MVH']
    al = 0.05
    dfs = sp.extractDFS(p,sp.ACE,sp.MTCE,al)
    xl = 'Carga na rede (erlangs)'
    yl = 'CE'
    sp.auxPlotLine(dfs,loads,s,xl,yl)
    
def bcrId():
    p = 'C:/Users/ialle/Dropbox/Simulacoes/Doutorado/Experimentos/NSFNet/politicas_espat_id'
    s = ['IACF_E2','BAS','IACF','MPH','MSU','MVH']
    al = 0.05
    dfs = sp.extractDFSBCR(p,al)    
    xl = 'Carga na rede (erlangs)'
    yl = 'BCR'
    sp.auxPlotLine(dfs,loads,s,xl,yl)
    
def txuId():
    p = 'C:/Users/ialle/Dropbox/Simulacoes/Doutorado/Experimentos/NSFNet/politicas_espat_id'
    s = ['IACF_E2','BAS','IACF','MPH','MSU','MVH']
    al = 0.05
    dfs = sp.extractDFS(p,sp.ATRRU,sp.MTXU,al)
    xl = 'Carga na rede (erlangs)'
    yl = 'CE'
    sp.auxPlotLine(dfs,loads,s,xl,yl)
    
#bcr('MVH')
pbb('IACF')
#ce('BAS')
#pbbId()
#ceId()
#bcrId()    
#txuId()