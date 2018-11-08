#include "(･ω´･ )"

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

int happy(int a){
    int b;
    b = a + 1;
    return b;
}

int main(){

    int a;
    char b;
    a = 1;
    b = 'a';
    a = happy(a);
    print_i(a);
    print_c(b);
    print_s((char *) "HelloWorld!");
    return 0;
}

