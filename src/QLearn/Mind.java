package QLearn;

import java.awt.Color;
import java.awt.GridLayout;

import java.util.*;
import javax.swing.*;

public class Mind {
    private Map<String,Double> Variable;
    private List<States> DStates;
    
    //Constructors
    public Mind(){
        this.Variable=new HashMap<String,Double>();
        this.DStates=new ArrayList<States>();
        this.LoadDefault();
    }
    public Mind(List<String> str,List<Double> doub){
        this.Variable=new HashMap<String,Double>();
        this.DStates=new ArrayList<States>();
        if(str.size()==doub.size()){
            for(int i=0;i<str.size();i++){
                this.Variable.put(str.get(i).toLowerCase(),doub.get(i));
            }
        }
        this.LoadDefault();
    }
    public Mind(List<States> state){
        this.Variable=new HashMap<String,Double>();
        this.DStates=new ArrayList<States>();
        this.LoadDefault();
        this.DStates.addAll(state);
    }    
    
    /*
        Equtations
        NOTE: For now Bellman is the best implemented method. I will try implementing them again later.
    */
    public double Bellman(States state,Actions act){
        return act.getState().getAttribute("exit")+
                Use("gamma")*Use("discount")*act.getState().getBestAction().getReward();
    }
    public double DoubleQ(Actions act){
        return act.getState().getAttribute("exit")+Use("gamma")*(Use("discount")*
                    act.getState().getBestAction().getReward());
    }
    /*
        Additional conditions
        https://pdfs.semanticscholar.org/fe60/f0d1bff543a86c15a5851ee6a948b04d10cf.pdf
    */
    //Getters, Setters, And Utility Functions
    public States getState(int i){
        return this.DStates.get(i);
    }
    public void AddStates(List<States> st){
        this.DStates.addAll(st);
    }
    public void AddVariable(String name,double value){
        this.Variable.put(name, value);
    }
    public void RemoveVariable(String name){
        this.Variable.remove(name);
    }
    public void setVariable(String name,double value){
        if(this.Variable.containsKey(name)){
            this.Variable.replace(name, value);
        }else{
            this.Variable.put(name,value);
        }
    }
    public double Use(String name){
        return this.Variable.get(name)==null?Double.NEGATIVE_INFINITY:this.Variable.get(name);
    }
    public void LoadDefault(){
        //Load some default memory values.
        this.Variable.put("gamma",0.86);
        this.Variable.put("discount",0.58);
        this.Variable.put("epsilon", 0.47);
       // this.Variable.put("start",0.0);
       // this.Variable.put("end", (double)this.DStates.size());
        this.Variable.put("cursor", 0.0);
        this.Variable.put("steps", 0.0);
        this.Variable.put("win", 0.0);
        this.Variable.put("lost", 0.0);
        this.Variable.put("generations", 0.0);
        this.Variable.put("max_steps", Double.POSITIVE_INFINITY);
        this.Variable.put("explore",0.0);
    }
    public void ClearMemory(){
        this.DStates.clear();
        this.Variable.clear();
        this.LoadDefault();
    }
    public int CountRewards(){
        int count=0;
        for(States state:this.DStates){
            if(state.getAttribute("exit")==1.0){
                count++;
            }
        }
        return count;
    }
    //Run time used. Separate different ideas of the algorithm.
    public void RewardUpdate(States state,Actions act){
        /*TODO:
            Source:https://hira.hope.ac.uk/id/eprint/231/1/Improved-Q-Learning-Oct19-2012.pdf
            Fuction:Lock Mechanism.
       
        if(act.getState().getAttribute("lock")==1.0){
            state.setAttribute("lock", 1.0);
            return;
        }
        */
        act.setReward(Bellman(state,act));
    }
    public void CheckExitState(States current){
        //  Lost
        if(current.getAttribute("exit")==-1.0){
            this.setVariable("lost", Use("lost")+1);
            this.setVariable("steps", 0.0);
        //  WON
        }else if(current.getAttribute("exit")==1.0){
            this.setVariable("win", Use("win")+1);
            this.setVariable("steps", 0.0);
        }
    }
    public States ExecuteAction(States current){
        Random rnd=new Random();
        Actions choosed;
        //Random exploration
        if(rnd.nextDouble()<Use("epsilon")){
            choosed=current.GetRandomAction();
            RewardUpdate(current,choosed);
            current=choosed.getState();
            this.setVariable("steps", Use("steps")+1);
            return current;
        }
        //Follow the best available action.
        choosed=current.getBestAction();
        RewardUpdate(current,choosed);
        current=choosed.getState();
        this.setVariable("steps", Use("steps")+1);
        return current;
    }
    public void ForceSuccess(){
        for(int i=0;i<this.DStates.size();i++){
            this.DStates.get(i).RemovePenalty();
        }
    }
    //TODO
    public void MultiReward(){
        for(int i=0;i<this.DStates.size();i++){
            if(this.DStates.get(i).getAttribute("exit")==1.0){
                
            }
        }
    }
  
