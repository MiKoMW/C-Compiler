        //VarExpr, FieldAccessExpr, ArrayAccessExpr or ValueAtExpr.

#include "minic-stdlib.h"

struct apple{
    int a;
    char ch[10];
};


char ch[10];

    struct apple ap1;
    struct apple ap2;


int main(){

struct apple apps[12];
int a;

struct apple * ptr;
struct apple * ptrs;

    ap1.a = 1;
    ap1.ch[2] = 'a';


    ap2 = ap1;

    apps[1] = ap2;

    apps[6] = apps[1];

    print_i(apps[6].a);

    apps[6].ch[1] = 'c';

    ap2 = apps[6];

    apps[0] = apps[1];

    ptr = (struct apple *) apps;




    print_i(ap2.a);
    print_c(ap2.ch[2]);
    print_c(ap2.ch[1]);

    a = 10;

    print_i(a);

    print_i((*ptr).a);

    print_c((*ptr).ch[2]);

    (*ptr).a = 10086;

    apps[11] = (*ptr);

    print_i(apps[11].a);


    return 0;


}