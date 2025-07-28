import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class Main {

    // Convert value from arbitrary base to BigInteger
    static BigInteger convertToDecimal(String value, int base) {
        return new BigInteger(value, base);
    }

    // Generate all k-combinations of input list
    static List<List<Map.Entry<Integer, BigInteger>>> getCombinations(List<Map.Entry<Integer, BigInteger>> entries, int k) {
        List<List<Map.Entry<Integer, BigInteger>>> result = new ArrayList<>();
        combine(entries, 0, k, new ArrayList<>(), result);
        return result;
    }

    static void combine(List<Map.Entry<Integer, BigInteger>> entries, int start, int k,
                        List<Map.Entry<Integer, BigInteger>> temp,
                        List<List<Map.Entry<Integer, BigInteger>>> result) {
        if (temp.size() == k) {
            result.add(new ArrayList<>(temp));
            return;
        }
        for (int i = start; i < entries.size(); i++) {
            temp.add(entries.get(i));
            combine(entries, i + 1, k, temp, result);
            temp.remove(temp.size() - 1);
        }
    }

    // Perform Lagrange interpolation to find f(0)
    static BigInteger lagrangeInterpolation(List<Map.Entry<Integer, BigInteger>> points) {
        BigInteger result = BigInteger.ZERO;
        int k = points.size();

        for (int i = 0; i < k; i++) {
            BigInteger xi = BigInteger.valueOf(points.get(i).getKey());
            BigInteger yi = points.get(i).getValue();

            BigInteger num = BigInteger.ONE;
            BigInteger den = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i == j) continue;
                BigInteger xj = BigInteger.valueOf(points.get(j).getKey());

                num = num.multiply(xj.negate());
                den = den.multiply(xi.subtract(xj));
            }

            BigInteger term = yi.multiply(num).divide(den);
            result = result.add(term);
        }

        return result;
    }

    static BigInteger findSecret(String filePath) throws Exception {
        // Parse JSON
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(new FileReader(filePath));

        JSONObject keysObj = (JSONObject) obj.get("keys");
        int n = Integer.parseInt(keysObj.get("n").toString());
        int k = Integer.parseInt(keysObj.get("k").toString());

        List<Map.Entry<Integer, BigInteger>> allPoints = new ArrayList<>();

        for (Object keyObj : obj.keySet()) {
            String keyStr = keyObj.toString();
            if (keyStr.equals("keys")) continue;

            int x = Integer.parseInt(keyStr);
            JSONObject valObj = (JSONObject) obj.get(keyStr);
            int base = Integer.parseInt(valObj.get("base").toString());
            String valueStr = valObj.get("value").toString();

            BigInteger y = convertToDecimal(valueStr, base);
            allPoints.add(Map.entry(x, y));
        }

        // Try all combinations of size k
        Map<BigInteger, Integer> freqMap = new HashMap<>();
        List<List<Map.Entry<Integer, BigInteger>>> combinations = getCombinations(allPoints, k);

        for (List<Map.Entry<Integer, BigInteger>> comb : combinations) {
            try {
                BigInteger secret = lagrangeInterpolation(comb);
                freqMap.put(secret, freqMap.getOrDefault(secret, 0) + 1);
            } catch (Exception e) {
                // Skip invalid combinations
            }
        }

        // Find the most common result
        BigInteger mostFrequentSecret = null;
        int maxFreq = 0;
        for (Map.Entry<BigInteger, Integer> entry : freqMap.entrySet()) {
            if (entry.getValue() > maxFreq) {
                maxFreq = entry.getValue();
                mostFrequentSecret = entry.getKey();
            }
        }

        return mostFrequentSecret;
    }

    public static void main(String[] args) throws Exception {
        BigInteger secret1 = findSecret("test1.json");
        BigInteger secret2 = findSecret("test2.json");

        System.out.println("Secret 1: " + secret1);
        System.out.println("Secret 2: " + secret2);
    }
}
