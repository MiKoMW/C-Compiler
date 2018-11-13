#include "minic-stdlib.h"

struct apple{

    int a;
    char ch[10];
};


int main(){
    int a[10];
    char ch[10];

    struct apple apps[10];
    ch[0] = 'a';
    ch[1] = 'b';
    ch[2] = 'c';
    ch[3] = 'd';
    ch[4] = 'e';
    a[0] = 1;
    a[1] = 2;
    a[2] = 3;
    a[3] = 4;
    a[4] = 5;
    apps[0].a  = 1;
    apps[0].ch[1] = 'a';

    apps[6].a = 2;
    apps[7].ch[6] = 'c';


    print_i(a[1+3]);
    print_i(a[a[a[1]]]);


    print_c(ch[1]);
    print_c(ch[2]);
    print_c(ch[0]);
    print_c(ch[4]);

    print_i(apps[0].a);
    print_c(apps[0].ch[1]);
    print_i(apps[6].a);
    print_c(apps[7].ch[6]);

    return 0;

}