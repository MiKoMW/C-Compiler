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

int test(int a, struct apple iphone, int q, int w, int e, int r){
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
    int dd[10];
    struct apple * iphone;
    struct apple iphones[10];
    struct apple aiphone;
    struct banana ba;
    struct banana banas[10];
        //test(1);
    return 0;

}