    //Different Starting Options
    public void StartGeneral(){
        States current=this.DStates.get((int)Use("cursor"));
        while(current.getAttribute("exit")==0.0){
            current=ExecuteAction(current);
            if(Use("steps")>=Use("max_steps")){
                this.setVariable("steps", 0.0);
                break;
            }
        }
        this.CheckExitState(current);
    }
    public void StartGeneral(int cursor){
        States current=this.DStates.get(cursor);
        while(current.getAttribute("exit")==0.0){
            current=ExecuteAction(current);
            this.setVariable("steps", Use("steps")+1.0);
            if(Use("steps")>=Use("max_steps")){
                this.setVariable("steps", 0.0);
                break;
            }
        }
        this.CheckExitState(current);
    }
    public void StartGeneral(int cursor,int generations){
        if(generations==0){
            return;
        }
        States current=this.DStates.get(cursor);
        while(current.getAttribute("exit")==0.0){
            current=ExecuteAction(current); 
            if(Use("steps")>=Use("max_steps")){
                this.setVariable("steps", 0.0);
                break;
            }
        }
        this.CheckExitState(current);
        StartGeneral(cursor,generations-1);
    }
    public void StartStudy(boolean verbose){
        for(int i=0;i<1000;i++){
            this.setVariable("epsilon",1.0/(i+1));
            this.StartRandom(1000);
            if(this.Use("win") > 900){
                break;
            }
            if(verbose){
                System.out.println("After "+((i+1)*1000)+" generations: ");
                this.Status();
            }
            this.setVariable("win", 0.0);
            this.setVariable("lost", 0.0);
        }
    }
    public void StartRandom(int generations){
        Random rnd=new Random();
        int index=0;
        //Keep trak of generations.
        setVariable("generations",Use("generations")+generations);
        //Execute for the given nuber of cycles/generations
        for(int i=0;i<generations;i++){
            //Do not land on a forbiden ground.
            do{
                index=rnd.nextInt(this.DStates.size());
            }while(this.DStates.get(index).getAttribute("exit")!=0.0);
            StartGeneral(index);
        }
    }
    public void StartMark(){
        States current=this.DStates.get((int)Use("cursor"));
        Random rnd=new Random();
        Actions choosed;
        while(current.getAttribute("exit")==0.0){
            current.setAttribute("mark", 2);
            if(rnd.nextDouble()<Use("epsilon")){
                choosed=current.GetRandomAction();
                RewardUpdate(current,choosed);
                current=choosed.getState();
                this.setVariable("steps", Use("steps")+1.0);
                current.setAttribute("mark", 2);
                continue;
            }
            //Follow the best available action.
            //current.setAttribute("mark", 2);
            choosed=current.getBestAction();
            RewardUpdate(current,choosed);
            current=choosed.getState();
            this.setVariable("steps", Use("steps")+1.0);
            
        }
        this.CheckExitState(current);
    }
    public void StartMoreThanOne(int cursor){
        int count=this.CountRewards();
        States current=this.DStates.get(cursor);
        while(count>0){
            current=ExecuteAction(current);
            this.setVariable("steps", Use("steps")+1.0);
            if(Use("steps")>=Use("max_steps")){
                this.setVariable("steps", 0.0);
                break;
            }
            if(current.getAttribute("exit")==1.0){
                count--;
                current.setAttribute("exit", 0.0);
            }
            if(current.getAttribute("exit")==-1.0){
                System.out.println("Could not reach the end .... :(");
                return;
            }
        }
        System.out.println("Needet "+Use("steps")+" in order to reach current location.");  
    }
    
