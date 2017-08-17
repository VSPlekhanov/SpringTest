package com.epam.lstrsum.configuration;

import com.mongodb.Mongo;
import com.mongodb.MongoClientOptions;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.ExtractedArtifactStoreBuilder;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongoCmdOptionsBuilder;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import de.flapdoodle.embed.process.io.Slf4jStreamProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.epam.lstrsum.configuration.TempNamingByFile.TEMP_FILE_NAME;

@Configuration
@EnableConfigurationProperties(MongoProperties.class)
@EnableAutoConfiguration(exclude = {EmbeddedMongoAutoConfiguration.class, MongoAutoConfiguration.class})
@AutoConfigureAfter(MongoAutoConfiguration.class)
@AutoConfigureBefore(MongoDataAutoConfiguration.class)
@Slf4j
public class DataBaseConfiguration extends AbstractMongoConfiguration {
    private final String DEFAULT_VALUE_FOR_MONGO_BIN = "0";
    @Autowired
    private MongoProperties properties;
    @Autowired(required = false)
    private MongoClientOptions options;
    @Autowired
    private Environment environment;
    @Autowired
    private ITempNaming tempNamingByFile;

    @Override
    protected String getDatabaseName() {
        return "ExperienceTestDataBase";
    }

    @Bean(destroyMethod = "close")
    @Override
    public Mongo mongo() throws IOException {
        MongodProcess mongodProcess = mongodProcess(mongodExecutable(mongodStarter(), mongodConfig()));
        Net net = mongodProcess.getConfig().net();
        properties.setHost(net.getServerAddress().getHostName());
        properties.setPort(net.getPort());
        return properties.createMongoClient(this.options, environment);
    }

    @Bean
    File tempFile() throws IOException {
        File tempFile;

        Path tempFilePath = Paths.get(System.getProperty("java.io.tmpdir") + File.separator + TEMP_FILE_NAME);
        if (Files.exists(tempFilePath)) {
            tempFile = tempFilePath.toFile();
        } else {
            tempFile = Files.createFile(tempFilePath).toFile();
            FileWriter fileWriter = new FileWriter(tempFile, false);
            fileWriter.write(DEFAULT_VALUE_FOR_MONGO_BIN);
            fileWriter.close();
        }
        tempFile.deleteOnExit();

        return tempFile;
    }

    @Bean(destroyMethod = "stop")
    public MongodProcess mongodProcess(MongodExecutable mongodExecutable) throws IOException {
        return mongodExecutable.start();
    }

    @Bean(destroyMethod = "stop")
    public MongodExecutable mongodExecutable(MongodStarter mongodStarter, IMongodConfig mongodConfig) throws IOException {
        return mongodStarter.prepare(mongodConfig);
    }

    @Bean
    public IMongodConfig mongodConfig() throws IOException {
        MongoCmdOptionsBuilder mongoCmdOptionsBuilder = new MongoCmdOptionsBuilder();
        mongoCmdOptionsBuilder.useStorageEngine("mmapv1");

        return new MongodConfigBuilder()
                .cmdOptions(new MongoCmdOptionsBuilder()
                        .useStorageEngine("mmapv1")
                        .enableTextSearch(true)
                        .build())
                .version(Version.Main.V3_3)
                .build();
    }

    @Bean
    public MongodStarter mongodStarter() {
        return MongodStarter.getInstance(new RuntimeConfigBuilder()
                .defaults(Command.MongoD)
                .processOutput(new ProcessOutput(
                        new Slf4jStreamProcessor(log, Slf4jLevel.TRACE),
                        new Slf4jStreamProcessor(log, Slf4jLevel.WARN),
                        new Slf4jStreamProcessor(log, Slf4jLevel.INFO)))
                .artifactStore(
                        new ExtractedArtifactStoreBuilder()
                                .defaults(Command.MongoD)
                                .executableNaming(tempNamingByFile)
                )
                .build());
    }

}
