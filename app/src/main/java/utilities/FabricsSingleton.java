package utilities;

import java.util.ArrayList;

import objects.Fabric;

/**
 * Created by pc on 5/9/2019.
 */

public class FabricsSingleton{
    public static final FabricsSingleton instance = new FabricsSingleton();
    private ArrayList<Fabric> indoorFabrics;
    private ArrayList<Fabric> outdoorFabrics;
    private ArrayList<Fabric> outdoorTemp;
    private ArrayList<Fabric> allFabrics;
    private ArrayList<Fabric> curtainFabrics;

    private FabricsSingleton() {}

    public ArrayList<Fabric> getIndoorFabrics()
    {
        return indoorFabrics;
    }

    public void setIndoorFabrics(ArrayList<Fabric> indoorFabrics)
    {
        this.indoorFabrics = indoorFabrics;
    }

    public ArrayList<Fabric> getOutdoorFabrics()
    {
        return outdoorFabrics;
    }

    public void setOutdoorFabrics(ArrayList<Fabric> outdoorFabrics)
    {
        this.outdoorFabrics = outdoorFabrics;
    }

    public ArrayList<Fabric> getOutdoorTemp()
    {
        return outdoorTemp;
    }

    public void setOutdoorTemp(ArrayList<Fabric> outdoorTemp)
    {
        this.outdoorTemp = outdoorTemp;
    }

    public ArrayList<Fabric> getAllFabrics()
    {
        return allFabrics;
    }

    public void setAllFabrics(ArrayList<Fabric> allFabrics)
    {
        this.allFabrics = allFabrics;
    }

    public ArrayList<Fabric> getCurtainFabrics()
    {
        return curtainFabrics;
    }

    public void setCurtainFabrics(ArrayList<Fabric> curtainFabrics)
    {
        this.curtainFabrics = curtainFabrics;
    }
}
