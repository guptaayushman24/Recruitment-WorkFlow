import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int[] height = readIntArray(br);
        System.out.println(new Solution().maxArea(height));
    }

    static int[] readIntArray(BufferedReader br) throws IOException {
        int n = Integer.parseInt(br.readLine().trim());
        String line = br.readLine();
        int[] arr = new int[n];
        if (n > 0) {
            String[] parts = line.trim().split("\\s+");
            for (int i = 0; i < n; i++) arr[i] = Integer.parseInt(parts[i]);
        }
        return arr;
    }
}

class Solution {
    public int maxArea(int[] height) {
        int l = 0;
        int r = height.length - 1;
        int ans = 0;
        while (l <= r) {
            int area = Math.min(height[l], height[r]) * (r - l);
            ans = Math.max(ans, area);
            if (height[l] < height[r]) {
                l++;
            } else {
                r--;
            }

        }

        return ans;
    }
}
