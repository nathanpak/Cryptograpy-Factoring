// Author: Nathan Pak
// Section: T1
// Documentation: found code for incrementing BigIntegers from java2s.com. Referenced stack overflow on the Java Executor Services to exit a service.

import java.math.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import java.util.Scanner;

public class Factoring extends Thread{

    // source: http://www.java2s.com/example/java-utility-method/biginteger-calculate/increment-biginteger-integer-654c3.html
    public static BigInteger increment(BigInteger integer) {
        return add(integer == null ? BigInteger.ZERO : integer, BigInteger.ONE);
    }
    // source: http://www.java2s.com/example/java-utility-method/biginteger-calculate/increment-biginteger-integer-654c3.html
    public static BigInteger add(BigInteger... operands) {
        BigInteger result = null;

        if (operands != null) {
            for (BigInteger operand : operands) {
                if (operand != null) {
                    if (result == null) {
                        result = operand;
                    } else {
                        result = result.add(operand);
                    }
                }
            }
        }

        return result;
    }
    // source: https://www.geeksforgeeks.org/java-program-to-check-if-a-number-is-prime-or-not/
    static boolean isPrime(int n)
    {
        // Corner case
        if (n <= 1)
            return false;

        // Check from 2 to n-1
        for (int i = 2; i < n; i++)
            if (n % i == 0)
                return false;

        return true;
    }

    static void bruteForce(BigInteger n){
        System.out.println("Brute Force Factoring");
        if(n.isProbablePrime(1)){
            System.out.println(n + " has no factors.");
            System.out.println("It took 0 seconds.");
        }

        BigInteger squarert = n.sqrt();
        long startTime = System.nanoTime(); // start the timer
        for(BigInteger i=BigInteger.valueOf(2); i.compareTo(squarert)==-1 || i.compareTo(squarert)==0; i=increment(i)){ // loop from 2 to sqrt(n)
            if(i.isProbablePrime(1) && n.mod(i)==BigInteger.valueOf(0)){
                System.out.println("Found a factor = " + i);
                double timing = (System.nanoTime() - startTime) / 1.0E09;
                System.out.println("It took " + timing + " seconds.");
                return;
            }
            else if((System.nanoTime()-startTime)/1.0E09 > 120){
                i = squarert;
                System.out.println("Took longer than 2 minutes");
                return;
            }
        }
    }

    static void pollardsRho(BigInteger n){
        System.out.println("Pollard's Rho");
        BigInteger a = new BigInteger("2");
        BigInteger b = new BigInteger("2");
        BigInteger d = new BigInteger("1");
        long startTime = System.nanoTime(); // start the timer
        while(d.compareTo(new BigInteger("1")) == 0){
            // use f(x) = x^2 + 1 (mod n)
            a = increment(a.multiply(a)).mod(n); // a = f(a)
            increment(b.multiply(b)).mod(n);
            b = increment(b.multiply(b)).mod(n);
            b = increment(b.multiply(b)).mod(n);

            d = (a.subtract(b)).gcd(n);
            if(d.compareTo(new BigInteger("1")) == 1 && d.compareTo(n) == -1){
                System.out.println("Found a factor = " + d);
                System.out.println("a = " + a + ", b = " + b);
                double timing = (System.nanoTime() - startTime) / 1.0E09;
                System.out.println("It took " + timing + " seconds.");
                return;
            }
            // check timing>2 min
            else if((System.nanoTime() - startTime) / 1.0E09 > 120){
                System.out.println("Took longer than 2 minutes");
                return;
            }
            else if (d==n){
                System.out.println(n + " has no factors");
                double timing = (System.nanoTime() - startTime) / 1.0E09;
                System.out.println("It took " + timing + " seconds.");
                return;
            }
        }
    }


