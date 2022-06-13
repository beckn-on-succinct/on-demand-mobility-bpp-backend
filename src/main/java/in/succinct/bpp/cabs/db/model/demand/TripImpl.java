package in.succinct.bpp.cabs.db.model.demand;

import com.venky.core.util.Bucket;
import com.venky.core.util.ObjectUtil;
import com.venky.geo.GeoCoordinate;
import com.venky.swf.db.JdbcTypeHelper.TypeConverter;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.db.table.ModelImpl;
import com.venky.swf.plugins.collab.util.BoundingBox;
import com.venky.swf.sql.Conjunction;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;
import in.succinct.bpp.cabs.db.model.pricing.TariffCard;
import in.succinct.bpp.cabs.db.model.supply.DeploymentPurpose;
import in.succinct.bpp.cabs.db.model.supply.DriverLogin;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class TripImpl extends ModelImpl<Trip> {
    public TripImpl(Trip trip){
        super(trip);
    }
    public void allocate(){
        Trip trip = getProxy();
        DeploymentPurpose purpose = trip.getDeploymentPurpose();

        List<TripStop> stops = trip.getTripStops();
        String vehicleTags = trip.getVehicleTags();
        SortedSet<String> tagSet = new TreeSet<>();
        StringTokenizer tokenizer = new StringTokenizer(vehicleTags,",");
        while (tokenizer.hasMoreTokens()) {
            tagSet.add(tokenizer.nextToken().trim());
        }


        List<DriverLogin> vehicles = getAvailableVehicles(stops.get(0),purpose, tagSet);

        if (!vehicles.isEmpty()){
            trip.setDriverLoginId(vehicles.get(0).getId());
        }

    }

    private List<DriverLogin> getAvailableVehicles(TripStop start, DeploymentPurpose purpose, SortedSet<String> tagSet) {
        ModelReflector<DriverLogin> ref = ModelReflector.instance(DriverLogin.class);
        Expression loggedInDriver = new Expression(ref.getPool(), Conjunction.OR);
        loggedInDriver.add(new Expression(ref.getPool(),"LOGGED_OFF_AT", Operator.EQ));
        loggedInDriver.add(new Expression(ref.getPool(),"LOGGED_OFF_AT", Operator.LT, "LOGGED_IN_AT"));

        StringBuilder tagQuery = new StringBuilder("%");
        for (String tag :tagSet){
            tagQuery.append(tag.trim()).append(",");
            tagQuery.append("%");
        }

        Expression expression = new BoundingBox(new GeoCoordinate(start),1,20).
                getWhereClause(DriverLogin.class).
                add(loggedInDriver);

        Select select = new Select().from(DriverLogin.class).where(expression);
        select.add(" and exists (select " +
                "1 from authorized_drivers a,  vehicles v , vehicle_deployment_purposes dp where dp.deployment_purpose_id = " +
                purpose.getId() + " and dp.vehicle_id = a.vehicle_id and v.id = a.vehicle_id and a.id = driver_logins.authorized_driver_id and v.tags like '"+ tagQuery +"' )  ");

        List<DriverLogin> logins = select.execute();

        List<DriverLogin> filtered = logins.stream().filter(l -> l.getAuthorizedDriver().getDriver().isAvailable()).sorted((o1, o2) -> {
            double d1 = new GeoCoordinate(start).distanceTo(new GeoCoordinate(o1));
            double d2 = new GeoCoordinate(start).distanceTo(new GeoCoordinate(o2));
            int ret = (int) (d1 - d2);
            if (ret == 0) {
                ret = (int) (o1.getId() - o2.getId());
            }
            return ret;
        }).collect(Collectors.toList());

        return filtered;

    }
    TripStop last = null;
    public TripStop getLastStop(){
        Trip trip = getProxy();
        List<TripStop> stops =trip.getTripStops();
        if (!stops.isEmpty()){
            last =  stops.get(stops.size()-1);
        }else {
            last = null;
        }
        return last;
    }

    public BigDecimal getLat(){
        Trip trip = getProxy();

        if (trip.getRawRecord().isNewRecord() || trip.getDriverLoginId() == null){
            return null;
        }
        if (ObjectUtil.equals(trip.getStatus(),Trip.Ended)){
            TripStop last = getLastStop();
            return last == null ? null : last.getLat();
        }else {
            return getProxy().getDriverLogin().getLat();
        }
    }

    public BigDecimal getLng() {
        Trip trip = getProxy();
        if (trip.getRawRecord().isNewRecord() || trip.getDriverLoginId() == null){
            return null;
        }
        if (ObjectUtil.equals(trip.getStatus(),Trip.Ended)){
            TripStop last = getLastStop();
            return last == null ? null : last.getLat();
        }else {
            return getProxy().getDriverLogin().getLat();
        }
    }
    public void rate(){
        Trip trip = getProxy();
        Bucket distance = new Bucket();
        Bucket time = new Bucket();
        trip.getTripStops().forEach(ts->{
            distance.increment(ts.getDistanceFromLastStop());
            time.increment(ts.getMinutesFromLastStop());
        });


        Set<String> tags = new HashSet<>();

        StringTokenizer tok = new StringTokenizer(trip.getVehicleTags(),",");
        while (tok.hasMoreTokens()){
            tags.add(tok.nextToken().trim());
        }

        Select select = new Select().from(TariffCard.class);
        Expression where = new Expression(select.getPool(),Conjunction.AND);
        where.add(new Expression(select.getPool(),"DEPLOYMENT_PURPOSE_ID",Operator.EQ,trip.getDeploymentPurposeId()));
        where.add(new Expression(select.getPool(),"TAG",Operator.IN,tags.toArray()));

        Expression fromKmWhere = new Expression(select.getPool(),Conjunction.OR);
        fromKmWhere.add(new Expression(select.getPool(), "FROM_KMS",Operator.EQ));
        fromKmWhere.add(new Expression(select.getPool(), "FROM_KMS",Operator.LE,distance.doubleValue()));
        where.add(fromKmWhere);

        Expression toKmWhere = new Expression(select.getPool(),Conjunction.OR);
        toKmWhere.add(new Expression(select.getPool(), "TO_KMS",Operator.EQ));
        toKmWhere.add(new Expression(select.getPool(), "TO_KMS",Operator.GE,distance.doubleValue()));
        where.add(toKmWhere);



        List<TariffCard> cards = select.where(where).execute(TariffCard.class);

        TypeConverter<Double> converter = getReflector().getJdbcTypeHelper().getTypeRef(double.class).getTypeConverter();

        Bucket totalSellingPrice = new Bucket();
        Bucket totalPrice = new Bucket();
        for (TariffCard card : cards) {
            Bucket sellingPrice = new Bucket();
            sellingPrice.increment(converter.valueOf(card.getFixedPrice()));
            sellingPrice.increment(converter.valueOf(card.getPricePerKm())  * distance.doubleValue());
            sellingPrice.increment(converter.valueOf(card.getPricePerHour())  * time.doubleValue());

            totalSellingPrice.increment(sellingPrice.doubleValue());
            totalPrice.increment(sellingPrice.doubleValue()/(1.0 +  converter.valueOf(card.getTaxRate()/100.0) ));
        }
        trip.setSellingPrice(totalSellingPrice.doubleValue());
        trip.setPrice(totalPrice.doubleValue());
        double tax = totalSellingPrice.doubleValue() - totalPrice.doubleValue();
        trip.setCGst(tax/2.0);
        trip.setSGst(tax/2.0);

        trip.save();

    }
}
