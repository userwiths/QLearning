/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QLearnRemastered;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import QLearnRemastered.States;
import QLearnRemastered.Actions;

public class Mind {
    private Map<String,Double> Variable;
    private List<States> DStates;
    
    public Mind(){
        this.Variable=new HashMap<String,Double>();
        this.DStates=new ArrayList<States>();
    }
    public Mind(List<String> str,List<Double> doub){
        this.Variable=new HashMap<String,Double>();
        this.DStates=new ArrayList<States>();
        if(str.size()==doub.size()){
            for(int i=0;i<str.size();i++){
                this.Variable.put(str.get(i).toLowerCase(),doub.get(i));
            }
        }
    }
    public Mind(List<States> state){
        this.Variable=new HashMap<String,Double>();
        this.DStates=new ArrayList<States>();
        this.DStates.addAll(state);
    }    
    
    public States GetState(int i){
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
    public void SetVariable(String name,double value){
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
    public void RewardUpdate(States state,Actions act){
        //Bellman Equtation
        double result=0;
        //Instant Reward
        result=act.GetReward();
        /*This is the lock of the improved ver. byt is malfunktioning.
          */
        if(act.GetState().GetAttribute("lock")==1.0){
            state.SetAttribute("lock", 1.0);
            return;
        }
        if(act.GetState().GetAttribute("exit")==Double.NEGATIVE_INFINITY){
            //This will be activated if the state is NOT an exit state.
            result=Use("gamma")*(Use("discount")*
                    act.GetState().GetBestAction().GetReward());//-act.GetReward());
        }else if(act.GetState().GetAttribute("exit")==1.0){
            //If the state is exit and has attribute reward set to 1;
            act.GetState().SetAttribute("lock", 1.0);
            result+=1+act.GetState().GetAttribute("reward")+Use("gamma")*(Use("discount")*
                    act.GetState().GetBestAction().GetReward());//-act.GetReward());
        }else{
            //State is exit but is unsatisfiable.
            result-=Use("gamma")*(Use("discount")*
                    act.GetState().GetBestAction().GetReward());//-act.GetReward());
        }
        //Set the new Reward Value
        act.SetReward(result);
    }
    public void CheckExitState(States current){
        //  Lost
        if(current.GetAttribute("exit")==0){
            this.SetVariable("lost", Use("lost")+1);
            this.SetVariable("steps", 0.0);
        //  WON
        }else if(current.GetAttribute("exit")==1){
            this.SetVariable("win", Use("win")+1);
            this.SetVariable("steps", 0.0);
        }
    }
    public States ExecuteAction(States current){
        Random rnd=new Random();
        Actions choosed;
        //Random exploration
        if(rnd.nextDouble()<Use("epsilon")){
            choosed=current.GetRandomAction();
            RewardUpdate(current,choosed);
            current=choosed.GetState();
            this.SetVariable("steps", Use("steps")+1);
            return current;
        }
        //Follow the best available action.
        choosed=current.ImprovedBestAction();
        RewardUpdate(current,choosed);
        current=choosed.GetState();
        this.SetVariable("steps", Use("steps")+1);
        return current;
    }
    public void ForceSuccess(){
        for(int i=0;i<this.DStates.size();i++){
            this.DStates.get(i).RemovePenalty();
        }
    }
    
    public void StartGeneral(){
        States current=this.DStates.get((int)Use("cursor"));
        while(current.GetAttribute("exit")==Double.NEGATIVE_INFINITY){
            current=ExecuteAction(current);
            if(Use("steps")>=Use("max_steps")){
                this.SetVariable("steps", 0.0);
                break;
            }
        }
        this.CheckExitState(current);
    }
    public void StartGeneral(int cursor){
        States current=this.DStates.get(cursor);
        while(current.GetAttribute("exit")==Double.NEGATIVE_INFINITY){
            current=ExecuteAction(current);
            this.SetVariable("steps", Use("steps")+1.0);
            if(Use("steps")>=Use("max_steps")){
                this.SetVariable("steps", 0.0);
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
        while(current.GetAttribute("exit")==Double.NEGATIVE_INFINITY){
            current=ExecuteAction(current); 
            if(Use("steps")>=Use("max_steps")){
                this.SetVariable("steps", 0.0);
                break;
            }
        }
        this.CheckExitState(current);
        StartGeneral(cursor,generations-1);
    }
    public void StartRandom(int generations){
        Random rnd=new Random();
        int index=0;
        //Keep trak of generations.
        SetVariable("generations",Use("generations")+generations);
        //Execute for the given nuber of cycles/generations
        for(int i=0;i<generations;i++){
            //Do not land on a forbiden ground.
            do{
                index=rnd.nextInt(this.DStates.size());
            }while(this.DStates.get(index).GetAttribute("exit")!=Double.NEGATIVE_INFINITY);
            StartGeneral(index);
        }
    }
    public void StartMark(){
        States current=this.DStates.get((int)Use("cursor"));
        Random rnd=new Random();
        Actions choosed;
        while(current.GetAttribute("exit")==Double.NEGATIVE_INFINITY){
            if(rnd.nextInt((int)10)<Use("epsilon")){
                choosed=current.GetRandomAction();
                RewardUpdate(current,choosed);
                current=choosed.GetState();
                this.SetVariable("steps", Use("steps")+1.0);
                current.SetAttribute("mark", 2);
                continue;
            }
            //Follow the best available action.
            choosed=current.GetBestAction();
            //System.out.println("Went : "+current.GetBestAction());
            RewardUpdate(current,choosed);
            current=choosed.GetState();
            this.SetVariable("steps", Use("steps")+1.0);
            current.SetAttribute("mark", 2);
        }
        this.CheckExitState(current);
    }
    
    public void Status(){
        System.out.println("Generations: "+Use("generations")+"\tWin: "+Use("win")+"\tLost: "+Use("lost")+"\tSteps: "+Use("steps"));
        //this.MatrixForm();
        this.SetVariable("steps", 0.0);
        this.SetVariable("win", 0.0);
        this.SetVariable("lost", 0.0);
    }
    public void ShowChecked(String checked,int n,int m){
        int ls_cursor=0;
        for(int i=0;i<m;i++){
            for(int j=0;j<n;j++){
                ls_cursor=i*n+j;
                if(this.DStates.get(ls_cursor).GetAttribute("mark")==1.0){
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
        while(current.GetAttribute("exit")==Double.NEGATIVE_INFINITY){
            System.out.println("Went: "+current.ImprovedBestAction().GetName());
            current=ExecuteAction(current);
            this.SetVariable("steps", Use("steps")+1.0);
            if(Use("steps")>=Use("max_steps")){
                this.SetVariable("steps", 0.0);
                break;
            }
        }
        System.out.println("Reached exit ...");
        this.CheckExitState(current);
    }
    public void MatrixForm(){
        int sz=(int)Math.sqrt(this.DStates.size());
        double node_type=0;
        for(int i=0;i<this.DStates.size();i++){
            if((i-1)%sz==0){
                System.out.println();
            }
            node_type=this.DStates.get(i).GetAttribute("exit");
            if(this.DStates.get(i).GetAttribute("mark")==2){
                System.out.print("-");
                continue;
            }
            if(node_type==Double.NEGATIVE_INFINITY){
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
            if(state.GetAttribute("exit")==Double.NEGATIVE_INFINITY){
                System.out.print("-");
            }else if(state.GetAttribute("exit")==0){
                System.out.print("#");
            }else{
                System.out.print("$");
            }
        }
    }
    public void RandomTable(int size,double density,int rewards){
        List<States> states=new ArrayList<States>();
        int temp=0;
        Random rnd=new Random();
        //Empty brain
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                states.add(new States());
            }
        }
        //Until last line
        for(int i=0;i<(size*size)-size;i++){
            states.get(i).SetAction("Down", new Actions(states.get(i+size)));
        }
        //For top line
        for(int i=0;i<size-2;i++){
            states.get(i).SetAction("Right", new Actions(states.get(i+1)));
        }
        //Top again
        for(int i=1;i<size-1;i++){
            states.get(i).SetAction("Left", new Actions(states.get(i-1)));
        }
        //Until top line
        for(int i=(size*size)-1;i>size-1;i--){
            states.get(i).SetAction("Up", new Actions(states.get(i-size)));
        }
        //Bottom Line
        for(int i=(size*size)-1;i>size*size-size;i--){
            states.get(i).SetAction("Left", new Actions(states.get(i-1)));
        }
        for(int i=(size*size)-2;i>size*size-size;i--){
            states.get(i).SetAction("Right", new Actions(states.get(i+1)));
        }
        for(int i=0;i<size*size-1;i+=size){
            states.get(i).SetAction("Right", new Actions(states.get(i+1)));
        }
        for(int i=size-1;i<=size*size-1;i+=size){
            states.get(i).SetAction("Left", new Actions(states.get(i-1)));
        }
        //Central
        for(int i=1;i<size;i++){
            for(int j=size;j<size*size-size;j+=size){
               /* temp=rnd.nextInt(60);
                if(temp>4 && temp <12){
                    states.get(i+j).SetObstacle();
                }*/
                states.get(i+j).SetAction("Right", new Actions(states.get((i+1)+j)));
                states.get(i+j).SetAction("Left", new Actions(states.get((i-1)+j)));
                states.get(i+j).SetAction("Up", new Actions(states.get(i+(j-size))));
                states.get(i+j).SetAction("Down",new Actions(states.get(i+(j+size))));
            }         
        }
        
        this.AddStates(states);
        
        this.SetRandomObstacles(density);
        this.SetRandomGoals(rewards,size);
        //for(int i=0;i<size;i++){
        //    states.get(rnd.nextInt(size*size)).SetGoal();
        //}
        
    }
    public void SetRandomObstacles(double density){
        for(int i=0;i<this.DStates.size();i++){
            if((new Random()).nextDouble()<density){
                this.DStates.get(i).SetObstacle();
            }
        }
    }
    public void SetRandomGoals(int number,int size){
        Random rnd=new Random();
        for(int i=0;i<number;i++){
            this.DStates.get(rnd.nextInt(size*size)).SetGoal();
        }
    }
    public void SetSquareTable(int[][] table){
        this.RandomTable(table.length, 0, 0);
        for(int i=0;i<table.length;i++){
            for(int j=0;j<table.length;j++){
                switch(table[i][j]){
                    case 0:this.DStates.get(j+i*table.length).SetAttribute("exit", Double.NEGATIVE_INFINITY);break;
                    case 1:this.DStates.get(j+i*table.length).SetGoal();break;
                    case -1:this.DStates.get(j+i*table.length).SetObstacle();break;
                    default:this.DStates.get(j+i*table.length).SetAttribute("exit", Double.NEGATIVE_INFINITY);break;
                }
            }
        }
        //this.DStates
    }

}
