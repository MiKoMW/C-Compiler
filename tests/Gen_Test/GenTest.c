//#include "(･ω´･ )"
#include "minic-stdlib.h"

struct apple{
    int a;
    char b;
    char c[11];

};

struct banana{
    int a;
    struct apple app;
};

int as[10];
char bs[10];
char bss[11];

struct apple app1;

struct banana ban2;

int happy(int a, int b){
    int c;
    c = a + b;
    return c;
}

int main(){

    print_s((char *) "HelloWorld!\n");
    return 0;
}

