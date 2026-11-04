package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import org.example.constant.ErrorMessage;
import org.example.model.User;
import org.example.utils.DBConnection;
import org.hibernate.Session;


public class LoginDAO {
    public boolean updateUser(User user){
        EntityManager em = DBConnection.getEntityManager();
        EntityTransaction entityTransaction = em.getTransaction();
        try{
            entityTransaction.begin();
            em.merge(user);
            entityTransaction.commit();
            return true;
        } catch (Exception e) {
            if(entityTransaction.isActive()){
                entityTransaction.rollback();
            }
            System.out.println(ErrorMessage.failLogin);
            e.printStackTrace();
            return false;
        }finally {
            if(em != null && em.isOpen()){
                em.close();
            }
        }
    }
    public User getUserByValue(String value, String nameField, String errorMessage){
        EntityManager em = DBConnection.getEntityManager();
        try{
            User user = em.createQuery("select u from User u where u." + nameField + " = :value", User.class)
                    .setParameter("value", value).getSingleResult();
            return user;
        } catch (NoResultException e) {
            System.out.println(errorMessage);
            return  null;
        }finally {
            if(em != null && em.isOpen()){
                em.close();
            }
        }
    }
    public User getUserByEmail(String email){
        return getUserByValue( email,"email", ErrorMessage.email);
    }
    public User getUserByUsername(String username){
        return getUserByValue( username,"username", ErrorMessage.username);
    }
}
