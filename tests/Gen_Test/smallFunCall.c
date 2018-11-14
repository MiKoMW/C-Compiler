#include "minic-stdlib.h"

int fun1(int a){
    return a +1;
}

int fun2(int b){
    return b + 1;
}


int main(){
    int a;


    print_i(fun1(fun1(1)));
    return 0;
}