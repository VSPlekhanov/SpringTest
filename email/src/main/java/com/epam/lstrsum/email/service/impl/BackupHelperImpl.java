package com.epam.lstrsum.email.service.impl;

import com.epam.lstrsum.email.persistence.EmailRepository;
import com.epam.lstrsum.email.service.BackupHelper;
import com.epam.lstrsum.model.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.util.MimeMessageUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.epam.lstrsum.email.service.MailService.getAddressFrom;
import static java.util.Objects.nonNull;

@Service
@ConfigurationProperties(prefix = "email")
@Slf4j
@RequiredArgsConstructor
public class BackupHelperImpl implements BackupHelper {
    private static final String BACKUP_DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss.SSSSSS";
    private static final int FILE_LIMIT = 100;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(BACKUP_DATE_FORMAT);

    private final EmailRepository emailRepository;

    private String backupDir = "";
    private Path backupDirPath;

    public void setBackupDir(String backupDir) {
        this.backupDir = backupDir;
        backupDirPath = Paths.get(backupDir);

    }

    @Override
    public void backupEmail(MimeMessage mimeMessage) throws IOException, MessagingException {
        LocalDateTime now = LocalDateTime.now();

        String baseFileName = DATE_TIME_FORMATTER.format(now) + ".eml";
        String fullFileName = baseFileName + ".zip";

        String addressFrom = getAddressFrom(mimeMessage.getFrom());

        Email email = Email.builder()
                .fileName(fullFileName)
                .from(addressFrom)
                .subject(mimeMessage.getSubject())
                .build();
        emailRepository.insert(email);

        if (!backupDir.isEmpty()) {
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(backupDir + File.separator + fullFileName);

                ZipOutputStream zipOutput = new ZipOutputStream(
                        fileOutputStream);

                ZipEntry zipEntry = new ZipEntry(baseFileName);
                zipOutput.putNextEntry(zipEntry);

                mimeMessage.writeTo(zipOutput);

                zipOutput.closeEntry();
                zipOutput.close();
            } finally {
                if (nonNull(fileOutputStream)) {
                    fileOutputStream.close();
                }
            }
        }
    }

    @Override
    public List<String> findAllBackup() {
        try (Stream<Path> paths = Files.walk(backupDirPath)) {
            Comparator<Pair<Path, FileTime>> fileTimeComparator = Comparator.comparing(Pair::getRight);
            return paths
                    .filter(Files::isRegularFile)
                    .map(path -> Pair.of(path, getFileDate(path)))
                    .sorted(fileTimeComparator.reversed())
                    .map(Pair::getLeft)
                    .map(Path::getFileName)
                    .map(Object::toString)
                    .limit(FILE_LIMIT)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.warn("Exception while walking through the dir\nWith error {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private FileTime getFileDate(Path path) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class).creationTime();
        } catch (IOException e) {
            return FileTime.from(0, TimeUnit.SECONDS);
        }
    }

    @Override
    public MimeMessage getMessageByFilename(String filename) throws IOException, MessagingException {
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(getSafePath(filename).toFile()));
        zipInputStream.getNextEntry();

        Properties properties = new Properties();
        Session session = Session.getDefaultInstance(properties, null);

        return MimeMessageUtils.createMimeMessage(session, zipInputStream);
    }

    public Path getSafePath(String filename) {
        Path normalized = Paths.get(filename).normalize();
        return Paths.get(backupDir, normalized.toString());
    }
}
