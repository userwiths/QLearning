/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QLearn;

import java.util.*;

public class States{
    private String Current="default";
    private Map<String,Double> Objectives=new HashMap<String,Double>();
    protected Map<String,Double> Attributes;
    protected List<Actions> Actions;
    
    public States(){
        this.Attributes=new HashMap<String,Double>();
        this.Actions=new ArrayList<Actions>();
        this.Attributes.put("exit", 0.0);
        this.Attributes.put("reward", 0.0);
        this.Attributes.put("value", 0.0);
        this.Attributes.put("lock", 0.0);
        this.Objectives.put(Current, 0.0);
    }
    public States(List<String> str,List<Double> doub){
        this.Attributes=new HashMap<String,Double>();
        if(str.size()==doub.size()){
            for(int i=0;i<str.size();i++){
                this.Attributes.put(str.get(i),doub.get(i));
            }
        }
        this.Actions=new ArrayList<Actions>();
        this.Attributes.put("exit", 0.0);
        this.Attributes.put("reward", 0.0);
        this.Attributes.put("value", 0.0);
        this.Objectives.put(Current, 0.0);
    }
    public States(Map<String,Double> map){
        this.Actions=new ArrayList<Actions>();
        this.Attributes=map;
        this.Objectives.put(Current, 0.0);
    }
    
    public void setAction(String name,Actions act){
        act.setName(name);
        for(int i=0;i<this.Actions.size();i++){
            if(this.Actions.get(i).getName()==name){
                this.Actions.set(i,act);
                return;
            }
        }
        this.Actions.add(act);
    }
    public void setAction(Actions act){
        for(int i=0;i<this.Actions.size();i++){
            if(this.Actions.get(i).getName()==act.getName()){
                this.Actions.remove(i);
            }
        }
        this.Actions.add(act);
    }
    public void setAttribute(String name,double val){
        if(this.Attributes.containsKey(name)){
            this.Attributes.replace(name, val);
            return;
        }
        this.Attributes.put(name, val);
    }
    public List<Actions> getActions(){
        return this.Actions;
    }
    public Actions getAction(String act){
        return this.Actions.get(this.Actions.indexOf(act));
    }
    public double getAttribute(String name){
        if(this.Attributes.get(name)==null){
            return Double.NEGATIVE_INFINITY;
        }
        return this.Attributes.get(name);
    }
    public Map<String,Double> getAttributes(){
        return this.Attributes;
    }
    public boolean hasAction(String name){
        for(Actions act:this.Actions){
            if(act.getName()==name){
                return true;
            }
        }
        return false;
    }
    public boolean hasAction(Actions act){
        return this.Actions.contains(act);
    }
    public List<States> getNeightbours(){
        List<States> states=new ArrayList<States>();
        for(Actions act:this.Actions){
            states.add(act.getState());
        }
        return states;
    }
    public List<States> getAccesors(int depth){
        int cursor=0;
        List<States> current=this.getNeightbours();
        List<States> temp=new ArrayList<States>();
        while(depth>0){
            while(cursor<current.size()){
                temp.addAll(current.get(cursor).getNeightbours());
                cursor++;
            }
            current.addAll(temp);
            temp.clear();
            depth--;
        }
        return current;
    }
    
    public void addObjective(String obj){
        this.Objectives.putIfAbsent(obj,0.0);
        for(int i=0;i<this.Actions.size();i++){
            this.Actions.get(i).addObjective(obj);
        }
    }
    public void setObjective(String obj){
        /*if(!this.Objectives.containsKey(Current)){
            this.Objectives.put(Current,this.Attributes.get("exit"));
        }
        if(!this.Objectives.containsKey(obj)){
            this.Objectives.put(obj,0.0);
        }*/
        
        this.Objectives.replace(Current, this.Attributes.get("exit"));
        this.Current=obj;
        this.Attributes.replace("exit", this.Objectives.get(obj));
        
        for(int i=0;i<this.Actions.size();i++){
            this.Actions.get(i).setObjective(obj);
        }
    }
    public Map<String,Double> getObjectives(){
        return this.Objectives;
    } 
    
    
    public void setGoal(){
        this.Objectives.replace(Current,1.0);
        this.Attributes.put("exit", 1.0);
        this.Attributes.put("reward", 100.0);
        this.Attributes.put("lock", 1.0);
    }
    public void setObstacle(){
        this.Objectives.replace(Current,-1.0);
        this.Attributes.put("exit", -1.0);
        this.Attributes.put("reward",-10.0);
        this.Attributes.put("lock", 0.0);
    }
    public Actions getBestAction(){
        double result=0;
        int index=0;
        for(int i=0;i<this.Actions.size();i++){
            if(this.Actions.get(i).getValue()>=result){
                result=this.Actions.get(i).getValue();
                index=i;
            }
        }
        if(this.Actions.get(index).getState().Actions.isEmpty()){
            this.Actions.remove(this.Actions.get(index));
            return this.getBestAction();
        }
        return this.Actions.get(index);
    }
    public Actions getBestAction(String objective){
        double result=0;
        int index=0;
        for(int i=0;i<this.Actions.size();i++){
            if(this.Actions.get(i).getObjectives().get(objective)>=result){
                result=this.Actions.get(i).getObjectives().get(objective);
                index=i;
            }
        }
        /*
        if(this.Actions.get(index).getState().Actions.isEmpty()){
            this.Actions.remove(this.Actions.get(index));
            return this.getBestAction();
        }*/
        return this.Actions.get(index);
    }
    public Actions getBestWithPriority(Map<String,Double> map){
        double sum=0.0;
        Actions action=new Actions(0.0);
        Actions act=this.Actions.get(0);
        for(int i=0;i<this.Actions.size();i++){
            act=this.Actions.get(i);
            for(String str:map.keySet()){
               sum+=map.get(str)*act.getObjectives().get(str);
            }
            if(sum>action.getValue()){
                action=act;
            }
        }
        return action;
    }
    public void RemovePenalty(){
        List<Integer> remove=new ArrayList<Integer>();
        for(int i=0;i<this.Actions.size();i++){
            if(this.Actions.get(i).getState().getAttribute("exit")==-1.0){
                remove.add(i);
            }
        }
        for(Integer i:remove){
            this.Actions.remove(i);
        }
        remove.clear();
    }
    public void clearMarked(){
        List<Integer> remove=new ArrayList<Integer>();
        for(int i=0;i<this.Actions.size();i++){
            if(this.Actions.get(i).getState().getAttribute("mark")==2 || this.Actions.get(i).getState().getAttribute("exit")==-1){
                remove.add(i);
            }
        }
        for(int i:remove){
            if(i<this.Actions.size()){
                this.Actions.remove(i);
            }
        }
    }
    public Actions improvedBestAction(){
        double result=0;
        int index=0;
        for(int i=0;i<this.Actions.size();i++){
            if(this.Actions.get(i).getValue()>=result &&
                    this.Actions.get(i).getState().getAttribute("exit")!=-1.0){
                result=this.Actions.get(i).getValue();
                index=i;
            }
        }
        return this.Actions.get(index);
    }
    public Actions getRandomAction(){
        Random rnd=new Random();
        return this.Actions.get(rnd.nextInt(this.Actions.size()));
    }
    public void PrintAttributes(){
        System.out.println(this.Attributes.toString());
        for(Actions act:this.Actions){
            System.out.println(act.getName()+"\t");
        }
    }
}
