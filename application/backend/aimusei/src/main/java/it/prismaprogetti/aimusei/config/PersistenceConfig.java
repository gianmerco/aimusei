package it.prismaprogetti.aimusei.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;

import lombok.extern.slf4j.Slf4j;

@Configuration
@PropertySource("file://${DATASOURCE_PATH:/opt/ai/config}/datasources.env")
@Slf4j
public class PersistenceConfig extends AbstractMongoClientConfiguration  {

	
	@Override
	public String getDatabaseName() {
		return env.getProperty("mongo.database");
	}

	@Autowired
	Environment env;

	@Override
	protected void configureClientSettings(MongoClientSettings.Builder builder) {

		ConnectionString connectionString = new ConnectionString(
				"mongodb://" 
						+ env.getProperty("mongo.host") + "/" + env.getProperty("mongo.database"));
		
		log.info("MongoDB connection string: " + connectionString);
		builder.applyConnectionString(connectionString);
	}
}
