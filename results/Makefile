all: *.tex figures/*
	pdflatex thesis.tex
	pdflatex thesis.tex
	pdflatex thesis.tex
	gnome-open thesis.pdf

clean:
	rm -rf *.lof *.lot  *.log *.toc *.aux *.pdf

.PHONY: all clean
