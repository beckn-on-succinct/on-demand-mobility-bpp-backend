package in.succinct.bpp.cabs.db.model.supply;

import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.model.Model;

public interface DeploymentPurpose extends Model {
    @UNIQUE_KEY
    public String getName();
    public void setName(String name);

    public String getImageUrl();
    public void setImageUrl(String imageUrl);
}
