# XTrain

XTrain is a Java 1.5 implementation of the Bestvina-Handel algorithm for homeomorphisms of surfaces with one puncture. Moreover, XTrain also computes triangulations of mapping tori of surface homeomorphisms, suitable for Jeff Weeks's program SnapPea. Surface homeomorphisms can be entered as a composition of Dehn twists with respect to a great variety of curves, or in terms of the induced automorphism of the fundamental group. XTrain was covered in depth here and here.

## Command-line Version and XTrain Development

XTrain can be run from the command line. With the command-line version, you can analyze a large number of examples as a batch job (very useful when testing conjectures or looking for counterexamples!).

In order to understand the input/output format of the package, you can play with the graphical user interface (`pbj.gui.XTrain`).

`jtwist [param]`: jtwist calls the main routine of the class pbj.math.graph.DehnTwist.

`jtrain [-m] [-q] [-v] [filename]`: jtrain calls the main routine of the class pbj.math.graph.train.TrainTrack. The option -m enables the marking feature. The option -v stands for verbose and prompts the program to print intermediate results to stderr. The option -q runs jtrain in quiet mode, i.e., the current PF-eigenvalue is not being printed to stderr (note that -q disables -v). If a filename is given, jtrain attempts to read a graph map (plus possibly a marking) from that file, otherwise it reads from stdin. The input format is the same as the output format, which you can see in the output window of the GUI version of the software.

`jdraw [-s<factor>] [filename]`: jdraw generates PostScript representations of train tracks; it calls the main routine of the class pbj.math.graph.train.TrainPic. The option -s allows users to scale the size of the pictures (for example, jdraw -s2.0 will double the size of the pictures). If a filename is given, jdraw attempts to read a train track from that file, otherwise it reads from stdin. The input format is the same for jdraw and jtrain.

`jdraw -p [filename]`: jdraw -p prints psfrag commands that replace PostScript labels in a picture by LaTeX labels (this is useful if you want to use the output of jdraw in a paper).

`jmt [-v] [file]`: jmt calls the main routine of the class pbj.math.graph.train.MappingTorus. This class takes a train track map and computes a triangulation of its mapping torus. The output format is suitable for jsnap. If a file name is given, jmt tries to read its input from this file, otherwise it reads from stdin. The output goes to stdout in both cases. The option -v (for verbose) prompts jmt to add some comments to its output.

`jsnap [file]`: jsnap calls the main routine of the class pbj.math.manifold.ThreeComplex. This class takes a list of tetrahedra and gluings and converts them to SnapPea's triangulation file format. This part of the package is not restricted to mapping tori. It is intended to facilitate the generation of input for SnapPea. If a file name is given, jsnap tries to read its input from this file, otherwise it reads from stdin. The output goes to stdout in both cases.

Typically, the programs jtwist, jtrain, jdraw, jmt, and jsnap are connected by pipes.

## Examples
`jtwist 3 d0c0d1c1d2C2` computes a homeomorphism using the standard set of Dehn twists (see the online help feature of the graphical user interface).

`jtwist abABDCdc "'-c(bD)aab'"` computes a homeomorphism (this is example 6.1 from [Bestvina, Handel: Train tracks for surface homeomorphisms). The word abABDCdc is an identification pattern on the boundary of a fundamental polygon (see the online help feature of the graphical user interface). Note the combination of quotation marks --- they are necessary to keep the shell from corrupting the string.

`jtwist fix.abcABC "'(ab)(bc)'"` works much like the examples above, except that abcABC is being treated as a fixed word rather than an identification pattern.

`jtwist 3 d0c0d1c1d2C2 | jtrain -m` computes a train track map and keeps track of the twisting curves.

`jtwist 3 d0c0d1c1d2C2 | jtrain | jdraw` draws a PostScript picture of the train track and prints the result to stdin.

`jtwist 3 d0c0d1c1d2C2 | jtrain | jdraw | gv -` displays the result on the screen using ghostview.

`jtwist pbj/examples/batman.tw | jtrain | jdraw -n >batman.ps` reads a homeomorphism from the file pbj/examples/batman.tw and writes the PostScript code to the file batman.ps.

`jtwist pbj/examples/batman.tw | jtrain | jmt | jsnap >foo` takes a composition of Dehn twists, computes a train track, triangulates its mapping torus, and converts the result to SnapPea's triangulation file format; the output is redirected into the file foo, which can be read by SnapPea. It's not really necessary to compute a train track (you can plug jtwist directly into jmt), but it's advantageous because the resulting triangulations tend to be much smaller this way.

## XTrain and SnapPea
XTrain communicates with SnapPea via Python, using shell scripts that may be of independent interest.
