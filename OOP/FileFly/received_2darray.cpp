#include<iostream>
using namespace std;
int main()
{
int A[3][4]={
             {11,12,13,14},
             {21,22,23,24},
             {31,32,33,34}


           };

for(int row=0;row<3;row++)
 {
   for(int col=0;col<4;col++)
   {
     cout<<A[row][col]<<" ";
   }
    cout<<endl;
 }
     return 0;
 }
