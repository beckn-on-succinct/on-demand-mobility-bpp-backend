package in.succinct.bpp.cabs.db.model.supply;

import com.venky.swf.db.Database;
import com.venky.swf.db.model.User;
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
        AuthorizedDriver proxy = getProxy();
        if (proxy.getRawRecord().isNewRecord()){
            proxy.save();
        }
        DriverLogin login = lastLogin();
        if (login == null || login.getLoggedOffAt() != null ){
            login = Database.getTable(DriverLogin.class).newRecord();
            login.setAuthorizedDriverId(proxy.getId());
            login.setLoggedInAt(new Timestamp(System.currentTimeMillis()));
            login.save();

        }
        return login;
    }
}
