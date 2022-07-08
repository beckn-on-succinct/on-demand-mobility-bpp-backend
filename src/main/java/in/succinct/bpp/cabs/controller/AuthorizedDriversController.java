package in.succinct.bpp.cabs.controller;

import com.venky.swf.controller.ModelController;
import com.venky.swf.controller.annotations.SingleRecordAction;
import com.venky.swf.db.Database;
import com.venky.swf.path.Path;
import com.venky.swf.views.ForwardedView;
import com.venky.swf.views.View;
import in.succinct.bpp.cabs.db.model.supply.AuthorizedDriver;
import in.succinct.bpp.cabs.db.model.supply.DriverLogin;

import java.util.List;

public class AuthorizedDriversController extends ModelController<AuthorizedDriver> {
    public AuthorizedDriversController(Path path) {
        super(path);
    }
    public View login(){
        List<AuthorizedDriver> authorizedDriverList = getIntegrationAdaptor().readRequest(getPath());
        if (authorizedDriverList.size() != 1){
            throw  new RuntimeException("You need to pass only one Driver's login information.");
        }
        DriverLogin login = authorizedDriverList.get(0).login();
        return new ForwardedView(getPath(),"/driver_logins","show/"+login.getId());

    }

    @SingleRecordAction
    public View login(long id){
        DriverLogin login = Database.getTable(getModelClass()).get(id).login();
        return new ForwardedView(getPath(),"/driver_logins","show/"+login.getId());

    }

}
