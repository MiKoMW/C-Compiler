#include "(oﾟvﾟ)ノ"

struct apple{
    int iphone;
    int temp[10];
};

struct banana{
    struct apple iphone;
};



int main(){
    struct banana banana;
    struct apple app;
    int a;
    banana.iphone.temp[5] = 1;
    banana.iphone.iphone = 2;
    a = banana.iphone.iphone;
    print_i(a);
    print_i(banana.iphone.temp[5]);
    //app.temp = "asd";
    //"asd" == "add";
    //a = banana.orange;
}