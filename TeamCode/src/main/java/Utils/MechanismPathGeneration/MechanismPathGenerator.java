package Utils.MechanismPathGeneration;

import MathSystems.Vector.Vector3;
import Utils.MechanismPathGeneration.AStar.Node;
import Utils.MechanismPathGeneration.Constraints.MechanismConstraint;

import java.util.*;

public class MechanismPathGenerator {
    //X is turret pos, Y is pitch pos, Z is extension pos
    private Vector3 start, end;
    private ArrayList<MechanismConstraint> constraints;
    private ArrayList<Node> nodes;

    public MechanismPathGenerator(List<MechanismConstraint> constraints){
        this.constraints = new ArrayList<>(constraints);
        nodes = new ArrayList<>();
    }

    public void generatePath(MechanismPathPos start, MechanismPathPos end, double exResolution) {
        this.start = (new Vector3(start.turretPos, start.pitchPos, start.extensionPos * exResolution));
        this.end = (new Vector3(end.turretPos, end.pitchPos, end.extensionPos * exResolution));

        LinkedList<int[]> queue = new LinkedList<>();

        int[][][] values = new int[(int) (RangeConstraint.DEFAULTS.turrMax - RangeConstraint.DEFAULTS.turrMin)]
                [(int) (RangeConstraint.DEFAULTS.pitchMax - RangeConstraint.DEFAULTS.pitchMin)]
                [(int) (RangeConstraint.DEFAULTS.exMax - RangeConstraint.DEFAULTS.exMin * exResolution)];

        values[(int) (start.turretPos - RangeConstraint.DEFAULTS.turrMin)]
                [(int) (start.pitchPos - RangeConstraint.DEFAULTS.pitchMin)]
                [(int) (start.extensionPos - RangeConstraint.DEFAULTS.exMin * exResolution)] = 1;

        queue.add(new int[]{(int)(start.turretPos - RangeConstraint.DEFAULTS.turrMin),
                (int)(start.pitchPos - RangeConstraint.DEFAULTS.pitchMin),
                (int)(start.extensionPos - RangeConstraint.DEFAULTS.exMin * exResolution), 1});

        int endVal = values[(int) (end.turretPos - RangeConstraint.DEFAULTS.turrMin)]
                [(int) (end.pitchPos - RangeConstraint.DEFAULTS.pitchMin)]
                [(int) (end.extensionPos - RangeConstraint.DEFAULTS.exMin * exResolution)];

        while(endVal == 0){
            int[] val = queue.removeFirst();
            exploreAround(val[0], val[1], val[2], val[3], queue, values, exResolution);

            endVal = values[(int) (end.turretPos - RangeConstraint.DEFAULTS.turrMin)]
                    [(int) (end.pitchPos - RangeConstraint.DEFAULTS.pitchMin)]
                    [(int) (end.extensionPos - RangeConstraint.DEFAULTS.exMin * exResolution)];
        }

        int lastVal = endVal;
        int[] coord = new int[]{(int) (end.turretPos - RangeConstraint.DEFAULTS.turrMin),
                (int) (end.pitchPos - RangeConstraint.DEFAULTS.pitchMin),
                (int) (end.extensionPos - RangeConstraint.DEFAULTS.exMin * exResolution)};

        ArrayList<double[]> path = new ArrayList<>();
        while(lastVal != 1){
            path.add(new double[]{coord[0], coord[1], coord[2]});
            lastVal = values[coord[0]][coord[1]][coord[2]];
            coord = getBestAround(coord[0], coord[1], coord[2], values);
        }

        Collections.reverse(path);
        path.set(0, new double[]{(start.turretPos - RangeConstraint.DEFAULTS.turrMin),
                (start.pitchPos - RangeConstraint.DEFAULTS.pitchMin),
                (start.extensionPos * exResolution - RangeConstraint.DEFAULTS.exMin * exResolution)});
        path.set(path.size()-1, new double[]{(end.turretPos - RangeConstraint.DEFAULTS.turrMin),
                (end.pitchPos - RangeConstraint.DEFAULTS.pitchMin),
                (end.extensionPos * exResolution - RangeConstraint.DEFAULTS.exMin * exResolution), 1});
        smoothPath(path, exResolution);
        for(double[] i : path){
            System.out.println((i[0] + RangeConstraint.DEFAULTS.turrMin) + ", " + (i[1] + RangeConstraint.DEFAULTS.pitchMin) + ", " + (i[2]/exResolution + RangeConstraint.DEFAULTS.exMin));
        }
    }

