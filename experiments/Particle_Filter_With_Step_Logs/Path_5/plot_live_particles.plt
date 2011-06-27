accel_file = "pfcandidates.2011-06-25-08-54-35.csv"

set terminal png size 1280,960 font "Times-New-Roman,22";
set output "live_particles.png";

set grid;

set datafile separator ",";

set title "Number of Live Particles";
set xlabel "Time";
set ylabel "Live Particles";
set noxtic;
set yrange [0:60];
plot accel_file using 1:2 with linespoints title "Live Particles";

reset;
