package QLearn;

import java.util.*;

public class Actions{
    private String Current="default";
    private Map<String,Double> Objectives=new HashMap<String,Double>();
    private double Value;
    private States EndState;
    private String Name;
    
    public Actions(){
        this.Value=(new Random().nextDouble());
        this.EndState=new States();
        this.Name="Action";
        this.Objectives.put(Current, Value);
    }
    public Actions(double reward){
        this.Value=reward;
        this.EndState=new States();
        this.Name="Action";
        this.Objectives.put(Current, Value);
    }
    public Actions(States state){
        this.EndState=state;
        this.Value=(new Random().nextDouble());
        this.Name="Action";
        this.Objectives.put(Current, Value);
    }
    public Actions(States state,String name){
        this.EndState=state;
        this.Value=(new Random().nextDouble());
        this.Name=name;
        this.Objectives.put(Current, Value);
    }
    public Actions(States state,double reward){
        this.EndState=state;
        this.Value=reward;
        this.Name="Action";
        this.Objectives.put(Current, Value);
    }
    
    public double getValue(){
        return this.Value;
    }
    public void setValue(double set){
        this.Value=set;
    }
    public States getState(){
        return this.EndState;
    }
    public void setState(States state){
        this.EndState=state;
    }
    public void setName(String name){
        this.Name=name;
    }
    public String getName(){
        return this.Name;
    }
    
    public void setObjective(String obj){
        if(!this.Objectives.containsKey(Current)){
            this.Objectives.put(Current, this.Value);
        }
        if(!this.Objectives.containsKey(obj)){
            this.Objectives.put(obj, new Random().nextDouble());
        }
        this.Objectives.replace(Current, this.Value);
        this.Current=obj;
        this.Value=this.Objectives.get(Current);
    }
    public void addObjective(String obj){
        this.Objectives.putIfAbsent(obj, new Random().nextDouble());
    }
    public String getObjective(){
        return this.Current;
    }
    public Map<String,Double> getObjectives(){
        return this.Objectives;
    }
}