    //Input/Output options
    public void Status(){
        System.out.println("Generations: "+Use("generations")+"\tWin: "+Use("win")+"\tLost: "+Use("lost")+"\tSteps: "+Use("steps"));
        this.setVariable("steps", 0.0);
        this.setVariable("win", 0.0);
        this.setVariable("lost", 0.0);
    }
    public void ShowChecked(String checked,int n,int m){
        int ls_cursor=0;
        for(int i=0;i<m;i++){
            for(int j=0;j<n;j++){
                ls_cursor=i*n+j;
                if(this.DStates.get(ls_cursor).getAttribute("mark")==2.0){
                    System.out.print(checked);
                    continue;
                }else{
                    System.out.print("-");
                }
            }
            System.out.println();
        }
    }
    public void PrintPath(int start){
        States current=this.DStates.get(start);
        System.out.println("Starting from : "+start);
        while(current.getAttribute("exit")==0.0){
            System.out.println("Went: "+current.ImprovedBestAction().getName());
            current=ExecuteAction(current);
            this.setVariable("steps", Use("steps")+1.0);
            if(Use("steps")>=Use("max_steps")){
                this.setVariable("steps", 0.0);
                break;
            }
        }
        System.out.println("Reached exit ...\nWith: "+Use("steps"));
        this.CheckExitState(current);
    }
    public void MatrixForm(){
        int sz=(int)Math.sqrt(this.DStates.size());
        double node_type=0;
        for(int i=0;i<this.DStates.size();i++){
            if(i%sz==0){
                System.out.println();
            }
            node_type=this.DStates.get(i).getAttribute("exit");
            if(this.DStates.get(i).getAttribute("mark")==2){
                System.out.print("-");
                continue;
            }
            if(node_type==0.0){
                System.out.print(" ");
            }else if(node_type==1){
                System.out.print("x");
            }else{
                System.out.print("#");
            }
        }
        System.out.println();
    }
    public void LinearForm(){
        for(States state:this.DStates){
            if(state.getAttribute("exit")==0.0){
                System.out.print("-");
            }else if(state.getAttribute("exit")==0){
                System.out.print("#");
            }else{
                System.out.print("$");
            }
        }
    }
    //Generating of [Random] enviroment.
    public void setRandomObstacles(double density){
        for(int i=0;i<this.DStates.size();i++){
            if((new Random()).nextDouble()<density){
                this.DStates.get(i).setObstacle();
            }
        }
    }
    public void setRandomGoals(int number,int size){
        Random rnd=new Random();
        for(int i=0;i<number;i++){
            this.DStates.get(rnd.nextInt(size*size)).setGoal();
        }
    }
    public void setSquareTable(int[][] table){
        this.setRandomTable(table.length, 0, 0);
        for(int i=0;i<table.length;i++){
            for(int j=0;j<table.length;j++){
                switch(table[i][j]){
                    case 0:this.DStates.get(j+i*table.length).setAttribute("exit", 0.0);break;
                    case 1:this.DStates.get(j+i*table.length).setGoal();break;
                    case -1:this.DStates.get(j+i*table.length).setObstacle();break;
                    default:this.DStates.get(j+i*table.length).setAttribute("exit",0.0);break;
                }
            }
        }
    }
    public void setRandomTable(int size,double density,int rewards){
        List<States> states=new ArrayList<States>();
        //Empty brain
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                states.add(new States());
            }
        }
        //Until last line
        for(int i=0;i<(size*size)-size;i++){
            states.get(i).setAction("Down", new Actions(states.get(i+size)));
        }
        //For top line
        for(int i=0;i<size-2;i++){
            states.get(i).setAction("Right", new Actions(states.get(i+1)));
        }
        //Top again
        for(int i=1;i<size-1;i++){
            states.get(i).setAction("Left", new Actions(states.get(i-1)));
        }
        //Until top line
        for(int i=(size*size)-1;i>size-1;i--){
            states.get(i).setAction("Up", new Actions(states.get(i-size)));
        }
        //Bottom Line
        for(int i=(size*size)-1;i>size*size-size;i--){
            states.get(i).setAction("Left", new Actions(states.get(i-1)));
        }
        for(int i=(size*size)-2;i>size*size-size;i--){
            states.get(i).setAction("Right", new Actions(states.get(i+1)));
        }
        for(int i=0;i<size*size-1;i+=size){
            states.get(i).setAction("Right", new Actions(states.get(i+1)));
        }
        for(int i=size-1;i<=size*size-1;i+=size){
            states.get(i).setAction("Left", new Actions(states.get(i-1)));
        }
        //Central
        for(int i=1;i<size;i++){
            for(int j=size;j<size*size-size;j+=size){
                states.get(i+j).setAction("Right", new Actions(states.get((i+1)+j)));
                states.get(i+j).setAction("Left", new Actions(states.get((i-1)+j)));
                states.get(i+j).setAction("Up", new Actions(states.get(i+(j-size))));
                states.get(i+j).setAction("Down",new Actions(states.get(i+(j+size))));
            }         
        }
        
        this.AddStates(states);
        this.setRandomObstacles(density);
        this.setRandomGoals(rewards,size);
        states.clear();
    }
    //Graphic solution, showed using [javax.swing]
    public void GraphicSolution(){
        int size= (int)Math.sqrt(this.DStates.size()+1);
        double state_type=0;
        JButton button;
        //Create and set up the window.
        JFrame frame ;
        this.StartMark();
        
        frame=new JFrame("Q-Learning");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(size,size));
        for(int i=0;i<size*size;i++){
            state_type=this.DStates.get(i).getAttribute("exit");
            button=new JButton();
            switch((int)state_type){
                case -1:button=new JButton("-1");
                          button.setBackground(Color.BLACK);
                          break;
                case 0:button=new JButton("0");
                       button.setBackground(Color.WHITE);
                       break;
                case 1:button=new JButton("1");
                       button.setBackground(Color.green);
                       break;
                default:button=new JButton("0");
                        button.setBackground(Color.WHITE);
                        break;
            
            }
            if(this.DStates.get(i).getAttribute("mark")==2){
                button.setBackground(Color.yellow);
            }
            frame.add(button);
        }
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    public void GraphicSolution(Color cl){
        int size= (int)Math.sqrt(this.DStates.size()+1);
        double state_type=0;
        JButton button;
        //Create and set up the window.
        JFrame frame ;
        this.StartMark();
        
        frame=new JFrame("Q-Learning");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(size,size));
        for(int i=0;i<size*size;i++){
            state_type=this.DStates.get(i).getAttribute("exit");
            button=new JButton();
            switch((int)state_type){
                case -1:button=new JButton("-1");
                          button.setBackground(Color.BLACK);
                          break;
                case 0:button=new JButton("0");
                       button.setBackground(Color.WHITE);
                       break;
                case 1:button=new JButton("1");
                       button.setBackground(Color.green);
                       break;
                default:button=new JButton("0");
                        button.setBackground(Color.WHITE);
                        break;
            
            }
            if(this.DStates.get(i).getAttribute("mark")==2){
                button.setBackground(cl);
            }
            frame.add(button);
        }
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public void GraphicSolutionRandom(){
        int temp=0;
        int size=(int)Math.sqrt(this.DStates.size()+1);
        Random rnd=new Random();
        
        do{
            temp=rnd.nextInt(size*size);
        }while(this.getState(temp).getAttribute("exit")!=0.0);
        this.setVariable("cursor", temp);
        this.GraphicSolution();
    }
    public void GraphicSolutionRandom(int generation){
        int temp=0;
        int size=(int)Math.sqrt(this.DStates.size()+1);
        Random rnd=new Random();
        for(int i=0;i<generation;i++){
            do{
                temp=rnd.nextInt(size*size);
                this.setVariable("cursor", temp);
            }while(this.getState(temp).getAttribute("exit")!=0.0);
            this.StartMark();
        }
        do{
            temp=rnd.nextInt(size*size);
        }while(this.getState(temp).getAttribute("exit")!=0.0);
        this.setVariable("cursor", temp);
        this.GraphicSolution();
    }
}
