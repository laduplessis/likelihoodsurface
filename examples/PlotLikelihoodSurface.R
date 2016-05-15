library(plot3D)

#' Read in BEAST2 logfile
#' 
#' @param filename The logfile.
#' @param burnin Discard this percentage of samples.
#' @param maxamples If > 0 stop after reading in this many lines (only for testing).
#' 
#' @export
readLogfile <- function(filename, burnin=0.1, maxsamples=-1) {
  logfile <- read.table(filename, sep="\t", header=TRUE, nrows=maxsamples)
  n <- nrow(logfile)
  return(logfile[floor(burnin*n):n,])
}


#' Remove all rows containing non-finite values
removeInfinite <- function(data) {
  idxs <- c()
  for (i in 1:ncol(data))
    idxs <- union(idxs, which(is.finite(data[,i]) == FALSE))
  print(idxs)
  print(data$popSize[idxs])
  
  if (length(idxs) > 0) {
    print(paste("Removed", length(idxs), "rows"))
    return(data[-idxs,])
  } else
    return(data)
}


#' Return datastructure with the meshed likelihood surface as well as the maximum
#' 
#' Does not assume par1 and par2 are ordered (so the code is not very efficient)
getSurface <- function(xpar, ypar, lk) {
  
  x  <- unique(xpar)
  y  <- unique(ypar)
  xy <- mesh(x, y)
  z  <- matrix(nrow=length(x), ncol=length(y))
  
  for (i in 1:length(x)) {
    for (j in 1:length(y)) {
      z[i,j] <- lk[which(xpar == x[i] & ypar == y[j])]
      if (z[i,j] == max(lk)) {
          maxx <- i
          maxy <- j
      }
    }
  }

  max <- c(x[maxx], y[maxy], z[maxx,maxy])
  
  return(list(x=x, y=y, z=z, xy=xy, max=max))

}




#' Plot cross section of the likelihood surface of par1, for given values of par2
#' Logfile is a BEAST2 log file
#' 
plotCrossSection <- function(par1, par2, lk, col=palette.dark[1], xlims=NULL, ylims=NULL, par1.label=NA, par2.label=NA, lk.label=NA, sections=-1) {
  
  plotsymbols   <- c(15,16,17,18, 0, 1, 2, 6, 3,4, 8, 9, 10, 13)
  
  if (is.na(par2.label)) 
    par2.label = ''
  
  if (is.null(xlims))
    xlims <- range(pretty(par1))
  
  if (is.null(ylims))
    ylims <- range(pretty(lk))
    
  par2.unique <- unique(par2)
  if (sections > 0) 
      sections  <- min(length(par2.unique), sections)
  else
      sections <- length(par2.unique)
  
  plot(1,type='n', xlim=xlims, ylim=ylims, bty='n', xlab=par1.label, ylab=lk.label)
  grid(col='lightgrey')
  
  legend   = c()
  legend.y = c()

  for (i in seq(1,length(par2.unique), length.out=sections)) {
    idxs <- which(par2 == par2.unique[i])
    
    points(par1[idxs], lk[idxs], col=col, pch=plotsymbols[i %% length(plotsymbols)])
    lines(par1[idxs], lk[idxs], col=paste0(col,"88"), lwd=1)
    
    legend.y <- c(legend.y, lk[idxs[which(par1[idxs] == max(par1[idxs]))]])
    legend   <- c(legend, paste0(par2.label," = ", par2.unique[i]))
  }
  axis(4, at=legend.y, labels=legend, tick=FALSE, lwd=0, las=1, line=-1)
    
}

