package QLearn;

import java.awt.Color;
import java.util.*;

public class Test {
    public static void PredefineTable(){
        int[][] maze=new int[][]{
                                { 0, 0, 0,-1, 0,-1,-1, 0},
                                {-1,-1, 0, 0, 0, 0,-1,-1},
                                { 0, 0, 0,-1,-1, 0, 0,-1},
                                {-1, 0,-1,-1,-1,-1, 0, 0},
                                {-1, 0, 0, 0,-1, 0, 0,-1},
                                { 0, 0, 0,-1, 0, 0, 0,-1},
                                {-1,-1, 0,-1,-1,-1, 0, 0},
                                {-1, 0, 0, 0, 1, 0, 0, 0}
                                };
        Mind brain=new Mind();
        brain.setSquareTable(maze);
        brain.MatrixForm();
        brain.StartGeneral(0, 1000);
        
        brain.setVariable("epsilon", 0.0);
        brain.GraphicSolution();
    }
    
    public static void main(String[] arg){
        
        Scanner in=new Scanner(System.in);
        int size=100;
        int temp=0;
        Random rnd=new Random();
        
        Mind brain=new Mind();
        brain.setRandomTable(size, 0.36, 1);
        brain.ForceSuccess();
        brain.setVariable("max_steps", 100);
        brain.StartStudy(100,false);
        brain.setVariable("epsilon", 0.0);
        brain.GraphicSolutionRandom(0);
        
        //PredefineTable();
    }
}
