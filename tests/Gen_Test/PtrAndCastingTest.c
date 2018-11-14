#include "minic-stdlib.h"

int main(){

    int arr[10];

    char * pt;
    int * ptint;
    char ch;


    ch = 'a';
    pt = (char*) "hihihi";

    print_s(pt);

    print_c('\n');

    print_c(*(pt));
    print_c('\n');

    print_c(*pt);
    print_c('\n');

    print_s(pt);
    print_c('\n');

    *pt = 'a';

    print_s(pt);


    print_c('\n');


    ptint = (int *)pt;
    print_c('\n');

    print_i(*ptint);
    print_c('\n');

    print_s(pt);

    print_i((int) ch);

}
