package in.succinct.bpp.cabs.db.model.routes.stops;

import com.venky.swf.db.annotations.column.IS_VIRTUAL;
import com.venky.swf.db.model.Model;
import com.venky.swf.plugins.collab.db.model.participants.admin.Address;

@IS_VIRTUAL
public interface Stop extends Model, Address {

}
