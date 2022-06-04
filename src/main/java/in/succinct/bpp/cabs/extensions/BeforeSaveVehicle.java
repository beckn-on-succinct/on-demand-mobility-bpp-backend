package in.succinct.bpp.cabs.extensions;

import com.venky.swf.db.extensions.BeforeModelSaveExtension;
import in.succinct.bpp.cabs.db.model.supply.Vehicle;

public class BeforeSaveVehicle extends BeforeModelSaveExtension<Vehicle> {
    static{
        registerExtension(new BeforeSaveTaggedModel<Vehicle>());
        registerExtension(new BeforeSaveVehicle());
    }

    @Override
    public void beforeSave(Vehicle model) {

    }
}
