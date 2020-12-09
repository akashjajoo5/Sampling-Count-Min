import java.util.*;
import java.io.FileWriter;
import java.io.File;
import java.io.BufferedWriter;

class CountMinSampling {
    public static void main(String args[]) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter number of counters");
        int k = sc.nextInt();
        System.out.println("Enter size of array");
        int w = sc.nextInt();

        int[] hashFunction = new int[k];
        int[][] counters = new int[k][w];

        Set <Integer> uniqueHash = new HashSet<>();
        int j = 0;
        while (j < k) {
            int randNum = getRandom();
            if(!uniqueHash.contains(randNum)) {
                hashFunction[j++] = randNum;
                uniqueHash.add(randNum);
            }
        }

        try {
            File file = new File("input.txt");
            sc = new Scanner(file);
        } catch (Exception e) {
            e.getStackTrace();
            return;
        }

        int n = Integer.parseInt(sc.nextLine());

        j = 0;
        Map <String, Packet> mapping = new HashMap<>();
        Set <Integer> uniqueFlowID = new HashSet<>();
        while (sc.hasNextLine()) {
            String[] str = sc.nextLine().split("\\s+");
            int flowId = getRandom();
            while (uniqueFlowID.contains(flowId)) {
                flowId = getRandom();
            }
            mapping.put(str[0], new Packet(Integer.parseInt(str[1]), flowId));
            uniqueFlowID.add(flowId);
        }
        
        try {
            FileWriter file = new FileWriter("output.txt");
            BufferedWriter output = new BufferedWriter(file);

            int p = 2;
            output.write("Probability\tMean Absolute Error\n");
            while (p <= 100) {
                float temp1 = p / 100.0f;
                float ans = countMin(temp1, counters, hashFunction, mapping);
                try {
                    output.write(String.format("%4s\t %10s\n",temp1, ans)); 
                } catch (Exception e) {
                    e.getStackTrace();
                }
                p += 2;
                for (int[] temp: counters) {
                    Arrays.fill(temp, 0);
                }
            }
            output.close();
        } catch (Exception e) {
            e.getStackTrace();
        }

    }

    public static float countMin(float prob, int[][] counters, int[] hashFunction, Map<String, Packet> mapping) {
        for (String s: mapping.keySet()) {
            Packet p = mapping.get(s);
            int flowId = p.flowId;
            int count = 0;
            for (int i = 0; i < p.numPackets; i++) {
                if (getRandom() < prob * Integer.MAX_VALUE) {
                    count++;
                }
            }
            for (int i = 0; i < hashFunction.length; i++) {
                int hashedVal = (hashFunction[i] ^ flowId) % counters[i].length;
                counters[i][hashedVal] += count;
            }
        }

        float avgError = 0;
        for (String s: mapping.keySet()) {
            Packet p = mapping.get(s);
            int flowId = p.flowId;
            int min = Integer.MAX_VALUE;
            for (int i = 0; i < hashFunction.length; i++) {
                int hashedVal = (hashFunction[i] ^ flowId) % counters[i].length;
                min = Math.min(min, counters[i][hashedVal]);
            }
            p.put(min / prob);
            avgError += Math.abs(p.estimatedSize - p.numPackets); 
        }

        return (avgError / mapping.size());
    }

    public static int getRandom() {
        Random r = new Random();
        return r.nextInt(Integer.MAX_VALUE);
    }
}

class Packet {
    int numPackets, flowId;
    float estimatedSize;
    Packet (int numPackets, int flowId) {
        this.numPackets = numPackets;
        this.flowId = flowId;
    }

    void put (float estimatedSize) {
        this.estimatedSize = estimatedSize;
    }
}