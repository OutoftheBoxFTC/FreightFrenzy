package Utils.MechanismPathGeneration.AStar;

import MathSystems.Vector.Vector3;

import java.util.ArrayList;

public class Node implements Cloneable{
    private ArrayList<Node> connectedNodes = new ArrayList<>();
    public Vector3 position;

    public Node(Vector3 position){
        this.position = position;
    }

    public void addNode(Node node){
        if(connectedNodes.contains(node)){
            return;
        }
        connectedNodes.add(node);
    }

    public ArrayList<Node> getConnectedNodes() {
        return connectedNodes;
    }

    @Override
    public Node clone() {
        return new Node(position);
    }
}
