/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package appserver.job.impl;

import appserver.job.Tool;


/**
 *
 * @author shriy
 */
public class Fibonacci implements Tool {

    FibonacciHelper helper = null;

    @Override
    public Object go(Object parameters) {

        helper = new FibonacciHelper((int) parameters);
        return helper.getFibonacci();
    }
}
