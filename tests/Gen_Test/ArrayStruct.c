#include "minic-stdlib.h"


struct apple{
    int a;
    int b;
    char c;
    int d[4];
};
int main(){

    struct apple app;
    struct apple ban;

    app.a = 1;
    app.b = 2;
    app.c = '3';
    app.d[1] = 4;
    app.d[3] = 4;

    ban = app;

    print_i(ban.a);
    print_i(ban.b);
    print_c(ban.c);
    print_i(ban.d[1]);
    print_i(ban.d[3]);

    return 0;


}