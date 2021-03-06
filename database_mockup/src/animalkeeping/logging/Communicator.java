package animalkeeping.logging;

import animalkeeping.ui.Main;
import org.hibernate.HibernateException;
import org.hibernate.Session;

/**
 * Created by huben on 06.03.17.
 */
public abstract class Communicator {
    public static boolean pushSaveOrUpdate(ChangeLogInterface E){
        ChangeLogInterceptor interceptorX = new ChangeLogInterceptor();
        Session session = Main.sessionFactory.withOptions().interceptor(interceptorX).openSession();
        boolean success = true;
        //interceptorX.setSession(session);
        try {
            session.beginTransaction();
            session.saveOrUpdate(E);
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            e.printStackTrace();
            success = false;
        } finally {
            session.close();
        }
        return success;
    }

    public static boolean pushDelete(ChangeLogInterface E){
        if (E == null)
            return false;
        ChangeLogInterceptor interceptorX = new ChangeLogInterceptor();
        Session session = Main.sessionFactory.withOptions().interceptor(interceptorX).openSession();
        //interceptorX.setSession(session);
        try {
            session.beginTransaction();
            session.delete(E);

            session.getTransaction().commit();
            session.close();
        } catch (HibernateException e) {
            e.printStackTrace();
            if (session.isOpen()) {
                session.close();
            }
            return false;
        }
        return true;
    }


}
