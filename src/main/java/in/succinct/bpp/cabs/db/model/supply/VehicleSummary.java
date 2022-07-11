package in.succinct.bpp.cabs.db.model.supply;

import com.venky.core.util.Bucket;
import com.venky.swf.db.model.Model;
import com.venky.swf.plugins.collab.db.model.participants.admin.Company;

public interface VehicleSummary extends Model {
    public Long getCompanyId();
    public void setCompanyId(Long id);
    public Company getCompany();


    public Bucket getVehicleCount();
    public void setVehicleCount(Bucket vehicleCount);

    public Bucket getVerifiedVehicleCount();
    public void setVerifiedVehicleCount(Bucket verifiedVehicleCount);


}
