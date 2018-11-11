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

struct apple fun2(struct apple ap, int a){
    //print_i(a);
    ap.a = a;

return ap;
}


int main(){

   int a;
   struct apple app;

   app.a = 2;
   print_i(fun2(app,fun1(1,2,3)).a);

   fun2(app,fun1(1,2,3)).a;
   fun2(app,fun1(1,2,3)).a;
   fun2(app,fun1(1,2,3)).a;
   fun2(app,fun1(1,2,3)).a;
   fun2(app,fun1(1,2,3)).a;

   print_i(1 + fun1(1,2,3));


}