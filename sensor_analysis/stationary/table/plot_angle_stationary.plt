accel_file = "angle.5-5-2011-20-0-19.csv";
accel_file_2 = "../handheld/standing/angle.5-5-2011-20-6-40.csv";

set terminal png size 1280,960 font "Times-New-Roman,22";
set output "angle_stationary.png";

set grid;

set datafile separator ",";
set ytic nomirror;
set y2tic;
set xrange [0:850];
set yrange [30:55];
set y2range [80:105];

set title "Sensed Orientation Data (Static Device)";
set xlabel "Sample Number";
set ylabel "Angle (degrees) (Table)";
set y2label "Angle (degrees) (Hand)";
plot accel_file using ($3*180/3.14159) with linespoints title "Sensed Orientation (Table)", accel_file_2 using ($3*180/3.14159) with linespoints title "Sensed Orientation (Hand)" axes x1y2;

reset;
