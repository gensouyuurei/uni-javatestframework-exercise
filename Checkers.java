package com.yuurei.testframework;

public class Checkers {

    public Checkers(){
    }

    public static void assertEquals(boolean assertion){
        if (!assertion){
            throw new AssertionError();
        }
    }
}
