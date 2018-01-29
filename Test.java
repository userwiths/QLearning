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
    public static void main(String[] arg){
        Random rnd=new Random();
        int size=100;
        int[][] maze=new int[][]{
                                { 0, 0, 0,-1, 0},
                                {-1,-1, 0, 0, 0},
                                { 0, 0, 0,-1,-1},
                                {-1, 0,-1,-1,-1},
                                {-1, 0, 0, 0, 1}
                                };
        Mind brain=new Mind();
        brain.LoadDefault();
        
        //brain.AddStates(states);
        brain.RandomTable(size,0.1,10);
        //brain.SetSquareTable(maze);
        brain.MatrixForm();
        
        //Best Way of action.
        brain.SetVariable("epsilon", 0.64);
        
        brain.ForceSuccess();
        
        for(int i=0;i<11;i++){
            brain.StartRandom(1000);
            brain.Status();
        }
        
        brain.SetVariable("epsilon",0.1);
        for(int i=0;i<11;i++){
            brain.StartRandom(1000);
            brain.Status();
        }
        
    }
}
