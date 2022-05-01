/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package appserver.job.impl;

/**
 *
 * @author shriy
 */
public class FibonacciHelper {

    int number;

    public FibonacciHelper(int number) { this.number = number; }

    /**
     * Method [getFibonacci] Get Fibonacci of number in O(n) time using O(1) space
     *
     * @return integer result of fibonacci algorithm
     */
    public int getFibonacci() {
        int[] lastTwo = {0, 1};
        int counter = 2;
        while (counter <= number) {
            int nextFib = lastTwo[0] + lastTwo[1];
            lastTwo[0] = lastTwo[1];
            lastTwo[1] = nextFib;
            counter++;
        }
        return number > 0 ? lastTwo[1] : lastTwo[0];
    }
}