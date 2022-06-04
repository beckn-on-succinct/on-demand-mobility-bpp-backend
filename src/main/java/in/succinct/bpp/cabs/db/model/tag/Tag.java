package in.succinct.bpp.cabs.db.model.tag;

import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.model.Model;



public interface Tag extends Model {
    @UNIQUE_KEY
    public String getTaggedModelName();
    public void setTaggedModelName(String taggedModelName);

    @UNIQUE_KEY
    public String getName();
    public void setName(String name);

}
