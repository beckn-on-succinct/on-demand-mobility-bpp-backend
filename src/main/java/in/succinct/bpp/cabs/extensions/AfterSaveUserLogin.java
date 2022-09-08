package in.succinct.bpp.cabs.extensions;

import com.venky.swf.db.extensions.AfterModelSaveExtension;
import com.venky.swf.db.model.UserLogin;
import com.venky.swf.pm.DataSecurityFilter;
import com.venky.swf.sql.Conjunction;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;
import in.succinct.bpp.cabs.db.model.supply.AuthorizedDriver;
import in.succinct.bpp.cabs.db.model.supply.DriverLogin;
import in.succinct.bpp.cabs.db.model.supply.User;

import java.util.List;

public class AfterSaveUserLogin extends AfterModelSaveExtension<UserLogin> {
    static {
        registerExtension(new AfterSaveUserLogin());
    }
    @Override
    public void afterSave(UserLogin model) {
        List<AuthorizedDriver> authorizedDrivers = model.getUser().getRawRecord().getAsProxy(User.class).getAuthorizedVehicles();
        Select select = new Select().from(DriverLogin.class);
        List<DriverLogin> logins = select.where(new Expression(select.getPool(), Conjunction.AND).
                add(new Expression(select.getPool(),"AUTHORIZED_DRIVER_ID", Operator.IN, DataSecurityFilter.getIds(authorizedDrivers).toArray())).
                add(new Expression(select.getPool(),"LOGGED_IN_AT", Operator.NE)).
                add(new Expression(select.getPool(), "LOGGED_OFF_AT",Operator.EQ))).orderBy("LOGGED_IN_AT DESC").execute(1);

        if (!logins.isEmpty() && !model.getReflector().isVoid(model.getLat()) && !model.getReflector().isVoid(model.getLng()) ){
            DriverLogin driverLogin = logins.get(0);
            driverLogin.setLat(model.getLat());
            driverLogin.setLng(model.getLng());
            driverLogin.save();
        }
    }
}
