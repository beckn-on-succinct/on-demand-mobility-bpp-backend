package in.succinct.bpp.cabs.db.model.supply;

import com.venky.swf.db.table.ModelImpl;

import java.util.HashSet;
import java.util.Set;

public class VehicleImpl extends ModelImpl<Vehicle> {
    public VehicleImpl(){

    }
    public VehicleImpl(Vehicle proxy){
        super(proxy);
    }

    public boolean isVerified(){
        Vehicle v = getProxy();
        Set<String> validatedDocuments = new HashSet<>();
        v.getDocuments().forEach(d->{
            if (d.isVerified() && !d.isExpired()) {
                validatedDocuments.add(d.getDocument());
            }
        });
        for (String document : VehicleDocument.DOCUMENTS_NEEDED) {
            if (!validatedDocuments.contains(document)){
                return false;
            }
        }
        return true;
    }
}