    static void dixons(BigInteger n){
        System.out.println("Dixon's Algorithm");
        System.out.print("Enter # of factors in factor base: ");
        Scanner scanner1 = new Scanner(System.in);
        int t = scanner1.nextInt();

        boolean found = false;
        int tries = 0;
        long startTime = System.nanoTime(); // start the timer
        while(!found && tries<3) {


            int[] FB = new int[t];
            // generate factor base
            int prime = 2;
            for (int i = 0; i < t; i++) {
                while (!isPrime(prime)) {
                    prime++;
                }
                FB[i] = prime;
                prime++;
            }
            int r = t + 1; // number of good equations
            BigInteger k = new BigInteger("1");
            BigInteger[] xlist = new BigInteger[r]; // x^2 (mod n)
            int[][] c = new int[r][t]; // vectors/exponents of good equations
            int[] factors = new int[t];
            // find valid x^2 mod n and their prime factors
            BigInteger x = new BigInteger("0");
            BigInteger[] randomX = new BigInteger[r];
            for (int i = 0; i < r; i++) { // for each good equation
                //BigInteger x = k.multiply(n).sqrt();
                boolean isGood = false;
                while (!isGood) {
                    //x = k.multiply(n).sqrt();
                    BigInteger randomNumber;
                    do {

                        randomNumber = new BigInteger(n.bitLength(), new Random());
                    } while (randomNumber.compareTo(n) >= 0);
                    x = randomNumber;
                    //x = n.multiply(k).sqrt();
                    for (int j = 0; j < t; j++) { // for each factor

                        if (x.multiply(x).mod(n).mod(BigInteger.valueOf(FB[j])).compareTo(BigInteger.valueOf(0)) == 0) { // check if current factor base divides x^2 mod n
                            c[i][j] = 1;
                            factors[j] = FB[j];
                            //isGood = true;
                        }
                    }

                    if (isBrokenDownByFB(x.multiply(x).mod(n), factors)) {
                        isGood = true;
                        if (x.multiply(x).mod(n).compareTo(BigInteger.valueOf(1)) == 0) {
                            isGood = false;
                        }
                        //factors = new int[t];
                    } else {
                        Arrays.fill(c[i], 0);
                    }
                    k = k.add(BigInteger.valueOf(1));
                }
                xlist[i] = x.modPow(BigInteger.valueOf(2), n);
                randomX[i] = x;

                //k = k.add(BigInteger.valueOf(1));
            }
            int[][] realC = getC(c, FB, xlist);
            int[][] cNot = new int[r][t];
            for (int i = 0; i < r; i++) {
                for (int j = 0; j < t; j++) {
                    cNot[i][j] = realC[i][j] % 2;
                }
            }
            System.out.println("Done generating factor base");
            // print the factor base
            for(int i=0; i<r; i++){
                int num = i+1;
                System.out.print(num + ") " + randomX[i] + "===" + xlist[i] + "  ");
                for (int j=0; j<t; j++){
                    System.out.print(realC[i][j] + " ");
                }
                System.out.println();
            }
            System.out.println();

            int perfSquareIndex = -1;
            for (int i = 0; i < r; i++) {
                if (checkPegs(cNot[i], t)) {
                    perfSquareIndex = i;
                    i = r;
                }
            }
            if (perfSquareIndex >= 0) {
                BigInteger X = xlist[perfSquareIndex].sqrt();
                BigInteger Y = randomX[perfSquareIndex];
                BigInteger gcd = X.subtract(Y).gcd(n);
                if (gcd.compareTo(BigInteger.valueOf(1)) != 0 && gcd.compareTo(n) != 0) {
                    System.out.println("Found a factor: " + gcd);

                    double timing = (System.nanoTime()-startTime) / 1.0E09;
                    System.out.println("It took " + timing + " seconds.");
                    found = true;
                }
                else{
                    System.out.println("Try again: could not find a factor\n");
                }
            }
            /*else if(perfSquareIndex<0){ // combine equations

            }*/
            else if(tries<2)
                System.out.println("Try again: could not find a factor\n");

            /*System.out.println(Arrays.deepToString(realC));
            System.out.println(Arrays.deepToString(cNot));
            System.out.println(Arrays.deepToString(c));
            System.out.println(Arrays.toString(xlist));
            System.out.println(Arrays.toString(randomX));*/
            //System.out.println(Arrays.deepToString(factors));
            tries++;
        }
        if(!found){
            System.out.println("Could not find factor\n");
            double timing = (System.nanoTime()-startTime) / 1.0E09;
            System.out.println("It took " + timing + " seconds.");
        }
    }
    static boolean checkPegs(int array[], int size)
    {
        for (int i = 0; i < size; i++) {
            if(array[i] != 0) {
                return false;
            }
        }
        return true;
    }

