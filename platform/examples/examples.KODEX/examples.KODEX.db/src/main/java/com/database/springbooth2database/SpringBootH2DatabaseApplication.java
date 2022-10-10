package com.database.springbooth2database;

import java.sql.SQLException;

import org.h2.tools.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBootH2DatabaseApplication {

    static String dbServerPort=null;
    
	@Bean(initMethod = "start", destroyMethod = "stop")
	public Server inMemoryH2DatabaseaServer() throws SQLException {
	    
	    if(dbServerPort == null) {
	        dbServerPort="9595";
        }
	    return Server.createTcpServer(
	      "-tcp", "-tcpAllowOthers", "-tcpPort", dbServerPort);
	}
	
	public static void main(String[] args) {

       if(args.length == 0) {
           dbServerPort="9595";
       } else {
           dbServerPort=args[0];
       }
	    
		SpringApplication.run(SpringBootH2DatabaseApplication.class, args);
		
		new H2jdbcCreateTable().CreateTimeSeriesTable(dbServerPort);
	}

}
