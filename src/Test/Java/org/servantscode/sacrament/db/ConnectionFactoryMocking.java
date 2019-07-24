package org.servantscode.sacrament.db;

import org.servantscode.commons.db.ConnectionFactory;

import java.sql.Connection;

//TODO implement this class in a neater place for use in multiple services testing
public class ConnectionFactoryMocking extends ConnectionFactory {

    //Allows mocking of Connection
    @Override
    public Connection getConnection() {
        return super.getConnection();
    }

}
