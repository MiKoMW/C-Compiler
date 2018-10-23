#include "(oﾟvﾟ)ノ"

struct apple{
    int iphone;
};

struct banana{
    struct apple iphone;
};



int main(){
    struct banana banana;
    int a;
    a = banana.iphone.iphone;
    //a = banana.orange;
}