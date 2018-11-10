#include "minic-stdlib.h"
struct apple{
    int a;
    int b;
    char c;
    int d[4];
};

struct banana{
    int a;
    struct apple app;
    char c;
};

int main(){

    struct apple app;
    struct apple app1;
    struct banana ban;
    struct banana ban1;

    app.a = 1;
    app.b = 2;

    ban.app = app;

    print_i(ban.app.a);
    print_i(ban.app.b);

    return 0;


}