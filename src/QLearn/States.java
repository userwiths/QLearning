package QLearn;

import java.util.*;

public class States {
    protected Map<String,Double> Attributes;
    protected List<Actions> Actions;
    
    public States(){
        this.Attributes=new HashMap<String,Double>();
        this.Actions=new ArrayList<Actions>();
        this.Attributes.put("exit", 0.0);
        this.Attributes.put("reward", 0.0);
        this.Attributes.put("lock", 0.0);
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
    }
    public States(Map<String,Double> map){
        this.Actions=new ArrayList<Actions>();
        this.Attributes=map;
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
    
    public void setGoal(){
        this.Attributes.put("exit", 1.0);
        this.Attributes.put("reward", 100.0);
        this.Attributes.put("lock", 1.0);
    }
    public void setObstacle(){
        this.Attributes.put("exit", -1.0);
        this.Attributes.put("reward",-10.0);
        this.Attributes.put("lock", 0.0);
    }
    public Actions getBestAction(){
        double result=0;
        int index=0;
        for(int i=0;i<this.Actions.size();i++){
            if(this.Actions.get(i).getReward()>=result){
                result=this.Actions.get(i).getReward();
                index=i;
            }
        }
        if(this.Actions.get(index).getState().Actions.isEmpty()){
            this.Actions.remove(this.Actions.get(index));
            return this.getBestAction();
        }
        return this.Actions.get(index);
    }
    public void RemovePenalty(){
        for(int i=0;i<this.Actions.size();i++){
            if(this.Actions.get(i).getState().getAttribute("exit")==-1.0){
                this.Actions.remove(i);
            }
        }
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
    public Actions ImprovedBestAction(){
        double result=0;
        int index=0;
        for(int i=0;i<this.Actions.size();i++){
            if(this.Actions.get(i).getReward()>=result &&
                    this.Actions.get(i).getState().getAttribute("exit")!=-1.0){
                result=this.Actions.get(i).getReward();
                index=i;
            }
        }
        return this.Actions.get(index);
    }
    public Actions GetRandomAction(){
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
