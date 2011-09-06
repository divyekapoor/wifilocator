default: paper

paper:*.tex figures/
	pdflatex paper.tex
	echo bibtex paper.aux
	pdflatex paper.tex
	pdflatex paper.tex
	gnome-open paper.pdf

dummy: *.tex figures/*
	pdflatex dummy.tex
	pdflatex dummy.tex
	pdflatex dummy.tex
	gnome-open dummy.pdf
	
all: *.tex figures/*
	pdflatex thesis.tex
	bibtex thesis.aux
	pdflatex thesis.tex
	pdflatex thesis.tex
	gnome-open thesis.pdf

clean:
	rm -f *.lof *.lot  *.log *.toc *.aux *.pdf

.PHONY: all clean
