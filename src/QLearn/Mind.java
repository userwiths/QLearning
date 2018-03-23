package QLearn;

import java.awt.Color;
import java.awt.GridLayout;

import java.util.*;
import java.util.stream.Collectors;
import javax.swing.*;

public class Mind {
    private Map<String,Double> Variable;
    private List<States> DStates;
    private String Current="default";
    
    //Constructors
    public Mind(){
        this.Variable=new HashMap<String,Double>();
        this.DStates=new ArrayList<States>();
        this.loadDefault();
    }
    public Mind(List<String> str,List<Double> doub){
        this.Variable=new HashMap<String,Double>();
        this.DStates=new ArrayList<States>();
        if(str.size()==doub.size()){
            for(int i=0;i<str.size();i++){
                this.Variable.put(str.get(i).toLowerCase(),doub.get(i));
            }
        }
        this.loadDefault();
    }
    public Mind(List<States> state){
        this.Variable=new HashMap<String,Double>();
        this.DStates=new ArrayList<States>();
        this.loadDefault();
        this.DStates.addAll(state);
    }    
    
    /*
        Equtations
        NOTE: For now Bellman's formula is the best implemented method. I will try implementing them again later.
        TODO:   Q(s,a)=V(s)+A(s,a)
    */
    public double Quality(States state,Actions act){
        return act.getState().getAttribute("exit")+
                Use("gamma")*Use("discount")*act.getState().getBestAction().getValue();
    }
    public double QualityAll(States state,Actions act){
        double result=0.0;
        for(String objective:state.getObjectives().keySet()){
            result=act.getState().getObjectives().get(objective)+
                Use("gamma")*Use("discount")*act.getState().getBestAction(objective).getObjectives().get(objective);
            act.getObjectives().replace(objective, result);
        }
        return Quality(state,act);
    }
    public double Value(States state){
        double result=0;
        for(Actions act:state.getActions()){
            result+=act.getValue();
        }
        return (double)result/state.getActions().size();
    }
    public double Advantage(States state,Actions act){
        return Quality(state,act)-Value(state);
    }
    public double DoubleQ(Actions act){
        return act.getState().getAttribute("exit")+Use("gamma")*(Use("discount")*
                    act.getState().getBestAction().getValue());
    }
    /*
    Different objectives for the agent to follow.
    */
    public double ShapeAttribute(States state,Actions act,String name){
        return this.Quality(state, act)+
                act.getState().getAttribute(name)-state.getAttribute(name);
    }
    public double MaxAttribute(States state,Actions act,String name){
        return Quality(state,act)+
                act.getState().getAttribute(name)-state.getAttribute(name);
    }
    public double MinAttribute(States state,Actions act,String name){
        return Quality(state,act)
                -1*(act.getState().getAttribute(name)+state.getAttribute(name));
    }
    public double DistancePassed(States state,Actions act,String name){
        return this.Quality(state, act)+this.Use("steps")/10.0;
    }
    /*
        Additional conditions
        https://pdfs.semanticscholar.org/fe60/f0d1bff543a86c15a5851ee6a948b04d10cf.pdf
    */
    //General Getters & Setters
    public States getState(int i){
        return this.DStates.get(i);
    }
    public List<States> getStates(){
        return this.DStates;
    }
    public void addStates(List<States> st){
        this.DStates.addAll(st);
    }
    public void addVariable(String name,double value){
        this.Variable.put(name, value);
    }
    public void removeVariable(String name){
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
        return this.Variable.get(name)==null?0.0:this.Variable.get(name);
    }
    public void clearMark(String attrib){
        for(int i=0;i<this.DStates.size();i++){
            this.DStates.get(i).setAttribute(attrib, 0.0);
        }
    }

