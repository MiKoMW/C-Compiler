#include "minic-stdlib.h"

struct apple{
    int a;
    int b;
    char c;

};

struct apple fun1(int a, int b, int c){

    struct apple ap;
    ap.a = 1;
    return ap;
}

struct apple fun2(struct apple ap, int a){
    print_i(a);
    ap.a = a;

return ap;
}


int main(){

    int a;
    int b;
   struct apple app;

    int temp;
   app.a = 2;
    //temp = fun1(1,2,3);
   print_i(fun2(app,fun1(1,2,3).a).a);

   fun2(app,fun1(1,2,3).a).a;


   print_i(1 + fun1(1,2,3).a);


}