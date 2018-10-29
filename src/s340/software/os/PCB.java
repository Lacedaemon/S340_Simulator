/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s340.software.os;

/**
 *
 * @author rak
 */
public class PCB implements Comparable<PCB>  {
    int acc;
    int x;
    int pc;
    int base;
    int limit;
    ProcessState status;
    public PCB(int base, int limit){
        //Creating the fields for the acc, x, and pc, all giving a status of ready
        this.acc=0;
        this.x=0;
        this.pc=0;
        this.base = base;
        this.limit = limit;
        this.status=status.ready;
    }
    public ProcessState getStatus(){
        return status;
    }

    @Override
    public String toString() {
        return "PCB{" + "acc=" + acc + ", x=" + x + ", pc=" + pc + ", base=" + base + ", limit=" + limit + ", status=" + status + '}';
    }


    @Override
    public int compareTo(PCB o) {
        return this.base - o.base;
    }

  


 
}
 