BEGIN {
    i = 0
}

{
    i = i + 1
    print $1 "," i
}

END {

}
