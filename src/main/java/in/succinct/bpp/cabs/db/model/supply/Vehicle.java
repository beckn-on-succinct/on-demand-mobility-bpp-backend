package in.succinct.bpp.cabs.db.model.supply;

import com.venky.swf.db.annotations.column.COLUMN_DEF;
import com.venky.swf.db.annotations.column.IS_VIRTUAL;
import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.annotations.column.defaulting.StandardDefault;
import com.venky.swf.db.annotations.column.indexing.Index;
import com.venky.swf.db.annotations.column.pm.PARTICIPANT;
import com.venky.swf.db.annotations.column.ui.PROTECTION;
import com.venky.swf.db.annotations.column.ui.PROTECTION.Kind;
import com.venky.swf.db.annotations.column.ui.WATERMARK;
import com.venky.swf.db.annotations.model.HAS_DESCRIPTION_FIELD;
import com.venky.swf.db.annotations.model.MENU;
import com.venky.swf.db.model.Model;
import in.succinct.bpp.cabs.db.model.tag.Tagable;
import com.venky.swf.db.annotations.column.validations.Enumeration;

import java.sql.Timestamp;
import java.util.List;
import java.util.SortedSet;

@MENU("Inventory")
@HAS_DESCRIPTION_FIELD("VEHICLE_NUMBER")
public interface Vehicle extends Model, Tagable {
    public static final  double AVERAGE_SPEED_PER_HOUR = 40 ; //km/hr
    public static final  double AVERAGE_SPEED_PER_MINUTE = AVERAGE_SPEED_PER_HOUR / 60.0 ; //km/hr

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

    @IS_VIRTUAL
    public SortedSet<String> getTagSet();


    List<VehicleDocument> getDocuments();

    @IS_VIRTUAL
    public boolean isApproved();


    List<AuthorizedDriver> getAuthorizedDrivers();
    List<VehicleDeploymentPurpose> getVehicleDeploymentPurposes();
}
