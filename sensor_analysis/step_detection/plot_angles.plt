accel_file = "./latest.accel.csv"
step_file = "./latest.steps.csv"

set terminal png size 1280,960;
set output "angles.png";

set grid;
set datafile separator ",";

set title "Step Detection from Accelerometer Data";
plot step_file  using 1:4 with linespoints title "Angles", accel_file using 1:3 with linespoints title "Acceleration" axes x1y2;

