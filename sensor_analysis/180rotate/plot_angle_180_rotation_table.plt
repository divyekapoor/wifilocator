accel_file = "angle.6-5-2011-5-1-25.csv"

set terminal png size 1280,960 font "Times-New-Roman,22";
set output "angle_180_rotation_table.png";

set grid;

set datafile separator ",";

set title "Sensed Orientation Data (Rotating Device)";
set xlabel "Sample Number";
set ylabel "Angle (degrees) (Table)";
plot accel_file using ($3*180/3.14159 < -150? 360 + $3*180/3.14159 : $3*180/3.14159) with linespoints title "Sensed Orientation (Table)";

reset;
