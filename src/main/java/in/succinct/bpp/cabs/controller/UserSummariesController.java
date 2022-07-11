package in.succinct.bpp.cabs.controller;

import com.venky.cache.Cache;
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
                        return userSummary;
                    }
                };
            }
        };


        Select select =new Select("1 AS ID", "COMPANY_ID",
                "COUNT(1) AS USER_COUNT",
                "ROLES.ID AS ROLE_ID"
        ).from(User.class, UserRole.class, Role.class).where(new Expression(getReflector().getPool(),"COMPANY_ID", Operator.IN,companyIds.toArray()));

        select.add(" AND user_roles.user_id = users.id and roles.id = user_roles.role_id");
        select.groupBy("COMPANY_ID","ROLE_ID");
        for (UserSummary userSummary : select.execute(UserSummary.class)) {
            map.get(userSummary.getCompanyId()).put(userSummary.getRoleId(),userSummary);
        }


        select =new Select("1 AS ID", "COMPANY_ID",
                "COUNT(1) AS UNVERIFIED_USER_COUNT",
                "ROLES.ID AS ROLE_ID"
        ).from(User.class, UserRole.class, Role.class).where(new Expression(getReflector().getPool(),"COMPANY_ID", Operator.IN,companyIds.toArray()));

        select.add(" AND user_roles.user_id = users.id and roles.id = user_roles.role_id and  ( exists (select 1 from driver_documents where " +
                                                "driver_documents.driver_id = users.id and driver_documents.verified = false ) or not exists (select 1 from driver_documents where " +
                                                "driver_documents.driver_id = users.id  ) )");
        select.groupBy("COMPANY_ID","ROLE_ID");
        for (UserSummary userSummary : select.execute(UserSummary.class)) {
            UserSummary old = map.get(userSummary.getCompanyId()).get(userSummary.getRoleId());
            old.setUnverifiedUserCount(userSummary.getUnverifiedUserCount());
        }
        List<UserSummary> out = new ArrayList<>();
        for (Long c: map.keySet()){
            for (Long  r: map.get(c).keySet()){
                out.add(map.get(c).get(r));
            }
        }



        return list(out,true);
    }
}
