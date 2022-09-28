package in.succinct.bpp.cabs.db.model.demand;

import com.venky.swf.db.annotations.column.indexing.Index;
import com.venky.swf.db.model.Model;
import in.succinct.bpp.cabs.db.model.supply.DeploymentPurpose;
import in.succinct.bpp.cabs.db.model.supply.DriverLogin;

public interface AllocationOption extends Model {
    public Long getTripId();
    public void setTripId(Long id);
    public Trip getTrip();


    @Index
    public Long getDeploymentPurposeId();
    public void setDeploymentPurposeId(Long id);
    public DeploymentPurpose getDeploymentPurpose();


    public Double getSellingPrice();
    public void setSellingPrice(Double sellingPrice);

    public Double getPrice();
    public void setPrice (Double price);

    public Double getIGst();
    public void setIGst(Double gst);

    public Double getCGst();
    public void setCGst(Double gst);

    public Double getSGst();
    public void setSGst(Double gst);

    public Long getDriverLoginId();
    public void setDriverLoginId(Long id);
    public DriverLogin getDriverLogin();

}
