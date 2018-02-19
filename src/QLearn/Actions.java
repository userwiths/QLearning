package QLearn;

import java.util.Random;

public class Actions {
    private double Reward;
    private States EndState;
    private String Name;
    
    public Actions(){
        this.Reward=(new Random().nextDouble());
        this.EndState=new States();
        this.Name="Action";
    }
    public Actions(double reward){
        this.Reward=reward;
        this.EndState=new States();
        this.Name="Action";
    }
    public Actions(States state){
        this.EndState=state;
        this.Reward=(new Random().nextDouble());
        this.Name="Action";
    }
    public Actions(States state,String name){
        this.EndState=state;
        this.Reward=(new Random().nextDouble());
        this.Name=name;
    }
    public Actions(States state,double reward){
        this.EndState=state;
        this.Reward=reward;
        this.Name="Action";
    }
    
    public double getReward(){
        return this.Reward;
    }
    public void setReward(double set){
        this.Reward=set;
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
}
