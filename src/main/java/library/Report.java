package library;

public class Report {
    private String reportType;
    private String title;
    private String content;

    public Report(String reportType, String title, String content) {
        this.reportType = reportType;
        this.title = title;
        this.content = content;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
