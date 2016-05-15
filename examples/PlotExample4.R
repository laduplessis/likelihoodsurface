rm(list = ls())
path <- "~/Documents/Projects/BEAST2/likelihoodsurface/examples/"

source(paste0(path,"PlotLikelihoodSurface.R"))
source(paste0(path,"Figure_Utilities.R"))

###############################################################################

lf <- readLogfile(paste0(path,"Example3.log"), burnin=0.0)
#lf <- lf[,-7]
#lf <- removeInfinite(lf)
surf <- getSurface(xpar=lf$R01, ypar=lf$R02, lf$prior)

NewFig(paste0(path,"Example4.pdf"), width=5, aspectratio=1)
par(mfrow=c(2,2))

###############################################################################
# Cross sections of parameters
par(mar=c(4, 4, 2, 4) + 0.1)

plotCrossSection(lf$R01, lf$R02, lf$prior, 
                 par1.label="R0 1", par2.label="R0 2", lk.label="Likelihood", sections=10)
abline(v=2, col=palette.dark[3], lty=2)
labelPlot("A", cex=1.5)

plotCrossSection(lf$R02, lf$R01, lf$prior, par1.label="R0 2", par2.label='R0 1', lk.label="Likelihood", sections=10)
abline(v=2, col=palette.dark[3], lty=2)
labelPlot("B", cex=1.5)


###############################################################################
# 3D surface with contour plot
par(mar=rep(1,4))
persp3D(z=surf$z, x=surf$x, y=surf$y, border='black', colkey=FALSE, contour=list(nlevels=20), zlim=c(min(surf$z)-100, max(surf$z)+100), 
        expand=1, ticktype='detailed', cex.axis=0.8, xlab="R0 1", ylab="R0 2", zlab="Log likelihood")
labelPlot("C", cex=1.5)


###############################################################################
# Contour plot showing real maximum and maximum-likelihood
par(mar=c(4, 4, 2, 4) + 0.1)
contour(z=surf$z, x=surf$x, y=surf$y, nlevels=20, xlab="R0 1", ylab="R0 2")
points(2, 2, pch=16)
points(surf$max[1],surf$max[2],pch=1)
legend('topleft', horiz=TRUE, inset=c(0,-0.075), pch=c(16,1), legend=c("Truth", "Maximum"), bty='n', xpd=TRUE)
labelPlot("D", cex=1.5)



par(mar=c(5, 4, 4, 2) + 0.1)
dev.off()
