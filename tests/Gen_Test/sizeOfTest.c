#include "minic-stdlib.h"

struct apple{
    int a;
    char b;
    char ch[10];
    int d[10];
};

struct banana{
    struct apple app;
};

int main(){

    print_i(sizeof(int));
    print_c('\n');
        print_i(sizeof(char));
            print_c('\n');

        print_i(sizeof(int *));
            print_c('\n');

        print_i(sizeof(struct apple));
            print_c('\n');

               print_i(sizeof(char [10]));
                   print_c('\n');

               print_i(sizeof(struct banana));


}