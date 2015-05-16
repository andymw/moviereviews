TO ADD A REVIEW:
Add review in correct alphabetical position to the appropriate file:
    ./reviews/[A-Z].tex
run build -a to compile the PDF

TO COMPILE TO PDF:
Run 'make'. Do not use TeX or LaTeX compiler directly

TO CLEAN THE DIRECTORY:
Run 'make clean'

TO CORRECT LAST NAME/FIRST NAME for index:
Add an entry to the last name corrections
    lnamecorrections.txt

TO CORRECT HYPHENATION:
Add an entry to the hyphenation dictionary
    hyphenation.tex

There is NO NEED TO EDIT the following files which are auto-generated:
    directors.tex
    stars.tex
DO NOT edit moviereviews.tex unless to change title or add review authors
DO NOT edit moviereviews-fmt.tex unless adding/editing custom macros
  or modifying document format

For INCOMPLETE REVIEWS in the ./reviews directory
    Surround the paragraph of the review with \iffalse and \fi like so:
    \iffalse
    % incomplete review here
    \fi
