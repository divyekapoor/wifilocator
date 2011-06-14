accel_file = "./latest.accel.csv"
step_file = "./latest.steps.csv"

set terminal png size 1280,960;
set output "step_detection.png";

set grid;
set datafile separator ",";
unset xtic;

set title "Step Detection from Accelerometer Data";
plot accel_file using 1:(abs($3) < 1.3? 0: $3) with linespoints title "Clamped Acceleration", \
    step_file using 1:($3-2) with linespoints title "Steps";

set output "accel_diff.png";
set title "Difference Signal: Raw - Filtered Acceleration";
set yrange [-8:8];
plot accel_file using 1:($3 - (abs($3) < 1.3? 0 : $3)) with linespoints title "Difference Signal";

set output "accel_raw.png";
set title "Raw Acceleration Sensor Data";
set y2range [-8:24];
set yrange [-24:8];
set ytics nomirror;
set y2tics;

plot accel_file using 1:3 with linespoints title "Raw Acceleration", \
    accel_file using 1:(abs($3) < 1.3? 0: $3) with linespoints title "Clamped Acceleration" axes x1y2, \
    step_file using 1:3 with linespoints title "Step Detected" axes x1y2;

