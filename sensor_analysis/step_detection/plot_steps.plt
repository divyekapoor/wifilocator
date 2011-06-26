accel_file = "./latest.accel.csv"
step_file = "./latest.steps.csv"

set terminal png size 1280,960 font "Times-New-Roman,22";
set output "step_detection.png";

set datafile separator ",";
unset xtic;
set y2tic;
set ytic nomirror;
set yrange [-5:10];
set y2range [-2:2];

set title "Step Detection from Accelerometer Data";
set xlabel "Time";
set ylabel "Acceleration";
set y2label "Step Size (m)";
plot accel_file using 1:(abs($3) < 1.3? 0: $3) with linespoints title "Clamped Acceleration", \
    step_file using 1:($3/11.3) with linespoints title "Step Size" axes x1y2;

reset;

set terminal png size 1280,960 font "Times-New-Roman,22";
set datafile separator ",";
unset xtic;
set output "accel_diff.png";
set title "Difference Signal: Raw - Filtered Acceleration";
set xlabel "Time";
set ylabel "Acceleration";
set yrange [-8:8];
plot accel_file using 1:($3 - (abs($3) < 1.3? 0 : $3)) with linespoints title "Difference Signal";
reset;


set terminal png size 1280,960 font "Times-New-Roman,22";
set datafile separator ",";
set output "accel_raw.png";
set title "Raw Acceleration Sensor Data";
set xlabel "Time";
set ylabel "Raw Acceleration";
set y2label "Clamped Acceleration";
unset xtic;
set y2range [-8:24];
set yrange [-24:8];
set ytics nomirror;
set y2tics;

plot accel_file using 1:3 with linespoints title "Raw Acceleration", \
    accel_file using 1:(abs($3) < 1.3? 0: $3) with linespoints title "Clamped Acceleration" axes x1y2, \
    step_file using 1:3 with linespoints title "Step Detected" axes x1y2;

reset;
