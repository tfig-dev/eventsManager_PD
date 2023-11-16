import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Event {
    private String name;
    private String location;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String code;
    private int codeMinutes;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 5;

    public Event(String name, String location, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.name = name;
        this.location = location;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.code = null;
        this.codeMinutes = 0;
    }

    @Override
    public String toString() {
        return "Event: " + name + "\nLocation: " + location + "\nDate: " + date + "\nStart Time: " + startTime + "\nEnd Time: " + endTime;
    }

    private String generateCode(int minutes) {
        SecureRandom random = new SecureRandom();
        StringBuilder codeBuilder = new StringBuilder();

        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            codeBuilder.append(randomChar);
        }
        codeBuilder.append(System.currentTimeMillis());
        code = codeBuilder.toString();
        codeMinutes = minutes;
        return code;
    }

    private boolean isCodeValid() {
        String timestampString = code.substring(CODE_LENGTH);
        long timestamp = Long.parseLong(timestampString);

        Instant now = Instant.now();
        Instant codeTimestamp = Instant.ofEpochMilli(timestamp);
        long elapsedMinutes = ChronoUnit.MINUTES.between(codeTimestamp, now);

        return elapsedMinutes <= codeMinutes;
    }
}
