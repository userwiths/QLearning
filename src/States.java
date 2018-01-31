/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QLearnRemastered;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import QLearnRemastered.Actions;

public class States {
    private Map<String,Double> Attributes;
    private List<Actions> Actions;
    
    
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
    
    public void SetAction(String name,Actions act){
        act.SetName(name);
        for(int i=0;i<this.Actions.size();i++){
            if(this.Actions.get(i).GetName()==name){
                this.Actions.set(i,act);
                return;
            }
        }
        this.Actions.add(act);
    }
    public void SetAttribute(String name,double val){
        if(this.Attributes.containsKey(name)){
            this.Attributes.replace(name, val);
            return;
        }
        this.Attributes.put(name, val);
    }
    public Actions GetAction(String act){
        return this.Actions.get(this.Actions.indexOf(act));
    }
    public double GetAttribute(String name){
        if(this.Attributes.get(name)==null){
            return Double.NEGATIVE_INFINITY;
        }
        return this.Attributes.get(name);
    }
    public void SetGoal(){
        this.Attributes.put("exit", 1.0);
        this.Attributes.put("reward", 100.0);
        this.Attributes.put("lock", 1.0);
    }
    public void SetObstacle(){
        this.Attributes.put("exit", -1.0);
        this.Attributes.put("reward",-10.0);
        this.Attributes.put("lock", 0.0);
    }
    public Actions GetBestAction(){
        double result=0;
        int index=0;
        for(int i=0;i<this.Actions.size();i++){
            if(this.Actions.get(i).GetReward()>=result){
                result=this.Actions.get(i).GetReward();
                index=i;
            }
        }
        if(this.Actions.get(index).GetState().Actions.isEmpty()){
            this.Actions.remove(this.Actions.get(index));
            return this.GetBestAction();
        }
        return this.Actions.get(index);
    }
    public void RemovePenalty(){
        for(int i=0;i<this.Actions.size();i++){
            if(this.Actions.get(i).GetState().GetAttribute("exit")==-1.0){
                this.Actions.remove(i);
            }
        }
    }
    public Actions ImprovedBestAction(){
        double result=0;
        int index=0;
        for(int i=0;i<this.Actions.size();i++){
            if(this.Actions.get(i).GetReward()>=result &&
                    this.Actions.get(i).GetState().GetAttribute("exit")!=-1.0){
                result=this.Actions.get(i).GetReward();
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
            System.out.println(act.GetName()+"\t");
        }
    }
}
