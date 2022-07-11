package in.succinct.bpp.cabs.db.model.supply;

import com.venky.core.util.Bucket;
import com.venky.swf.db.annotations.column.IS_VIRTUAL;
import com.venky.swf.db.model.Model;
import com.venky.swf.plugins.collab.db.model.participants.admin.Company;
import com.venky.swf.plugins.security.db.model.Role;

@IS_VIRTUAL
public interface UserSummary extends Model {
    public Long getCompanyId();
    public void setCompanyId(Long id);
    public Company getCompany();

    public int getUserCount();
    public void setUserCount(int userCount);


    public int getUnverifiedUserCount();
    public void setUnverifiedUserCount(int userCount);


    public Long getRoleId();
    public void setRoleId(Long id);
    public Role getRole();


}
