struct apple{
    int a;
    int b;
    char c;
    int d;
};
int main(){

    struct apple app;
    struct apple ban;

    app.a = 1;
    app.b = 2;
    app.c = '3';
    app.d = 4;

    ban = app;

    print_i(ban.a);
    print_i(ban.b);
    print_c(ban.c);
    print_i(ban.d);
    return 0;


}