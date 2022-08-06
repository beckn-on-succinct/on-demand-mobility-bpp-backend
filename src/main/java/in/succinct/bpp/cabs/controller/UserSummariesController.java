package in.succinct.bpp.cabs.controller;

import com.venky.cache.Cache;
import com.venky.core.util.Bucket;
import com.venky.swf.controller.VirtualModelController;
import com.venky.swf.db.Database;
import com.venky.swf.path.Path;
import com.venky.swf.plugins.security.db.model.Role;
import com.venky.swf.plugins.security.db.model.UserRole;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;
import com.venky.swf.views.View;
import in.succinct.bpp.cabs.db.model.supply.DriverDocument;
import in.succinct.bpp.cabs.db.model.supply.User;
import in.succinct.bpp.cabs.db.model.supply.UserSummary;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserSummariesController extends VirtualModelController<UserSummary> {
    public UserSummariesController(Path path) {
        super(path);
    }

    public View index(){
        User user = getSessionUser();
        List<Long> companyIds = user.getCompanyIds();

        Map<Long, Map<Long,UserSummary>> map = new Cache<Long, Map<Long, UserSummary>>() {
            @Override
            protected Map<Long, UserSummary> getValue(Long c) {
                return new Cache<Long, UserSummary>() {
                    @Override
                    protected UserSummary getValue(Long r) {
                        UserSummary userSummary = Database.getTable(UserSummary.class).newRecord();
                        userSummary.setCompanyId(c);
                        userSummary.setRoleId(r);
                        userSummary.setUnverifiedUserCount(new Bucket());
                        userSummary.setUserCount(new Bucket());
                        return userSummary;
                    }
                };
            }
        };

        List<UserSummary> out =new ArrayList<>();
        new Select().from(User.class).where(new Expression(getReflector().getPool(),"COMPANY_ID", Operator.IN,companyIds.toArray())).execute(User.class).forEach(u->{
            u.getUserRoles().forEach(ur->{
                UserSummary summary = map.get(u.getCompanyId()).get(ur.getRoleId());
                if (!u.isApproved()){
                    summary.getUnverifiedUserCount().increment();
                }
                summary.getUserCount().increment();
            });
        });

        for (Long c : map.keySet()) {
            for (Long r : map.get(c).keySet()){
                out.add(map.get(c).get(r));
            }
        }


        return list(out,true);
    }
}
