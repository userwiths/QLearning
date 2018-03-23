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
        int size=50;
        int temp=0;
        Random rnd=new Random();
        
        Mind brain=new Mind();
        brain.setRandomTable(size, 0.28, 5);
        
        System.out.println("Before addition ....");
        brain.addObjective("Gold");
        brain.addObjective("Heal");
        brain.addObjective("Coal");
        
        System.out.println("After addition ....");
        
        brain.StartStudy(true);
        brain.GraphicSolutionRandom();
        in.next();
        
        brain.setObjective("Gold");
        brain.StartStudy(true);
        brain.GraphicSolutionRandom();
        
        brain.setObjective("Heal");
        brain.StartStudy(true);
        brain.GraphicSolutionRandom();
        in.next();
        
        brain.setObjective("Coal");
        brain.GraphicSolutionRandom();
        System.out.println("Coal: withouth");
        in.next();
        //PredefineTable();
    }
}
