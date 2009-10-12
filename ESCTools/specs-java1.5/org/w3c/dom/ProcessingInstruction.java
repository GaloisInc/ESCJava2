package org.w3c.dom;

public interface ProcessingInstruction extends Node {
    
    public String getTarget();
    
    public String getData();
    
    public void setData(String data) throws DOMException;
}