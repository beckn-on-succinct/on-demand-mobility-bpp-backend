package in.succinct.bpp.cabs.db.model.supply;

import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.annotations.column.indexing.Index;
import com.venky.swf.db.annotations.column.pm.PARTICIPANT;
import com.venky.swf.db.annotations.column.ui.PROTECTION;
import com.venky.swf.db.annotations.column.ui.PROTECTION.Kind;
import com.venky.swf.db.annotations.column.ui.WATERMARK;
import com.venky.swf.db.annotations.model.MENU;
import com.venky.swf.db.model.Model;
import in.succinct.bpp.cabs.db.model.tag.Tagable;

import java.util.List;

@MENU("Inventory")
public interface Vehicle extends Model, Tagable {


    @UNIQUE_KEY
    @PARTICIPANT
    public Long getCreatorUserId();


    @Index
    @UNIQUE_KEY
    public String getVehicleNumber();
    public void setVehicleNumber(String vehicleNumber);



    @Index
    @WATERMARK("Comma Separated Values")
    public String getTags();
    public void setTags(String tags);


    List<VehicleDocument> getDocuments();

    /* done by the kyc agent */
    @PROTECTION(Kind.NON_EDITABLE)
    public boolean isVerified();
    public void setVerified(boolean verified);


    List<AuthorizedDriver> getAuthorizedDrivers();
    List<VehicleDeploymentPurpose> getVehicleDeploymentPurposes();
}
