#include "minic-stdlib.h"

int main(){
    int a;
    int b;
    int con;

    a = 1;
    con = 1;
    while(con <= 10){
    if(a){

        b = 0;
    }else{
        if(b)
        a = 0;
    }

    if( a != b){
        print_i(1);
    }else{
        print_i(2);
    }

    con =  con + 1;
    }

    while(con <1000)
    con = con + 1;

    print_i(con);
   return 0;



}