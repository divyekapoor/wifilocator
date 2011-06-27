accel_file = "./wifi_corridoor_walk.csv";

set terminal png size 1280,960 font "Times-New-Roman,22";
set output "wifi_corridoor_walk.png";

set grid;

set datafile separator ",";

set title "Sensed RSSI Data (Corridoor Walk)";
set xlabel "Time";
set ylabel "RSS of AP EC:92";
unset xtic;

plot accel_file using 1:2 with linespoints title "Wifi Received Signal Strength";

reset;
