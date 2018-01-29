/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QLearnRemastered;

//import QLearnRemastered.Mind;
import QLearnRemastered.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Test {
    public static void PredefineTable(){
        int[][] maze=new int[][]{
                                { 0, 0, 0,-1, 0},
                                {-1,-1, 0, 0, 0},
                                { 0, 0, 0,-1,-1},
                                {-1, 0,-1,-1,-1},
                                {-1, 0, 0, 0, 1}
                                };
        Mind brain=new Mind();
        brain.SetSquareTable(maze);
        brain.MatrixForm();
        //Start of tests.
        brain.SetVariable("epsilon", 0.64);
        brain.ForceSuccess();
        //First Test
        brain.StartGeneral(0,100);
        brain.Status();
        //Secon test
        brain.SetVariable("epsilon",0.1);
        brain.StartGeneral(0,100);
        brain.Status();      
        //Last test
        brain.SetVariable("epsilon",0.01);
        brain.StartGeneral(0,100);
        brain.Status();      
    }
    
    public static void main(String[] arg){
        Random rnd=new Random();
        int size=100;
        
        Mind brain=new Mind();
        brain.RandomTable(size, 0.09, 10);
        brain.MatrixForm();
        
        for(int i=0;i<1000;i++){
            brain.SetVariable("epsilon",1.0/(i+1));
            brain.StartRandom(1000);
            if(brain.Use("win") == 1000){
                brain.Status();
                return;
            }
            brain.Status();
        }
    }
}
