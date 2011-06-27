accel_file = "wifi_signal_strength_short_term_3.csv";

set terminal png size 1280,960 font "Times-New-Roman,22";
set output "wifi_short_term.png";

set grid;

set datafile separator ",";

set title "Short Term RSSI Variations";
set xlabel "Time";
set ylabel "RSS of AP EC:95";
unset xtic;

plot accel_file using 1:2 with linespoints title "Wifi Received Signal Strength";

reset;
