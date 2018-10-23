#include "<stdio.h>"
#include "iostream"
#include "string.h>"
#include "math.h"

int main(){

    int ch;
    char qwerty[10];
    //qwerty[] = "`1234567890-=QWERTYUIOP[]\\ASDFGHJKL;'ZXCVBNM,./";  -- array can not be empty.

    while((ch) == EOF){

        char* pq;
         pq = strchr(qwerty,ch);
        if(pq == NULL){
            putchar(ch);
        }else{
            putchar(pq[-1]);
        }

    }

    return 0;
}