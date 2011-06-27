accel_file = "angle.6-5-2011-0-31-1.csv";
accel_file_2 = "angle.6-5-2011-0-31-33.csv";

set terminal png size 1280,960 font "Times-New-Roman,22";
set output "angle_180_corridoor.png";

set grid;

set datafile separator ",";
set ytic nomirror;
set y2tic;
set yrange [0:120];
set y2range [-120:0];

set title "Sensed Orientation Data (Device in Motion)";
set xlabel "Sample Number";
set ylabel "Angle (degrees) (Forward Path)";
set y2label "Angle (degrees) (Reverse Path)";
plot accel_file using ($3*180/3.14159) with linespoints title "Sensed Orientation (Forward Path)", accel_file_2 using ($3*180/3.14159) with linespoints title "Sensed Orientation (Reverse Path)" axes x1y2;

reset;
