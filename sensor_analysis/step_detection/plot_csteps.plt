cstep_zcrossing = "./latest.zcstep.csv"
cstep_zcrossing_clamped = "./latest.zcstep.clamped.csv"
cstep_clamped_counter = "./latest.cstep.csv"
cstep_estimator = "./latest.steps.csv"

set terminal png size 1280,960;
set output "csteps.png";

set grid;
set datafile separator ",";
unset xtic;

set title "Step Detection from Accelerometer Data";
plot cstep_zcrossing using 1:2 with linespoints title "Zero Crossing Steps (Raw data)", \
    cstep_zcrossing_clamped using 1:2 with linespoints title "Zero Crossing (Clamped)", \
    cstep_clamped_counter using 1:2 with linespoints title "Proposed Steps (Counter)";