    private void exploreAround(int x, int y, int z, int val, LinkedList<int[]> queue, int[][][] values, double exResolution){
        int[][] toCheck = new int[][]{
                {x+1, y, z},
                {x, y+1, z},
                {x, y, z+1},

                {x-1, y, z},
                {x, y-1, z},
                {x, y, z-1},
        };

        for(int[] arr : toCheck){
            if(arr[0] < 0 || arr[1] < 0 || arr[2] < 0){
                continue;
            }
            if(arr[0] >= values.length || arr[1] >= values[0].length || arr[2] >= values[0][0].length){
                continue;
            }
            int i = values[arr[0]][arr[1]][arr[2]];
            if(i == -1){
                continue;
            }
            if(i == 0){
                if(isConstrained(arr[0], arr[1], arr[2]/exResolution)){
                    values[arr[0]][arr[1]][arr[2]] = -1;
                    continue;
                }
                values[arr[0]][arr[1]][arr[2]] = val+1;
                queue.add(new int[]{arr[0], arr[1], arr[2], val+1});
            }
        }
    }

    private int[] getBestAround(int x, int y, int z, int[][][] values){
        int[][] toCheck = new int[][]{
                {x+1, y, z},
                {x, y+1, z},
                {x, y, z+1},

                {x-1, y, z},
                {x, y-1, z},
                {x, y, z-1},
        };

        int[] bestCoord = new int[3];
        int min = Integer.MAX_VALUE;
        for(int[] arr : toCheck){
            if(arr[0] < 0 || arr[1] < 0 || arr[2] < 0){
                continue;
            }
            if(arr[0] >= values.length || arr[1] >= values[0].length || arr[2] >= values[0][0].length){
                continue;
            }
            int val = values[arr[0]][arr[1]][arr[2]];
            if(val < min && val > 0){
                min = val;
                bestCoord = arr;
            }
        }
        return bestCoord;
    }

    private void smoothPath(ArrayList<double[]> arr, double exResolution){
        ArrayList<double[]> newArray = new ArrayList<>();
        newArray.add(arr.get(0));
        double[] last = arr.get(0);
        double[] before = arr.get(0);
        for(int i = 1; i < arr.size(); i ++){
            double[] curr = arr.get(i);

            double[] direction = new double[]{curr[0] - last[0], curr[1] - last[1], curr[2] - last[2]};
            for(double d = 0; d <= 1; d += 0.05){
                double[] checking = new double[]{(last[0] + (direction[0] * d)), (last[1] + (direction[1] * d)), (last[2] + (direction[2] * d))};
                if(isConstrained(checking[0], checking[1], checking[2]/exResolution)){
                    newArray.add(before);
                    last = curr;
                    break;
                }
            }
            before = curr;
        }
        newArray.add(arr.get(arr.size()-1));
        arr.clear();
        arr.addAll(newArray);
    }

    public boolean isConstrained(int x, int y, int z){
        for(MechanismConstraint constraint : constraints){
            if(constraint.isConstrained(x + RangeConstraint.DEFAULTS.turrMin,
                    y + RangeConstraint.DEFAULTS.pitchMin,
                    (z + RangeConstraint.DEFAULTS.exMin))){
                return true;
            }
        }
        return false;
    }

    public boolean isConstrained(double x, double y, double z){
        for(MechanismConstraint constraint : constraints){
            if(constraint.isConstrained(x + RangeConstraint.DEFAULTS.turrMin,
                    y + RangeConstraint.DEFAULTS.pitchMin,
                    (z + RangeConstraint.DEFAULTS.exMin))){
                return true;
            }
        }
        return false;
    }
}
