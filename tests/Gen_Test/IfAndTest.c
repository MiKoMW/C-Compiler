#include "minic-stdlib.h"

int foo() {
  print_i(2);
  return 2;
}

int main(){

int a;

if ((1==0) && foo() == 2){
    print_s((char*)"emm");
}
if ((1==0) || foo() == 2){
    print_s((char*)"emm");
}
if ((1==1) && foo() == 2){
    print_s((char*)"emm");
}
if ((1==1) || foo() == 2){
    print_s((char*)"emm");
}


a  =((1==0) && foo() == 2);

print_i(a);

a  =((1==0) || foo() == 2);
print_i(a);

a  =((1==1) && foo() == 2);
print_i(a);

a  =((1==1) || foo() == 2);
print_i(a);


}