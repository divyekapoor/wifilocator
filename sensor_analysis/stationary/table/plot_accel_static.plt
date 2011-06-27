accel_file = "accel.5-5-2011-20-0-19.csv";
accel_file_2 = "../handheld/standing/accel.5-5-2011-20-6-40.csv";

set terminal png size 1280,960 font "Times-New-Roman,22";
set output "accel_static.png";

set grid;

set datafile separator ",";
set xrange [0:300];
set yrange [-0.6:1.2];
set ytic nomirror;
set y2range [-1.2:0.6];
set y2tic;

set title "Accelerometer Data (Static Accelerometer)";
set xlabel "Sample Number";
set ylabel "Acceleration (Table)";
set y2label "Acceleration (Hand)";
plot accel_file using 5 with linespoints title "Sensed Acceleration (Table)", accel_file_2 using 5 with linespoints title "Sensed Acceleration (Hand)" axes x1y2;

reset;
