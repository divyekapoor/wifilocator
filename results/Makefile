all: *.tex figures/*
	pdflatex thesis.tex
	bibtex thesis.aux
	pdflatex thesis.tex
	pdflatex thesis.tex
	gnome-open thesis.pdf

clean:
	rm -f *.lof *.lot  *.log *.toc *.aux *.pdf

.PHONY: all clean
