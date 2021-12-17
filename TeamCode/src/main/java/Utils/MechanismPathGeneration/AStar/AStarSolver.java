package Utils.MechanismPathGeneration.AStar;

import java.util.*;

public class AStarSolver {
    public static List<Node> solve(Node start, Node end){
        LinkedList<Node> checkQueue = new LinkedList<>(), nextCheckQueue = new LinkedList<>();

        HashMap<Node, Integer> visited = new HashMap<>();

        int val = 0;
        checkQueue.add(start);
        while(!visited.containsKey(end)){
            while(!checkQueue.isEmpty()){
                Node node = checkQueue.removeFirst();
                if(!visited.containsKey(node)) {
                    visited.put(node, val);
                    nextCheckQueue.addAll(node.getConnectedNodes());
                }
            }
            val ++;
            checkQueue.addAll(nextCheckQueue);
            nextCheckQueue.clear();
        }

        ArrayList<Node> pathNodes = new ArrayList<>();
        pathNodes.add(end);
        Node visitedNode = end;
        while(visitedNode != start){
            Node min = visitedNode;
            int minVal = Integer.MAX_VALUE;
            for(Node node : visitedNode.getConnectedNodes()){
                if(!visited.containsKey(node)){
                    continue;
                }
                if(visited.get(node) < minVal){
                    minVal = visited.get(node);
                    min = node;
                }
            }
            visitedNode = min;
            pathNodes.add(visitedNode);
        }

        Collections.reverse(pathNodes);

        return pathNodes;
    }
}
