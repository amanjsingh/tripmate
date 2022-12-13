package grp16.tripmate.vehicle.database;

import grp16.tripmate.db.connection.DatabaseConnection;
import grp16.tripmate.db.connection.IDatabaseConnection;
import grp16.tripmate.db.execute.DatabaseExecutor;
import grp16.tripmate.db.execute.IDatabaseExecutor;
import grp16.tripmate.logger.ILogger;
import grp16.tripmate.logger.MyLoggerAdapter;
import grp16.tripmate.vehicle.model.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class VehicleBookingPaymentDatabase implements IVehicleBookingPaymentDatabase
{

    private final ILogger logger = new MyLoggerAdapter(this);

    IVehicleBookingPaymentQueryBuilder queryBuilder;

    private final IDatabaseConnection dbConnection;

    private final IVehicleBookingPaymentFactory factory;

    private final IDatabaseExecutor databaseExecutor;

    public VehicleBookingPaymentDatabase()
    {
        queryBuilder = VehicleBookingPaymentQueryBuilder.getInstance();
        dbConnection = new DatabaseConnection();
        factory = VehicleBookingPaymentFactory.getInstance();
        databaseExecutor = new DatabaseExecutor();
    }

    private List<VehicleBookingPayment> listToVehicleBookingPayments(List<Map<String, Object>> results) throws ParseException {
        List<VehicleBookingPayment> bookingPayments = new ArrayList<>();
        for (Map<String, Object> result : results)
        {
            VehicleBookingPayment bookingPaymentObj = factory.getNewVehicleBookingPayment();
            bookingPaymentObj.setPaymentId((Integer) result.get(VehicleBookingPaymentDbColumns.ID));
            bookingPaymentObj.setAmount((Float) result.get(VehicleBookingPaymentDbColumns.AMOUNT));
            bookingPaymentObj.setVehicleBookingId((Integer) result.get(VehicleBookingPaymentDbColumns.VEHICLE_BOOKING_ID));
            bookingPaymentObj.setCreatedOn((Date) result.get(VehicleBookingPaymentDbColumns.CREATED_ON));
            bookingPayments.add(bookingPaymentObj);
        }
        logger.info("booking payment objects are: " + bookingPayments);
        return bookingPayments;
    }
    @Override
    public List<VehicleBookingPayment> getVehicleBookingPaymentByUserId(int userId) throws ParseException {
        String query = queryBuilder.getVehicleBookingPaymentByUserId(userId);
        return listToVehicleBookingPayments(databaseExecutor.executeSelectQuery(query));
    }

    @Override
    public boolean createVehicleBookingPayment(IVehicleBookingPayment vehicleBookingPayment)
    {
        String query = queryBuilder.createVehicleBookingPayment((VehicleBookingPayment) vehicleBookingPayment);
        return databaseExecutor.executeInsertQuery(query);
    }
}
