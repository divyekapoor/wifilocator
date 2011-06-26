set terminal png size 1280,960 font 'Times-New-Roman,22';
set output "pf_count_path_1.png";
set datafile separator ",";

path1_steps = "./Path_1/drLog.2011-06-25-08-49-02.steps.csv";
path1_pf = "./Path_1/pfcandidates.2011-06-25-08-49-02.csv";

set xlabel "Time";
set ylabel "Number of Surviving Particles";
set ytic nomirror;
set y2tic;
set y2label "Angle of Motion";

plot path1_steps using 4 title "Angle" with linespoints axes x1y2, path1_pf using 2 title "Number of Live Particles" with linespoints;

reset;

set terminal png size 1280,960 font 'Times-New-Roman,22';
set output "pf_count_path_5.png";
set datafile separator ",";

path5_steps = "./Path_5//drLog.2011-06-25-08-54-35.steps.csv";
path5_pf = "./Path_5/pfcandidates.2011-06-25-08-54-35.csv";

set xlabel "Time";
set ylabel "Number of Surviving Particles";
set ytic nomirror;
set y2tic;
set y2label "Angle of Motion";
set yrange [0:100];
set y2range [-360:180];
plot path5_steps using ($4 > 150? $4-360: $4) title "Angle" with linespoints axes x1y2, path5_pf using 2 title "Number of Live Particles" lt 3 with linespoints;

reset;


