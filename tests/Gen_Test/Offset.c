struct apple{

    int a;
    int b;
    char c;
    char d[10];
};

struct banana{

    int a;
    int b;
    struct apple c;
    struct apple * d;
    struct apple e[10];
};

int a;
int b;

int test(int a, struct apple iphone){
    int b;
    struct apple c;


    a  = 1;
    b = 2;

    {
    int a;
    }

}


int main(){
    int a;
    int b;
    int c;
    //test(1);
    return 0;

}