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
                                {-1, 0, 0, 0, 0, 0, 0, 0},//  5
                                { 0, 0, 0,-1, 0, 0, 0,-1},
                                {-1,-1,-1,-1,-1,-1, 0, 0},
                                {-1, 0, 0, 0, 1, 0, 0, 0}
                                };
        Mind brain=new Mind();
        brain.setSquareTable(maze);
        brain.MatrixForm();
        //Start of tests.
        brain.ForceSuccess();
        for(int i=0;i<10;i++){
            brain.setVariable("epsilon",1.0/((i+1)*10));
            //brain.StartRandom(1000);
            brain.StartGeneral(0,100);
        }
        brain.setVariable("epsilon", 0.00001);
        brain.GraphicSolution();
    }
    
    public static void main(String[] arg){
        Scanner in=new Scanner(System.in);
        int size=100;
        int temp=0;
        Random rnd=new Random();
        
        Mind brain=new Mind();
        brain.setRandomTable(size, 0.3, 1);
        //brain.MatrixForm();
        brain.ForceSuccess();

        brain.StartStudy(true);
        brain.setVariable("epsilon", 0.0);
        brain.GraphicSolutionRandom(0);
        //PredefineTable();
    }
}
