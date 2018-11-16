/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s340.software.os;

/**
 *
 * @author nathan
 */
public class IORequest {
    int prognum;
    int requestType;
    int payload;
    
    public IORequest(int request, int incomingPayload) {
        this.prognum = prognum;
        this.requestType = request;
        this.payload = incomingPayload;
    }
    
}
