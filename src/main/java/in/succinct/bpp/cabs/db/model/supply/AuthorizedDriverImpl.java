package in.succinct.bpp.cabs.db.model.supply;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.Database;
import com.venky.swf.db.table.ModelImpl;

import java.sql.Timestamp;
import java.util.List;

public class AuthorizedDriverImpl extends ModelImpl<AuthorizedDriver> {
    public AuthorizedDriverImpl(AuthorizedDriver p){
        super(p);
    }

    private DriverLogin lastLogin(){
        List<DriverLogin> logins = getProxy().getDriver().getMaxDriverLogins(1);
        return logins.isEmpty() ? null : logins.get(0);
    }

    public DriverLogin login(){
        User sessionUser = Database.getInstance().getCurrentUser().getRawRecord().getAsProxy(User.class);

        AuthorizedDriver proxy = getProxy();
        if (proxy.getRawRecord().isNewRecord()){
            proxy.save();
        }
        DriverLogin login = lastLogin();
        if (login == null || login.getLoggedOffAt() != null ){
            login = Database.getTable(DriverLogin.class).newRecord();
            login.setAuthorizedDriverId(proxy.getId());
            login.setLoggedInAt(new Timestamp(System.currentTimeMillis()));
            if (sessionUser != null && sessionUser.getCurrentLat() != null && ObjectUtil.equals(sessionUser.getId() ,proxy.getDriverId())){
                login.setLat(sessionUser.getCurrentLat());
                login.setLng(sessionUser.getCurrentLng());
            }
            login.save();
        }
        return login;
    }
}
