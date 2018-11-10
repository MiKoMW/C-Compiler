int main(){
   int * pt;
   char a;
   print_i(read_i());
   print_c('\n');
   print_c(read_c());
   print_s((char*)">_<");
   print_s((char*)"\n");
   pt = (int*)mcmalloc(10);
   a = read_c();

   *pt = 1;

   print_i(*pt);



}