
struct emma {
char b[100];
    int a;
};


struct emm {
char b[100];
    int a;
    struct emma kk;
};

char input[1000000];


int main(){

    int a;
    int remove;
//    cin >> a >> remove;
//   scanf("%s",input);

    //char temp = 'a';

    int index ;

    index = 0;

    while(true){

        if(remove == 0){
            //for(int con = 0; con < a; con++){
                if(isalpha(input[con])){
                    printf("%c",input[con]);
                }
            //}
            return 0;
        }

        if(index == (a-1)){
            //temp++;
            index = 0;
        }

       // for(int con = index; con < a; con++){
            index = con;
            if(input[index] == temp){
                input[index] = 0;
                //remove--;
                break;
            }

        //}

    }


    return 0;

}
