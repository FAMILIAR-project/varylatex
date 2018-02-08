library(rpart)
library(rpart.plot)
library(caret)
library(readr)
library(dplyr)
mystats <- read_csv("~/Downloads/statsFSEVary.csv", # "~/Documents/SANDBOX/varylatex/output-FSE/stats.csv", # 
                  col_types = cols(ACK = col_factor(levels = c("true",
                                                               "false")), 
                                   BIB = col_factor(levels = c("true",
                        "false")), BOLD_ACK = col_factor(levels = c("true",
                                                                    "false")), 
                        BREF = col_factor(levels = c("true",
                                    "false")), EMAIL = col_factor(levels = c("true",
                                                                             "false")), 
                   JS_FOOTNOTESIZE = col_factor(levels = c("true",
                                                                                                              "false")), JS_SCRIPTSIZE = col_factor(levels = c("true",
                                                                                                                                                               "false")), JS_STYLE = col_factor(levels = c("true",
                                                                                                                                                                                                           "false")), JS_TINY = col_factor(levels = c("true",
                                                                                                                                                                                                                                                      "false")), LONG_ACK = col_factor(levels = c("true",
                                                                                                                                                                                                                                                                                                  "false")), LONG_AFFILIATION = col_factor(levels = c("true",
                                                                                                                                                                                                                                                                                                                                                      "false")), PARAGRAPH_ACK = col_factor(levels = c("true",
                                                                                                                                                                                                                                                                                                                                                                                                       "false")), PL_FOOTNOTE = col_factor(levels = c("true",
                                                                                                                                                                                                                                                                                                                                                                                                                                                      "false")), VARY_LATEX = col_factor(levels = c("true",
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    "false")), nbPages = col_factor(levels = c("4",
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       "5"))))


evalCT <- function (stats, perc) {
# View(stats)
summary(stats$nbPages)
stats <- na.omit(stats)
#print(summary(stats$nbPages))
sample <- sample.int(n = nrow(stats), size = floor(perc*nrow(stats)), replace = F)
test  <- stats[-sample, ]
train <- stats[sample, ]
fit <- rpart(nbPages~.-idConfiguration-sizePDF,data=train,method="class")
#rpart.plot(fit,type=4, extra=0, box.palette=c("palegreen3", "red"))
pred = predict(fit, test, type="class")
cm <- confusionMatrix(pred, test$nbPages)
return (cm$overall['Accuracy'])
}

for (perc in 1:9) {
   accs <- c()
   for (repeatt in 1:50) {
     accs[repeatt] <- evalCT(stats=mystats, perc = (perc/10))
   }
   print(mean(accs))
}

evalCT(stats=mystats, perc = (7/10))