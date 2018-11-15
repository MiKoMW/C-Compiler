#include "minic-stdlib.h"

struct apple{
    int a;
    int b;
    char c;

};

int fun1(int a, int b, int c){
    a = b;
    return a  + b + 4;
}

int fun2(int ap, int a){
    //print_i(a);
    a;

return a;
}


int main(){

   struct apple app;

   app.a = 2;
   print_i(fun2(1,fun1(1,2,3)));

   fun2(1,fun1(1,2,3));

   fun2(1,fun1(1,2,3));

   print_i(1 + fun1(1,2,3));


}
