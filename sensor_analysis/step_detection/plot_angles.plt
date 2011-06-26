accel_file = "./latest.accel.csv"
step_file = "./latest.steps.csv"

set terminal png size 1280,960 font 'Times-New-Roman,22';
set output "angles.png";

set grid;
set datafile separator ",";

set title "Angles Per Step for Constant Unidirectional Motion";
set xlabel "Time";
unset xtic;
set y2tic;
set ytic nomirror;
set y2label "Acceleration (m/s^2)";
set ylabel "Angle (degrees)";
set y2range [-8:16];
set yrange [-140:-70];
plot step_file  using 1:4 with linespoints title "Angles", accel_file using 1:3 with linespoints title "Acceleration" axes x1y2;

reset;

