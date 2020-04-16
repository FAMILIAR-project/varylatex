# VaryLaTeX

How to submit a research paper, a technical report, a grant proposal , or a curriculum vitae that respect imposed constraints such as formatting instructions and page limits? It is a challenging task, especially when coping with time pressure, isn't it? VaryLaTeX is a solution based on variability, constraint programming , and machine learning techniques for documents written in LaTeX to meet constraints and deliver on time. 

![Alt Text](http://phdcomics.com/comics/archive/phd090617s.gif)
http://phdcomics.com/comics.php?f=1971

As a user, you simply have to annotate LaTeX source files with variability information, e.g., (de)activating portions of text, tuning figures' sizes, or tweaking line spacing. Then, a fully automated procedure learns constraints among Boolean and numerical values for avoiding non-acceptable paper variants, and finally, users can further configure their papers (e.g., aesthetic considerations) or pick a (random) paper variant that meets constraints, e.g., page limits. 

We are working on an integrated, lightweight, and usable solution. 
*Feel free to contribute, suggest features, provide feedbacks, use cases* 

## Publications

More details can be found in the following paper, published/presented at 12th International Workshop on Variability Modelling of Software-Intensive Systems https://vamos2018.wordpress.com/:
"VaryLaTeX: Learning Paper Variants That Meet Constraints" by Mathieu Acher, Paul Temple, Jean-Marc Jézéquel, José A. Galindo, Jabier Martinez, Tewfik Ziadi: https://hal.inria.fr/hal-01659161/

Screencast (demonstration performed in 2015, no sound unfortunately): https://www.youtube.com/watch?v=n9pdUddr5m4 

## Requirements

The code is mainly written in Java with a bunch of bash/R scripts 
We are using Mustache for the templating engine (https://mustache.github.io/ and specifically on Trimou: http://trimou.org/) and Choco for the solving part (http://www.choco-solver.org/). 
You also need FAMILIAR: https://github.com/FAMILIAR-project/familiar-language

# VaryVary 

This work benefited from the support of the project ANR-17-CE25-0010-01 VaryVary. 
We are seeking candidates for working around the topic of machine learning and configurable systems (VaryLaTeX is an interesting case of VaryVary): https://docs.google.com/document/d/1Vr8HByYefWDRDdVeMtToXtpauFwcxxQeXLZtsX7T1UI/edit?usp=sharing







