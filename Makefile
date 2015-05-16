# Moviereviews makefile
# targets are:
#    all - build the project
#    clean - clean the project
#    superclean - clean the project, including the LaTeX FMT file

.SILENT:

# Automatically run all compilation steps necessary to create moviereviews.pdf
all: stars.tex moviereviews.fmt moviereviews.aux compile

# Compile java program to generate stars and directors indices
DirStars.class: DirStars.java
	echo "Compiling DirStars.java . . ."
	javac DirStars.java

# Run Java program to generate stars and directors indices
stars.tex: DirStars.class reviews/*.tex lnamecorrections.txt
	echo "Creating stars/directors index . . ."
	java DirStars

# Precompile document preamble
moviereviews.fmt: moviereviews-fmt.tex
	echo "Compiling LaTeX format"
	pdftex -ini -jobname="moviereviews" "&pdflatex moviereviews-fmt.tex\dump"

# Extra compilation needed if reviews change or aux doesn't exist
moviereviews.aux: reviews/*.tex
	echo "First TeX pass . . ."
	pdftex -interaction=batchmode moviereviews.tex

# Always do at least one pdfLaTeX compilation
compile:
	echo "Compiling PDF document . . ."
	pdftex -interaction=batchmode moviereviews.tex

# Remove all intermediate output files
clean:
	rm -f *.aux *.log *.out *.class stars.tex directors.tex

# Remove the precompiled preamble FMT file
superclean: clean
	rm -f moviereviews.fmt
