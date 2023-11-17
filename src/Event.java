public class Event {
    private int ID;
    private String name;
    private String location;
    private String date;
    private String startTime;
    private String endTime;
    private String code;
    private String endHour;

    public Event(int ID, String name, String location, String date, String startTime, String endTime) {
        this.ID = ID;
        this.name = name;
        this.location = location;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.code = null;
        this.endHour = null;
    }

    @Override
    public String toString() {
        return "Event{" + "ID=" + ID + ", name=" + name + ", location=" + location + ", date=" + date + ", startTime=" + startTime + ", endTime=" + endTime;
    }
}