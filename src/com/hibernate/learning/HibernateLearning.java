package com.hibernate.learning;

import java.io.File;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import com.hibernate.learning.mapping.Employee;

public class HibernateLearning
{
    public static SessionFactory sessionFactory = null;

    public static void main( String args[] )
    {
        getSessionFactory();
        /*
         * 1)The mapping between the POJO and hbm.xml file should be added in the Config File
         * 2)In hbm.xml file ensure the mapping class is fully qualified
         * 3)The primary key can be "native" ( like auto increment) or "assigned" ( from code)
         * 4)Session.flush() will actually execute the statements
         */
        //insertEmployee();

        //firstLevelCache();

        //firstLevelCachewithChanges();

        //firstLevelCachewithChanges2();

        secondLevelCache();

        shutDownSessionFactory();

    }

    private static void secondLevelCache()
    {
        /*
         * Second Level cache is optional. Can be enabled. fllowing are the steps 
         * 1)Changes to hibernate,cgf.xml
         *      <property name="hibernate.cache.provider_class">org.hibernate.cache.EhCacheProvider</property>
                <property name="hibernate.cache.region.factory_class">org.hibernate.cache.ehcache.EhCacheRegionFactory</property>
           2)<Entity>.hbm.xml
                 <cache usage="read-write"/> within class tag
           3)Create new ehcache.xml file with following prop
                 <cache name="com.hibernate.learning.mapping.Employee"
                    maxElementsInMemory="100" eternal="false" timeToIdleSeconds="5"
                    timeToLiveSeconds="200" />
           4) Create annotation "@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="employee")" in POJO file
           
           Only when the session is closed, the second level cache will be updated.
         */

        Session session = sessionFactory.openSession();

        Employee employee = (Employee)session.load( Employee.class, new Integer( 1 ) );

        System.out.println( "Fetching Employee From DB " + employee.getFirstName() + " " + employee.getLastName() );

        session.close();

        Session session2 = sessionFactory.openSession();

        Employee employee2 = (Employee)session2.load( Employee.class, new Integer( 1 ) );

        System.out.println( "Fetching Employee using another session" + employee2.getFirstName() + " " + employee2.getLastName() );

        //session2.getTransaction().commit();

        System.out.println( sessionFactory.getStatistics().getSecondLevelCacheHitCount() );

    }

    public static void firstLevelCachewithChanges2()
    {
        /*
         * Here we can see that the object cached in one session, does not update the other.
         */
        Session session = sessionFactory.openSession();

        Session sessionUpdate = sessionFactory.openSession();

        Employee employee = (Employee)session.load( Employee.class, new Integer( 1 ) );

        System.out.println( "Fetching Employee From DB " + employee.getFirstName() + " " + employee.getLastName() );

        Employee employeeUpdate = (Employee)sessionUpdate.load( Employee.class, new Integer( 1 ) );

        employeeUpdate.setLastName( "LastNameChanged" );

        sessionUpdate.update( employeeUpdate );

        employee = (Employee)session.load( Employee.class, new Integer( 1 ) );

        System.out.println( "Fetching Employee From session cache " + employee.getFirstName() + " " + employee.getLastName() );

        session.close();

        sessionUpdate.flush();

        sessionUpdate.close();

    }

    public static void firstLevelCachewithChanges()
    {
        /*
         * Here we can see that the object in the cache is updated.
         */
        Session session = sessionFactory.openSession();

        Employee employee = (Employee)session.load( Employee.class, new Integer( 1 ) );

        System.out.println( "Fetching Employee From DB " + employee.getFirstName() + " " + employee.getLastName() );

        employee.setLastName( "LastNameChanged" );

        session.update( employee );

        employee = (Employee)session.load( Employee.class, new Integer( 1 ) );

        System.out.println( "Fetching Employee From session cache " + employee.getFirstName() + " " + employee.getLastName() );

        session.close();

    }

    public static void firstLevelCache()
    {
        //First Level Cache is mandatory cache. It cannot be disabled
        //First Level Cache is at the session level. Hence the object will not be available in the other session.

        /**
         * In the below example, the first session load will hit the db. Since show_sql is enabled, we can the see the query.
         * Next time when we try to get the same object from the session, it is returned from the cache.
         * session.evict can be used to remove it from the cache.
         * Incase there are any changes to the cached object from the hibernate framework (within the same session), then it will be updated in the cache.
         * 
         */

        Session session = sessionFactory.openSession();

        Employee employee = (Employee)session.load( Employee.class, new Integer( 1 ) );

        System.out.println( "Fetching Employee From DB " + employee.getFirstName() + " " + employee.getLastName() );

        employee = (Employee)session.load( Employee.class, new Integer( 1 ) );

        System.out.println( "Fetching Employee From session cache " + employee.getFirstName() + " " + employee.getLastName() );

        session.close();

    }

    public static void insertEmployee()
    {
        Session session = sessionFactory.openSession();

        Employee emp = new Employee();
        emp.setId( 1 );
        emp.setFirstName( "FirstName" );
        emp.setLastName( "LastName" );
        emp.setSalary( 10000 );

        session.save( emp );

        session.flush();

        session.close();

        System.out.println( "Inserted an employee" );
    }

    public static void getSessionFactory()
    {
        //Session Factory will be created per application ( may be per database). It is a heavy weight object
        sessionFactory = new AnnotationConfiguration().configure( new File( "D:\\Extra\\Hibernate\\src\\hibernate.cgf.xml" ) ).buildSessionFactory();
    }

    public static void shutDownSessionFactory()
    {
        if ( sessionFactory != null )
            sessionFactory.close();
    }
}
