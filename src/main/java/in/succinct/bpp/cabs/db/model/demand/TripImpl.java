package in.succinct.bpp.cabs.db.model.demand;

import com.venky.cache.Cache;
import com.venky.core.date.DateUtils;
import com.venky.core.string.StringUtil;
import com.venky.core.util.Bucket;
import com.venky.core.util.ObjectHolder;
import com.venky.core.util.ObjectUtil;
import com.venky.geo.GeoCoder;
import com.venky.geo.GeoCoder.GeoAddress;
import com.venky.geo.GeoCoordinate;
import com.venky.geo.GeoDistance;
import com.venky.geo.GeoLocation;
import com.venky.swf.db.Database;
import com.venky.swf.db.JdbcTypeHelper.TypeConverter;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.db.table.ModelImpl;
import com.venky.swf.plugins.background.core.Task;
import com.venky.swf.plugins.background.core.TaskManager;
import com.venky.swf.plugins.collab.db.model.config.City;
import com.venky.swf.plugins.collab.util.BoundingBox;
import com.venky.swf.routing.Config;
import com.venky.swf.sql.Conjunction;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;
import in.succinct.bpp.cabs.db.model.pricing.TariffCard;
import in.succinct.bpp.cabs.db.model.service.GeoFencePolicy;
import in.succinct.bpp.cabs.db.model.supply.DeploymentPurpose;
import in.succinct.bpp.cabs.db.model.supply.DriverLogin;
import in.succinct.bpp.cabs.db.model.supply.User;
import in.succinct.bpp.cabs.db.model.supply.Vehicle;
import org.bouncycastle.util.Times;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class TripImpl extends ModelImpl<Trip> {
    public TripImpl(Trip trip){
        super(trip);
    }

    public Long getDriverId() {
        User user = getDriver();
        if (user != null){
            return user.getId();
        }
        return null;
    }
    public void setDriverId(Long id){
        //
    }
    private User driver = null;
    public User getDriver(){
        if (driver != null){
            return driver;
        }
        DriverLogin driverLogin = getProxy().getDriverLogin();

        if (driverLogin == null){
            return null;
        }

        driver = driverLogin.getAuthorizedDriver().getDriver();
        return driver;
    }

    private List<DriverLogin> getAvailableVehicles(TripStop start, DeploymentPurpose purpose, SortedSet<String> tagSet) {
        ModelReflector<DriverLogin> ref = ModelReflector.instance(DriverLogin.class);
        Expression loggedInDriver = new Expression(ref.getPool(), Conjunction.OR);
        loggedInDriver.add(new Expression(ref.getPool(),"LOGGED_OFF_AT", Operator.EQ));

        StringBuilder tagQuery = new StringBuilder("%");
        for (String tag :tagSet){
            tagQuery.append(tag.trim());//.append(","); don't put last comma. last tag may not be  ound.
            tagQuery.append("%");
        }

        Expression expression = new BoundingBox(new GeoCoordinate(start),1,Config.instance().getIntProperty("bpp.cabs.search.radius",500)).
                getWhereClause(DriverLogin.class).
                add(loggedInDriver);


        Database.getInstance().getCache(ref).clear(); //Bug in Select

        Select select = new Select().from(DriverLogin.class).where(expression);
        select.add(" and exists (select " +
                "1 from authorized_drivers a,  vehicles v , vehicle_deployment_purposes dp where a.id = driver_logins.authorized_driver_id " +
                " and v.id = a.vehicle_id and dp.vehicle_id = v.id and v.tags like '" +tagQuery +"'" +
                ( purpose != null? String.format(" and dp.deployment_purpose_id = %d " , purpose.getId()) : "" ) +
                 String.format(") and not exists (select 1 from rejected_trips where driver_login_id = driver_logins.id and trip_id = %d)",start.getTripId()));


        List<DriverLogin> logins = select.execute();


        List<DriverLogin> filtered = logins.stream().filter(l -> {
            TripStop fs = getFirstStop();
            TripStop ls = getLastStop();
            if (!geoFencePolicyOk(getCityCode(l),getCityCode(fs),getCityCode(ls))){
                return false;
            }



            User driver = l.getAuthorizedDriver().getDriver();
            Timestamp availableAt = driver.getAvailableAt();
            if (availableAt != null) {
                Trip lastTrip = l.getLastTrip();
                if (lastTrip != null) {
                    TripStop lastStop = lastTrip.getLastStop();
                    double distanceToStartLocation = GeoDistance.getDrivingDistanceKms(start.getLat(), start.getLng(), lastStop.getLat(), lastStop.getLng(), Config.instance().getGeoProviderParams());
                    double minutesToStartLocation = distanceToStartLocation / Vehicle.AVERAGE_SPEED_PER_MINUTE;
                    availableAt = new Timestamp(DateUtils.addMinutes(availableAt.getTime(), (int) (Math.ceil(minutesToStartLocation + 1))));
                }
                return availableAt.getTime() < start.getTrip().getScheduledStart().getTime() + 10 * 60L * 1000L && l.getAuthorizedDriver().getVehicle().getTagSet().containsAll(tagSet);
            }else {
                return false;
            }
        }).sorted((o1, o2) -> {

            double d1 = new GeoCoordinate(start).distanceTo(new GeoCoordinate(o1.getLastTrip() == null ? o1 : o1.getLastTrip().getLastStop()));
            double d2 = new GeoCoordinate(start).distanceTo(new GeoCoordinate(o2.getLastTrip() == null ? o2 : o2.getLastTrip().getLastStop()));
            int ret = (int) (d1 - d2);
            if (ret == 0) {
                ret = (int) (o1.getId() - o2.getId());
            }
            return ret;
        }).collect(Collectors.toList());

        return filtered;

    }

    private boolean geoFencePolicyOk(String driverCity, String startCity, String endCity) {
        if (Database.getTable(GeoFencePolicy.class).recordCount() == 0){
            return true;
        }
        Expression where = new Expression(getReflector().getPool(),Conjunction.AND);
        {
            Expression cityWhere = new Expression(getPool(), Conjunction.OR);
            where.add(cityWhere);
            cityWhere.add(new Expression(getPool(), "DRIVER_CITY", Operator.EQ, driverCity));
            cityWhere.add(new Expression(getPool(), "DRIVER_CITY", Operator.EQ));
        }
        {
            Expression cityWhere = new Expression(getPool(), Conjunction.OR);
            where.add(cityWhere);
            cityWhere.add(new Expression(getPool(), "START_CITY", Operator.EQ, startCity));
            cityWhere.add(new Expression(getPool(), "START_CITY", Operator.EQ));
        }
        {
            Expression cityWhere = new Expression(getPool(), Conjunction.OR);
            where.add(cityWhere);
            cityWhere.add(new Expression(getPool(), "END_CITY", Operator.EQ, endCity));
            cityWhere.add(new Expression(getPool(), "END_CITY", Operator.EQ));
        }
        List<GeoFencePolicy> policies = new Select().from(GeoFencePolicy.class).where(where).execute(1);
        return !policies.isEmpty();
    }

    private String getCityCode(DriverLogin l) {
        GeoAddress address = getGeoCoder().getAddress(l,getGeoProviderParams());
        return City.findByCountryAndStateAndName(address.getCountry(),address.getState(),address.getCity()).getCode();
    }
    private String getCityCode(TripStop ts){
        if (ts.getCityId() != null){
            return ts.getCity().getCode();
        }
        GeoAddress address = getGeoCoder().getAddress(ts,getGeoProviderParams());
        City city = City.findByCountryAndStateAndName(address.getCountry(),address.getState(),address.getCity());
        ts.setCityId(city.getId());
        return city.getCode();
    }

    private static Map<String,String> geoProviderParams = new HashMap<String,String>(){{
        String provider = Config.instance().getProperty("bpp.cabs.geo.provider","here");
        put(provider+".app_key", Config.instance().getProperty("geocoder."+provider+".app_key"));
    }};
    private Map<String,String> getGeoProviderParams(){
        return geoProviderParams;
    }
    private static GeoCoder  geoCoder = new GeoCoder(Config.instance().getProperty("bpp.cabs.geo.provider","here"));
    private GeoCoder getGeoCoder(){
        return geoCoder;
    }

    ObjectHolder<TripStop> last = null;
    public TripStop getLastStop(){
        Trip trip = getProxy();
        if (last == null){
            last = new ObjectHolder<>(null);
            List<TripStop> stops =trip.getTripStops();
            if (!stops.isEmpty()){
                last.set(stops.get(stops.size()-1));
            }
        }
        return last.get();
    }
    ObjectHolder<TripStop> first = null;
    public TripStop getFirstStop(){
        Trip trip = getProxy();
        if (first == null){
            first = new ObjectHolder<>(null);
            List<TripStop> stops =trip.getTripStops();
            if (!stops.isEmpty()){
                first.set(stops.get(0));
            }
        }
        return first.get();
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
            return last == null ? null : last.getLng();
        }else {
            return getProxy().getDriverLogin().getLng();
        }
    }
    public void allocate(){
        Trip trip = getProxy();
        Bucket distance = new Bucket();
        Bucket time = new Bucket();
        TripStop start = null;
        TripStop end = null;
        for (TripStop ts : trip.getTripStops()) {
            distance.increment(ts.getDistanceFromLastStop());
            time.increment(ts.getEstimatedMinutesFromLastStop());
            if (start == null) {
                start = ts;
            }
            end = ts;
        }
        trip.setDriverLoginId(null);
        trip.setDriverAcceptanceStatus(null);
        SortedSet<String> tags = new TreeSet<>();

        StringTokenizer tok = new StringTokenizer(StringUtil.valueOf(trip.getVehicleTags()),",");
        while (tok.hasMoreTokens()){
            tags.add(tok.nextToken().trim());
        }

        Select select = new Select().from(TariffCard.class);
        Expression where = new Expression(select.getPool(),Conjunction.AND);
        if (!trip.getReflector().isVoid(trip.getDeploymentPurposeId() )) {
            where.add(new Expression(select.getPool(), "DEPLOYMENT_PURPOSE_ID", Operator.EQ, trip.getDeploymentPurposeId()));
        }
        if (!tags.isEmpty()) {
            where.add(new Expression(select.getPool(), "TAG", Operator.IN, tags.toArray()));
        }else {
            where.add(new Expression(select.getPool(), "TAG", Operator.EQ));
        }

        Expression fromKmWhere = new Expression(select.getPool(),Conjunction.OR);
        fromKmWhere.add(new Expression(select.getPool(), "FROM_KMS",Operator.EQ));
        fromKmWhere.add(new Expression(select.getPool(), "FROM_KMS",Operator.LE,distance.doubleValue()));
        where.add(fromKmWhere);

        Expression toKmWhere = new Expression(select.getPool(),Conjunction.OR);
        toKmWhere.add(new Expression(select.getPool(), "TO_KMS",Operator.EQ));
        toKmWhere.add(new Expression(select.getPool(), "TO_KMS",Operator.GT,distance.doubleValue()));
        where.add(toKmWhere);



        List<TariffCard> cards = select.where(where).execute(TariffCard.class);

        TypeConverter<Double> converter = getReflector().getJdbcTypeHelper().getTypeRef(double.class).getTypeConverter();

        Map<Long,Bucket> totalSellingPrice = new Cache<Long, Bucket>() {
            @Override
            protected Bucket getValue(Long aLong) {
                return new Bucket();
            }
        };
        Map<Long,Bucket> totalPrice = new Cache<Long, Bucket>() {
            @Override
            protected Bucket getValue(Long aLong) {
                return new Bucket();
            }
        };
        for (TariffCard card : cards) {
            Bucket sellingPrice = new Bucket();
            sellingPrice.increment(converter.valueOf(card.getFixedPrice()));
            sellingPrice.increment(converter.valueOf(card.getPricePerKm())  * distance.doubleValue());
            sellingPrice.increment(converter.valueOf(card.getPricePerHour())  * time.doubleValue());

            totalSellingPrice.get(card.getDeploymentPurposeId()).increment(sellingPrice.doubleValue());
            totalPrice.get(card.getDeploymentPurposeId()).increment(sellingPrice.doubleValue()/(1.0 +  converter.valueOf(card.getTaxRate()/100.0) ));
        }
        SortedSet<Long> purposeSet = new TreeSet<>(new Comparator<Long>() {
            @Override
            public int compare(Long o1, Long o2) {
                return Double.compare(totalSellingPrice.get(o1).doubleValue(),totalSellingPrice.get(o2).doubleValue());
            }
        });
        purposeSet.addAll(totalSellingPrice.keySet());
        for (Long deploymentPurposeId : purposeSet){
            DeploymentPurpose deploymentPurpose = Database.getTable(DeploymentPurpose.class).get(deploymentPurposeId);
            List<DriverLogin> logins = getAvailableVehicles(start,deploymentPurpose,tags);
            if (!logins.isEmpty()){
                trip.setDeploymentPurposeId(deploymentPurposeId);
                trip.setSellingPrice(totalSellingPrice.get(deploymentPurposeId).doubleValue());
                trip.setPrice(totalPrice.get(deploymentPurposeId).doubleValue());
                double tax = trip.getSellingPrice() - trip.getPrice();
                trip.setCGst(tax/2.0);
                trip.setSGst(tax/2.0);
                trip.setDriverLoginId(logins.get(0).getId());

                trip.save();
                break;
            }
        }
        if (trip.getDriverLoginId() == null){
            throw new RuntimeException("No Driver available");
        }


    }
    public void start(){
        Trip t = getProxy();

        DriverLogin driverLogin = t.getDriverLogin();

        if (driverLogin == null || driverLogin.getLoggedOffAt() != null ){
            throw new UnsupportedOperationException("Cannot start trip unless driver is assigned and is logged in");
        }
        if (!ObjectUtil.equals(t.getDriverAcceptanceStatus(),Trip.Accepted)){
            t.accept();
        }

        if (ObjectUtil.equals(t.getDriverAcceptanceStatus(),Trip.Accepted)){
            t.setStatus(Trip.Started);
            t.setStartTs(new Timestamp(System.currentTimeMillis()));
            t.save();
            t.getDriverLogin().updateLocation(new GeoCoordinate(t.getTripStops().get(0)));
        }

    }
    public void end(){
        Trip t = getProxy();
        List<TripStop> stops = t.getTripStops();

        DriverLogin driverLogin = t.getDriverLogin();

        if (driverLogin == null || driverLogin.getLoggedOffAt() != null ){
            throw new UnsupportedOperationException("Cannot start trip unless driver is assigned and is logged in");
        }
        if (!ObjectUtil.equals(t.getStatus(),Trip.Ended)){
            t.setStatus(Trip.Ended);
            t.setEndTs(new Timestamp(System.currentTimeMillis()));
            t.save();
            t.getDriverLogin().updateLocation(new GeoCoordinate(stops.get(stops.size()-1)));
        }
    }

    public void cancel(){
        Trip trip = getProxy();
        trip.setStatus(Trip.Canceled);
        trip.save();
    }

    public void accept(){
        Trip trip = getProxy();
        trip.setDriverAcceptanceStatus(Trip.Accepted);
        trip.save();
    }
    public void reject(){
        Trip trip = getProxy();
        trip.setDriverAcceptanceStatus(Trip.Rejected);
        trip.save();
    }

    public String getDisplayStatus(){
        StringBuilder status = new StringBuilder();
        Trip trip = getProxy();
        if (ObjectUtil.equals(trip.getStatus(),Trip.Confirmed)){
            if (!ObjectUtil.equals(trip.getDriverAcceptanceStatus(),Trip.Accepted)){
                status.append("Awaiting Driver acceptance");
            }else {
                TripStop first = getFirstStop();
                if (first != null && first.getLat() != null && trip.getLat() != null ){
                    if (new GeoCoordinate(first).distanceTo(new GeoCoordinate(trip)) < 0.4){
                        status.append("Reached");
                    }else {
                        status.append("Reaching");
                    }
                    status.append(" Pickup location");
                }
            }
        }

        if (status.length() == 0){
            status.append(trip.getStatus());
        }
        return status.toString();
    }
}
