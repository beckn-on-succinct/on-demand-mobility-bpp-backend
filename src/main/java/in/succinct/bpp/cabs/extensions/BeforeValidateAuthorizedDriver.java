package in.succinct.bpp.cabs.extensions;

import com.venky.swf.db.extensions.BeforeModelValidateExtension;
import in.succinct.bpp.cabs.db.model.supply.AuthorizedDriver;
import in.succinct.bpp.cabs.db.model.supply.User;
import in.succinct.bpp.cabs.db.model.supply.Vehicle;

public class BeforeValidateAuthorizedDriver extends BeforeModelValidateExtension<AuthorizedDriver> {
    static{
        registerExtension(new BeforeValidateAuthorizedDriver());
    }
    @Override
    public void beforeValidate(AuthorizedDriver model) {
        User user = model.getDriver();
        Vehicle vehicle = model.getVehicle();
        if (user != null && vehicle != null){
            if (!user.isApproved()){
                throw new RuntimeException("User is not verified yet");
            }
            if (!vehicle.isApproved()){
                throw new RuntimeException("Vehicle is not verified yet");
            }
        }
    }
}
