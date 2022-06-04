package in.succinct.bpp.cabs.db.model.routes.stops;

import com.venky.swf.db.annotations.column.IS_VIRTUAL;
import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.annotations.column.indexing.Index;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;

import java.util.List;

@IS_VIRTUAL(false)
public interface NamedStop extends Stop {
    /** Like stop names of trains or metro or bus */
    @UNIQUE_KEY
    @Index
    public String getName();
    public void setName(String name);

    static NamedStop find(String name){
        List<NamedStop> stops = new Select().from(NamedStop.class).where(new Expression(ModelReflector.instance(NamedStop.class).getPool(),
                "NAME", Operator.EQ,name)).execute();
        if (!stops.isEmpty()){
            return stops.get(0);
        }
        return null;
    }
}
