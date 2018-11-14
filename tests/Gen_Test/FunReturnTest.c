#include "minic-stdlib.h"

struct apple{
    int a;
};


char fun1(int a){

    return 'a';
}

int fun2(){
    return 3;
}

struct apple fun3(){
    struct apple ap;

    ap.a = 2;

    return ap;
}


void fun4(){

}

char * fun5(){
    return (char*) "hihihi";
}

int main(){


    int a;
    char b;
    struct apple c;


   char  * e;


a = fun2();
b = fun1(2);

c = fun3();
fun4();

e = fun5();

    print_i(a);
    print_c(b);
    print_i(c.a);
    print_s(e);
    return 1;


}