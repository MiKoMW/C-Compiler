#include "minic-stdlib.h"

int main(){

    int a;
    int b;
    a = 1;
    b = 2;

    a = 1 + 3 / 4 + 2;
    b = a - 2 + 3 / 5 + 2 * a;

    b =  a % 3 >=1;

    a = b <= a;

    a = a >1  + b < 1;
    a = a > b;
    a = a || b > 10;
    b = a && a > 10;

    print_i(a!=b);
    print_i(a);
    print_i(b);

}

//    ADD, SUB, MUL, DIV, MOD, GT, LT, GE, LE, NE, EQ, OR, AND;
