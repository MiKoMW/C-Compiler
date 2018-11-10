#include "minic-stdlib.h"

struct apple{
    int a;
    char b;
};

struct apple fun1(struct apple a, char b, int c){

    struct apple ban;
    print_i(a.a);
    print_c(a.b);
    print_c(b);
    print_i((int) b);
    print_i(c);
    ban = a;
    ban.b = 'c';
    return ban;
}

int main(){

    struct apple ap;
    struct apple ba;
    ap.a = 1;
    ap.b = 'a';
    ba = fun1(ap,'a',2);

    print_i(ba.a);
    print_c(ba.b);
    ap.a = 1;
    ap.b = 'a';
    ba = fun1(ap,'a',2);

    print_i(ba.a);
    print_c(ba.b);
    print_i(ba.a);
        print_c(ba.b);
        ap.a = 1;
        ap.b = 'a';
        ba = fun1(ap,'a',2);

        print_i(ba.a);
        print_c(ba.b);
        print_i(ba.a);
            print_c(ba.b);
            ap.a = 1;
            ap.b = 'a';
            ba = fun1(ap,'a',2);

            print_i(ba.a);
            print_c(ba.b);
            print_i(ba.a);
                print_c(ba.b);
                ap.a = 1;
                ap.b = 'a';
                ba = fun1(ap,'a',2);

                print_i(ba.a);
                print_c(ba.b);
                print_i(ba.a);
                    print_c(ba.b);
                    ap.a = 1;
                    ap.b = 'a';
                    ba = fun1(ap,'a',2);

                    print_i(ba.a);
                    print_c(ba.b);
                    print_i(ba.a);
                        print_c(ba.b);
                        ap.a = 1;
                        ap.b = 'a';
                        ba = fun1(ap,'a',2);

                        print_i(ba.a);
                        print_c(ba.b);
                        print_i(ba.a);
                            print_c(ba.b);
                            ap.a = 1;
                            ap.b = 'a';
                            ba = fun1(ap,'a',2);

                            print_i(ba.a);
                            print_c(ba.b);
                            print_i(ba.a);
                                print_c(ba.b);
                                ap.a = 1;
                                ap.b = 'a';
                                ba = fun1(ap,'a',2);

                                print_i(ba.a);
                                print_c(ba.b);
                                print_i(ba.a);
                                    print_c(ba.b);
                                    ap.a = 1;
                                    ap.b = 'a';
                                    ba = fun1(ap,'a',2);

                                    print_i(ba.a);
                                    print_c(ba.b);
                                    print_i(ba.a);
                                        print_c(ba.b);
                                        ap.a = 1;
                                        ap.b = 'a';
                                        ba = fun1(ap,'a',2);

                                        print_i(ba.a);
                                        print_c(ba.b);
                                        print_i(ba.a);
                                            print_c(ba.b);
                                            ap.a = 1;
                                            ap.b = 'a';
                                            ba = fun1(ap,'a',2);

                                            print_i(ba.a);
                                            print_c(ba.b);
                                            print_i(ba.a);
                                                print_c(ba.b);
                                                ap.a = 1;
                                                ap.b = 'a';
                                                ba = fun1(ap,'a',2);

                                                print_i(ba.a);
                                                print_c(ba.b);

    return 1;

}