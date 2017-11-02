package com.epam.lstrsum.service.mail;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.email.persistence.EmailRepository;
import com.epam.lstrsum.email.service.BackupHelper;
import com.epam.lstrsum.email.service.impl.BackupHelperImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.EmailException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.epam.lstrsum.testutils.MimeMessageCreatorUtil.createSimpleMimeMessage;
import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("email")
@Ignore
public class BackupHelperTest extends SetUpDataBaseCollections {
    private static final String TEMP_DIR_FOR_BACKUP = "backup";
    private static final int MAXIMUM_BACKUP = 15;

    @Autowired
    private BackupHelper backupHelper;

    @Autowired
    private EmailRepository emailRepository;

    private Path tempDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory(TEMP_DIR_FOR_BACKUP);
        ((BackupHelperImpl) backupHelper).setBackupDir(tempDirectory.toAbsolutePath().toString() + "/");
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.cleanDirectory(tempDirectory.toFile());
    }

    @Test
    public void backupEmail() throws Exception {
        int sizeBefore = emailRepository.findAll().size();

        int backedMessages = backupSomeMessages().size();

        assertThat(emailRepository.findAll())
                .hasSize(sizeBefore + backedMessages);
        assertThat(tempDirectory.toFile().listFiles())
                .hasSize(backedMessages);
    }

    @Test
    public void findAllBackup() throws Exception {
        int backedMessages = backupSomeMessages().size();

        assertThat(backupHelper.findAllBackup())
                .hasSize(backedMessages);
    }

    private List<String> mapToSubjects(List<MimeMessage> mimeMessages) {
        return mimeMessages.stream()
                .map(mimeMessage -> {
                    try {
                        return mimeMessage.getSubject();
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Test
    public void testLimitForBackupHelper() {
        backupMessages(101);

        assertThat(backupHelper.findAllBackup())
                .hasSize(100);
    }

    @Test
    public void getMessageByFilename() throws Exception {
        List<MimeMessage> mimeMessages = backupSomeMessages();
        List<String> allBackup = backupHelper.findAllBackup();

        List<String> allSubjects = mapToSubjects(mimeMessages);

        for (String fileName : allBackup) {
            assertThat(backupHelper.getMessageByFilename(fileName))
                    .matches(
                            mimeMessage -> {
                                try {
                                    return thereAreEquivalentSubjects(allSubjects, mimeMessage.getSubject());
                                } catch (MessagingException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
        }
    }

    private boolean thereAreEquivalentSubjects(List<String> subjects, String subject) {
        return subjects.stream()
                .anyMatch(subject::equals);
    }

    public boolean thereAreInputStreamThatEqualToGiven(List<InputStream> inputStreams, InputStream inputStream) {
        return inputStreams.stream()
                .anyMatch(i -> {
                    try {
                        return IOUtils.contentEquals(i, inputStream);
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

    private List<MimeMessage> backupMessages(int messagesAmount) {
        return IntStream.range(0, messagesAmount)
                .mapToObj(i -> createSimpleMimeMessageExceptionally())
                .map(this::backupExceptionally)
                .collect(Collectors.toList());
    }

    public List<MimeMessage> backupSomeMessages() throws EmailException, IOException, MessagingException {
        return backupMessages(ThreadLocalRandom.current().nextInt(MAXIMUM_BACKUP));
    }

    public MimeMessage backupExceptionally(MimeMessage mimeMessage) {
        try {
            backupHelper.backupEmail(mimeMessage);
            return mimeMessage;
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MimeMessage createSimpleMimeMessageExceptionally() {
        try {
            return createSimpleMimeMessage();
        } catch (EmailException e) {
            throw new RuntimeException(e);
        }
    }
}