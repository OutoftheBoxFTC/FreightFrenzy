package Utils;

public class OpmodeData {
    private static OpmodeData theInstance = null;

    private boolean dataStale = true;
    private int extensionPos = 0;

    public static OpmodeData getInstance() {
        if(theInstance == null){
            theInstance = new OpmodeData();
        }
        return theInstance;
    }

    public void setExtensionPos(int extensionPos) {
        dataStale = false;
        this.extensionPos = extensionPos;
    }

    public int getExtensionPos() {
        dataStale = true;
        return extensionPos;
    }

    public boolean isDataStale() {
        return dataStale;
    }
}
