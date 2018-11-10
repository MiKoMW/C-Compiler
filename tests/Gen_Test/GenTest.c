//#include "(･ω´･ )"
#include "minic-stdlib.h"
int arr[10];

int binarySearch(int target,  int startIndex, int endIndex){

	int midIndex;
	midIndex = ((endIndex - startIndex) / 2) + startIndex;

	if(midIndex == startIndex || midIndex == endIndex ){

		if(target == arr[midIndex]){return midIndex;}
		else{
		if(target == arr[midIndex+1])
		{return midIndex+1;}
		else {return -1;}
	    }
	}
	if(target > arr[midIndex]){ // go right
		midIndex = midIndex + 1;
		binarySearch(target,midIndex,endIndex);
	}
	else {if(target < arr[midIndex]){ // go left
		midIndex = midIndex - 1;
		binarySearch(target,startIndex,midIndex);
	}
	}
}

int main(){
    int temp;
    temp = 10;
    while(temp > 0){
        arr[temp] =  temp;
        temp = temp - 1;
    }
    print_i(binarySearch(3,0,9));
    return 0;

}