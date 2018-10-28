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
public class freeSpace implements Comparable<freeSpace> {
   int start;
   int size;        //*
    public freeSpace(int start , int size){
        this.start=start;
        this.size=size;
    }

    @Override
    public int compareTo(freeSpace o) {
       return this.start - o.start;
           
       
    }

    @Override
    public String toString() {
        return "freeSpace{" + "start=" + start + ", size=" + size + '}';
    }
}
