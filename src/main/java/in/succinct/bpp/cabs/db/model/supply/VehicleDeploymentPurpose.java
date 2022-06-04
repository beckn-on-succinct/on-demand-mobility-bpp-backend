package in.succinct.bpp.cabs.db.model.supply;

import com.venky.swf.db.model.Model;

public interface VehicleDeploymentPurpose extends Model {
    public Long getVehicleId();
    public void setVehicleId(Long id);
    public Vehicle getVehicle();

    public Long getDeploymentPurposeId();
    public void setDeploymentPurposeId(Long id);
    public DeploymentPurpose getDeploymentPurpose();

}