    static int[][] getC(int [][]indices, int []FB, BigInteger []xList){
        int [][]c = new int[indices.length][indices[0].length];
        for(int i=0; i<xList.length; i++){ // for each x^2 mod n
            BigInteger temp = xList[i];
            for(int j=0; j<indices[0].length; j++){ // for each prime factor, determine its exponent
                int count = 1;
                if(indices[i][j]!=0) { // move forward if you are looking at a actual factor
                    while (temp.mod(BigInteger.valueOf(FB[j])).compareTo(BigInteger.valueOf(0)) == 0){
                        temp = temp.divide(BigInteger.valueOf(FB[j]));
                        c[i][j] = count;
                        count++;
                    }
                }
            }
        }
        //System.out.println(Arrays.deepToString(c));
        return c;
    }

    static boolean isBrokenDownByFB(BigInteger n, int []FB){
        /*BigInteger temp = n;
        int t = FB.length;
        for(int i=0; i<t; i++){
            while(FB[i]!=0 && temp.mod(BigInteger.valueOf(FB[i])).compareTo(BigInteger.valueOf(0)) == 0){
                temp = temp.divide(BigInteger.valueOf(FB[i]));
            }
        }
        if(temp.compareTo(BigInteger.valueOf(1)) == 0)
            return true;
        else
            return false;*/
        int []factors = new int[50];
        int k = 0;
        while (n.mod(BigInteger.valueOf(2)).compareTo(BigInteger.valueOf(0)) == 0)
        {
            //System.out.print(2 + " ");
            n = n.divide(BigInteger.valueOf(2));
            factors[k] = 2;
        }
        k++;

        // n must be odd at this point.  So we can
        // skip one element (Note i = i +2)
        for (int i = 3; n.sqrt().compareTo(BigInteger.valueOf(i))==1 || n.sqrt().compareTo(BigInteger.valueOf(i))==0; i+= 2)
        {
            // While i divides n, print i and divide n
            while (n.mod(BigInteger.valueOf(i)).compareTo(BigInteger.valueOf(0)) == 0)
            {
                //System.out.print(i + " ");
                factors[k] = i;
                k++;
                n = n.divide(BigInteger.valueOf(i));
            }
        }

        // This condition is to handle the case when
        // n is a prime number greater than 2
        if (n.compareTo(BigInteger.valueOf(2)) == 1) {
            //System.out.print(n);
            factors[k] = n.intValue();
            k++;
        }
        //System.out.println(Arrays.toString(factors));
        for(int x : factors){
            if(x!=0) {
                if (!Arrays.stream(FB).anyMatch(i -> i == x)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println("PEX1 - Factoring! - by Cadet Nathan Pak\nCyS 431\n");
        System.out.println("Input 0 to quit.");
        BigInteger input;
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a number to factor: ");
        input = scanner.nextBigInteger();
        while(input.compareTo(BigInteger.valueOf(0)) != 0){
            System.out.println();
            bruteForce(input); System.out.println();
            pollardsRho(input); System.out.println();
            //dixons(input);

            final ExecutorService service = Executors.newSingleThreadExecutor();

            try {
                final BigInteger inputt = input;
                final Future<Object> f = service.submit(() -> {
                    /*System.out.println("Dixon's Algorithm");
                    System.out.print("Enter # of factors in factor base: ");
                    Scanner scanner1 = new Scanner(System.in);
                    int t = scanner1.nextInt();*/
                    dixons(inputt);
                    Thread.sleep(1337); // Simulate some delay
                    return "42";
                });

                System.out.println(f.get(120, TimeUnit.SECONDS));
            } catch (final TimeoutException e) {
                System.out.println("Took longer than 2 minutes");
            } catch (final Exception e) {
                throw new RuntimeException(e);
            } finally {
                service.shutdown();
            }

            System.out.print("\nEnter a number to factor: ");
            input = scanner.nextBigInteger();
        }
        System.out.println("Goodbye.");

        //int []testn = {2,3,5,7};
        //System.out.println(isBrokenDownByFB(BigInteger.valueOf(135), testn));

        //int [][]indices = {{1, 0, 1, 1}, {1, 0, 1, 1}, {0, 0, 1, 0}, {1, 0, 1, 1}, {1, 1, 0, 0}};
        //BigInteger []xList = {BigInteger.valueOf(5600), BigInteger.valueOf(5734400), BigInteger.valueOf(390625), BigInteger.valueOf(274400), BigInteger.valueOf(1327104)};
        //getC(indices,testn,xList);
    }
}