    //Nullify All Default Variables.
    public void loadDefault(){
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

    //Nullify all memory -> Same as creating a new instance.
    public void clearMemory(){
        this.DStates.clear();
        this.Variable.clear();
        this.loadDefault();
    }
    public int countRewards(){
        int count=0;
        for(States state:this.DStates){
            if(state.getAttribute("exit")==1.0){
                count++;
            }
        }
        return count;
    }

    //Run time used. Separate different ideas of the algorithm.
    public void rewardUpdate(States state,Actions act){
        /*
            TODO:
            Source:https://hira.hope.ac.uk/id/eprint/231/1/Improved-Q-Learning-Oct19-2012.pdf
            Fuction:Lock Mechanism.
        */
        //state.setAttribute("value", Value(state));
        act.setValue(QualityAll(state,act));
    }
    public void checkExitState(States current){
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
    public States executeAction(States current){
        Random rnd=new Random();
        Actions choosed;
        //Random exploration
        if(rnd.nextDouble()<Use("epsilon")){
            choosed=current.getRandomAction();
            rewardUpdate(current,choosed);
            current=choosed.getState();
            this.setVariable("steps", Use("steps")+1);
            return current;
        }
        //Follow the best available action.
        choosed=current.getBestAction();
        rewardUpdate(current,choosed);
        current=choosed.getState();
        this.setVariable("steps", Use("steps")+1);
        return current;
    }
    public void ForceSuccess(){
        for(int i=0;i<this.DStates.size();i++){
            this.DStates.get(i).RemovePenalty();
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
    public void setRandomGoals(int number){
        Random rnd=new Random();
        for(int i=0;i<number;i++){
            this.DStates.get(rnd.nextInt(this.DStates.size())).setGoal();
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
        
        this.addStates(states);
        this.setRandomObstacles(density);
        this.setRandomGoals(rewards);
        states.clear();
    }
 
    //Different Starting Options
    public void StartGeneral(){
        States current=this.DStates.get((int)Use("cursor"));
        while(current.getAttribute("exit")==0.0){
            current=executeAction(current);
            this.setVariable("steps", Use("steps")+1.0);
            if(Use("steps")>=Use("max_steps")){
                this.setVariable("steps", 0.0);
                break;
            }
        }
        this.checkExitState(current);
    }
    public void StartGeneral(int cursor){
        States current=this.DStates.get(cursor);
        while(current.getAttribute("exit")==0.0){
            current=executeAction(current);
            this.setVariable("steps", Use("steps")+1.0);
            if(Use("steps")>=Use("max_steps")){
                this.setVariable("steps", 0.0);
                break;
            }
        }
        this.checkExitState(current);}
    public void StartGeneral(int cursor,int generations){
        for(int i=0;i<generations;i++){
            States current=this.DStates.get(cursor);
            while(current.getAttribute("exit")==0.0){
                current=executeAction(current); 
                if(Use("steps")>=Use("max_steps")){
                    this.setVariable("steps", 0.0);
                    break;
                }
            }
            this.checkExitState(current);
        }
    }
    public void StartStudy(boolean verbose){
        this.setVariable("epsilon", 1.0);
        for(int i=0;i<100;i++){
            this.setVariable("epsilon",1.0/(i+1));
            this.StartRandom(1000);
            if(this.Use("win") > 900){
                break;
            }
            if(verbose){
                System.out.println("After "+((i+1)*1000)+" generations: ");
                System.out.println("Endet "+i+" cycle ...");
                this.Status();
            }
        }
        this.setVariable("win", 0.0);
        this.setVariable("epsilon", 0.001);
    }
    public void StartStudy(int episodes,boolean verbose){
        for(int i=0;i<episodes;i++){
            this.setVariable("epsilon",1.0/(i+1));
            this.StartRandom(1000);
            if(this.Use("win") > 900){
                break;
            }
            if(verbose){
                System.out.println("After "+((i+1)*1000)+" generations: ");
                this.Status();
            }
            
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
        this.setVariable("steps", 0.0);
        while(current.getAttribute("exit")==0.0){
            current.setAttribute("mark", 2);
            if(rnd.nextDouble()<Use("epsilon")){
                choosed=current.getRandomAction();
                rewardUpdate(current,choosed);
                current=choosed.getState();
                this.setVariable("steps", Use("steps")+1.0);
                continue;
            }
            choosed=current.getBestAction();
            rewardUpdate(current,choosed);
            current=choosed.getState();
            this.setVariable("steps", Use("steps")+1.0);
        }
        //System.out.println("\n\nSteps: "+Use("steps"));
        this.checkExitState(current);
    }
    public void StartWithPriority(Map<String,Double> map){
        States current = null;
        Actions action=new Actions(0.0);
        double sum=0.0;
        for(String str:map.keySet()){
            if(!this.DStates.get(0).getObjectives().containsKey(str)){
                //ERROR: No such objective.
                return;
            }
        }
        current=this.DStates.get((int)Use("cursor"));
        while(current.getAttribute("exit")==0.0){
           current=current.getBestWithPriority(map).getState();
        }
        this.checkExitState(current);
    }
    
    //Console Representation options
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
            System.out.println("Went: "+current.improvedBestAction().getName());
            current=executeAction(current);
            this.setVariable("steps", Use("steps")+1.0);
            if(Use("steps")>=Use("max_steps")){
                this.setVariable("steps", 0.0);
                break;
            }
        }
        System.out.println("Reached exit ...\nWith: "+Use("steps"));
        this.checkExitState(current);
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
    
    //Graphic solution, showed using [javax.swing]
    public void GraphicSolution(){
        int size= (int)Math.sqrt(this.DStates.size()+1);
        double state_type=0;
        JButton button;
        //Create and set up the window.
        JFrame frame ;
        this.StartMark();
        
        frame=new JFrame("Q-Learning");
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        this.clearMark("mark");
        frame.pack();
        frame.setVisible(true);
    }
    public void GraphicSolution(String asname){
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
            button=new JButton(String.valueOf(this.DStates.get(i).getAttribute(asname)));
            switch((int)state_type){
                case -1:button.setBackground(Color.BLACK);
                        break;
                case 0:button.setBackground(Color.WHITE);
                       break;
                case 1:button.setBackground(Color.green);
                       break;
                default:button.setBackground(Color.WHITE);
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
    public void GraphicSolution(int cursor){
        this.setVariable("cursor", cursor);
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
    public void GraphicSolution(int cursor,String asname){
        this.setVariable("cursor", cursor);
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
            button=new JButton(String.valueOf(this.DStates.get(i).getAttribute(asname)));
            switch((int)state_type){
                case -1:button.setBackground(Color.BLACK);
                        break;
                case 0:button.setBackground(Color.WHITE);
                       break;
                case 1:button.setBackground(Color.green);
                       break;
                default:button.setBackground(Color.WHITE);
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
    
    //Objectives
    public void fillObjective(String obj){
        String old=Current;
        this.setObjective(obj);
        this.setRandomObstacles(0.26);
        this.setRandomGoals(4);
        this.setObjective(old);
    }
    public void addObjective(String obj){
        for(int i=0;i<this.DStates.size();i++){
            this.DStates.get(i).addObjective(obj);
        }
        this.fillObjective(obj);
    }
    public void setObjective(String obj){
        for(int i=0;i<this.DStates.size();i++){
            this.DStates.get(i).setObjective(obj);
        }
        this.Current=obj;
    }
    
    //Misc.
    public List<States> getWhereLess(String name,double val){
        List<States> result=new ArrayList<States>();
        for(States st:this.DStates){
            if(st.getAttribute(name)<val){
                result.add(st);
            }
        }
        return result;
    }
    public List<States> getWhereMore(String name,double val){
        List<States> result=new ArrayList<States>();
        for(States st:this.DStates){
            if(st.getAttribute(name)>val){
                result.add(st);
            }
        }
        return result;
    }
    public List<States> getWhereEqual(String name,double val){
        List<States> result=new ArrayList<States>();
        for(States st:this.DStates){
            if(st.getAttribute(name)==val){
                result.add(st);
            }
        }
        return result;
    }
    public List<States> getMarked(){
        int cursor=(int)this.Use("cursor");
        States current=this.DStates.get(cursor);
        List<States> states=new ArrayList<States>();
        while(current.getAttribute("exit")==0.0){
            states.add(current);
            for(Actions act:current.getActions()){
                if(act.getState().getAttribute("mark")==2 || act.getState().getAttribute("exit")==1.0){
                    current=act.getState();
                    break;
                }
            }
        }
        return states.stream().distinct().collect(Collectors.toList());
    }
    
}
