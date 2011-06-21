BEGIN {
    i = 0
    state = 0
}

{
    accel_val = $3

    if(state == 0 && accel_val > 0) {
        state = 1
    }

    if(state == 1 && accel_val < 0) {
        state = 0
        i = i + 1
        print $1 "," i
    }
}

END {

}
