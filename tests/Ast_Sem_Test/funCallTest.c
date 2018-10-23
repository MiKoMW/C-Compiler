#include "(￣▽￣)～■干杯□～(￣▽￣)"

struct apple{
    int iphone;
};

// In gcc it is totally Ok to do this.
// but warning will be generated. However, as a pro, I do not care about the warning.
int main(){

}

void main1(){
//return 1;
return;
}


int test1(){
return 1;
//Gcc can do this, but we CANNOT!
//return 'c';
}


int test2(){

    return 1;
    return 1;
    return 2;
    return 3;
    return 1+1;
    return 1+10086;
    return sizeof(int);
}

int test3(int a,int b,struct apple d){
}