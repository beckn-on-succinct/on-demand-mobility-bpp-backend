package in.succinct.bpp.cabs.controller;

import com.venky.core.util.ObjectUtil;
import com.venky.geo.GeoCoordinate;
import com.venky.swf.controller.ModelController;
import com.venky.swf.controller.annotations.SingleRecordAction;
import com.venky.swf.db.Database;
import com.venky.swf.db.annotations.column.ui.mimes.MimeType;
import com.venky.swf.exceptions.AccessDeniedException;
import com.venky.swf.path.Path;
import com.venky.swf.views.BytesView;
import com.venky.swf.views.View;
import in.succinct.bpp.cabs.db.model.supply.DriverLogin;
import in.succinct.bpp.cabs.db.model.supply.User;

public class DriverLoginsController extends ModelController<DriverLogin> {
    public DriverLoginsController(Path path) {
        super(path);
    }

    @SingleRecordAction(icon = "fas fa-map-marker",tooltip = "update location")
    public View updateLocation(long id){
        DriverLogin login = Database.getTable(getModelClass()).get(id);

        if (ObjectUtil.equals(getPath().getRequest().getMethod().toUpperCase(),"GET")){
            if (ObjectUtil.equals(login.getAuthorizedDriver().getDriverId(),getPath().getSessionUserId())){
                //Only self can update.
                User u = getSessionUser();
                if (u.getCurrentLat() != null && u.getCurrentLng() != null){
                    login.updateLocation(new GeoCoordinate(u.getCurrentLat(),u.getCurrentLng()));
                }
            }else {
                throw new AccessDeniedException();
            }
        }
        return new BytesView(getPath(),new byte[]{}, MimeType.APPLICATION_JSON);
    }
}
