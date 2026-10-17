import java.io.*;
import java.math.*;
import java.util.*;
import java.util.regex.*;

public class Main {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws IOException {
        System.out.println(new Solution().maxPathSum(buildTree(rna())));
    }

    static String line() throws IOException {
        String s = br.readLine();
        return s == null ? "" : s;
    }

    static int ri() throws IOException {
        return Integer.parseInt(line().trim());
    }

    static String[] tokens() throws IOException {
        int n = ri();
        String l = line();
        if (n == 0) return new String[0];
        return Arrays.copyOf(l.split(" ", -1), n);
    }

    static Integer[] rna() throws IOException {
        String[] t = tokens();
        Integer[] a = new Integer[t.length];
        for (int i = 0; i < t.length; i++) a[i] = t[i].equals("null") ? null : Integer.parseInt(t[i].trim());
        return a;
    }

    static TreeNode buildTree(Integer[] v) {
        if (v.length == 0 || v[0] == null) return null;
        TreeNode root = new TreeNode(v[0]);
        Deque<TreeNode> q = new ArrayDeque<>();
        q.add(root);
        int i = 1;
        while (!q.isEmpty() && i < v.length) {
            TreeNode cur = q.poll();
            if (i < v.length && v[i] != null) {
                cur.left = new TreeNode(v[i]);
                q.add(cur.left);
            }
            i++;
            if (i < v.length && v[i] != null) {
                cur.right = new TreeNode(v[i]);
                q.add(cur.right);
            }
            i++;
        }
        return root;
    }
}

class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;
    TreeNode() {}
    TreeNode(int val) { this.val = val; }
    TreeNode(int val, TreeNode left, TreeNode right) { this.val = val; this.left = left; this.right = right; }
}

/*
 * Definition for a binary tree node (provided, do not redeclare):
 * class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode() {}
 *     TreeNode(int val) { this.val = val; }
 *     TreeNode(int val, TreeNode left, TreeNode right) { ... }
 * }
 */
class Solution {

    public int maxPathSum(TreeNode root) {
        // Write your code here
        return 0;
    }
}
