# -*- coding: utf-8 -*-
"""
Created on Fri Apr 10 16:41:01 2020

@author: ialle
"""
import matplotlib
import matplotlib.pyplot as plt
import numpy as np

#defaults
std_markers = ["o","v","^","s","x","P","D","_","*","1","2","3","4"]
std_linestyles = ['-','--', '-.', ':']
std_colors = ['b','g','r','c','m','y','k','w']


def line_plotter_ax(ax, dataframe, l, m, c):
    x = np.arange(len(dataframe['x'].values))
    y = dataframe['mean'].values
    e = dataframe['errors'].values
    out = ax.errorbar(x, y, xerr=0, yerr=e, linestyle=l, marker=m, color=c)
    return out

def lines_plotter_ax(ax,dataframes,linestyles=std_linestyles,markers=std_markers,colors=std_colors):
    
    for i in range(0,len(dataframes)):
        l = linestyles[i%len(linestyles)]
        m = markers[i%len(markers)]
        c = colors[i%len(colors)]
        line_plotter_ax(ax,dataframes[i],l,m,c)

def stacked_bar_plotter_ax(ax,dataframe,bottom=[],width=0.35):
    x = np.arange(len(dataframe['x'].values))
    y = dataframe['mean'].values
    e = dataframe['errors'].values
    
    if len(bottom)==0:
        bottom = [0 for i in y]
    out = ax.bar(x, y, width, yerr=e,bottom=bottom)
    return out

def stacked_bars_plotter_ax(ax,dataframes,width=0.35):
    stacked_bar_plotter_ax(ax,dataframes[0],[],width)
    bot = dataframes[0]['mean'].values
    for i in range(1,len(dataframes)):
        stacked_bar_plotter_ax(ax,dataframes[i],bot,width)
        bot = bot + dataframes[i]['mean'].values
         

def decorate_ax(ax,loads,xLegend="",yLegend="",title="",scilim=(-10, 10),axisGrid="y"):
    x = np.arange(len(loads))
    ax.set_xlim((x[0]-0.5, x[len(x)-1]+0.5))
    ax.set_xticks(x, minor=False)
    ax.set_xticklabels(loads, fontdict=None, minor=False)
    ax.ticklabel_format(axis='y', style='sci', scilimits=scilim)
    #ax.ticklabel_format(axis='y', style=yScale)
    ax.set_xlabel(xLegend)
    ax.set_ylabel(yLegend)
    if(axisGrid!=''):
        ax.grid(axis=axisGrid)
    ax.set_title(title)
    
def fig_resize(fig,sizeMultX=1,sizeMultY=1):
    fSize = fig.get_size_inches()
    fig.set_size_inches(fSize[0]*sizeMultX, fSize[1]*sizeMultY)
    
def fig_legend(fig,legends,correctY=0,ncol=3):
    #fig.tight_layout()
    #correctY = 0.27/nr    
    fig.subplots_adjust(bottom=correctY)        
    fig.legend(legends, loc='lower center', ncol=ncol)

def single_plot_lines(dfs,sol,loads,xLabel="",yLabel="",title="",scilimits=(-10, 10),axisGrid='y',correctY=0.2,legendCol=3,showLegend=True,sizeMultX=1,sizeMultY=1):
    fig, ax = plt.subplots(1, 1)
    fig_resize(fig,sizeMultX,sizeMultY)
    
    lines_plotter_ax(ax,dfs)    
    decorate_ax(ax,loads,xLabel,yLabel,title,scilimits,axisGrid)
    
    fig.tight_layout()
    
    if(showLegend):  
        fig_legend(fig,sol,correctY,legendCol)
        
    return fig, ax

def multi_plot_lines(nr,nc,ldfs,sol,lLoads,lXLabels,lYLabels,lTitles,scilimits=(-10, 10),axisGrid='y',legendCol=3,showLegend=True):
    fig, axs = plt.subplots(nrows=nr, ncols=nc, constrained_layout=False)
    fSize = fig.get_size_inches()
    fig.set_size_inches(fSize[0]*nc, fSize[1]*nr)
    
    if nr==1==nc:
        ax = axs
        lines_plotter_ax(ax,ldfs[0])
        decorate_ax(ax,lLoads[0],lXLabels[0],lYLabels[0],lTitles[0],scilimits,axisGrid)
    else:
        for i in range(len(ldfs)):
            ax = axs.flat[i]
            lines_plotter_ax(ax,ldfs[i])
            decorate_ax(ax,lLoads[i],lXLabels[i],lYLabels[i],lTitles[i],scilimits,axisGrid) 
    

    fig.tight_layout()
    
    if(showLegend):  
        correctY = 0.27/nr 
        fig_legend(fig,sol,correctY,legendCol)
    
    
    return fig, ax


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    