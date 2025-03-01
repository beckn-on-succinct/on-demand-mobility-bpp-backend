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
import com.venky.swf.db.Database;
import com.venky.swf.db.JdbcTypeHelper.TypeConverter;
import com.venky.swf.db.model.application.Event;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.db.table.ModelImpl;
import com.venky.swf.plugins.collab.db.model.config.City;
import com.venky.swf.plugins.collab.util.BoundingBox;
import com.venky.swf.routing.Config;
import com.venky.swf.sql.Conjunction;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;
import in.succinct.beckn.Context;
import in.succinct.beckn.Message;
import in.succinct.beckn.Order.Status;
import in.succinct.beckn.Request;
import in.succinct.bpp.cabs.BecknUtil;
import in.succinct.bpp.cabs.db.model.pricing.TariffCard;
import in.succinct.bpp.cabs.db.model.service.GeoFencePolicy;
import in.succinct.bpp.cabs.db.model.supply.DeploymentPurpose;
import in.succinct.bpp.cabs.db.model.supply.DriverLogin;
import in.succinct.bpp.cabs.db.model.supply.User;
import in.succinct.bpp.cabs.db.model.supply.Vehicle;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        if (getProxy().getDriverLoginId() != null){
            expression.add(new Expression(getReflector().getPool(), "ID" , Operator.EQ, getProxy().getDriverLoginId()));
        }


        Database.getInstance().getCache(ref).clear(); //Bug in Select

        Select select = new Select().from(DriverLogin.class).where(expression);
        select.add(" and exists (select " +
                "1 from authorized_drivers a,  vehicles v , vehicle_deployment_purposes dp where a.id = driver_logins.authorized_driver_id " +
                " and v.id = a.vehicle_id and dp.vehicle_id = v.id and v.tags like '" +tagQuery +"'" +
                ( purpose != null? String.format(" and dp.deployment_purpose_id = %d " , purpose.getId()) : "" ) +
                 String.format(") and not exists (select 1 from rejected_trips where driver_login_id = driver_logins.id and trip_id = %d)",start.getTripId()));


        List<DriverLogin> logins = select.execute();


        boolean checkGeoFence = Database.getTable(GeoFencePolicy.class).recordCount() > 0;
        List<DriverLogin> filtered = logins.stream().filter(l -> {
            TripStop fs = getFirstStop();
            TripStop ls = getLastStop();

            if (checkGeoFence && !geoFencePolicyOk(getCityCode(l),getCityCode(fs),getCityCode(ls))){
                return false;
            }


            User driver = l.getAuthorizedDriver().getDriver();
            Timestamp availableAt = driver.getAvailableAt();
            if (availableAt != null) {
                Trip lastTrip = l.getLastTrip();
                if (lastTrip != null) {
                    TripStop lastStop = lastTrip.getLastStop();
                    double distanceToStartLocation = GeoCoder.getInstance().getDrivingDistanceKms(start,lastStop, Config.instance().getGeoProviderParams());
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
    private static GeoCoder  geoCoder = GeoCoder.getInstance();
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

    public void allocate() {
        Trip trip = getProxy();

        List<AllocationOption> options = loadAllocationOptions();
        trip.setDriverLoginId(null);
        trip.setDriverAcceptanceStatus(null);
        //List<AllocationOption> options = trip.getAllocationOptions();
        if (!options.isEmpty()){
            AllocationOption option = options.get(0);
            trip.setDeploymentPurposeId(option.getDeploymentPurposeId());
            trip.setSellingPrice(option.getSellingPrice());
            trip.setPrice(option.getPrice());
            trip.setCGst(option.getCGst());
            trip.setSGst(option.getSGst());
            trip.setDriverLoginId(option.getDriverLoginId());
        }
        trip.save();

    }

    public List<AllocationOption> loadAllocationOptions(){
        Trip trip = getProxy();
        trip.getAllocationOptions().forEach(ao->ao.destroy());

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

        Expression tagWhere = new Expression(select.getPool(),Conjunction.OR);
        if (!tags.isEmpty()) {
            tagWhere.add(new Expression(select.getPool(), "TAG", Operator.IN, tags.toArray()));
        }
        tagWhere.add(new Expression(select.getPool(), "TAG", Operator.EQ));
        where.add(tagWhere);

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

        List<AllocationOption> options = new ArrayList<>();
        for (Long deploymentPurposeId : purposeSet){
            DeploymentPurpose deploymentPurpose = Database.getTable(DeploymentPurpose.class).get(deploymentPurposeId);
            List<DriverLogin> logins = getAvailableVehicles(start,deploymentPurpose,tags);
            for (DriverLogin login : logins){
                AllocationOption option = Database.getTable(AllocationOption.class).newRecord();
                option.setTripId(trip.getId());
                option.setDeploymentPurposeId(deploymentPurposeId);
                option.setSellingPrice(totalSellingPrice.get(deploymentPurposeId).doubleValue());
                option.setPrice(totalPrice.get(deploymentPurposeId).doubleValue());
                double tax = option.getSellingPrice() - option.getPrice();
                option.setCGst(tax/2.0);
                option.setSGst(tax/2.0);
                option.setDriverLoginId(logins.get(0).getId());
                option.save();
                options.add(option);
                break;
            }
        }
        return options;

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

    static final Map<Status,String> statusLiteralMap = new HashMap<Status,String>(){{
        put(Status.Created,"Not Confirmed");
        put(Status.Awaiting_Acceptance ,"Awaiting Driver acceptance");
        put(Status.Accepted,"Reaching Pickup location");
        put(Status.Prepared,"Reached Pickup location");
        put(Status.In_Transit,"Started");
        put(Status.Completed,"Ended");
        put(Status.Cancelled,"Cancelled");
    }};
    static final Map<String,Status> literalStatusMap = new HashMap<String,Status>(){{
        statusLiteralMap.forEach((s,l)->put(l,s));
    }};

    public Status getBecknOrderStatus(){
        return literalStatusMap.get(getDisplayStatus());
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

    public void notifyBap() {
        Trip model = getProxy();
        if (ObjectUtil.isVoid(model.getBapId()) || ObjectUtil.isVoid(model.getTransactionId())){
            return;
        }
        Request request = new Request();
        Context context = new Context();
        request.setContext(context);
        context.setBapId(model.getBapId());
        context.setCity(model.getDriverLogin().getAuthorizedDriver().getDriver().getCity().getCode());
        context.setTransactionId(model.getTransactionId());

        request.setMessage(new Message());
        request.getMessage().setOrder(new BecknUtil().getBecknOrder(model,request));


        Event.find("notify_bap").raise(request);

    }

}
