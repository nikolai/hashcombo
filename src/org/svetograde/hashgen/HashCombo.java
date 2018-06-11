package org.svetograde.hashgen;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;
import static javax.xml.bind.DatatypeConverter.printHexBinary;


public class HashCombo {

    static ThreadLocal<Map<String, MessageDigest>> algosTLocal = new ThreadLocal<>();

    private static MessageDigest getAlgo(String algoName) {
        Map<String, MessageDigest> algosMap = algosTLocal.get();
        if (algosMap == null) {
            algosTLocal.set(algosMap = new HashMap<>());
        }

        MessageDigest algo;
        if ((algo = algosMap.get(algoName)) == null) {
            algosMap.put(algoName, algo = createAlgo(algoName));
        }

        return algo;
    }

    static MessageDigest createAlgo(String algoName) {
        try {
            return MessageDigest.getInstance(algoName);
        } catch (NoSuchAlgorithmException e) {
            try {
                String algoClassName = algoName.replace("-", "");
                Class<?> algoClass = Class.forName("sun.security.provider." + algoClassName);
                Method getInstanceMethod = algoClass.getDeclaredMethod("getInstance");
                return (MessageDigest) getInstanceMethod.invoke(null);
            } catch (Exception e1) {
                e1.addSuppressed(e);
                throw new IllegalStateException(e1);
            }
        }
    }

    static String getValue(List<String> args, String key, boolean mandatory) {
        for (String arg : args) {
            if(arg.startsWith(key)) {
                String[] split = arg.split("=");
                if (split.length != 2) {
                    throw new IllegalArgumentException("invalid parameter " + arg);
                }
                return split[1];
            }
        }
        if (mandatory) {
            throw new IllegalArgumentException("missing argument: " + key);
        }
        return null;
    }
    public static void main(String[] args) throws Exception {
        List<String> argsList;

        if (args == null || args.length == 0
                || !(argsList = new ArrayList(Arrays.asList(args))).retainAll(Arrays.asList("-h", "--help", "help", "/?"))
                || !argsList.isEmpty()
                ) {
            System.out.println("Usage: <STDIN> | --algo-combo=<algorithms combination> [--target-hash=<hex>]" +
                    "\n\tExample:\n\tmp64 -1 ?l ?1?1?1?1?1 | java -jar HashCombo.jar --algo-combo=MD4,SHA-1 --target-hash=5c64aee6b2c51b814c7929132dec579677d175cf" +
                    "\n\tWhere mp64 - mask processor util");
            return;
        }
        argsList = Arrays.asList(args);

        String algoCombi = getValue(argsList, "--algo-combo", true);
        List<String> algoList = Arrays.asList(algoCombi.split(","));

        String ethalon = getValue(argsList, "--target-hash", false);
        final byte[] ethalonBytes = ethalon == null ? null : parseHexBinary(ethalon);

        AtomicLong count = new AtomicLong();
        long start = System.currentTimeMillis();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            br.lines().parallel().forEach(
                    pwd-> {
                        try {
                            byte[] bytesToHash = pwd.getBytes("UTF-8");
                            for (String algoName : algoList) {
                                bytesToHash = getAlgo(algoName).digest(bytesToHash);
                            }
                            byte[] result = bytesToHash;

                            long aCount = count.incrementAndGet();
                            if (aCount % 100000000 == 0) {
                                System.out.println("progress: "
                                        + " current password: " + pwd
                                        + ", tries: " + aCount
                                        + ", seconds spent: " + (System.currentTimeMillis() - start)/1000
                                );
                            }
                            if(ethalonBytes == null) {
                                System.out.println(printHexBinary(result) + "\t" + pwd);
                            } else if (Arrays.equals(ethalonBytes, result)) {
                                System.out.println("Password cracked: " + pwd
                                        + ", hash: " + printHexBinary(result)
                                        + ", tries: " + aCount
                                        + ", minutes spent: " + (System.currentTimeMillis() - start)/1000/60
                                );
                                System.exit(0);
                            }
                        } catch (Exception e) {
                            System.exit(2);
                            e.printStackTrace();
                        }
                    }
            );
        }
    }
}
