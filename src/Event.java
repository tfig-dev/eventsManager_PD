import java.time.LocalDate;
import java.time.LocalTime;

public class Event {
    private String name;
    private String location;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    public Event(String name, String location, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.name = name;
        this.location = location;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Event: " + name + "\nLocation: " + location + "\nDate: " + date + "\nStart Time: " + startTime + "\nEnd Time: " + endTime;
    }
}
